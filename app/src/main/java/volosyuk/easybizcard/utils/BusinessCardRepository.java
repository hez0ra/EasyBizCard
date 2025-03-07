package volosyuk.easybizcard.utils;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import volosyuk.easybizcard.models.BusinessCard;
import volosyuk.easybizcard.models.BusinessCardv0_5;

public class BusinessCardRepository {
    private final CollectionReference businessCardCollection;

    public BusinessCardRepository(FirebaseFirestore db) {
        this.businessCardCollection = db.collection("business_cards");
    }

    // Метод для получения визиток текущего пользователя
    public CompletableFuture<List<BusinessCardv0_5>> getUserBusinessCards(String userId) {
        CompletableFuture<List<BusinessCardv0_5>> future = new CompletableFuture<>();
        List<BusinessCardv0_5> businessCardv05List = new ArrayList<>();

        businessCardCollection
                .whereEqualTo("userId", userId) // Фильтр по userId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null) {
                            for (QueryDocumentSnapshot document : documents) {
                                BusinessCardv0_5 card = document.toObject(BusinessCardv0_5.class);
                                card.setCardId(document.getId());
                                businessCardv05List.add(card);
                            }
                            future.complete(businessCardv05List);
                        } else {
                            future.completeExceptionally(new Exception("No documents found"));
                        }
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }


    // Метод для получения всех визиток
    public CompletableFuture<List<BusinessCard>> getAllBusinessCards() {
        CompletableFuture<List<BusinessCard>> future = new CompletableFuture<>();
        List<BusinessCard> businessCardList = new ArrayList<>();

        businessCardCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null) {
                            for (QueryDocumentSnapshot document : documents) {
                                BusinessCard card = document.toObject(BusinessCard.class);
                                businessCardList.add(card);
                            }
                            future.complete(businessCardList);
                        }
                    }
                });
        return future;
    }

    // Метод для добавления визитки
    public CompletableFuture<Void> addBusinessCard(BusinessCardv0_5 card) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String businessCardId = businessCardCollection.document().getId();
        card.setCardId(businessCardId);
        businessCardCollection.document(businessCardId).set(card)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EasyBizCard", "Визитка успешно создана");
                    future.complete(null);
                })
                .addOnFailureListener(e -> {
                    Log.d("EasyBizCard", "Ошибка при создании визитки", e);
                    future.completeExceptionally(e);
                });
        return future;
    }

    // Метод для обновления визитки
    public void updateBusinessCard(BusinessCardv0_5 businessCardv05) {
        businessCardCollection.document(businessCardv05.getCardId()).set(businessCardv05);
    }

    // Метод для удаления визитки по ID
    public void deleteBusinessCardById(String cardId) {
        // Получаем визитку по ее ID
        businessCardCollection.document(cardId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Получаем визитку из документа
                        DocumentSnapshot document = task.getResult();
                        BusinessCardv0_5 businessCardv05 = document.toObject(BusinessCardv0_5.class);

                        // Удаляем саму визитку из Firestore
                        businessCardCollection.document(cardId).delete();

                        // Если у визитки есть imageUrl, удаляем фото из Firebase Storage
                        if (businessCardv05 != null && businessCardv05.getImageUrl() != null) {
                            String imageUrl = businessCardv05.getImageUrl();
                            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                            photoRef.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("EasyBizCard", "Изображение успешно удалено");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("EasyBizCard", "Ошибка при удалении изображения", e);
                                    });
                        }
                    } else {
                        Log.d("EasyBizCard", "Ошибка при получении визитки из Firestore");
                    }
                });
    }

    // Метод для удаления всех визиток пользователя по ID
    public void deleteBusinessCardsByUserId(String userId) {
        businessCardCollection
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            deleteBusinessCardById(document.getId());
                        }
                    }
                });
    }

    // Метод для поиска визитки по ID
    public CompletableFuture<BusinessCardv0_5> searchBusinessCardById(String cardId) {
        CompletableFuture<BusinessCardv0_5> future = new CompletableFuture<>();
        businessCardCollection.document(cardId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();  // Используйте DocumentSnapshot, а не QueryDocumentSnapshot
                        if (document.exists()) {
                            BusinessCardv0_5 businessCardv05 = document.toObject(BusinessCardv0_5.class);
                            businessCardv05.setCardId(document.getId());
                            future.complete(businessCardv05);
                        } else {
                            future.complete(null);  // Визитка не найдена
                        }
                    } else {
                        future.completeExceptionally(new Exception("Ошибка при поиске визитки"));
                    }
                });
        return future;
    }

    public CompletableFuture<Void> updateViewsCount(String cardId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference cardRef = businessCardCollection.document(cardId);

        cardRef.update("views", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Log.d("EasyBizCard", "Счётчик просмотров обновлен");
                    future.complete(null);
                })
                .addOnFailureListener(e -> {
                    future.completeExceptionally(new Exception("Ошибка при увелечении количетсва просмотров визитки"));
                });
        return future;
    }

    public CompletableFuture<Long[]> getViewsAndFavoritesCount(String cardId) {
        CompletableFuture<Long[]> future = new CompletableFuture<>();
        DocumentReference cardRef = businessCardCollection.document(cardId);

        cardRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long views = documentSnapshot.getLong("views");
                        Long favorites = documentSnapshot.getLong("favorites");

                        Long[] result = {
                                views != null ? views : 0L,
                                favorites != null ? favorites : 0L
                        };

                        future.complete(result);
                    } else {
                        future.completeExceptionally(new Exception("Документ не найден"));
                    }
                })
                .addOnFailureListener(e -> {
                    future.completeExceptionally(
                            new Exception("Ошибка получения данных: " + e.getMessage())
                    );
                });

        return future;
    }

    public CompletableFuture<Void> incrementFavoriteCount(String cardId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference cardRef = businessCardCollection.document(cardId);

        cardRef.update("favorites", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Log.d("EasyBizCard", "Счётчик избранных обновлен");
                    future.complete(null);
                })
                .addOnFailureListener(e -> {
                    future.completeExceptionally(new Exception("Ошибка при увелечении количетсва избранных визитки"));
                });
        return future;
    }

    public CompletableFuture<Void> decrementFavoriteCount(String cardId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference cardRef = businessCardCollection.document(cardId);

        cardRef.update("favorites", FieldValue.increment(-1))
                .addOnSuccessListener(aVoid -> {
                    Log.d("EasyBizCard", "Счётчик избранных обновлен");
                    future.complete(null);
                })
                .addOnFailureListener(e -> {
                    future.completeExceptionally(new Exception("Ошибка при увелечении количетсва избранных визитки"));
                });
        return future;
    }



}

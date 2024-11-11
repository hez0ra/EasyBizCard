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

public class BusinessCardRepository {
    private final CollectionReference businessCardCollection;

    public BusinessCardRepository(FirebaseFirestore db) {
        this.businessCardCollection = db.collection("business_cards");
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
                                card.setCardId(document.getId());
                                businessCardList.add(card);
                            }
                            future.complete(businessCardList);
                        }
                    }
                });
        return future;
    }

    // Метод для добавления визитки
    public CompletableFuture<Void> addBusinessCard(BusinessCard card) {
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
    public void updateBusinessCard(BusinessCard businessCard) {
        businessCardCollection.document(businessCard.getCardId()).set(businessCard);
    }

    // Метод для удаления визитки по ID
    public void deleteBusinessCardById(String cardId) {
        // Получаем визитку по ее ID
        businessCardCollection.document(cardId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Получаем визитку из документа
                        DocumentSnapshot document = task.getResult();
                        BusinessCard businessCard = document.toObject(BusinessCard.class);

                        // Удаляем саму визитку из Firestore
                        businessCardCollection.document(cardId).delete();

                        // Если у визитки есть imageUrl, удаляем фото из Firebase Storage
                        if (businessCard != null && businessCard.getImageUrl() != null) {
                            String imageUrl = businessCard.getImageUrl();
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
    public CompletableFuture<BusinessCard> searchBusinessCardById(String cardId) {
        CompletableFuture<BusinessCard> future = new CompletableFuture<>();
        businessCardCollection.document(cardId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();  // Используйте DocumentSnapshot, а не QueryDocumentSnapshot
                        if (document.exists()) {
                            BusinessCard businessCard = document.toObject(BusinessCard.class);
                            businessCard.setCardId(document.getId());
                            future.complete(businessCard);
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

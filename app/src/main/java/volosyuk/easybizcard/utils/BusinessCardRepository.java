package volosyuk.easybizcard.utils;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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

        businessCardCollection.orderBy("createTime").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BusinessCard businessCard = document.toObject(BusinessCard.class);
                            businessCardList.add(businessCard);
                        }
                        future.complete(businessCardList);
                    }
                });

        return future;
    }

    // Метод для добавления визитки
    public BusinessCard addBusinessCard(String title, String description, String number, String email, String site, String imageUrl, String userId) {
        String businessCardId = businessCardCollection.document().getId();
        Timestamp createTime = Timestamp.now();

        BusinessCard businessCard = new BusinessCard(businessCardId, userId, title, description, number, email, site, imageUrl, null);
        businessCardCollection.document(businessCardId).set(businessCard);
        return businessCard;
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

}

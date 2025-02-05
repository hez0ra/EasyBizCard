package volosyuk.easybizcard.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import volosyuk.easybizcard.models.BusinessCardv0_5;
import volosyuk.easybizcard.models.User;

public class UserRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final BusinessCardRepository businessCardRepository;

    public UserRepository(FirebaseFirestore db, FirebaseAuth auth) {
        this.db = db;
        this.auth = auth;
        this.businessCardRepository = new BusinessCardRepository(db);
    }

    // Метод для получения данных текущего пользователя
    public CompletableFuture<DocumentSnapshot> getUserData() {
        CompletableFuture<DocumentSnapshot> future = new CompletableFuture<>();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            future.complete(document);
                        } else {
                            future.completeExceptionally(new Exception("Пользователь не найден"));
                        }
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }

    // Метод для создания нового пользователя
    public CompletableFuture<Void> createUser() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            String email = currentUser.getEmail();

            // Создаем новый документ пользователя в коллекции "users"
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.set(new User(userId, email, null, System.currentTimeMillis(), System.currentTimeMillis(), false))
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EasyBizCard", "Пользователь успешно создан");
                        future.complete(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.d("EasyBizCard", "Ошибка при создании пользователя", e);
                        future.completeExceptionally(e);
                    });
        } else {
            future.completeExceptionally(new Exception("Пользователь не авторизован"));
        }

        return future;
    }

    // Метод для добавления визитки в закладки пользователя
    public void addCardToBookmarks(String cardId) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .update("bookmarkedCards", FieldValue.arrayUnion(cardId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("EasyBizCard", "Визитка добавлена в закладки");
                })
                .addOnFailureListener(e -> {
                    Log.d("EasyBizCard", "Ошибка при добавлении визитки в закладки", e);
                });

        businessCardRepository.incrementFavoriteCount(cardId);

    }

    // Метод для удаления визитки из закладок пользователя
    public void removeCardFromBookmarks(String cardId) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .update("bookmarkedCards", FieldValue.arrayRemove(cardId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("EasyBizCard", "Визитка удалена из закладок");
                })
                .addOnFailureListener(e -> {
                    Log.d("EasyBizCard", "Ошибка при удалении визитки из закладок", e);
                });

        businessCardRepository.decrementFavoriteCount(cardId);

    }

    // Метод для удаления пользователя
    public void deleteUser() {
        String userId = auth.getCurrentUser().getUid();

        // Удаление пользователя из коллекции "users"
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("EasyBizCard", "Пользователь успешно удален из базы данных");
                })
                .addOnFailureListener(e -> {
                    Log.d("EasyBizCard", "Ошибка при удалении пользователя из базы данных", e);
                });

        // Удаление пользователя из Firebase Authentication
        auth.getCurrentUser().delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("EasyBizCard", "Пользователь успешно удален из Firebase Authentication");
                })
                .addOnFailureListener(e -> {
                    Log.d("EasyBizCard", "Ошибка при удалении пользователя из Firebase Authentication", e);
                });
    }

    // Метод для проверки, добавлена ли визитка в закладки текущего пользователя
    public CompletableFuture<Boolean> isCardBookmarked(String cardId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        List<String> bookmarkedCards = (List<String>) document.get("bookmarkedCards");

                        if (bookmarkedCards != null && bookmarkedCards.contains(cardId)) {
                            future.complete(true);  // Карточка найдена в закладках
                        } else {
                            future.complete(false); // Карточка не найдена в закладках
                        }
                    } else {
                        Log.d("EasyBizCard", "Ошибка при проверке закладок");
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }

    // Метод для получения всех визиток, добавленных в закладки текущим пользователем
    public CompletableFuture<List<BusinessCardv0_5>> getAllBookmarkedCards() {
        CompletableFuture<List<BusinessCardv0_5>> future = new CompletableFuture<>();
        String userId = auth.getCurrentUser().getUid();

        // Получаем документ текущего пользователя
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        List<String> bookmarkedCards = (List<String>) document.get("bookmarkedCards");

                        if (bookmarkedCards == null || bookmarkedCards.isEmpty()) {
                            future.complete(new ArrayList<>()); // Если закладок нет, возвращаем пустой список
                            return;
                        }

                        // Загружаем визитки по списку идентификаторов
                        List<CompletableFuture<BusinessCardv0_5>> cardFutures = new ArrayList<>();
                        for (String cardId : bookmarkedCards) {
                            CompletableFuture<BusinessCardv0_5> cardFuture = new CompletableFuture<>();
                            db.collection("business_cards").document(cardId).get()
                                    .addOnSuccessListener(cardDoc -> {
                                        if (cardDoc.exists()) {
                                            BusinessCardv0_5 card = cardDoc.toObject(BusinessCardv0_5.class);
                                            if (card != null) {
                                                card.setCardId(cardId);
                                                cardFuture.complete(card);
                                            }
                                        } else {
                                            cardFuture.complete(null);
                                        }
                                    })
                                    .addOnFailureListener(cardFuture::completeExceptionally);
                            cardFutures.add(cardFuture);
                        }

                        // Объединяем результаты всех запросов в один список
                        CompletableFuture.allOf(cardFutures.toArray(new CompletableFuture[0]))
                                .thenAccept(v -> {
                                    List<BusinessCardv0_5> bookmarkedCardList = new ArrayList<>();
                                    for (CompletableFuture<BusinessCardv0_5> cardFuture : cardFutures) {
                                        try {
                                            BusinessCardv0_5 card = cardFuture.get();
                                            if (card != null) {
                                                bookmarkedCardList.add(card);
                                            }
                                        } catch (Exception e) {
                                            Log.d("EasyBizCard", "Ошибка при получении визитки из закладок", e);
                                        }
                                    }
                                    future.complete(bookmarkedCardList);
                                });
                    } else {
                        future.completeExceptionally(new Exception("Не удалось загрузить закладки пользователя"));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Boolean> isActiveUserAdmin(){
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        boolean result = document.getBoolean("admin");
                        future.complete(result);
                        Log.d("EasyBizCard", "Успешная проверка на админа");
                    }
                    else {
                        future.completeExceptionally(new Exception("Не удалось проверить админ ли текущий пользователь"));
                    }
        })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public CompletableFuture<Boolean> isUserExistByEmail(String email) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean userExists = false;
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getEmail().equals(email)) {
                            userExists = true;
                            break;
                        }
                    }
                    future.complete(userExists); // Здесь возвращаем false, если пользователь не найден
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    // Метод для обновления lastVisit (время последнего визита)
    public void updateLastVisit() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .update("lastVisit", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> Log.d("EasyBizCard", "lastVisit успешно обновлено"))
                .addOnFailureListener(e -> Log.e("EasyBizCard", "Ошибка при обновлении lastVisit", e));
    }

}




package volosyuk.easybizcard.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.concurrent.CompletableFuture;

import volosyuk.easybizcard.models.User;

public class UserRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public UserRepository(FirebaseFirestore db, FirebaseAuth auth) {
        this.db = db;
        this.auth = auth;
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
            String email = currentUser.getEmail(); // Используем email для создания пользователя

            // Создаем новый документ пользователя в коллекции "users"
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.set(new User(userId, email, null))
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
}

package volosyuk.easybizcard;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import volosyuk.easybizcard.adapters.MyCardsAdapter;
import volosyuk.easybizcard.models.BusinessCard;

public class UserDetailActivity extends AppCompatActivity {

    private TextView emailText, createdAtText, lastVisitText;
    private RecyclerView createdCardsRecycler, bookmarkedCardsRecycler;
    private MyCardsAdapter createdAdapter, bookmarkedAdapter;
    private List<BusinessCard> createdCards = new ArrayList<>();
    private List<BusinessCard> bookmarkedCards = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private CircleImageView avatar;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_user_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailText = findViewById(R.id.textUserEmail);
        createdAtText = findViewById(R.id.textUserCreatedAt);
        lastVisitText = findViewById(R.id.textUserLastVisit);
        createdCardsRecycler = findViewById(R.id.recyclerCreatedCards);
        bookmarkedCardsRecycler = findViewById(R.id.recyclerBookmarkedCards);
        avatar = findViewById(R.id.user_detail_avatar);

        createdCardsRecycler.setLayoutManager(new LinearLayoutManager(this));
        bookmarkedCardsRecycler.setLayoutManager(new LinearLayoutManager(this));

        createdAdapter = new MyCardsAdapter(this, createdCards);
        bookmarkedAdapter = new MyCardsAdapter(this, bookmarkedCards);

        createdCardsRecycler.setAdapter(createdAdapter);
        bookmarkedCardsRecycler.setAdapter(bookmarkedAdapter);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        userId = getIntent().getStringExtra("userId");
        loadUserData(userId);
        loadProfileImage();
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId).get().addOnSuccessListener(document -> {
            String email = document.getString("email");
            Long createdAt = document.getLong("createdAt");
            Long lastVisit = document.getLong("lastVisit");
            List<String> bookmarked = (List<String>) document.get("bookmarkedCards");

            emailText.setText(email);

            if (createdAt != null) {
                createdAtText.setText("Дата регистрации: " + formatDate(createdAt));
            }

            if (lastVisit != null) {
                lastVisitText.setText("Последний визит: " + formatLastVisit(lastVisit));
            }

            if (bookmarked != null && !bookmarked.isEmpty()) {
                loadBookmarkedCards(bookmarked);
            }
            loadCreatedCards(userId);
        });
    }

    private void loadCreatedCards(String userId) {
        db.collection("business_cards").whereEqualTo("user_id", userId).get()
                .addOnSuccessListener(query -> {
                    createdCards.clear();
                    if (!query.isEmpty()) {
                        for (DocumentSnapshot doc : query) {
                            createdCards.add(doc.toObject(BusinessCard.class));
                        }
                        createdAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadBookmarkedCards(List<String> cardIds) {
        db.collection("business_cards").whereIn("id", cardIds).get()
                .addOnSuccessListener(query -> {
                    bookmarkedCards.clear();
                    if (!query.isEmpty()) {
                        for (DocumentSnapshot doc : query) {
                            bookmarkedCards.add(doc.toObject(BusinessCard.class));
                        }
                        bookmarkedAdapter.notifyDataSetChanged();
                    }
                });
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String formatLastVisit(long timestamp) {
        long daysAgo = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - timestamp);
        if (daysAgo == 0) return "Сегодня";
        if (daysAgo == 1) return "Вчера";
        return daysAgo + " дней назад";
    }

    private void loadProfileImage() {
        StorageReference userImageRef = storage.getReference().child("users/" + userId + "/profile/");
        userImageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // Используем Picasso для загрузки изображения
                    Picasso.get()
                            .load(uri.toString()) // URL изображения
                            .placeholder(R.drawable.loading) // Плейсхолдер во время загрузки
                            .error(R.drawable.placeholder_image) // Изображение при ошибке
                            .into(avatar);
                })
                .addOnFailureListener(exception -> {
                    // Устанавливаем дефолтное изображение при ошибке
                    Log.e("EasyBizCard", "Ошибка загрузки URL: ", exception);
                    avatar.setImageResource(R.drawable.placeholder_image);
                });
    }
}

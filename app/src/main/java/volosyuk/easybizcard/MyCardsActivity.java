package volosyuk.easybizcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import volosyuk.easybizcard.adapters.MyCardsAdapter;
import volosyuk.easybizcard.models.BusinessCard;

// TODO: добавить сортировку по времени/статусу

public class MyCardsActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKMARD = "is_bookmars";
    private RecyclerView recyclerView;
    private MyCardsAdapter adapter;
    private List<BusinessCard> businessCards;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView hint;
    private ImageButton toAdd, toMyCards, toScan, toBookmarks, toProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_cards);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.my_cards), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recycler_view_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        toAdd = findViewById(R.id.to_add);
        toMyCards = findViewById(R.id.to_my_cards);
        toScan = findViewById(R.id.to_scan);
        toBookmarks = findViewById(R.id.to_bookmarks);
        toProfile = findViewById(R.id.to_profile);

        toAdd.setOnClickListener(v ->{
            Intent intent = new Intent(this, AddActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        toScan.setOnClickListener(v ->{
            Intent intent = new Intent(this, QRScannerActivity.class);
            startActivityForResult(intent, 365);
            overridePendingTransition(0, 0);
        });

        toProfile.setOnClickListener(v ->{
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        toBookmarks.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyCardsActivity.class);
            intent.putExtra(MyCardsActivity.EXTRA_BOOKMARD, true);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        hint = findViewById(R.id.my_cards_text);
        if(getIntent().getBooleanExtra(EXTRA_BOOKMARD, false)){
            hint.setText("У вас нет избранных");
            toBookmarks.setBackgroundColor(getColor(R.color.active));
            toBookmarks.setImageResource(R.drawable.icon_bookmark_blue);
            toMyCards.setBackgroundColor(getColor(android.R.color.transparent));
            toMyCards.setImageResource(R.drawable.icon_storage);
            toMyCards.setOnClickListener(v ->{
                Intent intent = new Intent(this, MyCardsActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
            toBookmarks.setOnClickListener(v -> {});
        }

        businessCards = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adapter = new MyCardsAdapter(this, businessCards);
        recyclerView.setAdapter(adapter);

        loadCards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCards();
    }

    private void loadCards() {
        businessCards.clear();
        adapter.notifyDataSetChanged(); // Обновляем адаптер перед загрузкой, чтобы исключить визуальные дубли

        String userId = mAuth.getCurrentUser().getUid();
    
        if (getIntent().getBooleanExtra(EXTRA_BOOKMARD, false)) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> bookmarkedCards = (List<String>) documentSnapshot.get("bookmarkedCards");
                            if (bookmarkedCards != null && !bookmarkedCards.isEmpty()) {
                                db.collection("business_cards")
                                        .whereIn("id", bookmarkedCards)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                Set<String> addedCardIds = new HashSet<>(); // Для предотвращения дубликатов
                                                businessCards.clear();

                                                for (DocumentSnapshot document : queryDocumentSnapshots) {
                                                    String id = document.getString("id");
                                                    if (id != null && !addedCardIds.contains(id)) { // Проверяем, нет ли уже этой карты
                                                        BusinessCard card = parseBusinessCard(document);
                                                        businessCards.add(card);
                                                        addedCardIds.add(id);
                                                    }
                                                }
                                                hint.setVisibility(View.GONE);
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                Log.d("Firestore", "Нет избранных визиток");
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при получении избранных визиток", e));
                            } else {
                                Log.d("Firestore", "Список избранных визиток пуст");
                            }
                        } else {
                            Log.d("Firestore", "Документ пользователя не найден");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при получении пользователя", e));

        } else {
            db.collection("business_cards")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            businessCards.clear();
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                BusinessCard card = parseBusinessCard(document);
                                businessCards.add(card);
                            }
                            hint.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d("Firestore", "Нет визиток для текущего пользователя");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при получении данных", e));
        }
    }

    private BusinessCard parseBusinessCard(DocumentSnapshot document) {
        String id = document.getString("id");
        Long createdAt = document.getLong("created_at");
        BusinessCard.Status status = BusinessCard.Status.valueOf(document.getString("status"));
        String fileUrl = document.getString("file_url");
        String user_id = document.getString("user_id");
        int backgroundColor = document.get("background_color", Long.class).intValue();

        BusinessCard result = new BusinessCard(id, createdAt, status, fileUrl, user_id, backgroundColor);

        result.setTitle(document.getString("title"));

        return result;
    }


}

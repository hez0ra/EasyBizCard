package volosyuk.easybizcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.List;

import volosyuk.easybizcard.adapters.CardAdapter;
import volosyuk.easybizcard.adapters.MyCardsAdapter;
import volosyuk.easybizcard.models.BusinessCard;

public class MyCardsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyCardsAdapter adapter;
    private List<BusinessCard> businessCards;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView hint;

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

        hint = findViewById(R.id.my_cards_text);

        businessCards = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adapter = new MyCardsAdapter(this, businessCards);
        recyclerView.setAdapter(adapter);

        loadCards();
    }

    private void loadCards() {
        String userId = mAuth.getCurrentUser().getUid();  // Получаем ID текущего пользователя

        // Запрос на получение визиток текущего пользователя
        db.collection("business_cards")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            // Обработка каждого документа
                            String id = document.getString("id");
                            Long createdAt = document.getLong("created_at");
                            String status = document.getString("status");
                            String fileUrl = document.getString("file_url");
                            String user_id = document.getString("user_id");
                            int backgroundColor = document.get("background_color", Long.class).intValue();

                            // Создаем объект бизнес-карты из полученных данных
                            BusinessCard card = new BusinessCard(id, createdAt, status, fileUrl, user_id, backgroundColor);

                            // Добавляем карту в список
                            businessCards.add(card);
                        }
                        // Обновляем адаптер (если он у вас есть)
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("Firestore", "Нет визиток для текущего пользователя");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Ошибка при получении данных", e);
                });
    }

}

package volosyuk.easybizcard;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import volosyuk.easybizcard.adapters.CardAdapter;
import volosyuk.easybizcard.models.BusinessCard;

public class MyCardsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<BusinessCard> businessCards;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cards);

        // Инициализация компонентов
        recyclerView = findViewById(R.id.recycler_view_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        businessCards = new ArrayList<>();
        adapter = new CardAdapter(this, businessCards);
        recyclerView.setAdapter(adapter);

        // Инициализация Firestore
        db = FirebaseFirestore.getInstance();

        // Загрузка визиток из Firestore
        loadBusinessCards();
    }

    private void loadBusinessCards() {
        db.collection("business_cards")  // Название коллекции в Firestore
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null) {
                            for (QueryDocumentSnapshot document : documents) {
                                BusinessCard card = document.toObject(BusinessCard.class);
                                card.setCardId(document.getId());
                                businessCards.add(card);
                            }
                            adapter.notifyDataSetChanged();  // Обновление адаптера с новыми данными
                        }
                    } else {
                        Toast.makeText(MyCardsActivity.this, "Error getting documents", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

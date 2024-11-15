package volosyuk.easybizcard;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import volosyuk.easybizcard.adapters.CardAdapter;
import volosyuk.easybizcard.models.BusinessCard;
import volosyuk.easybizcard.utils.BusinessCardRepository;
import volosyuk.easybizcard.utils.UserRepository;

public class MyCardsActivity extends AppCompatActivity {

    public final static String EXTRA_BOOKMARKS = "isBookmarks";

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<BusinessCard> businessCards = new ArrayList<>();
    private BusinessCardRepository businessCardRepository;
    private UserRepository userRepository;
    private FirebaseAuth mAuth;
    private TextView hint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cards);

        // Инициализация компонентов
        recyclerView = findViewById(R.id.recycler_view_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        businessCardRepository = new BusinessCardRepository(firebaseFirestore);
        userRepository = new UserRepository(firebaseFirestore, mAuth);
        hint = findViewById(R.id.my_cards_text);

        adapter = new CardAdapter(this, businessCards);
        recyclerView.setAdapter(adapter);

        loadCards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCards();
    }

    private void loadCards(){
        if(getIntent().getBooleanExtra(EXTRA_BOOKMARKS, false)){
            userRepository.getAllBookmarkedCards().thenAccept(result -> {
                businessCards.clear();  // Очищаем текущий список
                businessCards.addAll(result);  // Добавляем новые данные
                adapter.notifyDataSetChanged();  // Обновление адаптера с новыми данными
                hint.setVisibility(View.GONE);
                if(result.isEmpty()){
                    hint.setText("У ваз нет сохраненных визиток");
                    hint.setVisibility(View.VISIBLE);
                }
            });
        }
        else{
            if(mAuth.getCurrentUser().getUid() != null){
                // Загрузка визиток из Firestore
                businessCardRepository.getUserBusinessCards(mAuth.getCurrentUser().getUid()).thenAccept(result -> {
                    businessCards.clear();  // Очищаем текущий список
                    businessCards.addAll(result);  // Добавляем новые данные
                    adapter.notifyDataSetChanged();  // Обновление адаптера с новыми данными
                    hint.setVisibility(View.GONE);
                    if (result.isEmpty()){
                        hint.setText("У ваз нет созданных визиток");
                        hint.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }
}

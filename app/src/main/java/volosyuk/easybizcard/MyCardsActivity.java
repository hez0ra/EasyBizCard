package volosyuk.easybizcard;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cards);

        // Инициализация компонентов
        recyclerView = findViewById(R.id.recycler_view_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        businessCardRepository = new BusinessCardRepository(firebaseFirestore);
        userRepository = new UserRepository(firebaseFirestore, FirebaseAuth.getInstance());

        adapter = new CardAdapter(this, businessCards);
        recyclerView.setAdapter(adapter);

        if(getIntent().getBooleanExtra(EXTRA_BOOKMARKS, false)){
            userRepository.getAllBookmarkedCards().thenAccept(result -> {
                businessCards.clear();  // Очищаем текущий список
                businessCards.addAll(result);  // Добавляем новые данные
                adapter.notifyDataSetChanged();  // Обновление адаптера с новыми данными
            });
        }
        else{
            // Загрузка визиток из Firestore
            businessCardRepository.getAllBusinessCards().thenAccept(result -> {
                businessCards.clear();  // Очищаем текущий список
                businessCards.addAll(result);  // Добавляем новые данные
                adapter.notifyDataSetChanged();  // Обновление адаптера с новыми данными
            });
        }
    }
}

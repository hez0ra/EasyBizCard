package volosyuk.easybizcard;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import volosyuk.easybizcard.adapters.CardAdapter;
import volosyuk.easybizcard.models.BusinessCardv0_5;
import volosyuk.easybizcard.utils.BusinessCardRepository;
import volosyuk.easybizcard.utils.UserRepository;

public class MyCardsActivityv0_5 extends AppCompatActivity {

    public final static String EXTRA_BOOKMARKS = "isBookmarks";

    ImageButton toAdd, toMyCards, toScan, toBookmarks, toProfile;

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<BusinessCardv0_5> businessCards = new ArrayList<>();
    private BusinessCardRepository businessCardRepository;
    private UserRepository userRepository;
    private FirebaseAuth mAuth;
    private TextView hint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_cardsv0_5);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.my_cards), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toAdd = findViewById(R.id.my_cards_to_add);
        toBookmarks = findViewById(R.id.my_cards_to_bookmarks);
        toScan = findViewById(R.id.my_cards_to_scan);
        toMyCards = findViewById(R.id.my_cards_to_my_cards);
        toProfile = findViewById(R.id.my_cards_to_profile);

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

        toAdd.setOnClickListener(v ->{
            Intent intent = new Intent(this, AddActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        toMyCards.setOnClickListener(v ->{
            Intent intent = new Intent(this, MyCardsActivity.class);
            intent.putExtra(MyCardsActivityv0_5.EXTRA_BOOKMARKS, false);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        toScan.setOnClickListener(v ->{
            Intent intent = new Intent(this, QRScannerActivity.class);
            startActivityForResult(intent, 365);
            overridePendingTransition(0, 0);
        });

        toBookmarks.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyCardsActivity.class);
            intent.putExtra(MyCardsActivityv0_5.EXTRA_BOOKMARKS, true);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        toProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });


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
                toBookmarks.setOnClickListener(v -> {});
                toBookmarks.setBackgroundColor(getResources().getColor(R.color.active));
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
                    toMyCards.setOnClickListener(v -> {});
                    toMyCards.setBackgroundColor(getResources().getColor(R.color.active));
                    if (result.isEmpty()){
                        hint.setText("У ваз нет созданных визиток");
                        hint.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 365 && resultCode == RESULT_OK) {
            String qrCode = data.getStringExtra("SCAN_RESULT");

            businessCardRepository.searchBusinessCardById(qrCode).thenAccept(card ->{
                Intent intent = new Intent(this, BusinessCardDetailActivity.class);
                intent.putExtra(BusinessCardDetailActivity.EXTRA_CARD, card);
                overridePendingTransition(0, 0);
                startActivity(intent);
            });
        }
    }

}
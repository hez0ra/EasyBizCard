package volosyuk.easybizcard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import volosyuk.easybizcard.utils.BusinessCardRepository;
import volosyuk.easybizcard.utils.UserRepository;

public class AddActivity extends AppCompatActivity {

    ImageButton toProfile, toMyCards, toScan, toBookmarks;
    Button sample1, sample2, sample3;
    BusinessCardRepository businessCardRepository;
    FirebaseAuth mAuth;
    UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        businessCardRepository = new BusinessCardRepository(FirebaseFirestore.getInstance());
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(FirebaseFirestore.getInstance(), mAuth);

        toProfile = findViewById(R.id.add_to_profile);
        toMyCards = findViewById(R.id.add_to_my_cards);
        toScan = findViewById(R.id.add_to_scan);
        toBookmarks = findViewById(R.id.add_to_my_bookmarks);
        sample1 = findViewById(R.id.add_sample_1);
        sample2 = findViewById(R.id.add_sample_2);
        sample3 = findViewById(R.id.add_sample_3);


        sample1.setOnClickListener(v ->{
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(EditActivity.EXTRA_LAYOUT, 1);
            startActivity(intent);
        });

        sample2.setOnClickListener(v ->{
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(EditActivity.EXTRA_LAYOUT, 2);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        sample3.setOnClickListener(v ->{
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(EditActivity.EXTRA_LAYOUT, 3);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        toProfile.setOnClickListener(v ->{
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        toMyCards.setOnClickListener(v ->{
            Intent intent = new Intent(this, MyCardsActivity.class);
            intent.putExtra(MyCardsActivity.EXTRA_BOOKMARKS, false);
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
            intent.putExtra(MyCardsActivity.EXTRA_BOOKMARKS, true);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
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
package volosyuk.easybizcard;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import volosyuk.easybizcard.fragments.CardsFragment;
import volosyuk.easybizcard.fragments.UsersFragment;

public class AdminPanelActivity extends AppCompatActivity {

    ImageButton cardsButton, usersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_panel);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.business_card_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cardsButton = findViewById(R.id.admin_panel_cards);
        usersButton = findViewById(R.id.admin_panel_users);

        replaceFragment(new CardsFragment());

        // Назначаем обработчики кнопкам
        cardsButton.setOnClickListener(view -> replaceFragment(new CardsFragment()));
        usersButton.setOnClickListener(view -> replaceFragment(new UsersFragment()));
    }

    // Метод для замены фрагментов
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_panel_fragment_container_view, fragment)
                .commit();
    }
}
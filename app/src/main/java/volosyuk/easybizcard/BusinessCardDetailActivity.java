package volosyuk.easybizcard;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;
import volosyuk.easybizcard.models.BusinessCard;

public class BusinessCardDetailActivity extends AppCompatActivity {

    private CircleImageView imageView;
    private TextView title, description, phone, email, website;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_1);  // Ваш XML для подробной визитки

        // Инициализация компонентов
        imageView = findViewById(R.id.sample_1_image);
        title = findViewById(R.id.sample_1_title);
        description = findViewById(R.id.sample_1_description);
        phone = findViewById(R.id.sample_1_phone);
        email = findViewById(R.id.sample_1_email);
        website = findViewById(R.id.sample_1_site);

        // Получаем данные о визитке, переданные через Intent
        BusinessCard card = (BusinessCard) getIntent().getSerializableExtra("businessCard");

        // Заполняем данными
        if (card != null) {
            Glide.with(this)
                    .load(card.getImageUrl())  // Загрузка изображения
                    .into(imageView);

            title.setText(card.getTitle());
            description.setText(card.getDescription());
            phone.setText("Телефон: " + card.getPhoneNumber());
            email.setText("Email: " + card.getEmail());
            website.setText("Сайт: " + card.getWebsite());
        }
    }
}

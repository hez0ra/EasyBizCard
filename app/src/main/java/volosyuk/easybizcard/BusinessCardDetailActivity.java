package volosyuk.easybizcard;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;
import volosyuk.easybizcard.models.BusinessCard;
import volosyuk.easybizcard.utils.QRCodeGenerator;

public class BusinessCardDetailActivity extends AppCompatActivity {

    private CircleImageView imageView;
    private TextView title, description, phone, email, website;
    private ImageButton qrCodeBtn;

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
        qrCodeBtn = findViewById(R.id.sample_1_share);


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

            qrCodeBtn.setOnClickListener(v -> {
                String cardIdForQR = card.getCardId();

                Bitmap qrBitmap = QRCodeGenerator.generateQRCode(cardIdForQR);
                showQRCode(qrBitmap);
            });

        }
    }

    private void showQRCode(Bitmap qrBitmap) {
        // Создаём диалог для отображения QR-кода
        Dialog qrDialog = new Dialog(this);
        qrDialog.setContentView(R.layout.dialog_qr_code);  // Здесь ваш layout для QR-кода
        qrDialog.setCancelable(true);

        ImageView qrImageView = qrDialog.findViewById(R.id.qrImageView);
        Button qrShare = qrDialog.findViewById(R.id.qrShare);
        qrShare.setOnClickListener(v -> {
            QRCodeGenerator.sendQrCodeAsImage(this, qrBitmap);
        });

        qrImageView.setImageBitmap(qrBitmap);

        qrDialog.show();
    }


}

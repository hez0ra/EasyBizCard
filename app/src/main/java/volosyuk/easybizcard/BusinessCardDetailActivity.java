package volosyuk.easybizcard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import volosyuk.easybizcard.models.BusinessCard;
import volosyuk.easybizcard.utils.QRCodeGenerator;

public class BusinessCardDetailActivity extends AppCompatActivity {

    private final int REQUEST_CODE_SEND_QR = 1203;
    public final static String EXTRA_CARD = "businessCard";

    private CircleImageView imageView;
    private TextView title, description, phone, email, site;
    private ImageButton qrCodeBtn, whatsapp, viber, telegram, facebook, vkontakte, instagram;

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
        site = findViewById(R.id.sample_1_site);
        qrCodeBtn = findViewById(R.id.sample_1_share);
        whatsapp = findViewById(R.id.sample_1_links_whatsapp);
        viber = findViewById(R.id.sample_1_links_viber);
        telegram = findViewById(R.id.sample_1_links_telegram);
        facebook = findViewById(R.id.sample_1_links_facebook);
        vkontakte = findViewById(R.id.sample_1_links_vkontakte);
        instagram = findViewById(R.id.sample_1_links_instagram);


        // Получаем данные о визитке, переданные через Intent
        BusinessCard card = (BusinessCard) getIntent().getSerializableExtra(EXTRA_CARD);

        // Заполняем данными
        if (card != null) {
            Glide.with(this)
                    .load(card.getImageUrl())  // Загрузка изображения
                    .into(imageView);

            title.setText(card.getTitle());
            description.setText(card.getDescription());
            phone.setText(card.getNumber());
            email.setText(card.getEmail());
            site.setText(card.getSite());

            qrCodeBtn.setOnClickListener(v -> {
                String cardIdForQR = card.getCardId();

                Bitmap qrBitmap = QRCodeGenerator.generateQRCode(cardIdForQR);
                showQRCode(qrBitmap);
            });

        }

        setupSocialLinks(card);

        site.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = site.getText().toString();
                if (!url.isEmpty()) {
                    // Открытие ссылки в браузере
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            }
        });
    }

    private void showQRCode(Bitmap qrBitmap) {
        // Создаём диалог для отображения QR-кода
        Dialog qrDialog = new Dialog(this);
        qrDialog.setContentView(R.layout.dialog_qr_code);  // Здесь ваш layout для QR-кода
        qrDialog.setCancelable(true);

        ImageView qrImageView = qrDialog.findViewById(R.id.qrImageView);
        Button qrShare = qrDialog.findViewById(R.id.qrShare);
        qrShare.setOnClickListener(v -> {
            QRCodeGenerator.sendQrCodeAsImage(this, qrBitmap, REQUEST_CODE_SEND_QR);
        });

        qrImageView.setImageBitmap(qrBitmap);

        qrDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SEND_QR) {
            new Handler().postDelayed(this::deleteTempFiles, 1000); // Задержка в 1 секунду
        }
    }

    private void deleteTempFiles(){
        File cacheDir = getCacheDir();
        File[] tempFiles = cacheDir.listFiles((dir, name) -> name.startsWith("QRCode"));

        if (tempFiles != null) {
            for (File file : tempFiles) {
                if (file.exists()) {
                    boolean deleted = file.delete();
                    Log.d("FileDeletion", "File " + file.getName() + " deleted: " + deleted);
                }
            }
        }
    }

    private void setupSocialLinks(BusinessCard card) {
        setupLink(card.getWhatsApp(), whatsapp);
        setupLink(card.getViber(), viber);
        setupLink(card.getTelegram(), telegram);
        setupLink(card.getFacebook(), facebook);
        setupLink(card.getVk(), vkontakte);
        setupLink(card.getInstagram(), instagram);
        // Скрываем весь блок социальных сетей, если все ссылки пусты
        if (card.getWhatsApp() == null && card.getViber() == null && card.getTelegram() == null &&
                card.getFacebook() == null && card.getVk() == null && card.getInstagram() == null) {
            findViewById(R.id.sample_1_links_group).setVisibility(View.GONE);
        }
    }

    private void setupLink(String url, ImageButton button) {
        if (url == null || url.isEmpty()) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> {
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    showTextDialog(url);
                }
            });
        }
    }

    private void showTextDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text)
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

}

package volosyuk.easybizcard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import volosyuk.easybizcard.models.BusinessCard;
import volosyuk.easybizcard.utils.BusinessCardRepository;
import volosyuk.easybizcard.utils.QRCodeGenerator;
import volosyuk.easybizcard.utils.ReportRepository;
import volosyuk.easybizcard.utils.UserRepository;

public class BusinessCardDetailActivity extends AppCompatActivity {

    private final int REQUEST_CODE_SEND_QR = 1203;
    private UserRepository userRepository;
    public final static String EXTRA_CARD = "businessCard";
    private boolean isMarked = false;


    private CircleImageView imageView;
    private TextView title, description, phone, email, site;
    private ImageButton qrCodeBtn, whatsapp, viber, telegram, facebook, vkontakte, instagram, bookmark, report, analytics, delete, edit;
    private BusinessCardRepository businessCardRepository;
    private FirebaseAuth mAuth;
    private BusinessCard card;
    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_1);  // Ваш XML для подробной визитки

        userRepository = new UserRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());

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
        bookmark = findViewById(R.id.sample_1_bookmark);
        report = findViewById(R.id.sample_1_report);
        analytics = findViewById(R.id.sample_1_analytics);
        delete = findViewById(R.id.sample_1_delete);
        edit = findViewById(R.id.sample_1_edit_btn);

        // Получаем данные о визитке, переданные через Intent
        mAuth = FirebaseAuth.getInstance();
        businessCardRepository = new BusinessCardRepository(FirebaseFirestore.getInstance());
        reportRepository = new ReportRepository();

        card = (BusinessCard) getIntent().getSerializableExtra(EXTRA_CARD);

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
            businessCardRepository.updateViewsCount(card.getCardId());
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

        userRepository.isCardBookmarked(card.getCardId()).thenAccept(result -> {
            if(result){
                isMarked = result;
                bookmark.setImageResource(R.drawable.icon_bookmark_remove);
            }
        });

        report.setOnClickListener(v -> showReportDialog());

        bookmark.setOnClickListener(v -> {
            if(isMarked){
                isMarked = false;
                userRepository.removeCardFromBookmarks(card.getCardId());
                bookmark.setImageResource(R.drawable.icon_bookmark_add);
            }
            else{
                isMarked = true;
                userRepository.addCardToBookmarks(card.getCardId());
                bookmark.setImageResource(R.drawable.icon_bookmark_remove);
            }
        });

        userRepository.isActiveUserAdmin().thenAccept(result -> {
            if(mAuth.getCurrentUser() != null){

                if(mAuth.getCurrentUser().getUid().equals(card.getUserId()) || result){
                    analytics.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    edit.setVisibility(View.VISIBLE);

                    analytics.setOnClickListener(v -> {
                        showStatsDialog(card.getViews(), card.getFavorites());
                    });

                    delete.setOnClickListener(v -> {
                        // Создание и отображение диалога
                        new AlertDialog.Builder(this)
                                .setTitle("Удаление визитки")
                                .setMessage("Вы точно уверены, данные будут удалены БЕЗ ВОЗМОЖНОСТИ ВОЗВРАТА?")
                                .setPositiveButton("Ок", (dialog, which) -> {
                                    businessCardRepository.deleteBusinessCardById(card.getCardId());
                                    finish();
                                })
                                .setNegativeButton("Отмена", (dialog, which) -> {
                                    // Закрытие диалога
                                    dialog.dismiss();
                                })
                                .show();
                    });

                    edit.setOnClickListener(v -> {
                        Intent intent = new Intent(this, EditActivity.class);
                        intent.putExtra(EditActivity.EXTRA_CARD, card);
                        editCardLauncher.launch(intent);
                    });


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
        if (card.getWhatsApp() == null && card.getViber() == null && card.getTelegram() == null && card.getFacebook() == null && card.getVk() == null && card.getInstagram() == null) {
            TextView title = findViewById(R.id.sample_1_links_title);
            title.setVisibility(View.GONE);
            View splitBar = findViewById(R.id.sample_1_links_bar);
            splitBar.setVisibility(View.GONE);
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

    private void showStatsDialog(long viewsCount, long favoritesCount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_stats, null);
        builder.setView(dialogView);

        BarChart barChart = dialogView.findViewById(R.id.dialog_analytics_bar_chart);
        TextView viewsText = dialogView.findViewById(R.id.dialog_analytics_text_views);
        TextView favoritesText = dialogView.findViewById(R.id.dialog_analytics_text_favorites);

        viewsText.setText("Просмотры: " + viewsCount);
        favoritesText.setText("Избранное: " + favoritesCount);

        // Данные для диаграммы
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, viewsCount)); // Столбец для просмотров
        entries.add(new BarEntry(1f, favoritesCount)); // Столбец для избранных

        BarDataSet dataSet = new BarDataSet(entries, "Статистика");
        dataSet.setColors(new int[]{Color.GREEN, Color.YELLOW}); // Цвета для каждого столбца

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f); // Ширина столбцов
        barChart.setData(data);
        barChart.invalidate(); // Обновление диаграммы

        // Настройка отображения осей и подписи
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Просмотры", "Избранное"}));
        barChart.getDescription().setEnabled(false); // Убрать описание
        barChart.getAxisRight().setEnabled(false); // Отключить правую ось

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_report, null);
        builder.setView(dialogView);

        EditText titleInput = dialogView.findViewById(R.id.report_title);
        EditText messageInput = dialogView.findViewById(R.id.report_message);

        builder.setTitle("Сообщить о проблеме")
                .setPositiveButton("Отправить", (dialog, id) -> {
                    String title = titleInput.getText().toString().trim();
                    String message = messageInput.getText().toString().trim();
                    if (!title.isEmpty() && !message.isEmpty()) {
                        reportRepository.addReport(title, message, card.getCardId(), mAuth.getCurrentUser().getUid());
                    } else {
                        // Обработка случая с пустыми полями
                    }
                })
                .setNegativeButton("Отмена", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

    private final ActivityResultLauncher<Intent> editCardLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    BusinessCard updatedCard = (BusinessCard) result.getData().getSerializableExtra(EditActivity.EXTRA_UPDATED_CARD);
                    if (updatedCard != null) {
                        updateUI(updatedCard);
                    }
                }
            }
    );

    // Метод для обновления UI
    private void updateUI(BusinessCard updatedCard) {
        card = updatedCard; // Обновляем текущую визитку
        title.setText(card.getTitle());
        description.setText(card.getDescription());
        phone.setText(card.getNumber());
        email.setText(card.getEmail());
        site.setText(card.getSite());

        Glide.with(this)
                .load(card.getImageUrl())
                .into(imageView);

        setupSocialLinks(card); // Повторно инициализируем социальные ссылки
    }



}

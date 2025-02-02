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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


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

import volosyuk.easybizcard.models.BusinessCardv0_5;
import volosyuk.easybizcard.utils.BusinessCardRepository;
import volosyuk.easybizcard.utils.QRCodeGenerator;
import volosyuk.easybizcard.utils.ReportRepository;
import volosyuk.easybizcard.utils.UserRepository;

public class BusinessCardDetailActivity extends AppCompatActivity {

    private final int REQUEST_CODE_SEND_QR = 1203;
    private UserRepository userRepository;
    public final static String EXTRA_CARD = "businessCard";
    private boolean isMarked = false;


    private View bar;
    private ImageView imageView;
    private TextView title, description, number, email, site, titleSocial;
    private ImageButton qrCodeBtn, whatsapp, viber, telegram, facebook, vkontakte, instagram, bookmark, report, analytics, delete, edit, menu;
    private BusinessCardRepository businessCardRepository;
    private FirebaseAuth mAuth;
    private BusinessCardv0_5 card;
    private ReportRepository reportRepository;
    private ScrollView mainLayout;
    private int sample;
    private boolean isMenuOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        card = (BusinessCardv0_5) getIntent().getSerializableExtra(EXTRA_CARD);
        sample = card.getSample();
        switch (sample){
            case 1:
                setContentView(R.layout.sample_1);
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sample_1), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
                imageView = findViewById(R.id.sample_1_image);
                title = findViewById(R.id.sample_1_title);
                description = findViewById(R.id.sample_1_description);
                number = findViewById(R.id.sample_1_number);
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
                mainLayout = findViewById(R.id.sample_1);
                titleSocial = findViewById(R.id.sample_1_links_title);
                bar = findViewById(R.id.sample_1_links_bar);

                break;

            case 2:
                setContentView(R.layout.sample_2);
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sample_2), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
                imageView = findViewById(R.id.sample_2_image);
                title = findViewById(R.id.sample_2_title);
                description = findViewById(R.id.sample_2_description);
                number = findViewById(R.id.sample_2_number);
                email = findViewById(R.id.sample_2_email);
                site = findViewById(R.id.sample_2_site);
                qrCodeBtn = findViewById(R.id.sample_2_share);
                whatsapp = findViewById(R.id.sample_2_links_whatsapp);
                viber = findViewById(R.id.sample_2_links_viber);
                telegram = findViewById(R.id.sample_2_links_telegram);
                facebook = findViewById(R.id.sample_2_links_facebook);
                vkontakte = findViewById(R.id.sample_2_links_vkontakte);
                instagram = findViewById(R.id.sample_2_links_instagram);
                bookmark = findViewById(R.id.sample_2_bookmark);
                report = findViewById(R.id.sample_2_report);
                menu = findViewById(R.id.sample_2_menu);
                mainLayout = findViewById(R.id.sample_2);
                titleSocial = findViewById(R.id.sample_2_links_title);
                bar = findViewById(R.id.sample_2_links_bar);

                break;

            case 3:
                setContentView(R.layout.sample_3);
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sample_3), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
                imageView = findViewById(R.id.sample_3_image);
                title = findViewById(R.id.sample_3_title);
                description = findViewById(R.id.sample_3_description);
                number = findViewById(R.id.sample_3_number);
                email = findViewById(R.id.sample_3_email);
                site = findViewById(R.id.sample_3_site);
                qrCodeBtn = findViewById(R.id.sample_3_share);
                whatsapp = findViewById(R.id.sample_3_links_whatsapp);
                viber = findViewById(R.id.sample_3_links_viber);
                telegram = findViewById(R.id.sample_3_links_telegram);
                facebook = findViewById(R.id.sample_3_links_facebook);
                vkontakte = findViewById(R.id.sample_3_links_vkontakte);
                instagram = findViewById(R.id.sample_3_links_instagram);
                bookmark = findViewById(R.id.sample_3_bookmark);
                report = findViewById(R.id.sample_3_report);
                mainLayout = findViewById(R.id.sample_3);
                titleSocial = findViewById(R.id.sample_3_links_title);
                bar = findViewById(R.id.sample_3_links_bar);

                break;
            default:
                break;
        }
        // Получаем данные о визитке, переданные через Intent
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db =  FirebaseFirestore.getInstance();
        businessCardRepository = new BusinessCardRepository(db);
        reportRepository = new ReportRepository();
        userRepository = new UserRepository(db, mAuth);



        // Заполняем данными
        if (card != null) {
            Glide.with(this)
                    .load(card.getImageUrl())  // Загрузка изображения
                    .into(imageView);

            title.setText(card.getTitle());
            description.setText(card.getDescription());
            number.setText(card.getNumber());
            email.setText(card.getEmail());
            site.setText(card.getSite());
            setTextColor(mainLayout, Color.parseColor(card.getTextColor()));
            mainLayout.setBackgroundColor(Color.parseColor(card.getBackgroundColor()));
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
            if (sample == 2){
                menu.setOnClickListener(v -> {
                    isMenuOpen = !isMenuOpen;
                    toggleMenu(isMenuOpen, result);
                });

            }

            if(mAuth.getCurrentUser() != null){

                if(mAuth.getCurrentUser().getUid().equals(card.getUserId()) || result){
                    switch (sample){
                        case 1:
                            analytics = findViewById(R.id.sample_1_analytics);
                            delete = findViewById(R.id.sample_1_delete);
                            edit = findViewById(R.id.sample_1_edit_btn);
                            analytics.setVisibility(View.VISIBLE);
                            delete.setVisibility(View.VISIBLE);
                            edit.setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            analytics = findViewById(R.id.sample_2_analytics);
                            delete = findViewById(R.id.sample_2_delete);
                            edit = findViewById(R.id.sample_2_edit_btn);
                            break;
                        case 3:
                            analytics = findViewById(R.id.sample_3_analytics);
                            delete = findViewById(R.id.sample_3_delete);
                            edit = findViewById(R.id.sample_3_edit_btn);
                            analytics.setVisibility(View.VISIBLE);
                            delete.setVisibility(View.VISIBLE);
                            edit.setVisibility(View.VISIBLE);
                            break;
                    }

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
                        intent.putExtra(EditActivity.EXTRA_LAYOUT, sample);
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

    private void setupSocialLinks(BusinessCardv0_5 card) {
        setupLink(card.getWhatsApp(), whatsapp);
        setupLink(card.getViber(), viber);
        setupLink(card.getTelegram(), telegram);
        setupLink(card.getFacebook(), facebook);
        setupLink(card.getVk(), vkontakte);
        setupLink(card.getInstagram(), instagram);
        // Скрываем весь блок социальных сетей, если все ссылки пусты
        if (card.getWhatsApp() == null && card.getViber() == null && card.getTelegram() == null && card.getFacebook() == null && card.getVk() == null && card.getInstagram() == null) {
            titleSocial.setVisibility(View.GONE);
            bar.setVisibility(View.GONE);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
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
                    BusinessCardv0_5 updatedCard = (BusinessCardv0_5) result.getData().getSerializableExtra(EditActivity.EXTRA_UPDATED_CARD);
                    if (updatedCard != null) {
                        updateUI(updatedCard);
                    }
                }
            }
    );

    // Метод для обновления UI
    private void updateUI(BusinessCardv0_5 updatedCard) {
        card = updatedCard; // Обновляем текущую визитку
        title.setText(card.getTitle());
        description.setText(card.getDescription());
        number.setText(card.getNumber());
        email.setText(card.getEmail());
        site.setText(card.getSite());
        setTextColor(mainLayout, Color.parseColor(card.getTextColor()));
        mainLayout.setBackgroundColor(Color.parseColor(card.getBackgroundColor()));

        Glide.with(this)
                .load(card.getImageUrl())
                .into(imageView);

        setupSocialLinks(card); // Повторно инициализируем социальные ссылки
    }

    private void setTextColor(ViewGroup layout, int color) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            } else if (child instanceof ViewGroup) {
                setTextColor((ViewGroup) child, color);
            }
        }
    }

    private void toggleMenu(boolean isMenuOpen, boolean isAdmin) {
        float startAlpha = isMenuOpen ? 1f : 0f;
        float endAlpha = isMenuOpen ? 0f : 1f;

        float startTranslationY = isMenuOpen ? 100 : 0; // Начальная позиция по Y
        float endTranslationY = isMenuOpen ? 0 : 100; // Конечная позиция по Y

        long animationDuration = 300; // Длительность анимации в миллисекундах

        // Анимация для каждой кнопки
        animateButton(qrCodeBtn, startAlpha, endAlpha, startTranslationY, endTranslationY, animationDuration);
        animateButton(bookmark, startAlpha, endAlpha, startTranslationY, endTranslationY, animationDuration);
        animateButton(report, startAlpha, endAlpha, startTranslationY, endTranslationY, animationDuration);

        if (isAdmin){
            animateButton(analytics, startAlpha, endAlpha, startTranslationY, endTranslationY, animationDuration);
            animateButton(edit, startAlpha, endAlpha, startTranslationY, endTranslationY, animationDuration);
            animateButton(delete, startAlpha, endAlpha, startTranslationY, endTranslationY, animationDuration);
        }
    }

    private void animateButton(View button, float startAlpha, float endAlpha, float startTranslationY, float endTranslationY, long duration) {
        button.setVisibility(View.VISIBLE); // Убедимся, что кнопка видна

        button.setAlpha(startAlpha);
        button.setTranslationY(startTranslationY);

        button.animate()
                .alpha(endAlpha) // Прозрачность
                .translationY(endTranslationY) // Позиция
                .setDuration(duration)
                .withEndAction(() -> {
                    if (endAlpha == 0f) {
                        button.setVisibility(View.GONE); // Скрыть, если конечная альфа = 0
                    }
                })
                .start();
    }

}

package volosyuk.easybizcard;

import static volosyuk.easybizcard.TemplateEditorActivity.MM_TO_POINTS;
import static volosyuk.easybizcard.models.BusinessCard.STATUS_LABELS;
import static volosyuk.easybizcard.models.BusinessCard.STATUS_VALUES;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import volosyuk.easybizcard.models.BusinessCard;
import volosyuk.easybizcard.models.BusinessCardElement;
import volosyuk.easybizcard.models.SocialNetwork;
import volosyuk.easybizcard.utils.BusinessCardRepository;
import volosyuk.easybizcard.utils.QRCodeGenerator;
import volosyuk.easybizcard.utils.UserRepository;

public class BusinessCardViewActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "card_id";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private ConstraintLayout mainLayout;
    private static final int REQUEST_EDIT = 1;
    private ArrayList<BusinessCardElement> elements = new ArrayList<>();
    private LinearLayout layoutContainer, statusLayout;
    private ImageButton btnMenu, btnEdit, btnBookmark, btnShare, btnDelete, btnAnalytics;
    private boolean menuIsOpen = false;
    private String ownerId, currentUserId, cardId;
    private boolean isMarked, isAdmin;
    private UserRepository userRepository;
    private BusinessCardRepository businessCardRepository;
    private int backColor;
    private Handler handler;
    private Runnable hideButtonRunnable;
    private Spinner adminStatusSpinner;
    private Button adminStatusBtn;
    private BusinessCard.Status status;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_card_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.business_card_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cardId = getIntent().getStringExtra(EXTRA_ID);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository(db, mAuth);
        businessCardRepository = new BusinessCardRepository(db);

        currentUserId = mAuth.getCurrentUser().getUid();
        userRepository.isCardBookmarked(cardId).thenAccept(result -> {
            isMarked = result;
            if(isMarked){
                btnBookmark.setImageResource(R.drawable.icon_bookmark_remove);
            }
        });
        userRepository.isActiveUserAdmin().thenAccept(result -> {
            isAdmin = result;
        });

        mainLayout = findViewById(R.id.business_card_view);
        btnMenu = findViewById(R.id.btn_menu_view);
        btnEdit = findViewById(R.id.btn_edit_view);
        btnBookmark = findViewById(R.id.btn_bookmark_view);
        btnShare = findViewById(R.id.btn_share_view);
        btnDelete = findViewById(R.id.btn_delete_view);
        btnAnalytics = findViewById(R.id.btn_analytics_view);
        statusLayout = findViewById(R.id.admin_status_btns);
        adminStatusSpinner = findViewById(R.id.spinnerStatus);
        adminStatusBtn = findViewById(R.id.btnSaveStatus);
        layoutContainer = findViewById(R.id.business_card_view_content);

        adminStatusBtn.setOnClickListener(v -> {
            updateCardStatus();
        });

        setupMenu();

        handler = new Handler();
        hideButtonRunnable = () -> {
            if (!menuIsOpen) {
                hideButtonWithAnimation(); // Анимация исчезновения
            }
        };

        loadBusinessCard(cardId);
        // Запускаем таймер при старте
        resetTimer();

        // Обрабатываем касания экрана
        mainLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                showButtonWithAnimation(); // Анимация появления
                resetTimer(); // Перезапуск таймера
            }
            return false;
        });

    }

    private void setupMenu() {
        btnBookmark.setOnClickListener(v -> {
            changeBookmark();
        });

        btnEdit.setOnClickListener(v -> {
            editCard();
        });

        btnShare.setOnClickListener(v -> {
            share();
        });

        btnDelete.setOnClickListener(v -> {
            deleteCard();
        });

        btnAnalytics.setOnClickListener(v -> {
                    businessCardRepository.getViewsAndFavoritesCount(cardId).thenAccept(result -> {
                        showStatsDialog(result[0], result[1]);
                    });
        });

        btnMenu.setOnClickListener(v -> {
            if (menuIsOpen) {
                btnBookmark.setVisibility(View.GONE);
                btnShare.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnAnalytics.setVisibility(View.GONE);
            } else {
                btnBookmark.setVisibility(View.VISIBLE);
                btnShare.setVisibility(View.VISIBLE);
                if (Objects.equals(ownerId, currentUserId)) {
                    btnEdit.setVisibility(View.VISIBLE);
                    btnDelete.setVisibility(View.VISIBLE);
                    btnAnalytics.setVisibility(View.VISIBLE);
                }
            }
            menuIsOpen = !menuIsOpen;

            resetTimer(); // Сбрасываем таймер, когда меню открывается/закрывается
        });
    }

    private void updateCardStatus() {
        int selectedIndex = adminStatusSpinner.getSelectedItemPosition();
        BusinessCard.Status newStatus = STATUS_VALUES.get(selectedIndex); // Получаем соответствующий статус

        db.collection("business_cards").document(cardId)
                .update("status", newStatus.name())
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Статус обновлен", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show()
                );
    }

    private void setupStatusSpinner() {
        // Создаем список русских названий
        List<String> statusNames = new ArrayList<>();
        for (BusinessCard.Status status : STATUS_VALUES) {
            statusNames.add(STATUS_LABELS.get(status));
        }

        // Настройка адаптера
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adminStatusSpinner.setAdapter(adapter);
    }

    // ------------------ Load elements ---------------------------

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Загрузка...");
        progressDialog.setCancelable(false);  // Блокируем действия
        progressDialog.show();
    }

    private void hideLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK && data != null) {
            cardId = getIntent().getStringExtra(EXTRA_ID);
            if (cardId != null) {
                layoutContainer.removeAllViews();
                loadBusinessCard(cardId); // Обновляем данные в UI
            }
        }
    }

    private void loadBusinessCard(String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        showLoadingDialog();

        // Сначала получаем userId и backgroundColor из Firestore
        db.collection("business_cards").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userId = documentSnapshot.getString("user_id");
                        Long bgColorLong = documentSnapshot.getLong("background_color");
                        status = BusinessCard.Status.valueOf(documentSnapshot.getString("status"));
                        int backgroundColor = (bgColorLong != null) ? bgColorLong.intValue() : 0xFFFFFFFF; // Белый по умолчанию

                        // Устанавливаем цвет фона
                        mainLayout.setBackgroundColor(backgroundColor);
                        backColor = backgroundColor;
                        ownerId = userId;

                        if (isAdmin) {
                            setupStatusSpinner(); // Инициализируем спиннер только если админ
                            adminStatusSpinner.setSelection(STATUS_VALUES.indexOf(status));
                            statusLayout.setVisibility(View.VISIBLE);
                        }

                        if (userId != null) {
                            // Теперь загружаем JSON из Firebase Storage
                            loadBusinessCardFromStorage(userId, documentId);
                        } else {
                            Log.e("Firestore", "user_id не найден!");
                        }
                    } else {
                        Log.e("Firestore", "Документ не найден в Firestore!");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при получении данных из Firestore", e));
    }

    // Метод загрузки JSON-файла из Firebase Storage
    private void loadBusinessCardFromStorage(String userId, String documentId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("business_cards/" + userId + "/json_" + documentId + ".json");

        storageRef.getBytes(1024 * 1024) // Ограничение 1MB, можно увеличить
                .addOnSuccessListener(bytes -> {
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    parseBusinessCardJson(json);
                    hideLoadingDialog();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseStorage", "Ошибка загрузки JSON", e);
                    hideLoadingDialog();
                });
    }

    // Метод для парсинга JSON в объекты
    private void parseBusinessCardJson(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<BusinessCardElement>>() {}.getType();
        elements = gson.fromJson(json, listType);
        // Теперь у тебя есть список элементов, можешь отобразить их
        displayBusinessCard(elements);
    }

    // Метод для отображения элементов визитки
    private void displayBusinessCard(List<BusinessCardElement> elements) {
        layoutContainer.removeAllViews();
        for (BusinessCardElement element : elements) {
            switch (element.getType()) {
                case "text":
                    addTextView(element);
                    break;
                case "image":
                    addImageView(element);
                    break;
                case "link":
                    addLinkView(element);
                    break;
                case "socialMedia":
                    addSocialMediaView(element);
                    break;
                case "phone":
                    addPhoneView(element);
                    break;
                case "email":
                    addEmailView(element);
                    break;
                case "divider":
                    addDividerView(element);
                    break;
            }
        }
    }

    private void addTextView(BusinessCardElement element) {
        TextView textView = new TextView(this);
        textView.setText(element.getText());
        textView.setTextSize(element.getTextSize());
        textView.setTypeface(Typeface.create(element.getFontFamily(), Typeface.NORMAL));
        textView.setTextColor(element.getColorText());
        textView.setGravity(element.getAlignment());

        if (element.isBold()) textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        if (element.isItalic()) textView.setTypeface(textView.getTypeface(), Typeface.ITALIC);
        if (element.isUnderline()) textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (element.isStrikethrough()) textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        layoutContainer.addView(textView);
    }

    private void addImageView(BusinessCardElement element) {
        ImageView imageView = new ImageView(this);
        Picasso.get().load(element.getImageUrl()).into(imageView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 0);
        imageView.setLayoutParams(params);
        imageView.setPadding(0, 0, 0, 0);

        layoutContainer.addView(imageView);
    }

    private void addLinkView(BusinessCardElement element) {
        TextView linkView = new TextView(this);
        linkView.setText(element.getHyperText());
        linkView.setTextSize(element.getTextSize());
        linkView.setTypeface(Typeface.create(element.getFontFamily(), Typeface.NORMAL));
        linkView.setTextColor(element.getColorText());
        linkView.setGravity(element.getAlignment());

        if (element.isBold()) linkView.setTypeface(linkView.getTypeface(), Typeface.BOLD);
        if (element.isItalic()) linkView.setTypeface(linkView.getTypeface(), Typeface.ITALIC);
        if (element.isUnderline()) linkView.setPaintFlags(linkView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (element.isStrikethrough()) linkView.setPaintFlags(linkView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        linkView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Подтвердить действие")
                    .setMessage("Вы уверены, что хотите перейти по ссылке: " + element.getLink() + "? Мы не обеспечиваем безопасность внешних ресурсов.")
                    .setPositiveButton("Перейти", (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(element.getLink()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        layoutContainer.addView(linkView);
    }

    private void addDividerView(BusinessCardElement element) {
        // Создаем разделитель
        View divider = new View(this);

        // Конвертируем высоту и ширину в пиксели, используя плотность экрана
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                (int) (element.getWidth() * getResources().getDisplayMetrics().density),
                (int) (element.getTextSize() * getResources().getDisplayMetrics().density)
        );

        // Устанавливаем цвет для разделителя
        divider.setBackgroundColor(element.getColorText());

        // Устанавливаем выравнивание
        switch (element.getAlignment()) {
            case Gravity.CENTER:
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL; // Выровнять по центру
                break;
            case Gravity.LEFT:
                layoutParams.gravity = Gravity.START; // Выровнять по левому краю
                break;
            case Gravity.RIGHT:
                layoutParams.gravity = Gravity.END; // Выровнять по правому краю
                break;
        }

        // Устанавливаем параметры для разделителя
        divider.setLayoutParams(layoutParams);

        // Добавляем разделитель в контейнер
        layoutContainer.addView(divider);
    }

    private void addSocialMediaView(BusinessCardElement element) {
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        scrollView.setLayoutParams(scrollParams);


        LinearLayout socialLayout = new LinearLayout(this);
        socialLayout.setOrientation(LinearLayout.HORIZONTAL);
        socialLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        // Карта соответствий ссылки и иконки
        Map<String, Integer> socialIcons = getSocialIconsMap();

        if(element.getLinks() != null){
            for (String link : element.getLinks()) {
                ImageView icon = new ImageView(this);

                // Определяем иконку по ссылке
                int iconResId = getIconForLink(link, socialIcons);
                icon.setImageResource(iconResId);

                int size = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        48, // Размер в dp
                        getResources().getDisplayMetrics()
                );
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
                iconParams.setMargins(16, 16, 24, 16);
                icon.setLayoutParams(iconParams);

                icon.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(intent);
                });

                socialLayout.addView(icon);
            }
        }

        scrollView.addView(socialLayout);

        layoutContainer.addView(scrollView);
    }

    private int getIconForLink(String link, Map<String, Integer> socialIcons) {
        for (Map.Entry<String, Integer> entry : socialIcons.entrySet()) {
            if (link.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return R.drawable.placeholder_image; // Если нет совпадения, используем заглушку
    }

    private Map<String, Integer> getSocialIconsMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("https://wa.me/", R.drawable.whatsapp);
        map.put("https://vk.com/", R.drawable.vk);
        map.put("https://viber.click/", R.drawable.viber);
        map.put("https://t.me/", R.drawable.telegram);
        map.put("https://instagram.com/", R.drawable.instagram);
        map.put("https://facebook.com/", R.drawable.facebook);
        map.put("https://snapchat.com/add/", R.drawable.snapchat);
        map.put("https://linkedin.com/in/", R.drawable.linkedin);
        map.put("https://ok.ru/profile/", R.drawable.ok);
        map.put("https://figma.com/@", R.drawable.figma);
        map.put("https://dribbble.com/", R.drawable.dribbble);
        map.put("https://www.tiktok.com/@", R.drawable.tiktok);
        map.put("https://discord.com/users/", R.drawable.discord);
        map.put("skype:", R.drawable.skype);
        map.put("https://www.epicgames.com/id/", R.drawable.epicgames);
        map.put("https://open.spotify.com/user/", R.drawable.spotify);
        map.put("https://steamcommunity.com/id/", R.drawable.steam);
        return map;
    }

    private void addPhoneView(BusinessCardElement element) {
        TextView phoneView = new TextView(this);
        phoneView.setText(element.getText());
        phoneView.setTextSize(element.getTextSize());
        phoneView.setTypeface(Typeface.create(element.getFontFamily(), Typeface.NORMAL));
        phoneView.setTextColor(element.getColorText());
        phoneView.setGravity(element.getAlignment());

        if (element.isBold()) phoneView.setTypeface(phoneView.getTypeface(), Typeface.BOLD);
        if (element.isItalic()) phoneView.setTypeface(phoneView.getTypeface(), Typeface.ITALIC);
        if (element.isUnderline()) phoneView.setPaintFlags(phoneView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (element.isStrikethrough()) phoneView.setPaintFlags(phoneView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        phoneView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + element.getText()));
            startActivity(intent);
        });

        layoutContainer.addView(phoneView);
    }

    private void addEmailView(BusinessCardElement element) {
        TextView emailView = new TextView(this);
        emailView.setText(element.getText());
        emailView.setTextSize(element.getTextSize());
        emailView.setTypeface(Typeface.create(element.getFontFamily(), Typeface.NORMAL));
        emailView.setTextColor(element.getColorText());
        emailView.setGravity(element.getAlignment());

        if (element.isBold()) emailView.setTypeface(emailView.getTypeface(), Typeface.BOLD);
        if (element.isItalic()) emailView.setTypeface(emailView.getTypeface(), Typeface.ITALIC);
        if (element.isUnderline()) emailView.setPaintFlags(emailView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (element.isStrikethrough()) emailView.setPaintFlags(emailView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        emailView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + element.getText()));
            startActivity(intent);
        });

        layoutContainer.addView(emailView);
    }


    // ------------------ Buttons for everyone ---------------------------


    private void changeBookmark(){
        if(isMarked){
            isMarked = false;
            userRepository.removeCardFromBookmarks(cardId);
            btnBookmark.setImageResource(R.drawable.icon_bookmark_add);
        }
        else{
            isMarked = true;
            userRepository.addCardToBookmarks(cardId);
            btnBookmark.setImageResource(R.drawable.icon_bookmark_remove);
        }
    }

    private void share() {
        // Массив вариантов для отображения
        final String[] shareOptions = {"Ссылкой", "Как изображение", "Как PDF файл"};

        new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Поделиться визиткой")
                .setItems(shareOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Ссылкой
                            shareLink();
                            break;
                        case 1: // Изображением
                            shareImg();
                            break;
                        case 2: // PDF
                            sharePdf();
                            break;
                    }
                })
                .setNegativeButton("Отмена", null)
                .create()
                .show();
    }

    private void shareLink() {
        try {
            // Формируем ссылку
            String baseUrl = "https://easybizcard.com/";
            String shareUrl = baseUrl + ownerId + "/" + cardId;

            // Создаем интент для шаринга
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);

            // Добавляем описание для диалога выбора
            String shareTitle = "Поделиться визиткой";

            // Показываем системный диалог выбора приложения
            startActivity(Intent.createChooser(shareIntent, shareTitle));

        } catch (ActivityNotFoundException e) {
            // Обработка случая, когда нет приложений для шаринга
            Toast.makeText(this,
                    "Не найдено приложений для отправки",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImg(){
        exportBusinessCardAsImage();
    }

    private void sharePdf(){
        checkPermissionsAndGeneratePdf();
    }

    private void prepareLayoutToExport(){
        for (int i = 0; i < layoutContainer.getChildCount(); i++) {
            View childView = layoutContainer.getChildAt(i);

            if (childView instanceof HorizontalScrollView) {
                childView.setVisibility(View.GONE);
                List<String> links = elements.get(i).getLinks();
                for (String link : links) {
                    // Создаем новый TextView для отображения соцсети
                    TextView textView1 = new TextView(this);

                    // Находим название социальной сети по ссылке
                    String socialNetworkName = getSocialNetworkNameByLink(link);
                    textView1.setText(socialNetworkName + ": ");
                    textView1.setTextSize(16);
                    textView1.setGravity(Gravity.START);
                    textView1.setTextColor(Color.BLACK);
                    setContrastTextColor(textView1, mainLayout);

                    // Создаем второй TextView для отображения самой ссылки
                    TextView textView2 = new TextView(this);
                    textView2.setText(link);
                    textView2.setTextSize(16);
                    textView2.setGravity(Gravity.START);
                    setContrastTextColor(textView2, mainLayout);

                    // Устанавливаем параметры для TextView
                    textView1.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    textView2.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                    // Добавляем textView1 и textView2 в контейнер
                    layoutContainer.addView(textView1);
                    layoutContainer.addView(textView2);
                }
            }
        }

        layoutContainer.setBackgroundColor(backColor);

        // Обновляем layoutContainer перед созданием картинки
        layoutContainer.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        layoutContainer.layout(0, 0, layoutContainer.getMeasuredWidth(), layoutContainer.getMeasuredHeight());


        // Убедимся, что все элементы перерисованы
        layoutContainer.requestLayout();
        layoutContainer.invalidate();
    }

    private void exportBusinessCardAsImage() {
        // Скрываем ScrollView

        ImageView QRimage = new ImageView(this);
        if(cardId == null){
            cardId = FirebaseFirestore.getInstance().collection("business_cards").document().getId();
        }

        prepareLayoutToExport();

        Bitmap QRbitmap = QRCodeGenerator.generateQRCode("https://easybizcard/" + ownerId + "/" + cardId);
        QRimage.setImageBitmap(QRbitmap);
        layoutContainer.addView(QRimage);

        // Обновляем layoutContainer перед созданием картинки
        layoutContainer.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        layoutContainer.layout(0, 0, layoutContainer.getMeasuredWidth(), layoutContainer.getMeasuredHeight());


        // Убедимся, что все элементы перерисованы
        layoutContainer.requestLayout();
        layoutContainer.invalidate();

        // Создаем Bitmap из layoutContainer с учетом фона mainLayout
        Bitmap bitmap = getBitmapFromViewWithBackground(layoutContainer, mainLayout);

        if (bitmap != null) {
            try {
                // Сохранение изображения
                File file = saveBitmapToFile(bitmap);

                if (file != null) {
                    Toast.makeText(this, "Визитка сохранена: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    displayBusinessCard(elements);
                    // Поделиться изображением
                    shareImage(file);
                } else {
                    Toast.makeText(this, "Ошибка сохранения изображения", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка экспорта: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int getColorBrightness(int color) {
        // Получаем компоненты RGB
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        // Применяем формулу для вычисления яркости
        return (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }

    private void setContrastTextColor(TextView textView, View backgroundView) {
        // Получаем цвет фона
        int backgroundColor = ((ColorDrawable) backgroundView.getBackground()).getColor();

        // Вычисляем яркость фона
        int brightness = getColorBrightness(backgroundColor);

        // Если яркость фона темная, делаем текст светлым, иначе темным
        if (brightness < 128) {
            textView.setTextColor(Color.WHITE);  // Белый для темного фона
        } else {
            textView.setTextColor(Color.BLACK);  // Черный для светлого фона
        }
    }

    private String getSocialNetworkNameByLink(String link) {
        List<SocialNetwork> socialNetworks = Arrays.asList(
                new SocialNetwork("Ватсапп", R.drawable.whatsapp, "https://wa.me/{phone}"),
                new SocialNetwork("ВКонтакте", R.drawable.vk, "https://vk.com/{username}"),
                new SocialNetwork("Вайбер", R.drawable.viber, "https://viber.click/{phone}"),
                new SocialNetwork("Телеграм", R.drawable.telegram, "https://t.me/{username}"),
                new SocialNetwork("Инстаграм", R.drawable.instagram, "https://instagram.com/{username}"),
                new SocialNetwork("Фейсбук", R.drawable.facebook, "https://facebook.com/{username}"),
                new SocialNetwork("Снапчат", R.drawable.snapchat, "https://snapchat.com/add/{username}"),
                new SocialNetwork("Линкедин", R.drawable.linkedin, "https://linkedin.com/in/{username}"),
                new SocialNetwork("Одноклассники", R.drawable.ok, "https://ok.ru/profile/{username}"),
                new SocialNetwork("Фигма", R.drawable.figma, "https://figma.com/@{username}"),
                new SocialNetwork("Дрибл", R.drawable.dribbble, "https://dribbble.com/{username}"),
                new SocialNetwork("ТикТок", R.drawable.tiktok, "https://www.tiktok.com/@{username}"),
                new SocialNetwork("Дискорд", R.drawable.discord, "https://discord.com/users/{username}"),
                new SocialNetwork("Скайп", R.drawable.skype, "skype:{username}?chat"),
                new SocialNetwork("Эпик Геймс", R.drawable.epicgames, "https://www.epicgames.com/id/{username}"),
                new SocialNetwork("Спотифай", R.drawable.spotify, "https://open.spotify.com/user/{username}"),
                new SocialNetwork("Стим", R.drawable.steam, "https://steamcommunity.com/id/{username}")
        );

        for (SocialNetwork network : socialNetworks) {
            // Проверяем, если ссылка соответствует шаблону
            if (link.matches(network.getUrlTemplate().replace("{username}", ".*").replace("{phone}", ".*"))) {
                return network.getName();  // Возвращаем имя соцсети, если совпало
            }
        }
        return "Неизвестная сеть";  // Если соцсеть не найдена
    }

    private Bitmap getBitmapFromViewWithBackground(View view, View backgroundView) {
        // Получаем цвет фона из mainLayout
        int backgroundColor = ((ColorDrawable) backgroundView.getBackground()).getColor();

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(backgroundColor);  // Заполняем фон основным цветом

        view.draw(canvas);
        return bitmap;
    }

    private File saveBitmapToFile(Bitmap bitmap) throws IOException {
        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "BusinessCards");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "business_card_" + System.currentTimeMillis() + ".png";
        File file = new File(directory, fileName);

        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();

        return file;
    }

    private void shareImage(File file) {
        Uri uri = FileProvider.getUriForFile(this, "volosyuk.easybizcard.fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Поделиться визиткой"));
    }

    private void checkPermissionsAndGeneratePdf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Для Android 11 и выше
            // Просто генерируем PDF, без запроса дополнительных разрешений
            generatePrintDocument();
        } else {
            // Для Android 10 и ниже проверяем обычное разрешение
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                generatePrintDocument(); // Разрешение получено, генерируем PDF
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePrintDocument();
            } else {
                Toast.makeText(this, "Разрешение требуется для создания PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatePrintDocument() {
        // Подготовка данных для первой страницы
        prepareLayoutToExport();

        PdfDocument pdfDocument = new PdfDocument();

        // Размеры визитки (85x55 мм)
        int pageHeight = (int) (85 * MM_TO_POINTS);
        int pageWidth = (int) (55 * MM_TO_POINTS);

        // Страница 1: Информация
        PdfDocument.PageInfo pageInfo1 = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page1 = pdfDocument.startPage(pageInfo1);
        Canvas canvas1 = page1.getCanvas();

        // Установка цвета фона
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(backColor); // Используем выбранный цвет
        canvas1.drawRect(0, 0, pageWidth, pageHeight, backgroundPaint);

        // Получаем содержимое визитки как Bitmap
        Bitmap contentBitmap = getBitmapFromView(layoutContainer);

        // Масштабирование, чтобы контент поместился
        float scale = Math.min(
                pageWidth / (float) contentBitmap.getWidth(),
                pageHeight / (float) contentBitmap.getHeight()
        );

        // Новые размеры после масштабирования
        int newWidth = (int) (contentBitmap.getWidth() * scale);
        int newHeight = (int) (contentBitmap.getHeight() * scale);

        // Определение координат для центрирования
        float left = (pageWidth - newWidth) / 2f;
        float top = (pageHeight - newHeight) / 2f;

        // Создаём матрицу для масштабирования и центрирования
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        matrix.postTranslate(left, top);

        // Отрисовка содержимого визитки
        canvas1.drawBitmap(contentBitmap, matrix, null);

        pdfDocument.finishPage(page1);

        // Страница 2: QR-код
        PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create();
        PdfDocument.Page page2 = pdfDocument.startPage(pageInfo2);
        drawContentOnPage(page2.getCanvas(), pageWidth, pageHeight, true);
        pdfDocument.finishPage(page2);

        // Сохранение PDF
        File file = savePdfDocument(pdfDocument);
        if (file != null) {
            openPdfFile(file);
        }

        pdfDocument.close();
        displayBusinessCard(elements);
    }

    private void drawContentOnPage(Canvas canvas, int pageWidth, int pageHeight, boolean isQrPage) {
        if (isQrPage) {
            // Генерация QR-кода
            if (cardId == null) cardId = FirebaseFirestore.getInstance().collection("business_cards").document().getId();
            Bitmap qrBitmap = QRCodeGenerator.generateQRCode("https://easybizcard.com/" + ownerId + "/" + cardId);

            if (qrBitmap != null) {
                // Масштабирование QR-кода
                int size = Math.min(pageWidth, pageHeight) * 2/3;
                qrBitmap = Bitmap.createScaledBitmap(qrBitmap, size, size, true);

                // Центрирование
                float left = (pageWidth - qrBitmap.getWidth()) / 2f;
                float top = (pageHeight - qrBitmap.getHeight()) / 2f;
                canvas.drawBitmap(qrBitmap, left, top, null);
            }
        } else {
            // Отрисовка информации
            Bitmap infoBitmap = getBitmapFromView(layoutContainer);
            if (infoBitmap != null) {
                Matrix matrix = new Matrix();
                float scale = Math.min(
                        pageWidth / (float) infoBitmap.getWidth(),
                        pageHeight / (float) infoBitmap.getHeight()
                );
                matrix.postScale(scale, scale);
                canvas.drawBitmap(infoBitmap, matrix, null);
            }
        }
    }

    private Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) bgDrawable.draw(canvas);
        else canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return bitmap;
    }

    private File savePdfDocument(PdfDocument pdfDocument) {
        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "BusinessCards");
        if (!directory.exists() && !directory.mkdirs()) return null;

        File file = new File(directory, "business_card_" + System.currentTimeMillis() + ".pdf");
        if (file.exists()) {
            file.delete();
        }
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openPdfFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, "volosyuk.easybizcard.fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Установите программу для просмотра PDF", Toast.LENGTH_SHORT).show();
        }
    }


    // ------------------ Buttons for admin/creator ---------------------------


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

        // Настройка оси Y
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Установить минимальное значение оси Y равным 0
        leftAxis.setGranularity(1f); // Для корректного отображения шагов на оси Y

        // Опционально: Если хотите установить максимальное значение оси Y в зависимости от данных
        float maxValue = Math.max(viewsCount, favoritesCount);
        leftAxis.setAxisMaximum(maxValue + 10); // Добавить небольшой запас сверху

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteCard(){
        new AlertDialog.Builder(this)
                .setTitle("Удаление визитки")
                .setMessage("Вы точно уверены, данные будут удалены БЕЗ ВОЗМОЖНОСТИ ВОЗВРАТА?")
                .setPositiveButton("Ок", (dialog, which) -> {
                    businessCardRepository.deleteBusinessCardById(cardId);
                    finish();
                })
                .setNegativeButton("Отмена", (dialog, which) -> {
                    // Закрытие диалога
                    dialog.dismiss();
                })
                .show();
    }

    private void editCard(){
            Intent intent = new Intent(this, TemplateEditorActivity.class);
            intent.putExtra(TemplateEditorActivity.EXTRA_CARD, elements);
            intent.putExtra(TemplateEditorActivity.EXTRA_CARD_BACK, ((ColorDrawable) mainLayout.getBackground()).getColor());
            intent.putExtra(TemplateEditorActivity.EXTRA_CARD_ID, cardId);
            startActivityForResult(intent, REQUEST_EDIT);
    }


    // ------------------ Hide button ---------------------------

    private void resetTimer() {
        handler.removeCallbacks(hideButtonRunnable); // Убираем старый таймер
        if(isAdmin)         handler.postDelayed(hideButtonRunnable, 5000); // Запускаем новый на 2 секунды
        else         handler.postDelayed(hideButtonRunnable, 2000); // Запускаем новый на 2 секунды

    }

    private void showButtonWithAnimation() {
        if (btnMenu.getVisibility() != View.VISIBLE) {
            btnMenu.setVisibility(View.VISIBLE);
            animateFadeIn(btnMenu);
        }
        if (isAdmin) {
            if (adminStatusSpinner.getVisibility() != View.VISIBLE) {
                adminStatusSpinner.setVisibility(View.VISIBLE);
                adminStatusBtn.setVisibility(View.VISIBLE);

                adminStatusSpinner.requestLayout();
                adminStatusSpinner.invalidate();

                animateFadeIn(adminStatusSpinner);
                animateFadeIn(adminStatusBtn);
            }
        }
    }

    private void animateFadeIn(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }


    private void hideButtonWithAnimation() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(btnMenu, "alpha", 1f, 0f);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                btnMenu.setVisibility(View.GONE);
            }
        });

        if(isAdmin){
            animator = ObjectAnimator.ofFloat(adminStatusBtn, "alpha", 1f, 0f);
            animator.setDuration(300);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    adminStatusBtn.setVisibility(View.GONE);
                }
            });
            animator = ObjectAnimator.ofFloat(adminStatusSpinner, "alpha", 1f, 0f);
            animator.setDuration(300);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    adminStatusSpinner.setVisibility(View.GONE);
                }
            });
        }
    }

}
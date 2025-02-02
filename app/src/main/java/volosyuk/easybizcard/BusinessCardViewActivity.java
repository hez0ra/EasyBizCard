package volosyuk.easybizcard;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import volosyuk.easybizcard.models.BusinessCardElement;

public class BusinessCardViewActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "card_id";
    private static final int REQUEST_EDIT = 1;
    private ArrayList<BusinessCardElement> elements = new ArrayList<>();
    private LinearLayout layoutContainer;
    private String cardID;
    private ImageButton btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_card_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnEdit = findViewById(R.id.btn_edit_card);
        cardID = getIntent().getStringExtra(EXTRA_ID);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, TemplateEditorActivity.class);
            intent.putExtra(TemplateEditorActivity.EXTRA_CARD, elements);
            intent.putExtra(TemplateEditorActivity.EXTRA_CARD_ID, cardID);
            startActivityForResult(intent, REQUEST_EDIT);
        });
        loadBusinessCard(cardID);

        layoutContainer = findViewById(R.id.business_card_view_content);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK && data != null) {
            cardID = getIntent().getStringExtra(EXTRA_ID);
            if (cardID != null) {
                layoutContainer.removeAllViews();
                loadBusinessCard(cardID); // Обновляем данные в UI
            }
        }
    }

    private void loadBusinessCard(String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid(); // Получаем userId текущего пользователя

        // Запрос к Firestore для получения информации о визитке
        db.collection("business_cards").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Теперь загружаем JSON из Firebase Storage
                        loadBusinessCardFromStorage(userId, documentId);
                    } else {
                        Log.e("Firestore", "Документ не найден!");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при получении визитки", e));
    }

    // Метод загрузки JSON-файла из Firebase Storage
    private void loadBusinessCardFromStorage(String userId, String documentId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("business_cards/" + userId + "/json_" + documentId + ".json");

        storageRef.getBytes(1024 * 1024) // Ограничение 1MB, можно увеличить
                .addOnSuccessListener(bytes -> {
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    parseBusinessCardJson(json);
                })
                .addOnFailureListener(e -> Log.e("FirebaseStorage", "Ошибка загрузки JSON", e));
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
}
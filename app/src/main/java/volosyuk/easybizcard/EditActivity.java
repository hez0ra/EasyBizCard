package volosyuk.easybizcard;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import volosyuk.easybizcard.models.BusinessCardv0_5;
import volosyuk.easybizcard.utils.BusinessCardRepository;

public class EditActivity extends AppCompatActivity {

    public static final String EXTRA_LAYOUT = "layout type";
    public static final String EXTRA_CARD = "card";
    public static final String EXTRA_UPDATED_CARD = "updated card";
    private static final int STORAGE_PERMISSION_CODE = 101;
    private int textColor = Color.BLACK;  // По умолчанию чёрный
    private int backgroundColor = Color.WHITE;  // По умолчанию белый

    ImageView image;
    EditText title, description, number, email, site;
    ImageButton whatsapp, telegram, viber, vkontakte, instagram, facebook, backgroundColorBtn, textColorBtn;
    Button save;

    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;
    private Map<String, String> links = new HashMap<>();
    private String imageUrl;
    private Bitmap selectedBitmap;
    private BusinessCardRepository businessCardRepository;
    private ScrollView mainLayout;
    private int sample;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        sample = getIntent().getIntExtra(EXTRA_LAYOUT, 1);
        switch (sample){
            case 1:
                setContentView(R.layout.sample_1_edit);
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sample_1_edit), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
                image = (ImageView) findViewById(R.id.sample_1_edit_image);
                title = findViewById(R.id.sample_1_edit_title);
                description = findViewById(R.id.sample_1_edit_description);
                number = findViewById(R.id.sample_1_edit_number);
                email = findViewById(R.id.sample_1_edit_email);
                site = findViewById(R.id.sample_1_edit_site);
                whatsapp = findViewById(R.id.sample_1_edit_links_whatsapp);
                viber = findViewById(R.id.sample_1_edit_links_viber);
                vkontakte = findViewById(R.id.sample_1_edit_links_vkontakte);
                instagram = findViewById(R.id.sample_1_edit_links_instagram);
                facebook = findViewById(R.id.sample_1_edit_links_facebook);
                telegram = findViewById(R.id.sample_1_edit_links_telegram);
                save = findViewById(R.id.sample_1_edit_save);
                backgroundColorBtn = findViewById(R.id.sample_1_edit_background);
                textColorBtn = findViewById(R.id.sample_1_edit_background_format_text);
                mainLayout = findViewById(R.id.sample_1_edit);
                break;

            case 2:
                setContentView(R.layout.sample_2_edit);
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sample_2_edit), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
                image = findViewById(R.id.sample_2_edit_image);
                title = findViewById(R.id.sample_2_edit_title);
                description = findViewById(R.id.sample_2_edit_description);
                number = findViewById(R.id.sample_2_edit_number);
                email = findViewById(R.id.sample_2_edit_email);
                site = findViewById(R.id.sample_2_edit_site);
                whatsapp = findViewById(R.id.sample_2_edit_links_whatsapp);
                viber = findViewById(R.id.sample_2_edit_links_viber);
                vkontakte = findViewById(R.id.sample_2_edit_links_vkontakte);
                instagram = findViewById(R.id.sample_2_edit_links_instagram);
                facebook = findViewById(R.id.sample_2_edit_links_facebook);
                telegram = findViewById(R.id.sample_2_edit_links_telegram);
                save = findViewById(R.id.sample_3_edit_save);
                backgroundColorBtn = findViewById(R.id.sample_2_edit_background);
                textColorBtn = findViewById(R.id.sample_2_edit_background_format_text);
                mainLayout = findViewById(R.id.sample_2_edit);
                break;

            case 3:
                setContentView(R.layout.sample_3_edit);
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sample_3_edit), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
                image = findViewById(R.id.sample_3_edit_image);
                title = findViewById(R.id.sample_3_edit_title);
                description = findViewById(R.id.sample_3_edit_description);
                number = findViewById(R.id.sample_3_edit_number);
                email = findViewById(R.id.sample_3_edit_email);
                site = findViewById(R.id.sample_3_edit_site);
                whatsapp = findViewById(R.id.sample_3_edit_links_whatsapp);
                viber = findViewById(R.id.sample_3_edit_links_viber);
                vkontakte = findViewById(R.id.sample_3_edit_links_vkontakte);
                instagram = findViewById(R.id.sample_3_edit_links_instagram);
                facebook = findViewById(R.id.sample_3_edit_links_facebook);
                telegram = findViewById(R.id.sample_3_edit_links_telegram);
                save = findViewById(R.id.sample_3_edit_save);
                backgroundColorBtn = findViewById(R.id.sample_3_edit_background);
                textColorBtn = findViewById(R.id.sample_3_edit_background_format_text);
                mainLayout = findViewById(R.id.sample_3_edit);
                break;

            default:
                break;
        }
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        businessCardRepository = new BusinessCardRepository(FirebaseFirestore.getInstance());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Загрузка изображения...");
        progressDialog.setCancelable(false);  // Запрещаем отмену загрузки

        image.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Проверяем наличие нового разрешения для изображений
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    requestStoragePermission();
                }
            } else {
                // Для Android 12 и ниже проверяем старое разрешение
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    requestStoragePermission();
                }
            }
        });


        save.setOnClickListener(view -> {
            if (validateInputs()) {
                // Сначала загружаем изображение на Firebase
                uploadImageToFirebase(selectedBitmap, success -> {
                    if (success) {
                        // Затем сохраняем визитку в базе данных
                        saveBusinessCardToDatabase(imageUrl);
                    }
                });
            } else {
                Toast.makeText(this, "Пожалуйста, исправьте ошибки", Toast.LENGTH_SHORT).show();
            }
        });
        whatsapp.setOnClickListener(view -> showLinkInputDialog(whatsapp));
        telegram.setOnClickListener(view -> showLinkInputDialog(telegram));
        viber.setOnClickListener(view -> showLinkInputDialog(viber));
        vkontakte.setOnClickListener(view -> showLinkInputDialog(vkontakte));
        instagram.setOnClickListener(view -> showLinkInputDialog(instagram));
        facebook.setOnClickListener(view -> showLinkInputDialog(facebook));

        // Обработка выбора цвета текста
        backgroundColorBtn.setOnClickListener(v -> {
            ColorPickerDialogBuilder
                    .with(this)
                    .setTitle("Цвет текста")
                    .initialColor(backgroundColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .density(12)
                    .setOnColorSelectedListener(new OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int selectedColor) {

                        }
                    })
                    .setPositiveButton("Подтвердить", new ColorPickerClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                            backgroundColor = selectedColor;
                            mainLayout.setBackgroundColor(selectedColor);
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .build()
                    .show();
        });

        // Обработка выбора цвета фона
        textColorBtn.setOnClickListener(v -> {
            ColorPickerDialogBuilder
                    .with(this)
                    .setTitle("Choose color")
                    .initialColor(textColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .density(12)
                    .setOnColorSelectedListener(new OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int selectedColor) {

                        }
                    })
                    .setPositiveButton("Подтвердить", new ColorPickerClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                            textColor = selectedColor;
                            updatePreview();
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .build()
                    .show();
        });

        checkIntentExtra();
    }

    private ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                            selectedBitmap = rotateImageIfRequired(selectedImageUri, selectedBitmap);
                            image.setImageBitmap(selectedBitmap);  // Устанавливаем изображение без загрузки на Firebase
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private Bitmap rotateImageIfRequired(Uri imgUri, Bitmap img) throws IOException {
        InputStream input = getContentResolver().openInputStream(imgUri);
        if (input == null) {
            return img;
        }
        ExifInterface ei = new ExifInterface(input);
        input.close();

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Bitmap bitmap, OnSuccessListener<Boolean> onComplete) {
        if (mAuth.getCurrentUser() != null && bitmap != null) {
            StorageReference userImageRef = storageRef.child("users/" + mAuth.getCurrentUser().getUid() + "/business_cards/" + System.currentTimeMillis() + ".jpeg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            progressDialog.show();

            userImageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageUrl = uri.toString();
                        progressDialog.dismiss();
                        onComplete.onSuccess(true);
                    })).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditActivity.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                        onComplete.onSuccess(false);
                    });
        } else {
            onComplete.onSuccess(false);
        }
    }

    // Проверка и запрос разрешений
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES // Для изображений
            }, STORAGE_PERMISSION_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, STORAGE_PERMISSION_CODE);
        } else {
            openGallery(); // На старых версиях разрешения задаются в манифесте
        }
    }


    // Обработка результата
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            boolean isPermissionGranted = true;

            // Проверка всех запрошенных разрешений
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isPermissionGranted = false;
                    break;
                }
            }

            if (isPermissionGranted) {
                openGallery(); // Разрешение получено
            } else {
                Toast.makeText(this, "Нет доступа к галерее", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLinkInputDialog(ImageButton button) {
        // Создаём View для диалога
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_link_input, null);
        EditText inputLink = dialogView.findViewById(R.id.input_link);
        String linkKey = getLinkKey(button);
        String existingLink = links.get(linkKey);
        if (existingLink != null) {
            inputLink.setText(existingLink);  // Заполняем EditText существующей ссылкой
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Введите ссылку")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String link = inputLink.getText().toString().trim();
                    if (!link.isEmpty()) {
                        // Здесь можно сохранить ссылку для конкретной кнопки
                        saveLinkForButton(button, link);
                    } else {
                        Toast.makeText(this, "Ссылка не может быть пустой", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private String getLinkKey(ImageButton button) {
        if (button == whatsapp) return "whatsapp";
        if (button == telegram) return "telegram";
        if (button == viber) return "viber";
        if (button == vkontakte) return "vkontakte";
        if (button == instagram) return "instagram";
        if (button == facebook) return "facebook";
        return "";
    }

    // Метод для сохранения ссылки (пример)
    private void saveLinkForButton(ImageButton button, String link) {
        if (button == whatsapp) {
            links.put("whatsapp", link);
        } else if (button == telegram) {
            links.put("telegram", link);
        } else if (button == viber) {
            links.put("viber", link);
        } else if (button == vkontakte) {
            links.put("vkontakte", link);
        } else if (button == instagram) {
            links.put("instagram", link);
        } else if (button == facebook) {
            links.put("facebook", link);
        }
    }

    private boolean validateInputs() {
        // Проверка заголовка
        if (title.getText().toString().trim().isEmpty()) {
            title.setError("Заголовок обязателен");
            title.requestFocus();
            return false;
        }

        // Проверка описания
        if (description.getText().toString().trim().isEmpty()) {
            description.setError("Описание обязательно");
            description.requestFocus();
            return false;
        }

        // Проверка номера телефона
        if (!number.getText().toString().matches("^\\+?375\\s?[\\-\\(]?\\d{2}[\\-\\)]?\\s?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}$")) {
            number.setError("Введите корректный номер телефона в формате +375 XX XXX XX XX");
            number.requestFocus();
            return false;
        }

        // Проверка email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
            email.setError("Введите корректный email");
            email.requestFocus();
            return false;
        }

        // Проверка сайта (если поле не пустое, оно должно быть корректным URL)
        String siteText = site.getText().toString().trim();
        if (!siteText.isEmpty() && !android.util.Patterns.WEB_URL.matcher(siteText).matches()) {
            site.setError("Введите корректный URL");
            site.requestFocus();
            return false;
        }

        return true;
    }

    private void saveBusinessCardToDatabase(String imageUrl) {
        String userId = mAuth.getCurrentUser().getUid();
        BusinessCardv0_5 card = new BusinessCardv0_5(userId, title.getText().toString().trim(), description.getText().toString().trim(), number.getText().toString().trim(), email.getText().toString().trim(), site.getText().toString().trim(), imageUrl, links, sample);
        try {
            businessCardRepository.addBusinessCard(card);
            Toast.makeText(this, "Успешное сохранение визитки", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка сохранения визитки", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePreview() {
        title.setTextColor(textColor);
        description.setTextColor(textColor);
        number.setTextColor(textColor);
        email.setTextColor(textColor);
        site.setTextColor(textColor);
    }

    private void checkIntentExtra(){
        BusinessCardv0_5 card = (BusinessCardv0_5) getIntent().getSerializableExtra(EXTRA_CARD);
        if(card != null){
            Glide.with(this)
                    .load(card.getImageUrl())  // Загрузка изображения
                    .into(image);

            imageUrl = card.getImageUrl();
            title.setText(card.getTitle());
            description.setText(card.getDescription());
            number.setText(card.getNumber());
            email.setText(card.getEmail());
            site.setText(card.getSite());
            links = card.getLinks();
            textColor = Color.parseColor(card.getTextColor());
            backgroundColor = Color.parseColor(card.getBackgroundColor());
            setTextColor(mainLayout, textColor);
            mainLayout.setBackgroundColor(backgroundColor);
            save.setText("Сохранить изменения");

            save.setOnClickListener(v -> {
                try {
                    BusinessCardv0_5 result = new BusinessCardv0_5(card.getUserId(), title.getText().toString().trim(), description.getText().toString().trim(), number.getText().toString().trim(), email.getText().toString().trim(), site.getText().toString().trim(), imageUrl, links, card.getSample());
                    result.setCardId(card.getCardId());
                    result.setTextColor(String.format("#%08X", (0xFFFFFFFF & textColor)));
                    result.setBackgroundColor(String.format("#%08X", (0xFFFFFFFF & backgroundColor)));
                    result.setViews(card.getViews());
                    result.setFavorites(card.getFavorites());
                    businessCardRepository.updateBusinessCard(result);
                    Toast.makeText(this, "Успешное сохранение визитки", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(EXTRA_UPDATED_CARD, result); // Обновленная визитка
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, "Ошибка сохранения визитки", Toast.LENGTH_SHORT).show();
                }
            });
        }
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

}
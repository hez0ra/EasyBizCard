package volosyuk.easybizcard;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditActivity extends AppCompatActivity {

    public static final String EXTRA_LAYOUT = "layout type";
    private static final int STORAGE_PERMISSION_CODE = 101;

    CircleImageView image;
    EditText title, description, number, email, site;
    ImageButton whatsapp, telegram, viber, vkontakte, instagram, facebook;
    Button save;

    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;
    private Map<String, String> links = new HashMap<>();
    private String imageUrl;
    private Bitmap selectedBitmap;
    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        int sample = getIntent().getIntExtra(EXTRA_LAYOUT, 1);
        switch (sample){
            case 1:
                setContentView(R.layout.sample_1_edit);
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sample_1_edit), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
                image = findViewById(R.id.sample_1_edit_image);
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

                image.setOnClickListener(view -> {
                    if (ContextCompat.checkSelfPermission(EditActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        openGallery();
                    } else {
                        requestStoragePermission();
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

                break;
            default:
                break;
        }
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Загрузка изображения...");
        progressDialog.setCancelable(false);  // Запрещаем отмену загрузки
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

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        // Создаем Map для визитки
        Map<String, Object> card = new HashMap<>();
        card.put("userId", userId);
        card.put("title", title.getText().toString().trim());
        card.put("description", description.getText().toString().trim());
        card.put("number", number.getText().toString().trim());
        card.put("email", email.getText().toString().trim());
        card.put("site", site.getText().toString().trim());
        card.put("imageUrl", imageUrl);
        card.put("links", links); // ссылки на соцсети

        String TAG = "SaveToFirebase";

        // Добавляем документ с автогенерацией ID
        db.collection("business_cards")
                .add(card)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

}
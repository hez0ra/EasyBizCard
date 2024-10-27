package volosyuk.easybizcard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 101;

    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;  // Прогресс-диалог для отображения состояния загрузки

    TextView email;
    ImageButton toAdd;
    Button changePassword, signOut;
    CircleImageView avatar;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        email = findViewById(R.id.profile_email);
        toAdd = findViewById(R.id.profile_to_add);
        changePassword = findViewById(R.id.profile_change_password);
        signOut = findViewById(R.id.profile_sign_out);
        avatar = findViewById(R.id.profile_avatar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);  // Запрещаем отмену загрузки

        user = mAuth.getCurrentUser();
        if (user != null) {
            email.setText(user.getEmail());
        }

        loadProfileImage();

        // Устанавливаем слушатель нажатия на аватар для выбора изображения
        avatar.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(ProfileActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });

        toAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddActivity.class);
            startActivity(intent);
            finish();
        });

        changePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Change Password Clicked", Toast.LENGTH_SHORT).show();
        });

        signOut.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        changePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showChangePasswordDialog() {
        // Применение стиля к диалогу
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить пароль");

        // Установка пользовательского макета
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(view);

        // Инициализация полей ввода
        EditText oldPasswordInput = view.findViewById(R.id.dialog_change_password_old);
        EditText newPasswordInput = view.findViewById(R.id.dialog_change_password_new);
        EditText confirmNewPasswordInput = view.findViewById(R.id.dialog_change_password_confirm);

        // Установка кнопки подтверждения
        builder.setPositiveButton("Подтвердить", null); // null, чтобы не закрывать диалог автоматически

        // Создаем диалог
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setTextColor(ContextCompat.getColor(this, R.color.main)); // Задаем цвет кнопки
            button.setOnClickListener(v -> {
                String oldPassword = oldPasswordInput.getText().toString();
                String newPassword = newPasswordInput.getText().toString();
                String confirmNewPassword = confirmNewPasswordInput.getText().toString();

                // Выполнение проверок
                if (oldPassword.isEmpty()) {
                    oldPasswordInput.setError("Old password is required");
                    return;
                }
                if (newPassword.isEmpty()) {
                    newPasswordInput.setError("New password is required");
                    return;
                }
                if (!newPassword.equals(confirmNewPassword)) {
                    confirmNewPasswordInput.setError("Passwords do not match");
                    return;
                }

                // Изменение пароля
                changePassword(oldPassword, newPassword, dialog);
            });
        });

        dialog.show();
    }

    private void changePassword(String oldPassword, String newPassword, AlertDialog dialog) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

            EditText oldPasswordInput = dialog.findViewById(R.id.dialog_change_password_old);
            EditText newPasswordInput = dialog.findViewById(R.id.dialog_change_password_new);

            // Ребут авторизации
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    oldPasswordInput.setError("Неправильный старый пароль");
                } else {
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Пароль изменен успешно", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // Закрываем диалог при успешном изменении
                        } else {
                            newPasswordInput.setError("Пароль не изменен");
                        }
                    });
                }
            });
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

    private ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                            bitmap = rotateImageIfRequired(selectedImageUri, bitmap);
                            avatar.setImageBitmap(bitmap);
                            uploadImageToFirebase(bitmap);  // Запуск загрузки
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

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

    private void uploadImageToFirebase(Bitmap bitmap) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && bitmap != null) {
            StorageReference userImageRef = storageRef.child("users/" + currentUser.getUid() + "/profile/");

            // Сохранение изображения в ByteArrayOutputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            progressDialog.show();  // Показываем диалог загрузки

            userImageRef.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();  // Скрываем диалог при успешной загрузке
                            Toast.makeText(ProfileActivity.this, "Изображение загружено", Toast.LENGTH_SHORT).show();

                            userImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    saveImageUrlToPreferences(uri.toString());
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();  // Скрываем диалог при ошибке
                            Toast.makeText(ProfileActivity.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadProfileImage() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String imageUrl = getImageUrlFromPreferences();
            if (imageUrl != null) {
                Picasso.get().load(imageUrl).into(avatar);
            }
        }
    }

    private void saveImageUrlToPreferences(String url) {
        getSharedPreferences("userProfile", MODE_PRIVATE).edit().putString("profileImageUrl", url).apply();
    }

    private String getImageUrlFromPreferences() {
        return getSharedPreferences("userProfile", MODE_PRIVATE).getString("profileImageUrl", null);
    }
}
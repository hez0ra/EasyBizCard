package volosyuk.easybizcard;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

                    return insets;
                });
                break;
            default:
                break;
        }
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);  // Запрещаем отмену загрузки
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
                            image.setImageBitmap(bitmap);
                            uploadImageToFirebase(bitmap);  // Запуск загрузки
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

    private void uploadImageToFirebase(Bitmap bitmap) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && bitmap != null) {
            StorageReference userImageRef = storageRef.child("users/" + currentUser.getUid() + "/business cards/");

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
                            Toast.makeText(EditActivity.this, "Изображение загружено", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();  // Скрываем диалог при ошибке
                            Toast.makeText(EditActivity.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
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



}
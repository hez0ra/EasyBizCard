package volosyuk.easybizcard;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class QRScannerActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private PreviewView previewView;
    private Camera camera;
    private BarcodeScanner scanner;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private boolean isQrScanned = false; // Флаг, чтобы не сканировать несколько раз

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        scanner = BarcodeScanning.getClient();
        cameraExecutor = Executors.newSingleThreadExecutor();
        previewView = findViewById(R.id.previewView);

        showScanOptionsDialog();
    }

    private void showScanOptionsDialog() {
        // Создаем диалог
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_image, null);

        // Находим элементы в диалоге
        ImageView cameraImage = dialogView.findViewById(R.id.camera_image);
        ImageView galleryImage = dialogView.findViewById(R.id.gallery_image);

        // Создаем AlertDialog
        android.app.AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Выберите источник")
                .setNegativeButton("Отменить", null)
                .create();

        // Устанавливаем действия при нажатии
        cameraImage.setOnClickListener(v -> {
            startCamera();
            dialog.dismiss();  // Закрыть диалог после выбора камеры
        });

        galleryImage.setOnClickListener(v -> {
            pickImageFromGallery();
            dialog.dismiss();  // Закрыть диалог после выбора галереи
        });

        // Меняем цвет текста на кнопках
        dialog.setOnShowListener(dialogInterface -> {
            Button negativeButton = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Отменить"

            Button positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Добавить"
        });

        dialog.show();  // Показываем диалог
    }

    private void bindCameraPreview(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK) // Выбираем камеру
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider()); // Отправляем вывод в PreviewView

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    processQrFromGallery(imageUri);
                } else {
                    finish();
                }
            });

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void processQrFromGallery(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            processQrFromBitmap(bitmap);
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void processQrFromBitmap(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty()) {
                        handleQrResult(barcodes);
                    } else {
                        Toast.makeText(this, "QR-код не найден", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка сканирования", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void startCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCamera();
                bindCameraPreview(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera() {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, image -> processImage(image));

        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);
    }

    private void processImage(ImageProxy image) {
        // Если уже был считан QR-код, не обрабатываем изображение
        if (isQrScanned) {
            image.close();
            return;
        }

        @SuppressWarnings("UnsafeOptInUsageError")
        InputImage inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());

        scanner.process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty()) {
                        handleQrResult(barcodes);
                    }
                    image.close();
                })
                .addOnFailureListener(Throwable::printStackTrace)
                .addOnCompleteListener(task -> image.close());
    }

    private void handleQrResult(List<Barcode> barcodes) {
        String qrText = barcodes.get(0).getRawValue();
        String cardId = extractCardId(qrText);

        // Проверка, чтобы избежать повторного запуска
        if (cardId != null && !isQrScanned) {
            isQrScanned = true; // Устанавливаем флаг, чтобы не сканировать снова

            Intent resultIntent = new Intent(this, BusinessCardViewActivity.class);
            resultIntent.putExtra(BusinessCardViewActivity.EXTRA_ID, cardId);
            startActivity(resultIntent);
            finish();  // Закрыть текущую активность
        } else {
            Toast.makeText(this, "QR-код не содержит правильной ссылки", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String extractCardId(String url) {
        String[] parts = url.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        cameraExecutor.shutdown();
    }
}





package volosyuk.easybizcard.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;



public class QRCodeGenerator {

    public static Bitmap generateQRCode(String data) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.MARGIN, 1);  // Уменьшить отступ
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void sendQrCodeAsImage(Activity activity, Bitmap bitmap, int requestCode) {
        File tempFile = null;
        try {
            // Создаем временный файл для хранения QR-кода
            tempFile = File.createTempFile("QRCode", ".png", activity.getCacheDir());

            // Сохранение изображения QR-кода во временный файл
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
            }

            // Получаем URI для использования в Intent
            Uri qrCodeUri = FileProvider.getUriForFile(activity, "volosyuk.easybizcard.fileprovider", tempFile);

            // Создаем Intent для отправки изображения и текста
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("image/png");
            sendIntent.putExtra(Intent.EXTRA_STREAM, qrCodeUri);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Просмотри мою визитку в EasyBizCard");

            // Запускаем Intent с ожиданием результата
            activity.startActivityForResult(Intent.createChooser(sendIntent, "Send QR Code"), requestCode);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

package volosyuk.easybizcard;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.fonts.FontFamily;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import volosyuk.easybizcard.utils.AddElementBottomSheet;


public class TemplateEditorActivity extends AppCompatActivity {

    private final Integer[] fontSizes = {8, 10, 12, 14, 16, 18, 20, 24, 28};
    private int selectedTextSize = 14;  // Значение по умолчанию для текста
    private int selectedTextColor = Color.BLACK;
    final int[] selectedAlignment = {Gravity.START}; // По умолчанию - выравнивание влево
    private LinearLayout layoutContainer;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    private Uri photoUri;
    // Хранение выбранного выравнивания

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_template_editor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.template_editor), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        layoutContainer = findViewById(R.id.template_editor_content);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            AddElementBottomSheet bottomSheet = new AddElementBottomSheet();
            bottomSheet.setOnElementSelectedListener(elementType -> {
                switch (elementType) {
                    case "image":
                        openImagePicker();
                        break;
                    case "text":
                        openTextDialog();
                        break;
                    case "phone":
                        openPhoneDialog();
                        break;
                    case "email":
                        openEmailDialog();
                        break;
                    case "link":
                        openLinkDialog();
                        break;
                }
            });
            bottomSheet.show(getSupportFragmentManager(), "AddElementBottomSheet");
        });
    }

    private void openImagePicker() {
        // Создаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_image, null);

        // Находим элементы в диалоге
        ImageView cameraImage = dialogView.findViewById(R.id.camera_image);
        ImageView galleryImage = dialogView.findViewById(R.id.gallery_image);

        // Создаем AlertDialog
        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Выберите источник")
                .setNegativeButton("Отменить", null)
                .create();

        // Устанавливаем действия при нажатии
        cameraImage.setOnClickListener(v -> {
            openCamera();
            dialog.dismiss();  // Закрыть диалог после выбора камеры
        });

        galleryImage.setOnClickListener(v -> {
            openGallery();
            dialog.dismiss();  // Закрыть диалог после выбора галереи
        });

        // Меняем цвет текста на кнопках
        dialog.setOnShowListener(dialogInterface -> {
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Отменить"

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Добавить"
        });

        dialog.show();  // Показываем диалог
    }


    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                // Создание временного файла для фото
                File photoFile = createImageFile();
                if (photoFile != null) {
                    // Получение URI для файла с помощью FileProvider
                    photoUri = FileProvider.getUriForFile(this, "volosyuk.easybizcard.fileprovider", photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(cameraIntent, REQUEST_CAMERA);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Не удалось создать файл для фото", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Имя файла
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Директория для сохранения
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Создание файла
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                // Камера сделала фото, теперь редактируем его
                if (photoUri != null) {
                    imageRedactor(photoUri);  // Передаем URI фото в редактор
                }
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                // Пользователь выбрал фото из галереи
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    imageRedactor(selectedImage);  // Передаем URI выбранного фото в редактор
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                // Если результат редактирования изображения (из UCrop)
                if (data != null) {
                    Uri croppedImageUri = UCrop.getOutput(data); // Получаем URI обрезанного изображения
                    if (croppedImageUri != null) {
                        // Добавляем изображение в LinearLayout
                        addImageElement(croppedImageUri);
                    } else {
                        Toast.makeText(this, "Не удалось получить редактированное изображение", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            // Если ошибка при съемке или выборе
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Фото не было сделано или выбрано", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void imageRedactor(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "formated.jpg"));
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(this, R.color.white));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.main));
        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .start(this);
    }

    private void addImageElement(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        layoutContainer.addView(imageView); // Добавляем ImageView в LinearLayout
    }

    private void openTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_text, null);

        // Инициализация элементов
        EditText contentInput = dialogView.findViewById(R.id.text_content_input);
        Spinner fontFamilySpinner = dialogView.findViewById(R.id.font_family_spinner);
        Button colorPickerButton = dialogView.findViewById(R.id.color_picker_button);
        ImageButton btnBold = dialogView.findViewById(R.id.style_bold);
        ImageButton btnItalic = dialogView.findViewById(R.id.style_italic);
        ImageButton btnUnderline = dialogView.findViewById(R.id.style_underline);
        ImageButton btnStrikethrough = dialogView.findViewById(R.id.style_strikethrough);
        ImageButton alignLeft = dialogView.findViewById(R.id.align_left);
        ImageButton alignCenter = dialogView.findViewById(R.id.align_center);
        ImageButton alignRight = dialogView.findViewById(R.id.align_right);
        ImageButton alignJustify = dialogView.findViewById(R.id.align_justify);
        View selectedColorPreview = dialogView.findViewById(R.id.color_picker_preview);



        // Установка обработчиков
        setupButtonToggle(btnBold);
        setupButtonToggle(btnItalic);
        setupButtonToggle(btnUnderline);
        setupButtonToggle(btnStrikethrough);
        setupFontSizeSpinners(dialogView);
        setupFontFamilySpinner(fontFamilySpinner);
        setupColorPicker(colorPickerButton, selectedColorPreview);

        // Настройка кнопок выравнивания
        setupAlignmentButtons(new ImageButton[]{alignLeft, alignCenter, alignRight, alignJustify}, selectedAlignment);

        selectedColorPreview.setBackgroundColor(selectedTextColor);

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Текст")
                .setPositiveButton("Добавить", (s, which) -> {
                    String content = contentInput.getText().toString();
                    String fontFamily = fontFamilySpinner.getSelectedItem().toString();
                    // Сохранение текста
                    addTextElement(content, selectedTextSize, fontFamily, selectedTextColor,
                            btnBold.isSelected(), btnItalic.isSelected(), btnUnderline.isSelected(), btnStrikethrough.isSelected(),
                            selectedAlignment[0]);
                })
                .setNegativeButton("Отменить", null)
                .create();


        // Меняем цвет текста на кнопках
        dialog.setOnShowListener(dialogInterface -> {
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Отменить"

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Добавить"
        });

        dialog.show();
    }

    private void setupAlignmentButtons(ImageButton[] alignmentButtons, int[] selectedAlignment) {
        // По умолчанию активируем кнопку выравнивания по левому краю
        for (ImageButton button : alignmentButtons) {
            if (button.getId() == R.id.align_left) {
                button.setSelected(true);
                button.setBackgroundResource(R.drawable.bg_icon_selected);
                selectedAlignment[0] = Gravity.START; // Устанавливаем значение по умолчанию
            } else {
                button.setSelected(false);
                button.setBackgroundResource(android.R.color.transparent);
            }
        }

        // Устанавливаем обработчики кликов
        for (ImageButton button : alignmentButtons) {
            button.setOnClickListener(v -> {
                // Сброс состояния всех кнопок
                for (ImageButton b : alignmentButtons) {
                    b.setSelected(false);
                    b.setBackgroundResource(android.R.color.transparent); // Сбрасываем фон
                }

                // Активируем выбранную кнопку
                button.setSelected(true);
                button.setBackgroundResource(R.drawable.bg_icon_selected); // Устанавливаем фон

                // Устанавливаем значение выравнивания
                if (button.getId() == R.id.align_left) {
                    selectedAlignment[0] = Gravity.START;
                } else if (button.getId() == R.id.align_center) {
                    selectedAlignment[0] = Gravity.CENTER;
                } else if (button.getId() == R.id.align_right) {
                    selectedAlignment[0] = Gravity.END;
                } else if (button.getId() == R.id.align_justify) {
                    selectedAlignment[0] = Gravity.FILL_HORIZONTAL;
                }
            });
        }
    }

    private void setupFontFamilySpinner(Spinner spinner) {
        // Список шрифтов
        String[] fontFamilies = getResources().getStringArray(R.array.font_families);

        // Кастомный адаптер для Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_spinner, fontFamilies) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // Получаем элемент текста в текущем представлении
                TextView textView = (TextView) view;

                // Устанавливаем шрифт для каждого элемента
                String fontFamily = getItem(position);
                textView.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));

                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);

                // Получаем элемент текста в текущем представлении
                TextView textView = (TextView) view;

                // Устанавливаем шрифт для каждого элемента выпадающего списка
                String fontFamily = getItem(position);
                textView.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));

                return view;
            }
        };

        adapter.setDropDownViewResource(R.layout.item_spinner);
        spinner.setAdapter(adapter);
    }

    private void setupColorPicker(Button button, View preview) {
        button.setOnClickListener(v -> {
            ColorPickerDialogBuilder
                    .with(this)
                    .setTitle("Цвет текста")
                    .initialColor(selectedTextColor)
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
                            selectedTextColor = selectedColor;
                            preview.setBackgroundColor(selectedColor);
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
    }

    private void setupButtonToggle(ImageButton button) {
        button.setOnClickListener(v -> {
            // Проверка текущего состояния
            boolean isSelected = button.isSelected();

            // Обновление состояния кнопки
            button.setSelected(!isSelected);

            // Изменение фона в зависимости от состояния
            button.setBackgroundResource(isSelected ? R.drawable.bg_icon_normal : R.drawable.bg_icon_selected);
        });
    }

    private void setupFontSizeSpinners(View dialogView) {
        Spinner textSizeSpinner = dialogView.findViewById(R.id.spinner_text_size);

        // Создаём ArrayAdapter для отображения размеров
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, fontSizes);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        // Устанавливаем адаптеры для обоих спиннеров
        textSizeSpinner.setAdapter(adapter);

        // Установка дефолтного значения (например, 16)
        textSizeSpinner.setSelection(3);

        // Обработчики выбора
        textSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTextSize = fontSizes[position]; // Сохраняем выбранный размер текста
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void addTextElement(String content, int textSize, String fontFamily, int colorText, boolean isBold, boolean isItalic, boolean isUnderline, boolean isStrikethrough,
                                int alignment) {
        // Содержимое
        TextView contentView = new TextView(this);
        contentView.setText(content);
        contentView.setTextSize(textSize);
        contentView.setTextColor(colorText);

        // Применяем стиль шрифта
        int style = Typeface.NORMAL;
        if (isBold) style |= Typeface.BOLD;
        if (isItalic) style |= Typeface.ITALIC;
        contentView.setTypeface(Typeface.create(fontFamily, style));

        // Подчеркивание и зачеркивание
        if (isUnderline) contentView.setPaintFlags(contentView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (isStrikethrough) contentView.setPaintFlags(contentView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Установка выравнивания текста
        if (alignment == Gravity.FILL_HORIZONTAL && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            contentView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD); // Только API 26+
        } else {
            contentView.setGravity(alignment);
        }

        layoutContainer.addView(contentView);
    }

    private void openPhoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_phone, null);
        EditText phoneInput = dialogView.findViewById(R.id.phone_input);
        builder.setView(dialogView)
                .setTitle("Add Phone")
                .setPositiveButton("Add", (dialog, which) -> {
                    String phone = phoneInput.getText().toString();
                    addPhoneElement(phone);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addPhoneElement(String phone) {
        TextView phoneView = new TextView(this);
        phoneView.setText(phone);
        phoneView.setTextColor(Color.BLUE);
        phoneView.setPaintFlags(phoneView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        phoneView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });
        phoneView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        layoutContainer.addView(phoneView);
    }

    private void openEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_email, null);
        EditText emailInput = dialogView.findViewById(R.id.email_input);
        builder.setView(dialogView)
                .setTitle("Add Email")
                .setPositiveButton("Add", (dialog, which) -> {
                    String email = emailInput.getText().toString();
                    addEmailElement(email);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addEmailElement(String email) {
        TextView emailView = new TextView(this);
        emailView.setText(email);
        emailView.setTextColor(Color.BLUE);
        emailView.setPaintFlags(emailView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        emailView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            startActivity(intent);
        });
        emailView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        layoutContainer.addView(emailView);
    }

    private void openLinkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_link, null);
        EditText linkInput = dialogView.findViewById(R.id.link_input);
        builder.setView(dialogView)
                .setTitle("Add Link")
                .setPositiveButton("Add", (dialog, which) -> {
                    String link = linkInput.getText().toString();
                    addLinkElement(link);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addLinkElement(String link) {
        TextView linkView = new TextView(this);
        linkView.setText(link);
        linkView.setTextColor(Color.BLUE);
        linkView.setPaintFlags(linkView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        linkView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(link));
            startActivity(intent);
        });
        linkView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        layoutContainer.addView(linkView);
    }
}


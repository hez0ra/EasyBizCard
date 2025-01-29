package volosyuk.easybizcard;

import static volosyuk.easybizcard.utils.CountryManager.PHONE_CODES;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blongho.country_data.Country;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import volosyuk.easybizcard.adapters.CountryAdapter;
import volosyuk.easybizcard.adapters.SocialNetworkAdapter;
import volosyuk.easybizcard.models.BusinessCardElement;
import volosyuk.easybizcard.models.SocialNetwork;
import volosyuk.easybizcard.utils.AddElementBottomSheet;
import volosyuk.easybizcard.utils.CountryManager;


public class TemplateEditorActivity extends AppCompatActivity {

    private ImageButton saveBtn;
    private List<SocialNetwork> addedSocialMedia = new ArrayList<>();
    List<BusinessCardElement> businessCardElements = new ArrayList<>();
    private final Integer[] fontSizes = {10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 40, 50, 60};
    private int selectedTextSize = 14;  // Значение по умолчанию для текста
    private int selectedTextColor = Color.BLACK;
    final int[] selectedAlignment = {Gravity.START}; // По умолчанию - выравнивание влево
    private LinearLayout layoutContainer;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private Uri photoUri;
    String userId;
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
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                    case "social_media":
                        openSocialMediaDialog();
                        break;
                }
            });
            bottomSheet.show(getSupportFragmentManager(), "AddElementBottomSheet");
        });

        saveBtn = findViewById(R.id.btn_save_card);
        saveBtn.setOnClickListener(v -> {
            if(layoutContainer.getChildAt(0) != null){
                saveBusinessCardToFirebase();
            }
        });
    }

    private void setupAlignmentButtons(ImageButton[] alignmentButtons, int[] selectedAlignment) {
        // По умолчанию активируем кнопку выравнивания по левому краю
        for (ImageButton button : alignmentButtons) {
            if (button.getId() == alignmentButtons[0].getId()) {
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
                if (button.getId() == alignmentButtons[0].getId()) {
                    selectedAlignment[0] = Gravity.START;
                } else if (button.getId() == alignmentButtons[1].getId()) {
                    selectedAlignment[0] = Gravity.CENTER;
                } else if (button.getId() == alignmentButtons[2].getId()) {
                    selectedAlignment[0] = Gravity.END;
                } else if (button.getId() == alignmentButtons[3].getId()) {
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
                Typeface typeface = getCustomFont(fontFamily);
                textView.setTypeface(typeface);

                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);

                // Получаем элемент текста в текущем представлении
                TextView textView = (TextView) view;

                // Устанавливаем шрифт для каждого элемента выпадающего списка
                String fontFamily = getItem(position);
                Typeface typeface = getCustomFont(fontFamily);
                textView.setTypeface(typeface);

                return view;
            }
        };

        adapter.setDropDownViewResource(R.layout.item_spinner);
        spinner.setAdapter(adapter);
    }

    // TODO: сделать запоминание выбранного семейства шрифта

    private Typeface getCustomFont(String fontFamily) {
        switch (fontFamily.toLowerCase()) {
            // Встроенные шрифты
            case "sans-serif":
            case "sans-serif-light":
            case "sans-serif-thin":
            case "sans-serif-condensed":
            case "sans-serif-medium":
            case "sans-serif-black":
            case "sans-serif-condensed-light":
            case "sans-serif-condensed-medium":
            case "serif":
            case "monospace":
            case "cursive":
                return Typeface.create(fontFamily, Typeface.NORMAL);

            // Кастомные шрифты
            case "arimo":
                return ResourcesCompat.getFont(this, R.font.arimo);
            case "roboto":
                return ResourcesCompat.getFont(this, R.font.roboto);
            case "roboto condensed":
                return ResourcesCompat.getFont(this, R.font.roboto_condensed);
            case "caveat":
                return ResourcesCompat.getFont(this, R.font.caveat);
            case "comfortaa":
                return ResourcesCompat.getFont(this, R.font.comfortaa);
            case "great vibes":
                return ResourcesCompat.getFont(this, R.font.great_vibes);
            case "jura":
                return ResourcesCompat.getFont(this, R.font.jura);
            case "mulish":
                return ResourcesCompat.getFont(this, R.font.mulish);
            case "noto serif":
                return ResourcesCompat.getFont(this, R.font.noto_serif);
            case "playfair display":
                return ResourcesCompat.getFont(this, R.font.playfair_display);

            default:
                // Возвращает стандартный шрифт, если не найдено
                return Typeface.DEFAULT;
        }
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

    private void setupFontSizeSpinners(Spinner spinner) {
        // Создаём ArrayAdapter для отображения размеров
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, fontSizes);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        // Устанавливаем адаптеры для обоих спиннеров
        spinner.setAdapter(adapter);

        // Установка дефолтного значения (например, 16)
        spinner.setSelection(3);

        // Обработчики выбора
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTextSize = fontSizes[position]; // Сохраняем выбранный размер текста
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    // ------------------ Image ---------------------------


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

        addImageElementToJSON(imageUri);
    }


    // ------------------ Text ---------------------------


    private void openTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_text, null);

        // Инициализация элементов
        EditText contentInput = dialogView.findViewById(R.id.text_content_input);
        Spinner fontFamilySpinner = dialogView.findViewById(R.id.font_family_spinner);
        Spinner fontSizeSpinner = dialogView.findViewById(R.id.spinner_text_size);
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
        setupFontSizeSpinners(fontSizeSpinner);
        setupFontFamilySpinner(fontFamilySpinner);
        setupColorPicker(colorPickerButton, selectedColorPreview);

        // Настройка кнопок выравнивания
        setupAlignmentButtons(new ImageButton[]{alignLeft, alignCenter, alignRight, alignJustify}, selectedAlignment);

        selectedTextColor = Color.BLACK;

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
        contentView.setTypeface(Typeface.create(getCustomFont(fontFamily), style));

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

        addTextElementToJSON(content, textSize, fontFamily, colorText, isBold, isItalic, isUnderline, isStrikethrough, alignment);
    }


    // ------------------ Phone ---------------------------


    private void openPhoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_phone, null);

        EditText phoneInput = dialogView.findViewById(R.id.phone_input);
        Spinner countrySpinner = dialogView.findViewById(R.id.country_spinner);
        Spinner fontFamilySpinner = dialogView.findViewById(R.id.phone_font_family_spinner);
        Spinner fontSizeSpinner = dialogView.findViewById(R.id.phone_spinner_text_size);
        Button colorPickerButton = dialogView.findViewById(R.id.phone_color_picker_button);
        ImageButton btnBold = dialogView.findViewById(R.id.phone_style_bold);
        ImageButton btnItalic = dialogView.findViewById(R.id.phone_style_italic);
        ImageButton btnUnderline = dialogView.findViewById(R.id.phone_style_underline);
        ImageButton btnStrikethrough = dialogView.findViewById(R.id.phone_style_strikethrough);
        ImageButton alignLeft = dialogView.findViewById(R.id.phone_align_left);
        ImageButton alignCenter = dialogView.findViewById(R.id.phone_align_center);
        ImageButton alignRight = dialogView.findViewById(R.id.phone_align_right);
        ImageButton alignJustify = dialogView.findViewById(R.id.phone_align_justify);
        View selectedColorPreview = dialogView.findViewById(R.id.phone_color_picker_preview);

        // Установка обработчиков
        setupButtonToggle(btnBold);
        setupButtonToggle(btnItalic);
        setupButtonToggle(btnUnderline);
        setupButtonToggle(btnStrikethrough);
        setupFontSizeSpinners(fontSizeSpinner);
        setupFontFamilySpinner(fontFamilySpinner);
        setupColorPicker(colorPickerButton, selectedColorPreview);

        setupAlignmentButtons(new ImageButton[]{alignLeft, alignCenter, alignRight, alignJustify}, selectedAlignment);

        selectedTextColor = Color.BLUE;
        selectedColorPreview.setBackgroundColor(selectedTextColor);

        // Инициализация списка стран
        List<Country> countries = CountryManager.getCountries();
        CountryAdapter adapter = new CountryAdapter(this, countries);
        countrySpinner.setAdapter(adapter);

        // Устанавливаем Беларусь как выбранную страну
        for (int i = 0; i < countries.size(); i++) {
            if ("Belarus".equals(countries.get(i).getName())) {
                countrySpinner.setSelection(i);
                break;
            }
        }

        // Добавление TextWatcher для форматирования номера телефона
        phoneInput.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting; // Флаг для предотвращения зацикливания
            private final StringBuilder formatted = new StringBuilder();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                String rawInput = s.toString().replaceAll("\\s+", ""); // Убираем пробелы
                formatted.setLength(0);

                int length = rawInput.length();
                for (int i = 0; i < length; i++) {
                    if (i == 2 || i == 5 || i == 7 || i == 9) {
                        formatted.append(" ");
                    }
                    formatted.append(rawInput.charAt(i));
                }

                phoneInput.setText(formatted.toString());
                phoneInput.setSelection(formatted.length()); // Устанавливаем курсор в конец
                isFormatting = false;
            }
        });

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Добавить номер телефона")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Добавить", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text));

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String phone = phoneInput.getText().toString().replaceAll("\\s+", ""); // Убираем пробелы для проверки
                Country selectedCountry = (Country) countrySpinner.getSelectedItem();
                String countryCode = selectedCountry.getAlpha2();

                if (isValidPhoneNumber(phone, countryCode)) {
                    String fontFamily = fontFamilySpinner.getSelectedItem().toString();
                    addPhoneElement(PHONE_CODES.get(countryCode) + phone, selectedTextSize, fontFamily, selectedTextColor,
                            btnBold.isSelected(), btnItalic.isSelected(), btnUnderline.isSelected(), btnStrikethrough.isSelected(),
                            selectedAlignment[0]);
                    dialog.dismiss();
                } else {
                    phoneInput.setError("Неверный номер телефона");
                    Toast.makeText(this, "Неверный номер телефона", Toast.LENGTH_SHORT).show();
                }
            });
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text));
        });

        dialog.show();
    }

    private boolean isValidPhoneNumber(String phone, String countryCode) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance(this);

        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phone, countryCode);
            return phoneNumberUtil.isValidNumber(phoneNumber); // Проверяем валидность номера
        } catch (NumberParseException e) {
            return false; // Если номер не валиден
        }
    }

    private void addPhoneElement(String phone, int textSize, String fontFamily, int colorText, boolean isBold, boolean isItalic, boolean isUnderline, boolean isStrikethrough,
                                 int alignment) {
        TextView phoneView = new TextView(this);
        phoneView.setText(phone);
        phoneView.setTextSize(textSize);
        phoneView.setTextColor(colorText);

        // Применяем стиль шрифта
        int style = Typeface.NORMAL;
        if (isBold) style |= Typeface.BOLD;
        if (isItalic) style |= Typeface.ITALIC;
        phoneView.setTypeface(Typeface.create(getCustomFont(fontFamily), style));

        // Подчеркивание и зачеркивание
        if (isUnderline) phoneView.setPaintFlags(phoneView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (isStrikethrough) phoneView.setPaintFlags(phoneView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Установка выравнивания текста
        if (alignment == Gravity.FILL_HORIZONTAL && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            phoneView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD); // Только API 26+
        } else {
            phoneView.setGravity(alignment);
        }

        phoneView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });

        layoutContainer.addView(phoneView);

        addPhoneElementToJSON(phone, textSize, fontFamily, colorText, isBold, isItalic, isUnderline, isStrikethrough, alignment);
    }


    // ------------------ Email ---------------------------


    private void openEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_email, null);

        EditText emailInput = dialogView.findViewById(R.id.email_input);
        Spinner fontFamilySpinner = dialogView.findViewById(R.id.email_font_family_spinner);
        Spinner fontSizeSpinner = dialogView.findViewById(R.id.email_spinner_text_size);
        Button colorPickerButton = dialogView.findViewById(R.id.email_color_picker_button);
        ImageButton btnBold = dialogView.findViewById(R.id.email_style_bold);
        ImageButton btnItalic = dialogView.findViewById(R.id.email_style_italic);
        ImageButton btnUnderline = dialogView.findViewById(R.id.email_style_underline);
        ImageButton btnStrikethrough = dialogView.findViewById(R.id.email_style_strikethrough);
        ImageButton alignLeft = dialogView.findViewById(R.id.email_align_left);
        ImageButton alignCenter = dialogView.findViewById(R.id.email_align_center);
        ImageButton alignRight = dialogView.findViewById(R.id.email_align_right);
        ImageButton alignJustify = dialogView.findViewById(R.id.email_align_justify);
        View selectedColorPreview = dialogView.findViewById(R.id.email_color_picker_preview);

        // Установка обработчиков
        setupButtonToggle(btnBold);
        setupButtonToggle(btnItalic);
        setupButtonToggle(btnUnderline);
        setupButtonToggle(btnStrikethrough);
        setupFontSizeSpinners(fontSizeSpinner);
        setupFontFamilySpinner(fontFamilySpinner);
        setupColorPicker(colorPickerButton, selectedColorPreview);

        // Настройка кнопок выравнивания
        setupAlignmentButtons(new ImageButton[]{alignLeft, alignCenter, alignRight, alignJustify}, selectedAlignment);

        selectedTextColor = Color.BLUE;

        selectedColorPreview.setBackgroundColor(selectedTextColor);

        if(FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) emailInput.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());



        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Добавить электронную почту")
                .setPositiveButton("Добавить", null)
                .setNegativeButton("Отмена", null)
                .create();

        // Меняем цвет текста на кнопках
        dialog.setOnShowListener(dialogInterface -> {
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Отменить"

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String email = emailInput.getText().toString();

                String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
                Pattern pattern = Pattern.compile(emailRegex);
                Matcher matcher = pattern.matcher(email);

                // Валидация номера телефона
                if (matcher.matches()) {
                    String fontFamily = fontFamilySpinner.getSelectedItem().toString();
                    addEmailElement(email, selectedTextSize, fontFamily, selectedTextColor,
                            btnBold.isSelected(), btnItalic.isSelected(), btnUnderline.isSelected(), btnStrikethrough.isSelected(),
                            selectedAlignment[0]); // Ваш метод добавления номера
                    dialog.dismiss();
                } else {
                    emailInput.setError("Неверный адрес электронной почты");
                    Toast.makeText(this, "Неверный адрес электронной почты", Toast.LENGTH_SHORT).show();
                }
            });
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Добавить"
        });

        dialog.show();
    }

    private void addEmailElement(String email, int textSize, String fontFamily, int colorText, boolean isBold, boolean isItalic, boolean isUnderline, boolean isStrikethrough,
                                 int alignment) {
        TextView emailView = new TextView(this);
        emailView.setText(email);
        emailView.setTextSize(textSize);
        emailView.setTextColor(colorText);

        // Применяем стиль шрифта
        int style = Typeface.NORMAL;
        if (isBold) style |= Typeface.BOLD;
        if (isItalic) style |= Typeface.ITALIC;
        emailView.setTypeface(Typeface.create(getCustomFont(fontFamily), style));

        // Подчеркивание и зачеркивание
        if (isUnderline) emailView.setPaintFlags(emailView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (isStrikethrough) emailView.setPaintFlags(emailView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Установка выравнивания текста
        if (alignment == Gravity.FILL_HORIZONTAL && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            emailView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD); // Только API 26+
        } else {
            emailView.setGravity(alignment);
        }

        emailView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            startActivity(intent);
        });
        layoutContainer.addView(emailView);


        addEmailElementToJSON(email, textSize, fontFamily, colorText, isBold, isItalic, isUnderline, isStrikethrough, alignment);
    }


    // ------------------ Link ---------------------------


    private void openLinkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_link, null);

        Switch hyperLink = dialogView.findViewById(R.id.link_type_switch);
        EditText linkInput = dialogView.findViewById(R.id.link_input);
        EditText hyperLinkInput = dialogView.findViewById(R.id.hypertext_input);
        Spinner fontFamilySpinner = dialogView.findViewById(R.id.link_font_family_spinner);
        Spinner fontSizeSpinner = dialogView.findViewById(R.id.link_spinner_text_size);
        Button colorPickerButton = dialogView.findViewById(R.id.link_color_picker_button);
        ImageButton btnBold = dialogView.findViewById(R.id.link_style_bold);
        ImageButton btnItalic = dialogView.findViewById(R.id.link_style_italic);
        ImageButton btnUnderline = dialogView.findViewById(R.id.link_style_underline);
        ImageButton btnStrikethrough = dialogView.findViewById(R.id.link_style_strikethrough);
        ImageButton alignLeft = dialogView.findViewById(R.id.link_align_left);
        ImageButton alignCenter = dialogView.findViewById(R.id.link_align_center);
        ImageButton alignRight = dialogView.findViewById(R.id.link_align_right);
        ImageButton alignJustify = dialogView.findViewById(R.id.link_align_justify);
        View selectedColorPreview = dialogView.findViewById(R.id.link_color_picker_preview);

        // Установка обработчиков
        setupButtonToggle(btnBold);
        setupButtonToggle(btnItalic);
        setupButtonToggle(btnUnderline);
        setupButtonToggle(btnStrikethrough);
        setupFontSizeSpinners(fontSizeSpinner);
        setupFontFamilySpinner(fontFamilySpinner);
        setupColorPicker(colorPickerButton, selectedColorPreview);

        // Настройка кнопок выравнивания
        setupAlignmentButtons(new ImageButton[]{alignLeft, alignCenter, alignRight, alignJustify}, selectedAlignment);

        selectedTextColor = Color.BLUE;

        selectedColorPreview.setBackgroundColor(selectedTextColor);

        hyperLink.setOnClickListener(v -> {
            // Используем isChecked() для проверки состояния Switch
            if (hyperLink.isChecked()) {
                hyperLinkInput.setVisibility(View.VISIBLE);  // Показываем поле для гипертекста
            } else {
                hyperLinkInput.setVisibility(View.GONE);  // Скрываем поле для гипертекста
            }
        });


        AlertDialog dialog =  builder.setView(dialogView)
                .setTitle("Добавить ссылку")
                .setPositiveButton("Добавить", null)
                .setNegativeButton("Отмена", null)
                .create();

        // Меняем цвет текста на кнопках
        dialog.setOnShowListener(dialogInterface -> {
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Отменить"

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String link = linkInput.getText().toString();
                String hyperText = hyperLinkInput.getText().toString();
                boolean isHyperLink = hyperLink.isChecked();

                // Проверка на пустые поля
                if (isHyperLink) {
                    if (link.isEmpty()) {
                        linkInput.setError("Поле не должно быть пустым");
                        Toast.makeText(this, "Поле ссылки не должно быть пустым", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Валидация ссылки
                String urlRegex = "^(https?://)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/\\S*)?$";
                Pattern pattern = Pattern.compile(urlRegex);
                Matcher matcher = pattern.matcher(link);

                if (!matcher.matches()) {
                    linkInput.setError("Неверный формат ссылки");
                    Toast.makeText(this, "Введите корректную ссылку", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Если включен гипертекст, дополнительно проверяем текст ссылки
                if (isHyperLink && hyperText.isEmpty()) {
                    hyperLinkInput.setError("Поле текста не должно быть пустым");
                    Toast.makeText(this, "Поле текста для гиперссылки не должно быть пустым", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Если все проверки прошли успешно
                String fontFamily = fontFamilySpinner.getSelectedItem().toString();
                if(isHyperLink){
                    addLinkElement(link, hyperText, selectedTextSize, fontFamily, selectedTextColor,
                            btnBold.isSelected(), btnItalic.isSelected(), btnUnderline.isSelected(), btnStrikethrough.isSelected(),
                            selectedAlignment[0]);
                }
                else {
                    addLinkElement(link, selectedTextSize, fontFamily, selectedTextColor,
                            btnBold.isSelected(), btnItalic.isSelected(), btnUnderline.isSelected(), btnStrikethrough.isSelected(),
                            selectedAlignment[0]);
                }
                dialog.dismiss();
            });

            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Добавить"
        });

        dialog.show();
    }

    private void addLinkElement(String link, int textSize, String fontFamily, int colorText, boolean isBold, boolean isItalic, boolean isUnderline, boolean isStrikethrough,
                                int alignment) {
        TextView linkView = new TextView(this);
        linkView.setText(link);
        linkView.setTextSize(textSize);
        linkView.setTextColor(colorText);

        // Применяем стиль шрифта
        int style = Typeface.NORMAL;
        if (isBold) style |= Typeface.BOLD;
        if (isItalic) style |= Typeface.ITALIC;
        linkView.setTypeface(Typeface.create(getCustomFont(fontFamily), style));

        // Подчеркивание и зачеркивание
        if (isUnderline) linkView.setPaintFlags(linkView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (isStrikethrough) linkView.setPaintFlags(linkView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Установка выравнивания текста
        if (alignment == Gravity.FILL_HORIZONTAL && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            linkView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD); // Только API 26+
        } else {
            linkView.setGravity(alignment);
        }

        // Обработка клика на ссылку с подтверждением
        linkView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Подтвердить действие")
                    .setMessage("Вы уверены, что хотите перейти по ссылке: " + link + "? Мы не обеспечиваем безопасность внешних ресурсов.")
                    .setPositiveButton("Перейти", (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(link));
                        startActivity(intent);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        layoutContainer.addView(linkView);

        addLinkElementToJSON(link, textSize, fontFamily, colorText, isBold, isItalic, isUnderline, isStrikethrough, alignment);
    }

    private void addLinkElement(String link, String hyperText, int textSize, String fontFamily, int colorText, boolean isBold, boolean isItalic, boolean isUnderline,
                                boolean isStrikethrough, int alignment) {
        TextView linkView = new TextView(this);
        linkView.setText(hyperText); // Отображаем текст гиперссылки
        linkView.setTextSize(textSize);
        linkView.setTextColor(colorText);

        // Применяем стиль шрифта
        int style = Typeface.NORMAL;
        if (isBold) style |= Typeface.BOLD;
        if (isItalic) style |= Typeface.ITALIC;
        linkView.setTypeface(Typeface.create(getCustomFont(fontFamily), style));

        // Подчеркивание и зачеркивание
        if (isUnderline) linkView.setPaintFlags(linkView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        if (isStrikethrough) linkView.setPaintFlags(linkView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Установка выравнивания текста
        if (alignment == Gravity.FILL_HORIZONTAL && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            linkView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD); // Только API 26+
        } else {
            linkView.setGravity(alignment);
        }

        // Обработка клика на гиперссылку с подтверждением
        linkView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Подтвердить действие")
                    .setMessage("Вы уверены, что хотите перейти по ссылке: " + link + "? Мы не обеспечиваем безопасность внешних ресурсов.")
                    .setPositiveButton("Перейти", (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(link));
                        startActivity(intent);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        layoutContainer.addView(linkView);

        addLinkElementToJSON(link, hyperText, textSize, fontFamily, colorText, isBold, isItalic, isUnderline, isStrikethrough, alignment);
    }


    // ------------------ Social Media ---------------------------


    private void openSocialMediaDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_social_media, null);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_social_media);

        setupRecyclerSocialMedia(recyclerView);

        AlertDialog dialog =  builder.setView(dialogView)
                .setTitle("Добавить ссылку")
                .setPositiveButton("Добавить", null)
                .setNegativeButton("Отмена", null)
                .create();

        // Меняем цвет текста на кнопках
        dialog.setOnShowListener(dialogInterface -> {
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Отменить"

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                addSocialMediaElement(addedSocialMedia);
                addedSocialMedia.clear();
                dialog.dismiss();
            });

            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text));  // Устанавливаем цвет текста для кнопки "Добавить"
        });

        dialog.show();
    }

    private void showInputDialog(SocialNetwork socialNetwork) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Добавить/Изменить " + socialNetwork.getName());

        // Создание EditText для ввода
        EditText input = new EditText(this);

        int index = addedSocialMedia.indexOf(socialNetwork);

        // Проверяем, есть ли уже добавленная ссылка
        String currentLink = socialNetwork.getLink();
        if (currentLink != null && !currentLink.isEmpty()) {
            // Если ссылка есть, извлекаем id (username или phone) из шаблона
            String placeholder = socialNetwork.getUrlTemplate().contains("{phone}") ? "{phone}" : "{username}";
            String existingValue = currentLink.replace(
                    socialNetwork.getUrlTemplate().replace(placeholder, ""),
                    ""
            );
            input.setText(existingValue); // Устанавливаем значение в EditText
        }

        // Настройка подсказки и типа ввода в зависимости от соцсети
        if (socialNetwork.getUrlTemplate().contains("{phone}")) {
            input.setHint("Введите телефон");
            input.setInputType(InputType.TYPE_CLASS_PHONE); // Ввод только цифр
        } else if (socialNetwork.getUrlTemplate().contains("{username}")) {
            input.setHint("Введите юзернейм");
            input.setInputType(InputType.TYPE_CLASS_TEXT); // Ввод текста
        } else {
            input.setHint("Введите ссылку");
            input.setInputType(InputType.TYPE_TEXT_VARIATION_URI); // Ввод ссылки
        }

        // Установка цветов для текста и подсказки
        input.setHintTextColor(getResources().getColor(R.color.second));
        input.setTextColor(getResources().getColor(R.color.text));

        // Установка EditText в диалог
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String userInput = input.getText().toString().trim();
            if (!userInput.isEmpty()) {
                // Формирование новой или обновленной ссылки
                String link = socialNetwork.getUrlTemplate()
                        .replace("{username}", userInput)
                        .replace("{phone}", userInput);

                socialNetwork.setLink(link); // Сохраняем обновленную ссылку

                if(index != -1) addedSocialMedia.set(index, socialNetwork);
                else addedSocialMedia.add(socialNetwork);

                Toast.makeText(this, "Ссылка обновлена: " + link, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Поле не может быть пустым", Toast.LENGTH_SHORT).show();
            }
        });



        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void setupRecyclerSocialMedia(RecyclerView recyclerView){
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SocialNetworkAdapter adapter = new SocialNetworkAdapter(
                this,
                socialNetworks,
                socialNetwork -> showInputDialog(socialNetwork) // Передаём объект SocialNetwork
        );
        recyclerView.setAdapter(adapter);


    }

    private void addSocialMediaElement(List<SocialNetwork> addedSocialNetworks) {
        // Создаем ScrollView
        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        scrollView.setLayoutParams(scrollParams);

        // Горизонтальный LinearLayout внутри ScrollView
        LinearLayout horizontalLayout = new LinearLayout(this);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Добавляем иконки соцсетей с установленными ссылками
        for (SocialNetwork socialNetwork : addedSocialNetworks) {
            // Создаем ImageView для иконки
            ImageView iconView = new ImageView(this);
            iconView.setImageResource(socialNetwork.getIconResId());
            int size = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    48, // Размер в dp
                    getResources().getDisplayMetrics()
            );
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
            iconParams.setMargins(16, 16, 16, 16);
            iconView.setLayoutParams(iconParams);

            // Устанавливаем клик-слушатель для иконки
            iconView.setOnClickListener(view -> {
                // Открываем диалог подтверждения перехода
                showConfirmationDialog(socialNetwork.getName(), socialNetwork.getLink());
            });

            // Добавляем ImageView в горизонтальный LinearLayout
            horizontalLayout.addView(iconView);
        }

        // Добавляем горизонтальный LinearLayout в ScrollView
        scrollView.addView(horizontalLayout);

        // Добавляем ScrollView в родительский LinearLayout
        layoutContainer.addView(scrollView);

        addSocialMediaElementToJSON(addedSocialNetworks);
    }

    // Диалог подтверждения перехода
    private void showConfirmationDialog(String socialNetworkName, String link) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Перейти в " + socialNetworkName);

        // Настройка кастомного текста для сообщения
        TextView messageView = new TextView(this);
        messageView.setText("Вы уверены, что хотите открыть ссылку?\n" + link);
        messageView.setTextColor(getResources().getColor(R.color.text));
        messageView.setPadding(50, 30, 50, 30);
        messageView.setTextSize(16);

        builder.setView(messageView);

        builder.setPositiveButton("Да", (dialog, which) -> {
            // Открытие ссылки
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(browserIntent);
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    // ------------------ Save information ---------------------------

    // Функция для добавления текстового элемента
    private void addTextElementToJSON(String content, int textSize, String fontFamily, int colorText, boolean isBold,
                                      boolean isItalic, boolean isUnderline, boolean isStrikethrough, int alignment) {
        BusinessCardElement element = new BusinessCardElement(
                "text",
                content,
                textSize,
                fontFamily,
                colorText,
                isBold,
                isItalic,
                isUnderline,
                isStrikethrough,
                alignment // Храним выравнивание как строку
        );
        businessCardElements.add(element);
    }

    // Функция для добавления изображения
    private void addImageElementToJSON(Uri imageUri) {
        BusinessCardElement element = new BusinessCardElement(
                "image",
                imageUri.toString() // Преобразуем Uri в строку для хранения
        );
        businessCardElements.add(element);
    }

    // Функция для добавления ссылки с кастомным текстом
    private void addLinkElementToJSON(String link, String hyperText, int textSize, String fontFamily, int colorText,
                                      boolean isBold, boolean isItalic, boolean isUnderline, boolean isStrikethrough, int alignment) {
        BusinessCardElement element = new BusinessCardElement(
                "link",
                link,
                hyperText,
                textSize,
                fontFamily,
                colorText,
                isBold,
                isItalic,
                isUnderline,
                isStrikethrough,
                alignment
        );
        businessCardElements.add(element);
    }

    // Функция для добавления ссылки (без кастомного текста)
    private void addLinkElementToJSON(String link, int textSize, String fontFamily, int colorText,
                                      boolean isBold, boolean isItalic, boolean isUnderline, boolean isStrikethrough, int alignment) {
        addLinkElementToJSON(link, link, textSize, fontFamily, colorText, isBold, isItalic, isUnderline, isStrikethrough, alignment);
    }

    // Функция для добавления социальных сетей в виде списка строк
    private void addSocialMediaElementToJSON(List<SocialNetwork> addedSocialNetworks) {
        if (addedSocialNetworks != null && !addedSocialNetworks.isEmpty()) {
            // Преобразуем список SocialNetwork в список строк (например, ссылки)
            List<String> socialNetworkLinks = new ArrayList<>();
            for (SocialNetwork socialNetwork : addedSocialNetworks) {
                // Если ты хочешь хранить ссылки, используй getLink
                socialNetworkLinks.add(socialNetwork.getLink());
            }

            // Создаем элемент для бизнес-карты с типом "socialMedia" и списком ссылок
            BusinessCardElement element = new BusinessCardElement(
                    "socialMedia",
                    socialNetworkLinks // Здесь будет список строк с ссылками
            );
            businessCardElements.add(element);

            Log.d("SocialNetworks", "Добавлены соцсети: " + socialNetworkLinks.toString());
        } else {
            Log.d("SocialNetworks", "Нет социальных сетей для добавления");
        }
    }


    // Функция для добавления номера телефона
    private void addPhoneElementToJSON(String phone, int textSize, String fontFamily, int colorText, boolean isBold,
                                       boolean isItalic, boolean isUnderline, boolean isStrikethrough, int alignment) {
        BusinessCardElement element = new BusinessCardElement(
                "phone",
                phone,
                textSize,
                fontFamily,
                colorText,
                isBold,
                isItalic,
                isUnderline,
                isStrikethrough,
                alignment
        );
        businessCardElements.add(element);
    }

    // Функция для добавления email
    private void addEmailElementToJSON(String email, int textSize, String fontFamily, int colorText, boolean isBold,
                                       boolean isItalic, boolean isUnderline, boolean isStrikethrough, int alignment) {
        BusinessCardElement element = new BusinessCardElement(
                "email",
                email,
                textSize,
                fontFamily,
                colorText,
                isBold,
                isItalic,
                isUnderline,
                isStrikethrough,
                alignment
        );
        businessCardElements.add(element);
    }


//    private JSONObject extractDataFromLayout(ViewGroup layout) {
//        JSONObject json = new JSONObject();
//        try {
//            for (int i = 0; i < layout.getChildCount(); i++) {
//                View child = layout.getChildAt(i);
//
//                if (child instanceof TextView) {
//                    // Извлечение текста из TextView
//                    TextView textView = (TextView) child;
//                    json.put("text_" + i, textView.getText().toString());
//
//                } else if (child instanceof ImageView) {
//                    // Обработка ImageView: добавляем тег, если он задан
//                    ImageView imageView = (ImageView) child;
//                    Drawable drawable = imageView.getDrawable();
//                    if (drawable != null) {
//                        json.put("image_" + i, "image_placeholder"); // Можно заменить на URL, если изображение уже загружено
//                    }
//
//                } else if (child instanceof ScrollView) {
//                    // Рекурсивно извлекаем данные из ScrollView
//                    ScrollView scrollView = (ScrollView) child;
//                    ViewGroup innerLayout = (ViewGroup) scrollView.getChildAt(0);
//                    json.put("scrollView_" + i, extractDataFromLayout(innerLayout));
//
//                } else if (child instanceof ViewGroup) {
//                    // Рекурсивно обрабатываем вложенные макеты
//                    ViewGroup group = (ViewGroup) child;
//                    json.put("layout_" + i, extractDataFromLayout(group));
//                }
//            }
//        } catch (JSONException e) {
//            Log.e("JSON", "Error creating JSON", e);
//        }
//        return json;
//    }
//
//    public void saveJsonWithMetadata(JSONObject json) {
//        // Генерация уникального идентификатора
//        String documentId = FirebaseFirestore.getInstance().collection("business_cards").document().getId();
//        String filePath = "business_cards/" + userId + "/json_" + documentId + ".json";
//
//        // Ссылка на Firebase Storage
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReference().child(filePath);
//
//        try {
//            // Преобразование JSON в байты
//            String jsonString = json.toString();
//            byte[] jsonData = jsonString.getBytes(StandardCharsets.UTF_8);
//
//            // Загрузка JSON в Firebase Storage
//            UploadTask uploadTask = storageRef.putBytes(jsonData);
//            uploadTask.addOnSuccessListener(taskSnapshot -> {
//                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                    // После успешной загрузки файла создаем документ в Firestore
//                    saveMetadataToFirestore(documentId, uri.toString());
//                });
//            }).addOnFailureListener(e -> {
//                Log.e("Firebase", "Error uploading JSON", e);
//            });
//        } catch (Exception e) {
//            Log.e("Firebase", "Error converting JSON to bytes", e);
//        }
//    }

    private void saveBusinessCardToFirebase() {
        // Преобразуем список элементов в JSON
        Gson gson = new Gson();
        String json = gson.toJson(businessCardElements);


        // Сохраняем JSON в Firebase Storage
        String documentId = FirebaseFirestore.getInstance().collection("business_cards").document().getId();

        String filePath = "business_cards/" + userId + "/json_" + documentId + ".json";

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(filePath);
        storageRef.putBytes(json.getBytes())
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("Firebase", "JSON успешно загружен.");
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // После успешной загрузки файла создаем документ в Firestore
                        saveMetadataToFirestore(documentId, uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Ошибка загрузки JSON: " + e.getMessage());
                });
    }

    private void saveMetadataToFirestore(String documentId, String fileUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Метаданные для документа
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", documentId);
        metadata.put("user_id", userId);
        metadata.put("status", "обрабатывается");
        metadata.put("file_url", fileUrl);
        metadata.put("created_at", System.currentTimeMillis());

        // Сохранение метаданных в Firestore
        db.collection("business_cards").document(documentId)
                .set(metadata)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Metadata saved successfully with ID: " + documentId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving metadata", e);
                });
    }

}
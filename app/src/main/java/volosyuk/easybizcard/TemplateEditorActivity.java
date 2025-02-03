package volosyuk.easybizcard;

import static volosyuk.easybizcard.utils.CountryManager.PHONE_CODES;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
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
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import volosyuk.easybizcard.adapters.CountryAdapter;
import volosyuk.easybizcard.adapters.EditElementsAdapter;
import volosyuk.easybizcard.adapters.SocialNetworkAdapter;
import volosyuk.easybizcard.models.BusinessCardElement;
import volosyuk.easybizcard.models.SocialNetwork;
import volosyuk.easybizcard.utils.AddElementBottomSheet;
import volosyuk.easybizcard.utils.CountryManager;
import volosyuk.easybizcard.utils.QRCodeGenerator;

// TODO: изменение фона
// TODO: экраны загрузок при сохранении и открытии


public class TemplateEditorActivity extends AppCompatActivity {

    private final String domen = "https:/easybizcard.com/";
    public static final String EXTRA_CARD = "card_elements";
    public static final String EXTRA_CARD_ID = "card_id";
    public static final String EXTRA_CARD_BACK = "card_background";
    private boolean editExistedImage = false;
    private boolean editExistedCard = false;
    private int posistionEditedImage = -1;
    private ImageButton saveBtn, editBtn, shareBtn, printBtn;
    private ArrayList<SocialNetwork> addedSocialMedia = new ArrayList<>();
    private ConstraintLayout mainLayout;
    private EditElementsAdapter adapter;
    List<BusinessCardElement> businessCardElements = new ArrayList<>();
    private final Integer[] fontSizes = {10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 40, 50, 60};
    private int selectedTextSize = 14;  // Значение по умолчанию для текста
    private int selectedTextColor = Color.BLACK;
    private int selectedBackgroundColor = Color.WHITE;
    final int[] selectedAlignment = {Gravity.START}; // По умолчанию - выравнивание влево
    private LinearLayout layoutContainer;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    private static final int REQUEST_CODE_PERMISSION = 4;
    private static final int REQUEST_CODE_CREATE_FILE = 5;
    public static final float MM_TO_POINTS = 2.83465f;
    private Uri photoUri;
    String userId, cardId;
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

        mainLayout = findViewById(R.id.template_editor);
        layoutContainer = findViewById(R.id.template_editor_content);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        selectedBackgroundColor = getColor(R.color.background);


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
                if (editExistedCard) {
                    cardId = getIntent().getStringExtra(EXTRA_CARD_ID);
                    updateBusinessCardInFirebase(cardId);
                }
                else {
                    saveBusinessCardToFirebase();
                };
            }
        });

        editBtn = findViewById(R.id.btn_edit_elements);
        editBtn.setOnClickListener(v -> {
            showElementsDialog();
        });

        shareBtn = findViewById(R.id.btn_share_editor);
        shareBtn.setOnClickListener(v -> {
            if(layoutContainer.getChildAt(0) != null){
                exportBusinessCardAsImage();
            }
            else {
                Toast.makeText(this, "Добавьте элементы", Toast.LENGTH_SHORT).show();
            }
        });

        printBtn = findViewById(R.id.btn_print_editor);
        printBtn.setOnClickListener(v -> {
            if (layoutContainer.getChildAt(0) != null) {
                checkPermissionsAndGeneratePdf();
            } else {
                Toast.makeText(this, "Добавьте элементы", Toast.LENGTH_SHORT).show();
            }
        });

        if(getIntent().getSerializableExtra(EXTRA_CARD) != null){
            businessCardElements = (ArrayList<BusinessCardElement>) getIntent().getSerializableExtra(EXTRA_CARD);
            editExistedCard = true;
            selectedBackgroundColor = getIntent().getIntExtra(EXTRA_CARD_BACK, getColor(R.color.background));
            mainLayout.setBackgroundColor(selectedBackgroundColor);
            layoutContainer.setBackgroundColor(selectedBackgroundColor);
            refreshLayout();
        }
    }

    private void showElementsDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_elements, null);
        bottomSheetDialog.setContentView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_edit_elements);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button btnChangeBackground = dialogView.findViewById(R.id.btnChangeBackground);
        btnChangeBackground.setOnClickListener(v -> {
            ColorPickerDialogBuilder
                    .with(this)
                    .setTitle("Выберите цвет фона")
                    .showAlphaSlider(false)
                    .initialColor(selectedBackgroundColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .density(12)
                    .setPositiveButton("OK", new ColorPickerClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                            mainLayout.setBackgroundColor(selectedColor);
                            layoutContainer.setBackgroundColor(selectedColor);
                            selectedBackgroundColor = selectedColor;
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .build()
                    .show();
        });

        adapter = new EditElementsAdapter(businessCardElements, new EditElementsAdapter.OnElementActionListener() {
            @Override
            public void onEdit(int position) {
                editElement(position);
                bottomSheetDialog.dismiss();
            }

            @Override
            public void onDelete(int position) {
                deleteElement(position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onMoveUp(int position) {
                if (position > 0) {
                    Collections.swap(businessCardElements, position, position-1);
                    refreshLayout();
                    adapter.notifyItemMoved(position, position-1);
                    adapter.notifyItemChanged(position);     // Бывшая верхняя позиция
                    adapter.notifyItemChanged(position-1);   // Текущая позиция
                }
            }

            @Override
            public void onMoveDown(int position) {
                if (position < businessCardElements.size()-1) {
                    Collections.swap(businessCardElements, position, position+1);
                    refreshLayout();
                    adapter.notifyItemMoved(position, position+1);
                    adapter.notifyItemChanged(position);     // Бывшая верхняя позиция
                    adapter.notifyItemChanged(position+1);   // Текущая позиция
                }
            }
        });

        recyclerView.setAdapter(adapter);
        bottomSheetDialog.show();
    }

    private void deleteElement(int position) {
        businessCardElements.remove(position);
        refreshLayout();
    }

    private void refreshLayout() {
        layoutContainer.removeAllViews();
        for (BusinessCardElement element : businessCardElements) {
            addElementToLayout(element);
        }
    }

    private void editElement(int position){
        BusinessCardElement element = businessCardElements.get(position);
        switch (element.getType()) {
            case "text":
                editTextElement(position);
                break;
            case "image":
                editImageElement(position);
                break;
            case "link":
                editLinkElement(position);
                break;
            case "socialMedia":
                editSocialMediaElement(position);
                break;
            case "phone":
                editPhoneElement(position);
                break;
            case "email":
                editEmailElement(position);
                break;
        }
    }

    private void addElementToLayout(BusinessCardElement element) {
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

    private void setupAlignmentButtons(ImageButton[] alignmentButtons, int[] selectedAlignment) {
        // По умолчанию активируем кнопку выравнивания, соответствующую текущему выбранному значению
        for (ImageButton button : alignmentButtons) {
            boolean isSelected = false;
            // Проверяем выравнивание и выбираем соответствующую кнопку
            if (button.getId() == alignmentButtons[0].getId() && selectedAlignment[0] == Gravity.START) {
                isSelected = true;
            } else if (button.getId() == alignmentButtons[1].getId() && selectedAlignment[0] == Gravity.CENTER) {
                isSelected = true;
            } else if (button.getId() == alignmentButtons[2].getId() && selectedAlignment[0] == Gravity.END) {
                isSelected = true;
            } else if (button.getId() == alignmentButtons[3].getId() && selectedAlignment[0] == Gravity.FILL_HORIZONTAL) {
                isSelected = true;
            }

            // Обновляем визуальное состояние кнопки в зависимости от выбранного выравнивания
            if (isSelected) {
                button.setSelected(true);
                button.setBackgroundResource(R.drawable.bg_icon_selected);
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
                    .showAlphaSlider(false) // Отключаем выбор прозрачности
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
        if(button.isSelected()){
            button.setBackgroundResource(R.drawable.bg_icon_selected);
        }
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

    // Вспомогательные методы для поиска позиций в спиннерах
    private int getFontPosition(String font, Spinner fontFamilySpinner) {
        ArrayAdapter adapter = (ArrayAdapter) fontFamilySpinner.getAdapter();
        return Math.max(adapter.getPosition(font), 0);
    }

    private int getSizePosition(int size, Spinner fontSizeSpinner) {
        for (int i = 0; i < fontSizeSpinner.getCount(); i++) {
            if (Integer.parseInt(fontSizeSpinner.getItemAtPosition(i).toString()) == size) {
                return i;
            }
        }
        return 0;
    }


    // ------------------ Image ---------------------------

    // TODO: При выборе источника камера фотка добавляется с гигантскими отступами

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
            if (requestCode == REQUEST_CAMERA && photoUri != null) {
                compressAndEditImage(photoUri);
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    compressAndEditImage(selectedImage);
                }
            } else if (requestCode == UCrop.REQUEST_CROP && data != null) {
                Uri croppedImageUri = UCrop.getOutput(data);
                if (croppedImageUri != null) {
                    if (editExistedImage) {
                        uploadImageToFirebase(croppedImageUri, posistionEditedImage);
                    } else {
                        addImageElement(croppedImageUri);
                    }
                } else {
                    Toast.makeText(this, "Не удалось получить редактированное изображение", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Фото не было сделано или выбрано", Toast.LENGTH_SHORT).show();
        }

        posistionEditedImage = -1;
        editExistedImage = false;
    }

    private void imageRedactor(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "formated.jpg"));
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(this, R.color.white));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.main));
        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .withMaxResultSize(1080,1080)
                .start(this);
    }

    private void compressAndEditImage(Uri sourceUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), sourceUri);
            int maxSize = 1080;
            Bitmap resizedBitmap = resizeBitmap(bitmap, maxSize, maxSize);
            Uri resizedUri = saveBitmapToCache(resizedBitmap);
            imageRedactor(resizedUri);
        } catch (IOException e) {
            e.printStackTrace();
            imageRedactor(sourceUri); // Если ошибка, передаем оригинал
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
        return Bitmap.createScaledBitmap(bitmap, (int) (width * scale), (int) (height * scale), true);
    }

    private Uri saveBitmapToCache(Bitmap bitmap) throws IOException {
        File cacheFile = new File(getCacheDir(), "compressed.jpg");
        FileOutputStream out = new FileOutputStream(cacheFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        out.flush();
        out.close();
        return Uri.fromFile(cacheFile);
    }

    private void addImageElement(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        layoutContainer.addView(imageView); // Добавляем ImageView в LinearLayout

        uploadImageToFirebase(imageUri);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) return;

        // Генерируем уникальное имя файла
        String fileName = "images/" + UUID.randomUUID().toString() + ".jpg";

        // Ссылка на Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);

        // Загружаем изображение
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Получаем URL загруженного изображения
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        addImageElementToJSON(imageUrl); // Сохраняем в JSON URL вместо локального URI
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
                );
    }

    private void uploadImageToFirebase(Uri imageUri, int position) {
        BusinessCardElement element = businessCardElements.get(position);

        if (imageUri == null) return;

        // Генерируем уникальное имя файла
        String fileName = "images/" + UUID.randomUUID().toString() + ".jpg";

        // Ссылка на Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);

        // Загружаем изображение
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Получаем URL загруженного изображения
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        deleteImageFromFirebase(element.getImageUrl());
                        String imageUrlNew = uri.toString();
                        element.setImageUrl(imageUrlNew);

                        adapter.notifyItemChanged(position);
                        refreshLayout();
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
                );
    }

    private void editImageElement(int posistion){
        BusinessCardElement element = businessCardElements.get(posistion);

        Uri uri = Uri.parse(element.getImageUrl());

        posistionEditedImage = posistion;
        editExistedImage = true;
        imageRedactor(uri);
    }

    private void deleteImageFromFirebase(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "Ссылка на изображение отсутствует", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);

        imageRef.delete();
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

    private void editTextElement(int position) {
        BusinessCardElement element = businessCardElements.get(position);

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

        // Заполняем текущими значениями элемента
        contentInput.setText(element.getText());
        selectedTextColor = element.getColorText();
        selectedTextSize = element.getTextSize();
        selectedAlignment[0] = element.getAlignment();

        // Установка стилей
        btnBold.setSelected(element.isBold());
        btnItalic.setSelected(element.isItalic());
        btnUnderline.setSelected(element.isUnderline());
        btnStrikethrough.setSelected(element.isStrikethrough());

        // Установка обработчиков
        setupButtonToggle(btnBold);
        setupButtonToggle(btnItalic);
        setupButtonToggle(btnUnderline);
        setupButtonToggle(btnStrikethrough);
        setupFontSizeSpinners(fontSizeSpinner);
        setupFontFamilySpinner(fontFamilySpinner);
        setupColorPicker(colorPickerButton, selectedColorPreview);

        // Установка выравнивания
        setupAlignmentButtons(new ImageButton[]{alignLeft, alignCenter, alignRight, alignJustify}, selectedAlignment);

        // Установка текущего цвета
        selectedColorPreview.setBackgroundColor(selectedTextColor);

        // Установка шрифта и размера
        fontFamilySpinner.setSelection(getFontPosition(element.getFontFamily(), fontFamilySpinner));
        fontSizeSpinner.setSelection(getSizePosition(element.getTextSize(), fontSizeSpinner));

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Редактирование текста")
                .setPositiveButton("Сохранить", (s, which) -> {
                    // Обновляем элемент
                    element.setText(contentInput.getText().toString());
                    element.setTextSize(selectedTextSize);
                    element.setFontFamily(fontFamilySpinner.getSelectedItem().toString());
                    element.setColorText(selectedTextColor);
                    element.setBold(btnBold.isSelected());
                    element.setItalic(btnItalic.isSelected());
                    element.setUnderline(btnUnderline.isSelected());
                    element.setStrikethrough(btnStrikethrough.isSelected());
                    element.setAlignment(selectedAlignment[0]);

                    // Обновляем отображение
                    adapter.notifyItemChanged(position);
                    refreshLayout();
                })
                .setNegativeButton("Отменить", null)
                .create();

        // Меняем цвет текста на кнопках
        dialog.setOnShowListener(dialogInterface -> {
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text));

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text));
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

    private void editPhoneElement(int position) {
        BusinessCardElement element = businessCardElements.get(position);

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

        // Заполняем текущими значениями элемента
        phoneInput.setText(element.getText());
        selectedTextColor = element.getColorText();
        selectedTextSize = element.getTextSize();
        selectedAlignment[0] = element.getAlignment();

        // Парсинг номера
        String fullPhoneNumber = element.getText();
        String countryCode = "";
        String localNumber = "";
        String longestPrefix = "";

        for (Map.Entry<String, String> entry : PHONE_CODES.entrySet()) {
            String code = entry.getValue();
            if (fullPhoneNumber.startsWith(code) && code.length() > longestPrefix.length()) {
                longestPrefix = code;
                countryCode = entry.getKey();
                localNumber = fullPhoneNumber.substring(code.length());
            }
        }

        // Настройка спиннера стран
        List<Country> countries = CountryManager.getCountries();
        CountryAdapter countryAdapter = new CountryAdapter(this, countries);
        countrySpinner.setAdapter(countryAdapter);

        int countryPosition = 0;
        for (int i = 0; i < countries.size(); i++) {
            if (countries.get(i).getAlpha2().equals(countryCode)) {
                countryPosition = i;
                break;
            }
        }
        countrySpinner.setSelection(countryPosition);

        phoneInput.setText(localNumber);

        // Установка стилей
        btnBold.setSelected(element.isBold());
        btnItalic.setSelected(element.isItalic());
        btnUnderline.setSelected(element.isUnderline());
        btnStrikethrough.setSelected(element.isStrikethrough());

        // Установка обработчиков
        setupButtonToggle(btnBold);
        setupButtonToggle(btnItalic);
        setupButtonToggle(btnUnderline);
        setupButtonToggle(btnStrikethrough);
        setupFontSizeSpinners(fontSizeSpinner);
        setupFontFamilySpinner(fontFamilySpinner);
        setupColorPicker(colorPickerButton, selectedColorPreview);

        // Установка выравнивания
        setupAlignmentButtons(new ImageButton[]{alignLeft, alignCenter, alignRight, alignJustify}, selectedAlignment);

        // Установка шрифта и размера
        fontFamilySpinner.setSelection(getFontPosition(element.getFontFamily(), fontFamilySpinner));
        fontSizeSpinner.setSelection(getSizePosition(element.getTextSize(), fontSizeSpinner));

        selectedColorPreview.setBackgroundColor(selectedTextColor);

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
                .setTitle("Изменить номер телефона")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text));

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String phone = phoneInput.getText().toString().replaceAll("\\s+", ""); // Убираем пробелы для проверки
                Country selectedCountry = (Country) countrySpinner.getSelectedItem();
                String code = selectedCountry.getAlpha2();

                if (isValidPhoneNumber(phone, code)) {
                    element.setText(PHONE_CODES.get(code) + phone);
                    element.setTextSize(selectedTextSize);
                    element.setFontFamily(fontFamilySpinner.getSelectedItem().toString());
                    element.setColorText(selectedTextColor);
                    element.setBold(btnBold.isSelected());
                    element.setItalic(btnItalic.isSelected());
                    element.setUnderline(btnUnderline.isSelected());
                    element.setStrikethrough(btnStrikethrough.isSelected());
                    element.setAlignment(selectedAlignment[0]);

                    // Обновляем отображение
                    adapter.notifyItemChanged(position);
                    refreshLayout();

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

    private void editEmailElement(int position) {
        BusinessCardElement element = businessCardElements.get(position);

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

        // Заполняем текущими значениями элемента
        emailInput.setText(element.getText());
        selectedTextColor = element.getColorText();
        selectedTextSize = element.getTextSize();
        selectedAlignment[0] = element.getAlignment();

        // Установка стилей
        btnBold.setSelected(element.isBold());
        btnItalic.setSelected(element.isItalic());
        btnUnderline.setSelected(element.isUnderline());
        btnStrikethrough.setSelected(element.isStrikethrough());

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

        // Установка шрифта и размера
        fontFamilySpinner.setSelection(getFontPosition(element.getFontFamily(), fontFamilySpinner));
        fontSizeSpinner.setSelection(getSizePosition(element.getTextSize(), fontSizeSpinner));

        selectedColorPreview.setBackgroundColor(selectedTextColor);

        if(FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) emailInput.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Изменить электронную почту")
                .setPositiveButton("Сохранить", null)
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
                    element.setText(emailInput.getText().toString());
                    element.setTextSize(selectedTextSize);
                    element.setFontFamily(fontFamilySpinner.getSelectedItem().toString());
                    element.setColorText(selectedTextColor);
                    element.setBold(btnBold.isSelected());
                    element.setItalic(btnItalic.isSelected());
                    element.setUnderline(btnUnderline.isSelected());
                    element.setStrikethrough(btnStrikethrough.isSelected());
                    element.setAlignment(selectedAlignment[0]);

                    // Обновляем отображение
                    adapter.notifyItemChanged(position);
                    refreshLayout();

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

    private void editLinkElement(int position) {
        BusinessCardElement element = businessCardElements.get(position);

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

        // Заполняем текущими значениями элемента
        linkInput.setText(element.getLink());
        hyperLinkInput.setText(element.getHyperText());
        if(element.getLink() != element.getHyperText()){
            hyperLink.setChecked(true);
            hyperLinkInput.setVisibility(View.VISIBLE);
        }
        selectedTextColor = element.getColorText();
        selectedTextSize = element.getTextSize();
        selectedAlignment[0] = element.getAlignment();

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

        // Установка шрифта и размера
        fontFamilySpinner.setSelection(getFontPosition(element.getFontFamily(), fontFamilySpinner));
        fontSizeSpinner.setSelection(getSizePosition(element.getTextSize(), fontSizeSpinner));

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
                .setTitle("Изменить ссылку")
                .setPositiveButton("Сохранить", null)
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
                    element.setLink(linkInput.getText().toString());
                    element.setHyperText(hyperLinkInput.getText().toString());
                    element.setTextSize(selectedTextSize);
                    element.setFontFamily(fontFamilySpinner.getSelectedItem().toString());
                    element.setColorText(selectedTextColor);
                    element.setBold(btnBold.isSelected());
                    element.setItalic(btnItalic.isSelected());
                    element.setUnderline(btnUnderline.isSelected());
                    element.setStrikethrough(btnStrikethrough.isSelected());
                    element.setAlignment(selectedAlignment[0]);
                }
                else {
                    element.setLink(linkInput.getText().toString());
                    element.setHyperText(linkInput.getText().toString());
                    element.setTextSize(selectedTextSize);
                    element.setFontFamily(fontFamilySpinner.getSelectedItem().toString());
                    element.setColorText(selectedTextColor);
                    element.setBold(btnBold.isSelected());
                    element.setItalic(btnItalic.isSelected());
                    element.setUnderline(btnUnderline.isSelected());
                    element.setStrikethrough(btnStrikethrough.isSelected());
                    element.setAlignment(selectedAlignment[0]);
                }
                // Обновляем отображение
                adapter.notifyItemChanged(position);
                refreshLayout();

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

    private void setupRecyclerSocialMedia(RecyclerView recyclerView, BusinessCardElement element) {
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

        // Заполняем соцсети из BusinessCardElement
        for (String link : element.getLinks()) {
            for (SocialNetwork network : socialNetworks) {
                if (link.startsWith(network.getUrlTemplate().replace("{username}", "").replace("{phone}", ""))) {
                    String placeholder = network.getUrlTemplate().contains("{phone}") ? "{phone}" : "{username}";
                    String value = link.replace(network.getUrlTemplate().replace(placeholder, ""), "");
                    network.setLink(link); // Устанавливаем сохранённую ссылку
                    addedSocialMedia.add(network);
                }
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SocialNetworkAdapter adapter = new SocialNetworkAdapter(
                this,
                socialNetworks,
                socialNetwork -> showInputDialog(socialNetwork) // Передаём объект SocialNetwork
        );
        recyclerView.setAdapter(adapter);
    }

    private void editSocialMediaElement(int position) {
        BusinessCardElement element = businessCardElements.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_social_media, null);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_social_media);

        // Передаём текущие соцсети
        setupRecyclerSocialMedia(recyclerView, element);

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Редактировать ссылки")
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отмена", null)
                .create();

        // Настраиваем кнопки
        dialog.setOnShowListener(dialogInterface -> {
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.text));

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {

                if (addedSocialMedia != null && !addedSocialMedia.isEmpty()) {
                    // Преобразуем список SocialNetwork в список строк (например, ссылки)
                    List<String> socialNetworkLinks = new ArrayList<>();
                    for (SocialNetwork socialNetwork : addedSocialMedia) {
                        // Если ты хочешь хранить ссылки, используй getLink
                        socialNetworkLinks.add(socialNetwork.getLink());
                    }

                    element.setLinks(socialNetworkLinks);

                    Log.d("SocialNetworks", "Добавлены соцсети: " + socialNetworkLinks.toString());
                } else {
                    Log.d("SocialNetworks", "Нет социальных сетей для добавления");
                }

                addedSocialMedia.clear();

                adapter.notifyItemChanged(position);
                refreshLayout();

                dialog.dismiss();
            });

            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.text));
        });

        dialog.show();
    }

    private void addSocialMediaElement(List<SocialNetwork> addedSocialNetworks) {
        // Создаем ScrollView
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
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
            iconParams.setMargins(16, 16, 24, 16);
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
    private void addImageElementToJSON(String imageUrl) {
        BusinessCardElement element = new BusinessCardElement(
                "image",
                imageUrl  // Преобразуем Uri в строку для хранения
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

    private void saveBusinessCardToFirebase() {
        // Преобразуем список элементов в JSON
        Gson gson = new Gson();
        String json = gson.toJson(businessCardElements);

        if(cardId == null){
            cardId = FirebaseFirestore.getInstance().collection("business_cards").document().getId();
        }

        // Сохраняем JSON в Firebase Storage
        String filePath = "business_cards/" + userId + "/json_" + cardId + ".json";

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(filePath);
        storageRef.putBytes(json.getBytes())
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("Firebase", "JSON успешно загружен.");
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // После успешной загрузки файла создаем документ в Firestore
                        saveMetadataToFirestore(cardId, uri.toString());
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
        metadata.put("background_color", selectedBackgroundColor);

        // Сохранение метаданных в Firestore
        db.collection("business_cards").document(documentId)
                .set(metadata)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Metadata saved successfully with ID: " + documentId);
                    Toast.makeText(this, "Визитка успешно сохранена", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving metadata", e);
                });
    }


    // ------------------ Update information ---------------------------


    private void updateBusinessCardInFirebase(String documentId) {
        // Преобразуем список элементов в JSON
        Gson gson = new Gson();
        String json = gson.toJson(businessCardElements);

        // Путь к файлу в Firebase Storage
        String filePath = "business_cards/" + userId + "/json_" + documentId + ".json";

        // Ссылка на файл в Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(filePath);

        // Загружаем обновленный JSON
        storageRef.putBytes(json.getBytes())
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("Firebase", "JSON успешно обновлен.");
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // После успешного обновления файла, обновляем метаданные в Firestore
                        updateMetadataInFirestore(documentId, uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Ошибка обновления JSON: " + e.getMessage());
                });
    }

    private void updateMetadataInFirestore(String documentId, String fileUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Обновленные метаданные для документа
        Map<String, Object> updatedMetadata = new HashMap<>();
        updatedMetadata.put("file_url", fileUrl);
        updatedMetadata.put("background_color", selectedBackgroundColor);

        // Обновление метаданных в Firestore
        db.collection("business_cards").document(documentId)
                .update(updatedMetadata)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Metadata updated successfully with ID: " + documentId);
                    Toast.makeText(this, "Визитка успешно обновлена", Toast.LENGTH_LONG).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish(); // Закрываем `EditActivity`
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating metadata", e);
                });
    }


    // ------------------ Export to PNG ---------------------------


    private void prepareLayoutToExport(){
        for (int i = 0; i < layoutContainer.getChildCount(); i++) {
            View childView = layoutContainer.getChildAt(i);

            if (childView instanceof HorizontalScrollView) {
                childView.setVisibility(View.GONE);
                List<String> links = businessCardElements.get(i).getLinks();
                for (String link : links) {
                    // Создаем новый TextView для отображения соцсети
                    TextView textView1 = new TextView(this);

                    // Находим название социальной сети по ссылке
                    String socialNetworkName = getSocialNetworkNameByLink(link);
                    textView1.setText(socialNetworkName + ": ");
                    textView1.setTextSize(16);
                    textView1.setTypeface(Typeface.create(getCustomFont("sans-serif"), Typeface.NORMAL));
                    textView1.setGravity(Gravity.START);
                    textView1.setTextColor(Color.BLACK);
                    setContrastTextColor(textView1, mainLayout);

                    // Создаем второй TextView для отображения самой ссылки
                    TextView textView2 = new TextView(this);
                    textView2.setText(link);
                    textView2.setTextSize(16);
                    textView2.setTypeface(Typeface.create(getCustomFont("sans-serif"), Typeface.NORMAL));
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

        Bitmap QRbitmap = QRCodeGenerator.generateQRCode("https://easybizcard/" + userId + "/" + cardId);
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
                    refreshLayout();
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


    // ------------------ Export to PDF ---------------------------

    private void checkPermissionsAndGeneratePdf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Для Android 11 и выше
            // Просто генерируем PDF, без запроса дополнительных разрешений
            generatePrintDocument();
        } else {
            // Для Android 10 и ниже проверяем обычное разрешение
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        backgroundPaint.setColor(selectedBackgroundColor); // Используем выбранный цвет
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
        refreshLayout();
    }

    private void drawContentOnPage(Canvas canvas, int pageWidth, int pageHeight, boolean isQrPage) {
        if (isQrPage) {
            // Генерация QR-кода
            if (cardId == null) cardId = FirebaseFirestore.getInstance().collection("business_cards").document().getId();
            Bitmap qrBitmap = QRCodeGenerator.generateQRCode(domen + userId + "/" + cardId);

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

}
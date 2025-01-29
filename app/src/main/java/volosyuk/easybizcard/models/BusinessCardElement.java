package volosyuk.easybizcard.models;

import java.util.List;

public class BusinessCardElement {
    String type; // Тип элемента (text, image, link, socialMedia, phone, email)
    String imageUri; // Для изображений
    String text; // Текстовое содержимое
    String link; // URL для ссылок
    String hyperText; // Текст для отображения ссылки
    int textSize;
    String fontFamily;
    int colorText;
    boolean isBold;
    boolean isItalic;
    boolean isUnderline;
    boolean isStrikethrough;
    int alignment;
    List<String> links; // Для соцсетей

    // Конструктор для текстовых элементов (текст, номер телефона, email)
    public BusinessCardElement(String type, String text, int textSize, String fontFamily, int colorText, boolean isBold,
                               boolean isItalic, boolean isUnderline, boolean isStrikethrough, int alignment) {
        this.type = type;
        this.text = text;
        this.textSize = textSize;
        this.fontFamily = fontFamily;
        this.colorText = colorText;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderline = isUnderline;
        this.isStrikethrough = isStrikethrough;
        this.alignment = alignment;
    }

    // Конструктор для изображений
    public BusinessCardElement(String type, String imageUri) {
        this.type = type;
        this.imageUri = imageUri;
    }

    // Конструктор для ссылок
    public BusinessCardElement(String type, String link, String hyperText, int textSize, String fontFamily, int colorText,
                               boolean isBold, boolean isItalic, boolean isUnderline, boolean isStrikethrough, int alignment) {
        this.type = type;
        this.link = link;
        this.hyperText = hyperText;
        this.textSize = textSize;
        this.fontFamily = fontFamily;
        this.colorText = colorText;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderline = isUnderline;
        this.isStrikethrough = isStrikethrough;
        this.alignment = alignment;
    }

    // Конструктор для социальных сетей
    public BusinessCardElement(String type, List<String> links) {
        this.type = type;
        this.links = links;
    }
}

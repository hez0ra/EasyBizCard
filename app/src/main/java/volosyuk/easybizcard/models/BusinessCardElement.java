package volosyuk.easybizcard.models;

import java.io.Serializable;
import java.util.List;

public class BusinessCardElement implements Serializable {
    String type; // Тип элемента (text, image, link, socialMedia, phone, email)
    String imageUrl; // Для изображений
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
    int width;
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
    public BusinessCardElement(String type, String imageUrl) {
        this.type = type;
        this.imageUrl = imageUrl;
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

    // Конструктор для разделителей
    public BusinessCardElement(String type, int height, int width, int color, int alignment){
        this.type = type;
        this.textSize = height;
        this.colorText = color;
        this.alignment = alignment;
        this.width = width;
    }

    // Конструктор для социальных сетей
    public BusinessCardElement(String type, List<String> links) {
        this.type = type;
        this.links = links;
    }

    public String getType(){
        return this.type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }

    public String getHyperText() {
        return hyperText;
    }

    public int getTextSize() {
        return textSize;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public int getColorText() {
        return colorText;
    }

    public boolean isBold() {
        return isBold;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public boolean isUnderline() {
        return isUnderline;
    }

    public boolean isStrikethrough() {
        return isStrikethrough;
    }

    public int getAlignment() {
        return alignment;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setHyperText(String hyperText) {
        this.hyperText = hyperText;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public void setColorText(int colorText) {
        this.colorText = colorText;
    }

    public void setBold(boolean bold) {
        isBold = bold;
    }

    public void setItalic(boolean italic) {
        isItalic = italic;
    }

    public void setUnderline(boolean underline) {
        isUnderline = underline;
    }

    public void setStrikethrough(boolean strikethrough) {
        isStrikethrough = strikethrough;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}

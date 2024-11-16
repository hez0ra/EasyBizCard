package volosyuk.easybizcard.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BusinessCard implements Serializable {
    public String cardId;
    public String userId;
    public String title;
    public String description;
    public String number;
    public String email;
    public String site;
    public String imageUrl;
    public Map<String, String> links = new HashMap<>();
    public Map<String, String> colors = new HashMap<>();

    private long views;
    private long favorites;

    // Конструктор
    public BusinessCard(String userId, String title, String description, String number, String email, String site, String imageUrl, Map<String, String> links) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.number = number;
        this.email = email;
        this.site = site;
        this.imageUrl = imageUrl;
        this.links = links;
        this.views = 0;
        this.favorites = 0;
        this.colors.put("background", "#FFFFFF"); // Белый фон
        this.colors.put("text", "#000000"); // Чёрный текст
    }

    // Конструктор
    public BusinessCard(String cardId, String userId, String title, String description, String number, String email, String site, String imageUrl, Map<String, String> links, Map<String, String> colors, long views, long favorites) {
        this(userId, title, description, number, email, site, imageUrl, links);
        this.cardId = cardId;
        this.colors = colors;
        this.views = views;
        this.favorites = favorites;
    }

    // Пустой конструктор для Firebase
    public BusinessCard() {}

    // Геттеры
    public String getCardId() {
        return cardId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getNumber() {
        return number;
    }

    public String getEmail() {
        return email;
    }

    public String getSite() {
        return site;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    // Геттеры для ссылок на социальные сети
    public String getWhatsApp() {
        return links != null ? links.get("whatsapp") : null;
    }

    public String getViber() {
        return links != null ? links.get("viber") : null;
    }

    public String getTelegram() {
        return links != null ? links.get("telegram") : null;
    }

    public String getFacebook() {
        return links != null ? links.get("facebook") : null;
    }

    public String getVk() {
        return links != null ? links.get("vkontakte") : null;
    }

    public String getInstagram() {
        return links != null ? links.get("instagram") : null;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public long getFavorites() {
        return favorites;
    }

    public void setFavorites(long favorites) {
        this.favorites = favorites;
    }

    public Map<String, String> getColors() {
        return colors;
    }

    public void setColors(Map<String, String> colors) {
        this.colors = colors;
    }

    public String getBackgroundColor(){
        return this.colors.get("background");
    }

    public String getTextColor(){
        return this.colors.get("text");
    }

    public void setBackgroundColor(String color){
        this.colors.put("background", color);
    }

    public void setTextColor(String color){
        this.colors.put("text", color);
    }
}

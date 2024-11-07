package volosyuk.easybizcard.models;

import java.util.Map;

public class BusinessCard {
    public String cardId;
    public String userId;
    public String title;
    public String description;
    public String phoneNumber;
    public String email;
    public String website;
    public String imageUrl;
    public Map<String, String> links;

    // Конструктор
    public BusinessCard(String cardId, String userId, String title, String description, String phoneNumber, String email, String website, String imageUrl, Map<String, String> links) {
        this.cardId = cardId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.website = website;
        this.imageUrl = imageUrl;
        this.links = links;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getWebsite() {
        return website;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Map<String, String> getLinks() {
        return links;
    }
}

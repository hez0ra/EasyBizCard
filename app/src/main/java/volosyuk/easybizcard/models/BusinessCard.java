package volosyuk.easybizcard.models;

public class BusinessCard {

    private String id;
    private Long createdAt;
    private String status;
    private String fileUrl;
    private String userId;  // Добавляем поле user_id
    private int backgroundColor;
    private long views;
    private long favorites;

    public BusinessCard(String id, Long createdAt, String status, String fileUrl, String userId, int backgroundColor) {
        this.id = id;
        this.createdAt = createdAt;
        this.status = status;
        this.fileUrl = fileUrl;
        this.userId = userId;  // Инициализируем user_id
        this.backgroundColor = backgroundColor;
    }

    // Геттеры
    public String getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getUserId() {
        return userId;  // Метод для получения user_id
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
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
}

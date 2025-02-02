package volosyuk.easybizcard.models;

public class BusinessCard {

    private String id;
    private Long createdAt;
    private String status;
    private String fileUrl;
    private String userId;  // Добавляем поле user_id

    public BusinessCard(String id, Long createdAt, String status, String fileUrl, String userId) {
        this.id = id;
        this.createdAt = createdAt;
        this.status = status;
        this.fileUrl = fileUrl;
        this.userId = userId;  // Инициализируем user_id
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
}

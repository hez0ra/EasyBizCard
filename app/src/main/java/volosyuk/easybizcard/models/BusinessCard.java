package volosyuk.easybizcard.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessCard {

    public enum Status {
        PENDING, APPROVED, REJECTED;
    }

    private String id;
    private long created_at;
    private Status status;
    private String file_url;
    private String user_id;
    private int background_color;
    private long views;
    private long favorites;
    private String title;

    // Сопоставление статусов с русскими названиями
    public static final Map<BusinessCard.Status, String> STATUS_LABELS = new HashMap<>();
    static {
        STATUS_LABELS.put(BusinessCard.Status.APPROVED, "ОДОБРЕНО");
        STATUS_LABELS.put(BusinessCard.Status.PENDING, "НА РАССМОТРЕНИИ");
        STATUS_LABELS.put(BusinessCard.Status.REJECTED, "ОТКЛОНЕНО");
    }


    public static final List<Status> STATUS_VALUES = Arrays.asList(
            BusinessCard.Status.APPROVED,
            BusinessCard.Status.PENDING,
            BusinessCard.Status.REJECTED
    );

    public BusinessCard() {
        this.id = "";
        this.created_at = 0;
        this.status = Status.PENDING;
        this.file_url = "";
        this.user_id = "";
        this.background_color = 0;
        this.views = 0;
        this.favorites = 0;
    }

    public BusinessCard(String id, long created_at, Status status, String file_url, String user_id, int background_color) {
        this.id = id;
        this.created_at = created_at;
        this.status = status;
        this.file_url = file_url;
        this.user_id = user_id;
        this.background_color = background_color;
        this.views = 0;
        this.favorites = 0;
    }

    public BusinessCard(String id, long created_at, Status status, String file_url, String user_id, int background_color, long views, long favorites) {
        this.id = id;
        this.created_at = created_at;
        this.status = status;
        this.file_url = file_url;
        this.user_id = user_id;
        this.background_color = background_color;
        this.views = views;
        this.favorites = favorites;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public Long getCreated_at() {
        return created_at;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getFile_url() {
        return file_url;
    }

    public String getUser_id() {
        return user_id;
    }

    public int getBackground_color() {
        return background_color;
    }

    public void setBackground_color(int background_color) {
        this.background_color = background_color;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

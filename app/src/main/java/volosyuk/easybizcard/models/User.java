package volosyuk.easybizcard.models;

import java.util.List;

public class User {
    public String userId;
    public String email;
    public List<String> bookmarkedCards;
    public boolean admin;
    public long createdAt;
    public long lastVisit;

    public User(){

    }

    public User(String userId, String email, List<String> bookmarkedCards) {
        this.userId = userId;
        this.email = email;
        this.bookmarkedCards = bookmarkedCards;
        this.admin = false;
    }

    public User(String userId, String email, List<String> bookmarkedCards, long createdAt, long lastVisit, boolean admin) {
        this.userId = userId;
        this.email = email;
        this.bookmarkedCards = bookmarkedCards;
        this.createdAt = createdAt;
        this.lastVisit = lastVisit;
        this.admin = admin;
    }

    public User(String userId, String email, List<String> bookmarkedCards, boolean admin) {
        this(userId, email, bookmarkedCards);
        this.admin = admin;
    }

    public boolean getAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getBookmarkedCards() {
        return bookmarkedCards;
    }

    public void setBookmarkedCards(List<String> bookmarkedCards) {
        this.bookmarkedCards = bookmarkedCards;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(long lastVisit) {
        this.lastVisit = lastVisit;
    }
}

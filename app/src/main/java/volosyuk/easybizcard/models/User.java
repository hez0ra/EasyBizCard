package volosyuk.easybizcard.models;

import java.util.List;

public class User {
    private String userId;
    private String email;
    private List<String> bookmarkedCards;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String userId, String email, List<String> bookmarkedCards) {
        this.userId = userId;
        this.email = email;
        this.bookmarkedCards = bookmarkedCards;
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
}

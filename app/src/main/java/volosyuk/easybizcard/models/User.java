package volosyuk.easybizcard.models;

import java.util.List;

public class User {
    private String userId;
    private String email;
    private List<String> bookmarkedCards;
    private boolean admin;

    public User(String userId, String email, List<String> bookmarkedCards) {
        this.userId = userId;
        this.email = email;
        this.bookmarkedCards = bookmarkedCards;
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
}

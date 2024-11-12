package volosyuk.easybizcard.models;

public class Report {
    private String reportId;
    private String cardId;
    private String userId;
    private String title;
    private String message;
    private boolean considered;

    // Пустой конструктор для Firestore
    public Report() {}

    public Report(String reportId, String cardId, String userId, String title, String message, boolean considered) {
        this.reportId = reportId;
        this.cardId = cardId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.considered = considered;
    }

    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean getConsidered() { return considered; }
    public void setConsidered(boolean considered) { this.considered = considered; }
}

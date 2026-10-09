package vn.edu.tdmu.vita.models;

public class WaterLog {
    private long id;
    private long userId;
    private String date;
    private int amountMl;
    private String loggedAt;

    public WaterLog() {}

    public WaterLog(long id, long userId, String date, int amountMl, String loggedAt) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.amountMl = amountMl;
        this.loggedAt = loggedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getAmountMl() { return amountMl; }
    public void setAmountMl(int amountMl) { this.amountMl = amountMl; }

    public String getLoggedAt() { return loggedAt; }
    public void setLoggedAt(String loggedAt) { this.loggedAt = loggedAt; }
}

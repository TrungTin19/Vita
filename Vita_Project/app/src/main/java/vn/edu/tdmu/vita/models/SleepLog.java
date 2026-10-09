package vn.edu.tdmu.vita.models;

public class SleepLog {
    private long id;
    private long userId;
    private String date; // Wake up date yyyy-MM-dd
    private String sleepTime; // HH:mm
    private String wakeTime;  // HH:mm
    private float durationH;

    public SleepLog() {}

    public SleepLog(long id, long userId, String date, String sleepTime, String wakeTime, float durationH) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.sleepTime = sleepTime;
        this.wakeTime = wakeTime;
        this.durationH = durationH;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSleepTime() { return sleepTime; }
    public void setSleepTime(String sleepTime) { this.sleepTime = sleepTime; }

    public String getWakeTime() { return wakeTime; }
    public void setWakeTime(String wakeTime) { this.wakeTime = wakeTime; }

    public float getDurationH() { return durationH; }
    public void setDurationH(float durationH) { this.durationH = durationH; }
}

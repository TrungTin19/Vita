package vn.edu.tdmu.vita.models;

public class Medicine {
    private long id;
    private long userId;
    private String name;
    private String dosage;
    private String time;
    private int daysMask;
    private boolean isActive;

    public Medicine() {
        this.daysMask = 127;
        this.isActive = true;
    }

    public Medicine(long id, long userId, String name, String dosage, String time, int daysMask, boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.daysMask = daysMask;
        this.isActive = isActive;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getDaysMask() { return daysMask; }
    public void setDaysMask(int daysMask) { this.daysMask = daysMask; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}

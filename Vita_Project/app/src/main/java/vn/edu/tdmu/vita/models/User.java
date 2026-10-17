package vn.edu.tdmu.vita.models;

public class User {
    private long id;
    private String username;
    private String passwordHash;
    private String salt;
    private String fullname;
    private int birthYear;
    private String gender;
    private float heightCm;
    private float baseWeightKg;
    private int waterGoalMl;
    private String bmiStandard;
    private String waterStart = "07:00";
    private String waterEnd = "22:00";
    private int waterAmount = 250;
    private boolean weatherAdjustmentEnabled;
    private String waterTimes = "";

    public String getWaterTimes() { return waterTimes; }
    public void setWaterTimes(String value) { waterTimes = value; }

    public String getWaterStart() { return waterStart; }
    public void setWaterStart(String value) { waterStart = value; }
    public String getWaterEnd() { return waterEnd; }
    public void setWaterEnd(String value) { waterEnd = value; }
    public int getWaterAmount() { return waterAmount; }
    public void setWaterAmount(int value) { waterAmount = value; }
    public boolean isWeatherAdjustmentEnabled() { return weatherAdjustmentEnabled; }
    public void setWeatherAdjustmentEnabled(boolean value) { weatherAdjustmentEnabled = value; }

    public User() {
        this.waterGoalMl = 2000;
        this.bmiStandard = "ASIAN";
    }

    public User(long id, String username, String passwordHash, String salt, String fullname,
                int birthYear, String gender, float heightCm, float baseWeightKg,
                int waterGoalMl, String bmiStandard) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullname = fullname;
        this.birthYear = birthYear;
        this.gender = gender;
        this.heightCm = heightCm;
        this.baseWeightKg = baseWeightKg;
        this.waterGoalMl = waterGoalMl;
        this.bmiStandard = bmiStandard;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public int getBirthYear() { return birthYear; }
    public void setBirthYear(int birthYear) { this.birthYear = birthYear; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public float getHeightCm() { return heightCm; }
    public void setHeightCm(float heightCm) { this.heightCm = heightCm; }

    public float getBaseWeightKg() { return baseWeightKg; }
    public void setBaseWeightKg(float baseWeightKg) { this.baseWeightKg = baseWeightKg; }

    public int getWaterGoalMl() { return waterGoalMl; }
    public void setWaterGoalMl(int waterGoalMl) { this.waterGoalMl = waterGoalMl; }

    public String getBmiStandard() { return bmiStandard; }
    public void setBmiStandard(String bmiStandard) { this.bmiStandard = bmiStandard; }
}

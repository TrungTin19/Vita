package vn.edu.tdmu.vita.models;

public class HealthRecord {
    private long id;
    private long userId;
    private String date;
    private Float weightKg;
    private Integer systolic;
    private Integer diastolic;
    private Integer heartRate;
    private Float bloodSugar;
    private String note;

    public HealthRecord() {}

    public HealthRecord(long id, long userId, String date, Float weightKg,
                        Integer systolic, Integer diastolic, Integer heartRate,
                        Float bloodSugar, String note) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.weightKg = weightKg;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.heartRate = heartRate;
        this.bloodSugar = bloodSugar;
        this.note = note;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Float getWeightKg() { return weightKg; }
    public void setWeightKg(Float weightKg) { this.weightKg = weightKg; }

    public Integer getSystolic() { return systolic; }
    public void setSystolic(Integer systolic) { this.systolic = systolic; }

    public Integer getDiastolic() { return diastolic; }
    public void setDiastolic(Integer diastolic) { this.diastolic = diastolic; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Float getBloodSugar() { return bloodSugar; }
    public void setBloodSugar(Float bloodSugar) { this.bloodSugar = bloodSugar; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

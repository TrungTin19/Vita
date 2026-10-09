package vn.edu.tdmu.vita.models;

public class MedicineLog {
    private long id;
    private long medicineId;
    private String date;
    private String status; // 'TAKEN' or 'SKIPPED'
    private String loggedAt;

    public MedicineLog() {}

    public MedicineLog(long id, long medicineId, String date, String status, String loggedAt) {
        this.id = id;
        this.medicineId = medicineId;
        this.date = date;
        this.status = status;
        this.loggedAt = loggedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMedicineId() { return medicineId; }
    public void setMedicineId(long medicineId) { this.medicineId = medicineId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLoggedAt() { return loggedAt; }
    public void setLoggedAt(String loggedAt) { this.loggedAt = loggedAt; }
}

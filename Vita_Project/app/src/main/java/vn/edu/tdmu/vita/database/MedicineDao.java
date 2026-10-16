package vn.edu.tdmu.vita.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.models.MedicineLog;

public class MedicineDao {

    private final DatabaseHelper dbHelper;

    public MedicineDao(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public MedicineDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(Medicine medicine) {
        if (medicine == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(medicine);
        return db.insert(DatabaseHelper.TABLE_MEDICINES, null, values);
    }

    public boolean update(Medicine medicine) {
        if (medicine == null || medicine.getId() <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(medicine);
        int rows = db.update(
                DatabaseHelper.TABLE_MEDICINES,
                values,
                DatabaseHelper.COL_MED_ID + " = ?",
                new String[]{String.valueOf(medicine.getId())}
        );
        return rows > 0;
    }

    public boolean delete(long id) {
        if (id <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_MEDICINES,
                DatabaseHelper.COL_MED_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        return rows > 0;
    }

    public boolean toggleActive(long id, boolean isActive) {
        if (id <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_MED_IS_ACTIVE, isActive ? 1 : 0);
        int rows = db.update(
                DatabaseHelper.TABLE_MEDICINES,
                values,
                DatabaseHelper.COL_MED_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        return rows > 0;
    }

    public Medicine getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MEDICINES,
                null,
                DatabaseHelper.COL_MED_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            Medicine med = cursorToMedicine(cursor);
            cursor.close();
            return med;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Medicine> getAllByUserId(long userId) {
        List<Medicine> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MEDICINES,
                null,
                DatabaseHelper.COL_MED_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                DatabaseHelper.COL_MED_TIME + " ASC"
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToMedicine(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<Medicine> getActiveMedicines(long userId) {
        List<Medicine> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MEDICINES,
                null,
                DatabaseHelper.COL_MED_USER_ID + " = ? AND " + DatabaseHelper.COL_MED_IS_ACTIVE + " = 1",
                new String[]{String.valueOf(userId)},
                null, null,
                DatabaseHelper.COL_MED_TIME + " ASC"
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToMedicine(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public long logMedicineStatus(long medicineId, String date, String scheduledTime, String status, String loggedAt) {
        if (!"TAKEN".equals(status) && !"SKIPPED".equals(status)) throw new IllegalArgumentException("Invalid dose status");
        if (scheduledTime == null || !scheduledTime.matches("([01]\\d|2[0-3]):[0-5]\\d")) throw new IllegalArgumentException("Invalid dose time");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_MED_LOG_MEDICINE_ID, medicineId);
        values.put(DatabaseHelper.COL_MED_LOG_DATE, date);
        values.put(DatabaseHelper.COL_MED_LOG_TIME, scheduledTime);
        values.put(DatabaseHelper.COL_MED_LOG_STATUS, status);
        values.put(DatabaseHelper.COL_MED_LOG_LOGGED_AT, loggedAt);
        db.beginTransaction();
        try {
            MedicineLog previous = getLogForDose(medicineId, date, scheduledTime);
            long id;
            if (previous == null) id = db.insertOrThrow(DatabaseHelper.TABLE_MEDICINE_LOGS, null, values);
            else {
                id = previous.getId();
                db.update(DatabaseHelper.TABLE_MEDICINE_LOGS, values, "id = ?", new String[]{String.valueOf(id)});
            }
            db.setTransactionSuccessful();
            return id;
        } finally { db.endTransaction(); }
    }

    public MedicineLog getLogForDose(long medicineId, String date, String time) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_MEDICINE_LOGS, null,
                "medicine_id = ? AND date = ? AND scheduled_time = ?",
                new String[]{String.valueOf(medicineId), date, time}, null, null, "id DESC", "1")) {
            return cursor.moveToFirst() ? cursorToLog(cursor) : null;
        }
    }

    public List<MedicineLog> getHistory(long medicineId) {
        List<MedicineLog> logs = new ArrayList<>();
        try (Cursor cursor = dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_MEDICINE_LOGS,
                null, "medicine_id = ?", new String[]{String.valueOf(medicineId)},
                null, null, "date DESC, scheduled_time DESC, id DESC")) {
            while (cursor.moveToNext()) logs.add(cursorToLog(cursor));
        }
        return logs;
    }

    private ContentValues toContentValues(Medicine med) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_MED_USER_ID, med.getUserId());
        values.put(DatabaseHelper.COL_MED_NAME, med.getName());
        values.put(DatabaseHelper.COL_MED_DOSAGE, med.getDosage());
        values.put(DatabaseHelper.COL_MED_TIME, vn.edu.tdmu.vita.utils.MedicineSchedule.normalizeTimes(med.getTime()));
        values.put(DatabaseHelper.COL_MED_DAYS_MASK, med.getDaysMask());
        values.put(DatabaseHelper.COL_MED_IS_ACTIVE, med.isActive() ? 1 : 0);
        return values;
    }

    private Medicine cursorToMedicine(Cursor cursor) {
        Medicine med = new Medicine();
        med.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_ID)));
        med.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_USER_ID)));
        med.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_NAME)));
        med.setDosage(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_DOSAGE)));
        med.setTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_TIME)));
        med.setDaysMask(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_DAYS_MASK)));
        med.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_IS_ACTIVE)) == 1);
        return med;
    }

    private MedicineLog cursorToLog(Cursor cursor) {
        MedicineLog log = new MedicineLog();
        log.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_LOG_ID)));
        log.setMedicineId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_LOG_MEDICINE_ID)));
        log.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_LOG_DATE)));
        log.setScheduledTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_LOG_TIME)));
        log.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_LOG_STATUS)));
        log.setLoggedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_LOG_LOGGED_AT)));
        return log;
    }
}

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

    public List<Medicine> getAllActiveMedicinesAcrossAllUsers() {
        List<Medicine> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MEDICINES,
                null,
                DatabaseHelper.COL_MED_IS_ACTIVE + " = 1",
                null,
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

    // Medicine Logs
    public long logMedicineStatus(long medicineId, String date, String status, String loggedAt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_MED_LOG_MEDICINE_ID, medicineId);
        values.put(DatabaseHelper.COL_MED_LOG_DATE, date);
        values.put(DatabaseHelper.COL_MED_LOG_STATUS, status);
        values.put(DatabaseHelper.COL_MED_LOG_LOGGED_AT, loggedAt);

        // Check if log exists for this medicine and date, update if so
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MEDICINE_LOGS,
                new String[]{DatabaseHelper.COL_MED_LOG_ID},
                DatabaseHelper.COL_MED_LOG_MEDICINE_ID + " = ? AND " + DatabaseHelper.COL_MED_LOG_DATE + " = ?",
                new String[]{String.valueOf(medicineId), date},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            long existingId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_LOG_ID));
            cursor.close();
            db.update(
                    DatabaseHelper.TABLE_MEDICINE_LOGS,
                    values,
                    DatabaseHelper.COL_MED_LOG_ID + " = ?",
                    new String[]{String.valueOf(existingId)}
            );
            return existingId;
        }
        if (cursor != null) cursor.close();

        return db.insert(DatabaseHelper.TABLE_MEDICINE_LOGS, null, values);
    }

    public MedicineLog getLogForMedicine(long medicineId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_MEDICINE_LOGS,
                null,
                DatabaseHelper.COL_MED_LOG_MEDICINE_ID + " = ? AND " + DatabaseHelper.COL_MED_LOG_DATE + " = ?",
                new String[]{String.valueOf(medicineId), date},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            MedicineLog log = cursorToLog(cursor);
            cursor.close();
            return log;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    private ContentValues toContentValues(Medicine med) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_MED_USER_ID, med.getUserId());
        values.put(DatabaseHelper.COL_MED_NAME, med.getName());
        values.put(DatabaseHelper.COL_MED_DOSAGE, med.getDosage());
        values.put(DatabaseHelper.COL_MED_TIME, med.getTime());
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
        log.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_LOG_STATUS)));
        log.setLoggedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MED_LOG_LOGGED_AT)));
        return log;
    }
}

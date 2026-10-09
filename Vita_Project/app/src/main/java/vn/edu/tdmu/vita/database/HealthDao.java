package vn.edu.tdmu.vita.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdmu.vita.models.HealthRecord;

public class HealthDao {

    private final DatabaseHelper dbHelper;

    public HealthDao(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public HealthDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(HealthRecord record) {
        if (record == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(record);
        return db.insert(DatabaseHelper.TABLE_HEALTH_RECORDS, null, values);
    }

    public boolean update(HealthRecord record) {
        if (record == null || record.getId() <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(record);
        int rows = db.update(
                DatabaseHelper.TABLE_HEALTH_RECORDS,
                values,
                DatabaseHelper.COL_HEALTH_ID + " = ?",
                new String[]{String.valueOf(record.getId())}
        );
        return rows > 0;
    }

    public boolean delete(long id) {
        if (id <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_HEALTH_RECORDS,
                DatabaseHelper.COL_HEALTH_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        return rows > 0;
    }

    public HealthRecord getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_HEALTH_RECORDS,
                null,
                DatabaseHelper.COL_HEALTH_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            HealthRecord record = cursorToRecord(cursor);
            cursor.close();
            return record;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<HealthRecord> getAllByUserId(long userId, String dateFilter) {
        List<HealthRecord> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COL_HEALTH_USER_ID + " = ?";
        List<String> argsList = new ArrayList<>();
        argsList.add(String.valueOf(userId));

        if (dateFilter != null && !dateFilter.trim().isEmpty()) {
            selection += " AND " + DatabaseHelper.COL_HEALTH_DATE + " = ?";
            argsList.add(dateFilter.trim());
        }

        String[] selectionArgs = argsList.toArray(new String[0]);
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_HEALTH_RECORDS,
                null,
                selection,
                selectionArgs,
                null, null,
                DatabaseHelper.COL_HEALTH_DATE + " DESC, " + DatabaseHelper.COL_HEALTH_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToRecord(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public Float getLatestWeight(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_HEALTH_RECORDS,
                new String[]{DatabaseHelper.COL_HEALTH_WEIGHT_KG},
                DatabaseHelper.COL_HEALTH_USER_ID + " = ? AND " + DatabaseHelper.COL_HEALTH_WEIGHT_KG + " IS NOT NULL",
                new String[]{String.valueOf(userId)},
                null, null,
                DatabaseHelper.COL_HEALTH_DATE + " DESC, " + DatabaseHelper.COL_HEALTH_ID + " DESC",
                "1"
        );

        if (cursor != null && cursor.moveToFirst()) {
            float weight = cursor.getFloat(0);
            cursor.close();
            return weight;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    private ContentValues toContentValues(HealthRecord record) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_HEALTH_USER_ID, record.getUserId());
        values.put(DatabaseHelper.COL_HEALTH_DATE, record.getDate());

        if (record.getWeightKg() != null) values.put(DatabaseHelper.COL_HEALTH_WEIGHT_KG, record.getWeightKg());
        else values.putNull(DatabaseHelper.COL_HEALTH_WEIGHT_KG);

        if (record.getSystolic() != null) values.put(DatabaseHelper.COL_HEALTH_SYSTOLIC, record.getSystolic());
        else values.putNull(DatabaseHelper.COL_HEALTH_SYSTOLIC);

        if (record.getDiastolic() != null) values.put(DatabaseHelper.COL_HEALTH_DIASTOLIC, record.getDiastolic());
        else values.putNull(DatabaseHelper.COL_HEALTH_DIASTOLIC);

        if (record.getHeartRate() != null) values.put(DatabaseHelper.COL_HEALTH_HEART_RATE, record.getHeartRate());
        else values.putNull(DatabaseHelper.COL_HEALTH_HEART_RATE);

        if (record.getBloodSugar() != null) values.put(DatabaseHelper.COL_HEALTH_BLOOD_SUGAR, record.getBloodSugar());
        else values.putNull(DatabaseHelper.COL_HEALTH_BLOOD_SUGAR);

        values.put(DatabaseHelper.COL_HEALTH_NOTE, record.getNote());
        return values;
    }

    private HealthRecord cursorToRecord(Cursor cursor) {
        HealthRecord r = new HealthRecord();
        r.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_ID)));
        r.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_USER_ID)));
        r.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_DATE)));

        int weightIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_WEIGHT_KG);
        r.setWeightKg(cursor.isNull(weightIdx) ? null : cursor.getFloat(weightIdx));

        int sysIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_SYSTOLIC);
        r.setSystolic(cursor.isNull(sysIdx) ? null : cursor.getInt(sysIdx));

        int diaIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_DIASTOLIC);
        r.setDiastolic(cursor.isNull(diaIdx) ? null : cursor.getInt(diaIdx));

        int hrIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_HEART_RATE);
        r.setHeartRate(cursor.isNull(hrIdx) ? null : cursor.getInt(hrIdx));

        int bsIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_BLOOD_SUGAR);
        r.setBloodSugar(cursor.isNull(bsIdx) ? null : cursor.getFloat(bsIdx));

        r.setNote(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_NOTE)));
        return r;
    }
}

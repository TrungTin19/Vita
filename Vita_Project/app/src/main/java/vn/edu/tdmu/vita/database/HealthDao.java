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

    public boolean update(HealthRecord record, long userId) {
        if (record == null || record.getId() <= 0 || record.getUserId() != userId || userId <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(record);
        int rows = db.update(
                DatabaseHelper.TABLE_HEALTH_RECORDS,
                values,
                DatabaseHelper.COL_HEALTH_ID + " = ? AND " + DatabaseHelper.COL_HEALTH_USER_ID + " = ?",
                new String[]{String.valueOf(record.getId()), String.valueOf(userId)}
        );
        return rows > 0;
    }

    public boolean delete(long id, long userId) {
        if (id <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_HEALTH_RECORDS,
                DatabaseHelper.COL_HEALTH_ID + " = ? AND " + DatabaseHelper.COL_HEALTH_USER_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(userId)}
        );
        return rows > 0;
    }

    public HealthRecord getById(long id, long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_HEALTH_RECORDS,
                null,
                DatabaseHelper.COL_HEALTH_ID + " = ? AND " + DatabaseHelper.COL_HEALTH_USER_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(userId)},
                null, null, null
        )) {
            return cursor.moveToFirst() ? cursorToRecord(cursor) : null;
        }
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
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_HEALTH_RECORDS,
                null,
                selection,
                selectionArgs,
                null, null,
                DatabaseHelper.COL_HEALTH_DATE + " DESC, COALESCE(measured_time, '') DESC, " + DatabaseHelper.COL_HEALTH_ID + " DESC"
        )) {
            while (cursor.moveToNext()) {
                list.add(cursorToRecord(cursor));
            }
        }
        return list;
    }

    public List<HealthRecord> getRecordsRange(long userId, String startDate, String endDate) {
        List<HealthRecord> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_HEALTH_RECORDS,
                null,
                DatabaseHelper.COL_HEALTH_USER_ID + " = ? AND " +
                        DatabaseHelper.COL_HEALTH_DATE + " >= ? AND " +
                        DatabaseHelper.COL_HEALTH_DATE + " <= ?",
                new String[]{String.valueOf(userId), startDate, endDate},
                null, null,
                DatabaseHelper.COL_HEALTH_DATE + " ASC, COALESCE(measured_time, '') ASC, " + DatabaseHelper.COL_HEALTH_ID + " ASC"
        )) {
            while (cursor.moveToNext()) {
                list.add(cursorToRecord(cursor));
            }
        }
        return list;
    }

    public Float getLatestWeight(long userId) {
        HealthRecord record = getLatestWeightRecord(userId);
        return record == null ? null : record.getWeightKg();
    }

    public HealthRecord getPreviousComparable(long userId, HealthRecord current,
            vn.edu.tdmu.vita.utils.HealthChartData.Metric metric) {
        if (current == null || getById(current.getId(), userId) == null) return null;
        if (metric == vn.edu.tdmu.vita.utils.HealthChartData.Metric.BLOOD_SUGAR
                && current.getMealContext() == HealthRecord.MealContext.UNSPECIFIED) return null;
        String required;
        switch (metric) {
            case WEIGHT: required = "weight_kg IS NOT NULL"; break;
            case HEART_RATE: required = "heart_rate IS NOT NULL"; break;
            case BLOOD_PRESSURE: required = "systolic IS NOT NULL AND diastolic IS NOT NULL"; break;
            default: required = "blood_sugar IS NOT NULL AND meal_context = ?";
        }
        String time = current.getMeasuredTime() == null ? "" : current.getMeasuredTime();
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));
        if (metric == vn.edu.tdmu.vita.utils.HealthChartData.Metric.BLOOD_SUGAR)
            args.add(current.getMealContext().name());
        java.util.Collections.addAll(args, current.getDate(), current.getDate(), time,
                current.getDate(), time, String.valueOf(current.getId()));
        try (Cursor cursor = dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_HEALTH_RECORDS,
                null, "user_id = ? AND " + required + " AND (date < ? OR (date = ? AND COALESCE(measured_time,'') < ?)"
                        + " OR (date = ? AND COALESCE(measured_time,'') = ? AND id < ?))",
                args.toArray(new String[0]), null, null,
                "date DESC, COALESCE(measured_time,'') DESC, id DESC", "1")) {
            return cursor.moveToFirst() ? cursorToRecord(cursor) : null;
        }
    }

    public HealthRecord getLatestWeightRecord(long userId) {
        try (Cursor cursor = dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_HEALTH_RECORDS,
                null, "user_id = ? AND weight_kg IS NOT NULL", new String[]{String.valueOf(userId)},
                null, null, "date DESC, COALESCE(measured_time, '') DESC, id DESC", "1")) {
            return cursor.moveToFirst() ? cursorToRecord(cursor) : null;
        }
    }

    private ContentValues toContentValues(HealthRecord record) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_HEALTH_USER_ID, record.getUserId());
        values.put(DatabaseHelper.COL_HEALTH_DATE, record.getDate());
        values.put("measured_time", record.getMeasuredTime());
        values.put("meal_context", record.getMealContext().name());

        values.put(DatabaseHelper.COL_HEALTH_WEIGHT_KG, record.getWeightKg());
        values.put(DatabaseHelper.COL_HEALTH_SYSTOLIC, record.getSystolic());
        values.put(DatabaseHelper.COL_HEALTH_DIASTOLIC, record.getDiastolic());
        values.put(DatabaseHelper.COL_HEALTH_HEART_RATE, record.getHeartRate());
        values.put(DatabaseHelper.COL_HEALTH_BLOOD_SUGAR, record.getBloodSugar());

        values.put(DatabaseHelper.COL_HEALTH_NOTE, record.getNote());
        return values;
    }

    private HealthRecord cursorToRecord(Cursor cursor) {
        HealthRecord r = new HealthRecord();
        r.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_ID)));
        r.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_USER_ID)));
        r.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HEALTH_DATE)));
        r.setMeasuredTime(cursor.getString(cursor.getColumnIndexOrThrow("measured_time")));
        r.setMealContext(cursor.getString(cursor.getColumnIndexOrThrow("meal_context")));

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

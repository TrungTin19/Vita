package vn.edu.tdmu.vita.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.edu.tdmu.vita.models.WaterLog;

public class WaterDao {

    private final DatabaseHelper dbHelper;

    public WaterDao(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public WaterDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long addWater(long userId, String date, int amountMl, String loggedAt) {
        if (amountMl <= 0) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_WATER_USER_ID, userId);
        values.put(DatabaseHelper.COL_WATER_DATE, date);
        values.put(DatabaseHelper.COL_WATER_AMOUNT_ML, amountMl);
        values.put(DatabaseHelper.COL_WATER_LOGGED_AT, loggedAt);
        return db.insert(DatabaseHelper.TABLE_WATER_LOGS, null, values);
    }

    public int getDailyTotal(long userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT SUM(" + DatabaseHelper.COL_WATER_AMOUNT_ML + ") FROM " +
                DatabaseHelper.TABLE_WATER_LOGS + " WHERE " +
                DatabaseHelper.COL_WATER_USER_ID + " = ? AND " +
                DatabaseHelper.COL_WATER_DATE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), date});
        int total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getInt(0);
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        return total;
    }

    public List<WaterLog> getLogsByDate(long userId, String date) {
        List<WaterLog> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_WATER_LOGS,
                null,
                DatabaseHelper.COL_WATER_USER_ID + " = ? AND " + DatabaseHelper.COL_WATER_DATE + " = ?",
                new String[]{String.valueOf(userId), date},
                null, null,
                DatabaseHelper.COL_WATER_LOGGED_AT + " DESC"
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToLog(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public Map<String, Integer> getDailyTotalsRange(long userId, String startDate, String endDate) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DatabaseHelper.COL_WATER_DATE + ", SUM(" + DatabaseHelper.COL_WATER_AMOUNT_ML + ") as total " +
                "FROM " + DatabaseHelper.TABLE_WATER_LOGS + " " +
                "WHERE " + DatabaseHelper.COL_WATER_USER_ID + " = ? AND " +
                DatabaseHelper.COL_WATER_DATE + " >= ? AND " +
                DatabaseHelper.COL_WATER_DATE + " <= ? " +
                "GROUP BY " + DatabaseHelper.COL_WATER_DATE + " " +
                "ORDER BY " + DatabaseHelper.COL_WATER_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), startDate, endDate});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String date = cursor.getString(0);
                int total = cursor.getInt(1);
                totals.put(date, total);
            }
            cursor.close();
        }
        return totals;
    }

    public boolean deleteLog(long id) {
        if (id <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_WATER_LOGS,
                DatabaseHelper.COL_WATER_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        return rows > 0;
    }

    private WaterLog cursorToLog(Cursor cursor) {
        WaterLog log = new WaterLog();
        log.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WATER_ID)));
        log.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WATER_USER_ID)));
        log.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WATER_DATE)));
        log.setAmountMl(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WATER_AMOUNT_ML)));
        log.setLoggedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WATER_LOGGED_AT)));
        return log;
    }
}

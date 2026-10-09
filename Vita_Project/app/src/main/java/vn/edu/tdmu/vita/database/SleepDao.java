package vn.edu.tdmu.vita.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdmu.vita.models.SleepLog;

public class SleepDao {

    private final DatabaseHelper dbHelper;

    public SleepDao(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public SleepDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(SleepLog log) {
        if (log == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(log);
        return db.insert(DatabaseHelper.TABLE_SLEEP_LOGS, null, values);
    }

    public boolean update(SleepLog log) {
        if (log == null || log.getId() <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(log);
        int rows = db.update(
                DatabaseHelper.TABLE_SLEEP_LOGS,
                values,
                DatabaseHelper.COL_SLEEP_ID + " = ?",
                new String[]{String.valueOf(log.getId())}
        );
        return rows > 0;
    }

    public boolean delete(long id) {
        if (id <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_SLEEP_LOGS,
                DatabaseHelper.COL_SLEEP_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        return rows > 0;
    }

    public SleepLog getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_SLEEP_LOGS,
                null,
                DatabaseHelper.COL_SLEEP_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            SleepLog log = cursorToLog(cursor);
            cursor.close();
            return log;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public SleepLog getByDate(long userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_SLEEP_LOGS,
                null,
                DatabaseHelper.COL_SLEEP_USER_ID + " = ? AND " + DatabaseHelper.COL_SLEEP_DATE + " = ?",
                new String[]{String.valueOf(userId), date},
                null, null,
                DatabaseHelper.COL_SLEEP_ID + " DESC",
                "1"
        );
        if (cursor != null && cursor.moveToFirst()) {
            SleepLog log = cursorToLog(cursor);
            cursor.close();
            return log;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<SleepLog> getLatest7Days(long userId) {
        List<SleepLog> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_SLEEP_LOGS,
                null,
                DatabaseHelper.COL_SLEEP_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                DatabaseHelper.COL_SLEEP_DATE + " DESC",
                "7"
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToLog(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<SleepLog> getLogsRange(long userId, String startDate, String endDate) {
        List<SleepLog> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_SLEEP_LOGS,
                null,
                DatabaseHelper.COL_SLEEP_USER_ID + " = ? AND " +
                        DatabaseHelper.COL_SLEEP_DATE + " >= ? AND " +
                        DatabaseHelper.COL_SLEEP_DATE + " <= ?",
                new String[]{String.valueOf(userId), startDate, endDate},
                null, null,
                DatabaseHelper.COL_SLEEP_DATE + " ASC"
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToLog(cursor));
            }
            cursor.close();
        }
        return list;
    }

    private ContentValues toContentValues(SleepLog log) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SLEEP_USER_ID, log.getUserId());
        values.put(DatabaseHelper.COL_SLEEP_DATE, log.getDate());
        values.put(DatabaseHelper.COL_SLEEP_TIME, log.getSleepTime());
        values.put(DatabaseHelper.COL_SLEEP_WAKE_TIME, log.getWakeTime());
        values.put(DatabaseHelper.COL_SLEEP_DURATION_H, log.getDurationH());
        return values;
    }

    private SleepLog cursorToLog(Cursor cursor) {
        SleepLog log = new SleepLog();
        log.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SLEEP_ID)));
        log.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SLEEP_USER_ID)));
        log.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SLEEP_DATE)));
        log.setSleepTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SLEEP_TIME)));
        log.setWakeTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SLEEP_WAKE_TIME)));
        log.setDurationH(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SLEEP_DURATION_H)));
        return log;
    }
}

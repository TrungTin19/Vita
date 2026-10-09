package vn.edu.tdmu.vita.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.SecurityUtils;

public class UserDao {

    private final DatabaseHelper dbHelper;

    public UserDao(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public UserDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean isUsernameExists(String username) {
        if (username == null) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COL_USER_ID},
                DatabaseHelper.COL_USER_USERNAME + " = ?",
                new String[]{username.trim()},
                null, null, null
        );
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public long register(User user, String rawPassword) {
        if (user == null || user.getUsername() == null || rawPassword == null) {
            return -1;
        }
        if (isUsernameExists(user.getUsername())) {
            return -1; // Duplicate username
        }

        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword(rawPassword, salt);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_USERNAME, user.getUsername().trim());
        values.put(DatabaseHelper.COL_USER_PASSWORD_HASH, hash);
        values.put(DatabaseHelper.COL_USER_SALT, salt);
        values.put(DatabaseHelper.COL_USER_FULLNAME, user.getFullname());
        values.put(DatabaseHelper.COL_USER_BIRTH_YEAR, user.getBirthYear());
        values.put(DatabaseHelper.COL_USER_GENDER, user.getGender());
        values.put(DatabaseHelper.COL_USER_HEIGHT_CM, user.getHeightCm());
        values.put(DatabaseHelper.COL_USER_BASE_WEIGHT_KG, user.getBaseWeightKg());
        values.put(DatabaseHelper.COL_USER_WATER_GOAL_ML, user.getWaterGoalMl() > 0 ? user.getWaterGoalMl() : 2000);
        values.put(DatabaseHelper.COL_USER_BMI_STANDARD, user.getBmiStandard() != null ? user.getBmiStandard() : "ASIAN");

        return db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    public User login(String username, String rawPassword) {
        if (username == null || rawPassword == null) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COL_USER_USERNAME + " = ?",
                new String[]{username.trim()},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String dbHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD_HASH));
            String dbSalt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_SALT));

            String computedHash = SecurityUtils.hashPassword(rawPassword, dbSalt);
            if (dbHash != null && dbHash.equals(computedHash)) {
                User user = cursorToUser(cursor);
                cursor.close();
                return user;
            }
            cursor.close();
        }
        return null;
    }

    public User getUserById(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            User user = cursorToUser(cursor);
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public boolean updateProfile(User user) {
        if (user == null || user.getId() <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_FULLNAME, user.getFullname());
        values.put(DatabaseHelper.COL_USER_BIRTH_YEAR, user.getBirthYear());
        values.put(DatabaseHelper.COL_USER_GENDER, user.getGender());
        values.put(DatabaseHelper.COL_USER_HEIGHT_CM, user.getHeightCm());
        values.put(DatabaseHelper.COL_USER_BASE_WEIGHT_KG, user.getBaseWeightKg());
        values.put(DatabaseHelper.COL_USER_WATER_GOAL_ML, user.getWaterGoalMl());

        int rows = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COL_USER_ID + " = ?",
                new String[]{String.valueOf(user.getId())}
        );
        return rows > 0;
    }

    public boolean updateBmiStandard(long userId, String standard) {
        if (userId <= 0 || standard == null) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_BMI_STANDARD, standard);

        int rows = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
        return rows > 0;
    }

    private User cursorToUser(Cursor cursor) {
        User u = new User();
        u.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
        u.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)));
        u.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD_HASH)));
        u.setSalt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_SALT)));
        u.setFullname(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FULLNAME)));
        u.setBirthYear(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_BIRTH_YEAR)));
        u.setGender(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_GENDER)));
        u.setHeightCm(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_HEIGHT_CM)));
        u.setBaseWeightKg(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_BASE_WEIGHT_KG)));
        u.setWaterGoalMl(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_WATER_GOAL_ML)));
        u.setBmiStandard(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_BMI_STANDARD)));
        return u;
    }
}

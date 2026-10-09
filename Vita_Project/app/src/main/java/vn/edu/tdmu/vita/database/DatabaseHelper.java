package vn.edu.tdmu.vita.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "vita_health.db";
    public static final int DATABASE_VERSION = 1;

    // Table: users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_PASSWORD_HASH = "password_hash";
    public static final String COL_USER_SALT = "salt";
    public static final String COL_USER_FULLNAME = "fullname";
    public static final String COL_USER_BIRTH_YEAR = "birth_year";
    public static final String COL_USER_GENDER = "gender";
    public static final String COL_USER_HEIGHT_CM = "height_cm";
    public static final String COL_USER_BASE_WEIGHT_KG = "base_weight_kg";
    public static final String COL_USER_WATER_GOAL_ML = "water_goal_ml";
    public static final String COL_USER_BMI_STANDARD = "bmi_standard";

    // Table: health_records
    public static final String TABLE_HEALTH_RECORDS = "health_records";
    public static final String COL_HEALTH_ID = "id";
    public static final String COL_HEALTH_USER_ID = "user_id";
    public static final String COL_HEALTH_DATE = "date";
    public static final String COL_HEALTH_WEIGHT_KG = "weight_kg";
    public static final String COL_HEALTH_SYSTOLIC = "systolic";
    public static final String COL_HEALTH_DIASTOLIC = "diastolic";
    public static final String COL_HEALTH_HEART_RATE = "heart_rate";
    public static final String COL_HEALTH_BLOOD_SUGAR = "blood_sugar";
    public static final String COL_HEALTH_NOTE = "note";

    // Table: medicines
    public static final String TABLE_MEDICINES = "medicines";
    public static final String COL_MED_ID = "id";
    public static final String COL_MED_USER_ID = "user_id";
    public static final String COL_MED_NAME = "name";
    public static final String COL_MED_DOSAGE = "dosage";
    public static final String COL_MED_TIME = "time";
    public static final String COL_MED_DAYS_MASK = "days_mask";
    public static final String COL_MED_IS_ACTIVE = "is_active";

    // Table: medicine_logs
    public static final String TABLE_MEDICINE_LOGS = "medicine_logs";
    public static final String COL_MED_LOG_ID = "id";
    public static final String COL_MED_LOG_MEDICINE_ID = "medicine_id";
    public static final String COL_MED_LOG_DATE = "date";
    public static final String COL_MED_LOG_STATUS = "status";
    public static final String COL_MED_LOG_LOGGED_AT = "logged_at";

    // Table: water_logs
    public static final String TABLE_WATER_LOGS = "water_logs";
    public static final String COL_WATER_ID = "id";
    public static final String COL_WATER_USER_ID = "user_id";
    public static final String COL_WATER_DATE = "date";
    public static final String COL_WATER_AMOUNT_ML = "amount_ml";
    public static final String COL_WATER_LOGGED_AT = "logged_at";

    // Table: sleep_logs
    public static final String TABLE_SLEEP_LOGS = "sleep_logs";
    public static final String COL_SLEEP_ID = "id";
    public static final String COL_SLEEP_USER_ID = "user_id";
    public static final String COL_SLEEP_DATE = "date";
    public static final String COL_SLEEP_TIME = "sleep_time";
    public static final String COL_SLEEP_WAKE_TIME = "wake_time";
    public static final String COL_SLEEP_DURATION_H = "duration_h";

    // DDL Queries
    public static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
            COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_USER_USERNAME + " TEXT UNIQUE NOT NULL, " +
            COL_USER_PASSWORD_HASH + " TEXT NOT NULL, " +
            COL_USER_SALT + " TEXT NOT NULL, " +
            COL_USER_FULLNAME + " TEXT, " +
            COL_USER_BIRTH_YEAR + " INTEGER, " +
            COL_USER_GENDER + " TEXT, " +
            COL_USER_HEIGHT_CM + " REAL, " +
            COL_USER_BASE_WEIGHT_KG + " REAL, " +
            COL_USER_WATER_GOAL_ML + " INTEGER DEFAULT 2000, " +
            COL_USER_BMI_STANDARD + " TEXT DEFAULT 'ASIAN'" +
            ");";

    public static final String CREATE_TABLE_HEALTH_RECORDS =
            "CREATE TABLE " + TABLE_HEALTH_RECORDS + " (" +
            COL_HEALTH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_HEALTH_USER_ID + " INTEGER NOT NULL, " +
            COL_HEALTH_DATE + " TEXT NOT NULL, " +
            COL_HEALTH_WEIGHT_KG + " REAL, " +
            COL_HEALTH_SYSTOLIC + " INTEGER, " +
            COL_HEALTH_DIASTOLIC + " INTEGER, " +
            COL_HEALTH_HEART_RATE + " INTEGER, " +
            COL_HEALTH_BLOOD_SUGAR + " REAL, " +
            COL_HEALTH_NOTE + " TEXT, " +
            "FOREIGN KEY(" + COL_HEALTH_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" +
            ");";

    public static final String CREATE_TABLE_MEDICINES =
            "CREATE TABLE " + TABLE_MEDICINES + " (" +
            COL_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_MED_USER_ID + " INTEGER NOT NULL, " +
            COL_MED_NAME + " TEXT NOT NULL, " +
            COL_MED_DOSAGE + " TEXT, " +
            COL_MED_TIME + " TEXT NOT NULL, " +
            COL_MED_DAYS_MASK + " INTEGER DEFAULT 127, " +
            COL_MED_IS_ACTIVE + " INTEGER DEFAULT 1, " +
            "FOREIGN KEY(" + COL_MED_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" +
            ");";

    public static final String CREATE_TABLE_MEDICINE_LOGS =
            "CREATE TABLE " + TABLE_MEDICINE_LOGS + " (" +
            COL_MED_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_MED_LOG_MEDICINE_ID + " INTEGER NOT NULL, " +
            COL_MED_LOG_DATE + " TEXT NOT NULL, " +
            COL_MED_LOG_STATUS + " TEXT NOT NULL, " +
            COL_MED_LOG_LOGGED_AT + " TEXT NOT NULL, " +
            "FOREIGN KEY(" + COL_MED_LOG_MEDICINE_ID + ") REFERENCES " + TABLE_MEDICINES + "(" + COL_MED_ID + ") ON DELETE CASCADE" +
            ");";

    public static final String CREATE_TABLE_WATER_LOGS =
            "CREATE TABLE " + TABLE_WATER_LOGS + " (" +
            COL_WATER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_WATER_USER_ID + " INTEGER NOT NULL, " +
            COL_WATER_DATE + " TEXT NOT NULL, " +
            COL_WATER_AMOUNT_ML + " INTEGER NOT NULL, " +
            COL_WATER_LOGGED_AT + " TEXT NOT NULL, " +
            "FOREIGN KEY(" + COL_WATER_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" +
            ");";

    public static final String CREATE_TABLE_SLEEP_LOGS =
            "CREATE TABLE " + TABLE_SLEEP_LOGS + " (" +
            COL_SLEEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_SLEEP_USER_ID + " INTEGER NOT NULL, " +
            COL_SLEEP_DATE + " TEXT NOT NULL, " +
            COL_SLEEP_TIME + " TEXT NOT NULL, " +
            COL_SLEEP_WAKE_TIME + " TEXT NOT NULL, " +
            COL_SLEEP_DURATION_H + " REAL NOT NULL, " +
            "FOREIGN KEY(" + COL_SLEEP_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" +
            ");";

    // Indexes
    public static final String CREATE_INDEX_HEALTH =
            "CREATE INDEX IF NOT EXISTS idx_health_user_date ON " + TABLE_HEALTH_RECORDS + "(" + COL_HEALTH_USER_ID + ", " + COL_HEALTH_DATE + ");";

    public static final String CREATE_INDEX_WATER =
            "CREATE INDEX IF NOT EXISTS idx_water_user_date ON " + TABLE_WATER_LOGS + "(" + COL_WATER_USER_ID + ", " + COL_WATER_DATE + ");";

    public static final String CREATE_INDEX_SLEEP =
            "CREATE INDEX IF NOT EXISTS idx_sleep_user_date ON " + TABLE_SLEEP_LOGS + "(" + COL_SLEEP_USER_ID + ", " + COL_SLEEP_DATE + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_HEALTH_RECORDS);
        db.execSQL(CREATE_TABLE_MEDICINES);
        db.execSQL(CREATE_TABLE_MEDICINE_LOGS);
        db.execSQL(CREATE_TABLE_WATER_LOGS);
        db.execSQL(CREATE_TABLE_SLEEP_LOGS);

        db.execSQL(CREATE_INDEX_HEALTH);
        db.execSQL(CREATE_INDEX_WATER);
        db.execSQL(CREATE_INDEX_SLEEP);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SLEEP_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATER_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICINE_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICINES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HEALTH_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}

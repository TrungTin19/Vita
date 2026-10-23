package vn.edu.tdmu.vita.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "vita_health.db";
    public static final int DATABASE_VERSION = 6;

    // Bảng: người dùng
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

    // Bảng: hồ sơ sức khỏe
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

    // Bảng: thuốc
    public static final String TABLE_MEDICINES = "medicines";
    public static final String COL_MED_ID = "id";
    public static final String COL_MED_USER_ID = "user_id";
    public static final String COL_MED_NAME = "name";
    public static final String COL_MED_DOSAGE = "dosage";
    public static final String COL_MED_TIME = "time";
    public static final String COL_MED_DAYS_MASK = "days_mask";
    public static final String COL_MED_IS_ACTIVE = "is_active";

    // Bảng: nhật ký uống thuốc
    public static final String TABLE_MEDICINE_LOGS = "medicine_logs";
    public static final String COL_MED_LOG_ID = "id";
    public static final String COL_MED_LOG_MEDICINE_ID = "medicine_id";
    public static final String COL_MED_LOG_DATE = "date";
    public static final String COL_MED_LOG_TIME = "scheduled_time";
    public static final String COL_MED_LOG_STATUS = "status";
    public static final String COL_MED_LOG_LOGGED_AT = "logged_at";

    // Bảng: nhật ký uống nước
    public static final String TABLE_WATER_LOGS = "water_logs";
    public static final String COL_WATER_ID = "id";
    public static final String COL_WATER_USER_ID = "user_id";
    public static final String COL_WATER_DATE = "date";
    public static final String COL_WATER_AMOUNT_ML = "amount_ml";
    public static final String COL_WATER_LOGGED_AT = "logged_at";

    // Các câu lệnh tạo cấu trúc cơ sở dữ liệu
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
            COL_USER_BMI_STANDARD + " TEXT DEFAULT 'ASIAN', " +
            "water_start TEXT NOT NULL DEFAULT '07:00', water_end TEXT NOT NULL DEFAULT '22:00', " +
            "water_amount INTEGER NOT NULL DEFAULT 250, " +
            "water_times TEXT NOT NULL DEFAULT ''" +
            ");";

    public static final String CREATE_TABLE_HEALTH_RECORDS =
            "CREATE TABLE " + TABLE_HEALTH_RECORDS + " (" +
            COL_HEALTH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_HEALTH_USER_ID + " INTEGER NOT NULL, " +
            COL_HEALTH_DATE + " TEXT NOT NULL, " +
            "measured_time TEXT, meal_context TEXT NOT NULL DEFAULT 'UNSPECIFIED', " +
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
            COL_MED_LOG_TIME + " TEXT NOT NULL, " +
            COL_MED_LOG_STATUS + " TEXT NOT NULL, " +
            COL_MED_LOG_LOGGED_AT + " TEXT NOT NULL, " +
            "medicine_name_snapshot TEXT, dosage_snapshot TEXT, snapshot_inherited INTEGER NOT NULL DEFAULT 0, " +
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

    // Các chỉ mục cơ sở dữ liệu
    public static final String CREATE_INDEX_HEALTH =
            "CREATE INDEX IF NOT EXISTS idx_health_user_date ON " + TABLE_HEALTH_RECORDS + "(" + COL_HEALTH_USER_ID + ", " + COL_HEALTH_DATE + ");";

    public static final String CREATE_INDEX_WATER =
            "CREATE INDEX IF NOT EXISTS idx_water_user_date ON " + TABLE_WATER_LOGS + "(" + COL_WATER_USER_ID + ", " + COL_WATER_DATE + ");";

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

        db.execSQL(CREATE_INDEX_HEALTH);
        db.execSQL(CREATE_INDEX_WATER);
        db.execSQL("CREATE INDEX idx_medicine_dose ON medicine_logs(medicine_id,date,scheduled_time)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6 && newVersion >= 6) {
            db.execSQL("ALTER TABLE medicine_logs ADD COLUMN medicine_name_snapshot TEXT");
            db.execSQL("ALTER TABLE medicine_logs ADD COLUMN dosage_snapshot TEXT");
            db.execSQL("ALTER TABLE medicine_logs ADD COLUMN snapshot_inherited INTEGER NOT NULL DEFAULT 1");
            db.execSQL("UPDATE medicine_logs SET medicine_name_snapshot = (SELECT name FROM medicines WHERE id = medicine_id), dosage_snapshot = (SELECT dosage FROM medicines WHERE id = medicine_id)");
        }
        if (oldVersion < 5 && newVersion >= 5) {
            db.execSQL("ALTER TABLE users ADD COLUMN water_times TEXT NOT NULL DEFAULT ''");
        }
        if (oldVersion < 2 && newVersion >= 2) {
            db.execSQL("ALTER TABLE medicine_logs ADD COLUMN scheduled_time TEXT NOT NULL DEFAULT ''");
            db.execSQL("UPDATE medicine_logs SET scheduled_time = COALESCE((SELECT time FROM medicines WHERE medicines.id = medicine_logs.medicine_id), '')");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_medicine_dose ON medicine_logs(medicine_id,date,scheduled_time)");
        }
        if (oldVersion < 4 && newVersion >= 4) {
            db.execSQL("ALTER TABLE users ADD COLUMN water_start TEXT NOT NULL DEFAULT '07:00'");
            db.execSQL("ALTER TABLE users ADD COLUMN water_end TEXT NOT NULL DEFAULT '22:00'");
            db.execSQL("ALTER TABLE users ADD COLUMN water_amount INTEGER NOT NULL DEFAULT 250");
        }
        if (oldVersion < 3 && newVersion >= 3) {
            db.execSQL("ALTER TABLE health_records ADD COLUMN measured_time TEXT");
            db.execSQL("ALTER TABLE health_records ADD COLUMN meal_context TEXT NOT NULL DEFAULT 'UNSPECIFIED'");
        }
    }
}

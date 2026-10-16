package vn.edu.tdmu.vita;

import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.database.DatabaseHelper;
import vn.edu.tdmu.vita.database.HealthDao;
import vn.edu.tdmu.vita.models.HealthRecord;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MeasurementDeviceTest {
    @Test public void upgrade_keepsUnknownLegacyTimeAndOrdersNewMeasurementsByTime() {
        try (SQLiteDatabase db = SQLiteDatabase.create(null)) {
            db.execSQL("CREATE TABLE health_records(id INTEGER PRIMARY KEY, user_id INTEGER, date TEXT, weight_kg REAL, systolic INTEGER, diastolic INTEGER, heart_rate INTEGER, blood_sugar REAL, note TEXT)");
            db.execSQL("INSERT INTO health_records VALUES(1,7,'2026-10-16',NULL,NULL,NULL,NULL,100,'old')");
            DatabaseHelper helper = new DatabaseHelper(ApplicationProvider.getApplicationContext()) {
                @Override public SQLiteDatabase getWritableDatabase() { return db; }
                @Override public SQLiteDatabase getReadableDatabase() { return db; }
            };
            helper.onUpgrade(db, 2, 3);
            HealthDao dao = new HealthDao(helper);
            assertNull(dao.getById(1).getMeasuredTime());
            assertEquals(HealthRecord.MealContext.UNSPECIFIED, dao.getById(1).getMealContext());
            HealthRecord evening = new HealthRecord(0,7,"2026-10-16",null,null,null,null,140f,null);
            evening.setMeasuredTime("19:00");
            evening.setMealContext("AFTER_MEAL");
            assertTrue(dao.insert(evening) > 0);
            HealthRecord morning = new HealthRecord(0,7,"2026-10-16",null,null,null,null,95f,null);
            morning.setMeasuredTime("07:00");
            morning.setMealContext("FASTING");
            assertTrue(dao.insert(morning) > 0);
            java.util.List<HealthRecord> records = dao.getRecordsRange(7,"2026-10-16","2026-10-16");
            assertEquals("07:00", records.get(1).getMeasuredTime());
            assertEquals("19:00", records.get(2).getMeasuredTime());
            assertEquals(HealthRecord.MealContext.AFTER_MEAL, records.get(2).getMealContext());
        }
    }
}

package vn.edu.tdmu.vita;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.database.DatabaseHelper;
import vn.edu.tdmu.vita.database.MedicineDao;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MedicineDoseDeviceTest {
    @Test public void upgrade_preservesOldDoseAndSeparatesNewTimes() {
        Context context = ApplicationProvider.getApplicationContext();
        try (SQLiteDatabase db = SQLiteDatabase.create(null)) {
            db.execSQL("CREATE TABLE medicines(id INTEGER PRIMARY KEY, time TEXT, user_id INTEGER, name TEXT, dosage TEXT, days_mask INTEGER, is_active INTEGER)");
            db.execSQL("CREATE TABLE medicine_logs(id INTEGER PRIMARY KEY, medicine_id INTEGER, date TEXT, status TEXT, logged_at TEXT)");
            db.execSQL("INSERT INTO medicines VALUES(9, '07:00',7,'Test','1',127,1)");
            db.execSQL("INSERT INTO medicine_logs VALUES(1,9,'2026-10-16','TAKEN','2026-10-16 07:03:00')");
            DatabaseHelper helper = new DatabaseHelper(context) {
                @Override public SQLiteDatabase getWritableDatabase() { return db; }
                @Override public SQLiteDatabase getReadableDatabase() { return db; }
            };
            helper.onUpgrade(db, 1, 2);
            helper.onUpgrade(db, 5, 6);
            MedicineDao dao = new MedicineDao(helper);
            assertEquals("TAKEN", dao.getLogForDose(9,7, "2026-10-16", "07:00").getStatus());
            dao.logMedicineStatus(9,7, "2026-10-16", "12:00", "SKIPPED", "2026-10-16 12:03:00");
            dao.logMedicineStatus(9,7, "2026-10-16", "19:00", "TAKEN", "2026-10-16 19:03:00");
            assertEquals("TAKEN", dao.getLogForDose(9,7, "2026-10-16", "07:00").getStatus());
            assertEquals("SKIPPED", dao.getLogForDose(9,7, "2026-10-16", "12:00").getStatus());
            dao.logMedicineStatus(9,7, "2026-10-16", "12:00", "TAKEN", "2026-10-16 12:05:00");
            try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM medicine_logs", null)) {
                assertTrue(cursor.moveToFirst());
                assertEquals(3, cursor.getInt(0));
            }
        }
    }
}

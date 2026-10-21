package vn.edu.tdmu.vita;

import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MedicineHistoryMigrationDeviceTest {
    @Test public void nextDoseSkipsHandledDoseAndForeignMedicine() throws Exception {
        try (SQLiteDatabase db = SQLiteDatabase.create(null)) {
            DatabaseHelper helper = new DatabaseHelper(ApplicationProvider.getApplicationContext()) {
                @Override public SQLiteDatabase getWritableDatabase() { return db; }
                @Override public SQLiteDatabase getReadableDatabase() { return db; }
            };
            helper.onCreate(db);
            MedicineDao dao = new MedicineDao(helper);
            long id = dao.insert(new Medicine(0, 7, "Owned", "1", "08:00,19:00", 127, true));
            dao.insert(new Medicine(0, 8, "Foreign", "1", "07:00", 127, true));
            dao.logMedicineStatus(id, 7, "2026-10-20", "08:00", "TAKEN", "2026-10-20 06:59:00");
            long now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).parse("2026-10-20 06:00").getTime();
            MedicineDao.NextDose next = dao.getNextUnloggedDose(7, now);
            assertNotNull(next); assertEquals(id, next.medicine.getId()); assertEquals("19:00", next.time);
            assertEquals("2026-10-20", next.date);
            assertNull(dao.getNextUnloggedDose(99, now));
        }
    }
    @Test public void migrationPreservesLegacyTimesAndMarksInheritedNames() {
        try (SQLiteDatabase db = SQLiteDatabase.create(null)) {
            db.execSQL("CREATE TABLE medicines(id INTEGER PRIMARY KEY,name TEXT,dosage TEXT)");
            db.execSQL("CREATE TABLE medicine_logs(id INTEGER PRIMARY KEY,medicine_id INTEGER,date TEXT,scheduled_time TEXT,status TEXT,logged_at TEXT)");
            db.execSQL("INSERT INTO medicines VALUES(9,'Legacy','1 tablet')");
            db.execSQL("INSERT INTO medicine_logs VALUES(1,9,'2026-10-17','07:00','TAKEN','old')");
            db.execSQL("INSERT INTO medicine_logs VALUES(2,9,'2026-10-17','19:00','SKIPPED','old')");
            DatabaseHelper helper = new DatabaseHelper(ApplicationProvider.getApplicationContext());
            helper.onUpgrade(db, 5, 6);
            try (android.database.Cursor cursor = db.rawQuery("SELECT scheduled_time,medicine_name_snapshot,snapshot_inherited FROM medicine_logs ORDER BY id", null)) {
                assertEquals(2,cursor.getCount()); assertTrue(cursor.moveToFirst());
                assertEquals("07:00",cursor.getString(0)); assertEquals("Legacy",cursor.getString(1)); assertEquals(1,cursor.getInt(2));
                assertTrue(cursor.moveToNext()); assertEquals("19:00",cursor.getString(0));
            }
        }
    }

    @Test public void editingMedicineOrStatusKeepsOriginalDoseMeaning() {
        try (SQLiteDatabase db = SQLiteDatabase.create(null)) {
            DatabaseHelper helper = new DatabaseHelper(ApplicationProvider.getApplicationContext()) {
                @Override public SQLiteDatabase getWritableDatabase() { return db; }
                @Override public SQLiteDatabase getReadableDatabase() { return db; }
            };
            helper.onCreate(db);
            MedicineDao medicines = new MedicineDao(helper);
            Medicine med = new Medicine(0, 7, "Original", "1 tablet", "08:00", 127, true);
            med.setId(medicines.insert(med));
            assertTrue(medicines.logMedicineStatus(med.getId(), 7, "2026-10-19", "08:00", "TAKEN", "2026-10-19 08:01:00") > 0);
            med.setName("Renamed"); med.setDosage("2 tablets"); med.setTime("09:00");
            assertTrue(medicines.update(med, 7));
            medicines.logMedicineStatus(med.getId(), 7, "2026-10-19", "08:00", "SKIPPED", "2026-10-19 09:02:00");
            MedicineLog log = medicines.getHistory(med.getId(), 7).get(0);
            assertEquals("Original", log.getMedicineNameSnapshot());
            assertEquals("1 tablet", log.getDosageSnapshot());
            assertEquals("08:00", log.getScheduledTime());
            assertEquals("SKIPPED", log.getStatus());
            assertFalse(log.isSnapshotInherited());
            assertFalse(medicines.clearDoseStatus(med.getId(), 8, "2026-10-19", "08:00"));
            assertTrue(medicines.clearDoseStatus(med.getId(), 7, "2026-10-19", "08:00"));
            assertTrue(medicines.getHistory(med.getId(), 7).isEmpty());
        }
    }
}

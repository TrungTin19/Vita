package vn.edu.tdmu.vita.database;

import org.junit.Test;
import static org.junit.Assert.*;

import vn.edu.tdmu.vita.models.HealthRecord;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.models.MedicineLog;
import vn.edu.tdmu.vita.models.SleepLog;
import vn.edu.tdmu.vita.models.WaterLog;

public class DaosTest {

    @Test
    public void testModelInstantiationsForDaos() {
        HealthRecord record = new HealthRecord(1L, 10L, "2026-10-09", 65.5f, 120, 80, 75, 5.5f, "Normal checkup");
        assertEquals(1L, record.getId());
        assertEquals(10L, record.getUserId());
        assertEquals("2026-10-09", record.getDate());
        assertEquals(Float.valueOf(65.5f), record.getWeightKg());

        Medicine med = new Medicine(2L, 10L, "Panadol", "500mg", "08:00", 127, true);
        assertEquals(2L, med.getId());
        assertEquals("Panadol", med.getName());
        assertTrue(med.isActive());

        MedicineLog medLog = new MedicineLog(3L, 2L, "2026-10-09", "TAKEN", "2026-10-09 08:05:00");
        assertEquals("TAKEN", medLog.getStatus());

        WaterLog waterLog = new WaterLog(4L, 10L, "2026-10-09", 250, "2026-10-09 09:00:00");
        assertEquals(250, waterLog.getAmountMl());

        SleepLog sleepLog = new SleepLog(5L, 10L, "2026-10-09", "23:00", "07:00", 8.0f);
        assertEquals(8.0f, sleepLog.getDurationH(), 0.01f);
    }
}

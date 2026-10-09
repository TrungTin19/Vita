package vn.edu.tdmu.vita.database;

import org.junit.Test;
import static org.junit.Assert.*;

public class DatabaseHelperTest {

    @Test
    public void testDatabaseConstants() {
        assertEquals("vita_health.db", DatabaseHelper.DATABASE_NAME);
        assertEquals(1, DatabaseHelper.DATABASE_VERSION);
    }

    @Test
    public void testTableCreationQueries() {
        assertTrue(DatabaseHelper.CREATE_TABLE_USERS.contains("CREATE TABLE users"));
        assertTrue(DatabaseHelper.CREATE_TABLE_USERS.contains("FOREIGN KEY") == false);
        assertTrue(DatabaseHelper.CREATE_TABLE_HEALTH_RECORDS.contains("ON DELETE CASCADE"));
        assertTrue(DatabaseHelper.CREATE_TABLE_MEDICINES.contains("days_mask INTEGER DEFAULT 127"));
        assertTrue(DatabaseHelper.CREATE_TABLE_MEDICINE_LOGS.contains("REFERENCES medicines(id)"));
        assertTrue(DatabaseHelper.CREATE_TABLE_WATER_LOGS.contains("amount_ml INTEGER NOT NULL"));
        assertTrue(DatabaseHelper.CREATE_TABLE_SLEEP_LOGS.contains("duration_h REAL NOT NULL"));
    }

    @Test
    public void testIndexCreationQueries() {
        assertTrue(DatabaseHelper.CREATE_INDEX_HEALTH.contains("idx_health_user_date"));
        assertTrue(DatabaseHelper.CREATE_INDEX_WATER.contains("idx_water_user_date"));
        assertTrue(DatabaseHelper.CREATE_INDEX_SLEEP.contains("idx_sleep_user_date"));
    }
}

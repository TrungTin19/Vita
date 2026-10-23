package vn.edu.tdmu.vita;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.User;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ProfileDeletionDaoDeviceTest {
    @Test public void legacyLoginUpgradesOnlyAfterCorrectPasswordAndKeepsProfile() throws Exception {
        try (SQLiteDatabase db = fixture()) {
            UserDao users = new UserDao(helper(db));
            User original = users.getUserById(1);
            byte[] bytes = java.security.MessageDigest.getInstance("SHA-256").digest(
                    (original.getSalt() + "password1").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder legacy = new StringBuilder();
            for (byte value : bytes) legacy.append(String.format("%02x", value));
            db.execSQL("UPDATE users SET password_hash=? WHERE id=1", new Object[]{legacy.toString()});
            assertNull(users.login("user1", "wrong"));
            assertEquals(legacy.toString(), users.getUserById(1).getPasswordHash());
            User loggedIn = users.login("user1", "password1");
            assertNotNull(loggedIn);
            assertFalse(vn.edu.tdmu.vita.utils.SecurityUtils.isLegacyHash(loggedIn.getPasswordHash()));
            assertEquals(loggedIn.getPasswordHash(), users.getUserById(1).getPasswordHash());
            assertEquals(original.getHeightCm(), loggedIn.getHeightCm(), 0.0f);
            assertNotNull(users.login("user1", "password1"));
            assertTrue(users.deleteProfileWithPassword(1, "password1"));
            assertNotNull(users.getUserById(2));
        }
    }
    private DatabaseHelper helper(SQLiteDatabase db) {
        return new DatabaseHelper(ApplicationProvider.getApplicationContext()) {
            @Override public SQLiteDatabase getWritableDatabase() { return db; }
            @Override public SQLiteDatabase getReadableDatabase() { return db; }
        };
    }
    private SQLiteDatabase fixture() {
        SQLiteDatabase db=SQLiteDatabase.create(null); db.setForeignKeyConstraintsEnabled(true);
        DatabaseHelper helper=helper(db); helper.onCreate(db); UserDao users=new UserDao(helper);
        for(int i=1;i<=2;i++) {
            User user=new User(); user.setUsername("user"+i); user.setHeightCm(170); user.setBaseWeightKg(60);
            assertEquals(i,users.register(user,"password"+i));
            db.execSQL("INSERT INTO health_records(user_id,date,weight_kg) VALUES(?,'2026-10-17',60)",new Object[]{i});
            db.execSQL("INSERT INTO medicines(id,user_id,name,time) VALUES(?,?,'Test','07:00,19:00')",new Object[]{i,i});
            db.execSQL("INSERT INTO medicine_logs(medicine_id,date,scheduled_time,status,logged_at) VALUES(?,'2026-10-17','07:00','TAKEN','2026-10-17 07:01:00')",new Object[]{i});
            db.execSQL("INSERT INTO water_logs(user_id,date,amount_ml,logged_at) VALUES(?,'2026-10-17',250,'2026-10-17 07:00:00')",new Object[]{i});
        }
        return db;
    }
    private long count(SQLiteDatabase db,String table) {
        try(Cursor cursor=db.rawQuery("SELECT COUNT(*) FROM "+table,null)) { cursor.moveToFirst(); return cursor.getLong(0); }
    }
    @Test public void wrongPassword_doesNotDeleteEitherProfile() {
        try(SQLiteDatabase db=fixture()) {
            UserDao users=new UserDao(helper(db));
            assertFalse(users.deleteProfileWithPassword(1,"password2"));
            assertFalse(users.deleteProfileWithPassword(1,""));
            assertNotNull(users.login("user1","password1")); assertNotNull(users.login("user2","password2"));
            for(String table:new String[]{"users","health_records","medicines","medicine_logs","water_logs"}) assertEquals(2,count(db,table));
        }
    }
    @Test public void authenticatedDeletion_cascadesOnlyOwnedRows() {
        try(SQLiteDatabase db=fixture()) {
            UserDao users=new UserDao(helper(db));
            assertTrue(users.deleteProfileWithPassword(1,"password1"));
            assertNull(users.getUserById(1)); assertNotNull(users.login("user2","password2"));
            for(String table:new String[]{"users","health_records","medicines","medicine_logs","water_logs"}) assertEquals(1,count(db,table));
            assertFalse(users.deleteProfileWithPassword(1,"password1"));
        }
    }
    @Test public void failedCascade_keepsProfileAndAllOwnedRows() {
        try(SQLiteDatabase db=fixture()) {
            db.execSQL("CREATE TRIGGER block_delete BEFORE DELETE ON health_records WHEN OLD.user_id=1 BEGIN SELECT RAISE(ABORT,'test'); END");
            UserDao users=new UserDao(helper(db));
            assertThrows(android.database.sqlite.SQLiteException.class,()->users.deleteProfileWithPassword(1,"password1"));
            assertNotNull(users.getUserById(1));
            for(String table:new String[]{"users","health_records","medicines","medicine_logs","water_logs"}) assertEquals(2,count(db,table));
        }
    }
}

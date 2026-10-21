package vn.edu.tdmu.vita;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.database.*;
import vn.edu.tdmu.vita.models.User;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class WaterSettingsDeviceTest {
    @Test public void upgradeAndSave_keepsPersonalGoalAndWaterSchedule() {
        try (SQLiteDatabase db = SQLiteDatabase.create(null)) {
            db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY, username TEXT, password_hash TEXT, salt TEXT, fullname TEXT, birth_year INTEGER, gender TEXT, height_cm REAL, base_weight_kg REAL, water_goal_ml INTEGER, bmi_standard TEXT)");
            db.execSQL("INSERT INTO users VALUES(7,'local','hash','salt','Name',1990,'Nam',170,60,1500,'ASIAN')");
            DatabaseHelper helper = new DatabaseHelper(ApplicationProvider.getApplicationContext()) {
                @Override public SQLiteDatabase getWritableDatabase() { return db; }
                @Override public SQLiteDatabase getReadableDatabase() { return db; }
            };
            helper.onUpgrade(db,3,5);
            UserDao dao = new UserDao(helper);
            User user = dao.getUserById(7);
            assertEquals(1500,user.getWaterGoalMl());
            assertEquals("07:00",user.getWaterStart());
            user.setWaterStart("20:00"); user.setWaterEnd("06:00");
            user.setWaterAmount(350);
            assertTrue(dao.updateWaterSettings(7, user.getWaterGoalMl(), user.getWaterStart(),
                    user.getWaterEnd(), user.getWaterAmount(), ""));
            user=dao.getUserById(7);
            assertEquals(350,user.getWaterAmount());
            assertEquals("20:00",user.getWaterStart());
            assertEquals(1500,user.getWaterGoalMl());
        }
    }
}

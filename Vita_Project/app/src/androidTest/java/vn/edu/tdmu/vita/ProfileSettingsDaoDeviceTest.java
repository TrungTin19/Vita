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
public class ProfileSettingsDaoDeviceTest {
    private DatabaseHelper helper(SQLiteDatabase db) {
        return new DatabaseHelper(ApplicationProvider.getApplicationContext()) {
            @Override public SQLiteDatabase getWritableDatabase() { return db; }
            @Override public SQLiteDatabase getReadableDatabase() { return db; }
        };
    }
    private User newUser(UserDao users,String name) {
        User user=new User(); user.setUsername(name); user.setBirthYear(2000);
        user.setHeightCm(170); user.setBaseWeightKg(60); user.setFullname(name);
        user.setId(users.register(user,"test-password")); assertTrue(user.getId()>0); return user;
    }
    @Test public void savingWaterSettings_changesOnlyThatProfilesWaterColumns() {
        try(SQLiteDatabase db=SQLiteDatabase.create(null)) {
            DatabaseHelper helper=helper(db); helper.onCreate(db);
            UserDao users=new UserDao(helper); User owner=newUser(users,"owner"), other=newUser(users,"other");
            assertTrue(users.updateWaterSettings(owner.getId(),1500,"20:00","06:00",350,""));
            User saved=users.getUserById(owner.getId());
            assertEquals(1500,saved.getWaterGoalMl()); assertEquals("20:00",saved.getWaterStart());
            assertEquals("owner",saved.getFullname()); assertEquals(2000,saved.getBirthYear()); assertEquals(170,saved.getHeightCm(),0);
            assertNotNull(users.login("owner","test-password"));
            assertEquals(2000,users.getUserById(other.getId()).getWaterGoalMl());
            assertEquals(250,users.getUserById(other.getId()).getWaterAmount());
            assertFalse(users.updateWaterSettings(owner.getId(),1500,"07:00","07:00",350,""));
            assertFalse(users.updateWaterSettings(owner.getId(),0,"07:00","22:00",350,""));
            assertFalse(users.updateWaterSettings(owner.getId(),1500,"07:00","22:00",0,""));
            assertEquals("20:00",users.getUserById(owner.getId()).getWaterStart());
        }
    }
    @Test public void savingPersonalFields_doesNotOverwriteNewerWaterPreferences() {
        try(SQLiteDatabase db=SQLiteDatabase.create(null)) {
            DatabaseHelper helper=helper(db); helper.onCreate(db);
            UserDao users=new UserDao(helper); User stale=newUser(users,"owner");
            assertTrue(users.updateWaterSettings(stale.getId(),1500,"20:00","06:00",350,""));
            stale.setFullname("Updated"); stale.setBirthYear(0);
            assertTrue(users.updateProfile(stale));
            User saved=users.getUserById(stale.getId());
            assertEquals("Updated",saved.getFullname()); assertEquals(0,saved.getBirthYear());
            assertEquals(1500,saved.getWaterGoalMl()); assertEquals(350,saved.getWaterAmount());
        }
    }
}

package vn.edu.tdmu.vita;

import android.content.Context;
import android.content.Intent;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import vn.edu.tdmu.vita.activities.DetailHealthActivity;
import vn.edu.tdmu.vita.database.HealthDao;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.models.HealthRecord;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.SessionManager;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AccountIsolationDeviceTest {
    @Test public void profileDraftCannotMoveToAnotherAccountAfterRecreation() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        UserDao users = new UserDao(context);
        User owner = new User(); owner.setUsername("draft_owner_" + System.nanoTime());
        User actor = new User(); actor.setUsername("draft_actor_" + System.nanoTime());
        long ownerId = users.register(owner, "test-password"), actorId = users.register(actor, "test-password");
        SessionManager session = new SessionManager(context); session.saveSession(ownerId, owner.getUsername(), "");
        try (ActivityScenario<vn.edu.tdmu.vita.activities.EditProfileActivity> screen = ActivityScenario.launch(vn.edu.tdmu.vita.activities.EditProfileActivity.class)) {
            screen.onActivity(a -> ((android.widget.EditText) a.findViewById(R.id.et_edit_fullname)).setText("Owner private draft"));
            session.saveSession(actorId, actor.getUsername(), "");
            try { screen.recreate(); }
            catch (AssertionError closedDuringRecreation) {
                // ActivityScenario expects RESUMED, but rejecting the changed owner must finish.
                if (screen.getState() != Lifecycle.State.DESTROYED) throw closedDuringRecreation;
            }
            assertEquals(Lifecycle.State.DESTROYED, screen.getState());
            assertNotEquals("Owner private draft", users.getUserById(actorId).getFullname());
        } finally {
            users.deleteProfileWithPassword(ownerId, "test-password"); users.deleteProfileWithPassword(actorId, "test-password"); session.clearSession();
        }
    }
    @Test public void daoRejectsForeignMutationWithoutChangingOwnerData() {
        Context context = ApplicationProvider.getApplicationContext();
        try (android.database.sqlite.SQLiteDatabase db = android.database.sqlite.SQLiteDatabase.create(null)) {
            vn.edu.tdmu.vita.database.DatabaseHelper helper = new vn.edu.tdmu.vita.database.DatabaseHelper(context) {
                @Override public android.database.sqlite.SQLiteDatabase getWritableDatabase() { return db; }
                @Override public android.database.sqlite.SQLiteDatabase getReadableDatabase() { return db; }
            };
            helper.onCreate(db);
            HealthDao health = new HealthDao(helper);
            HealthRecord record = new HealthRecord(0, 1, "2026-10-19", 65f, null, null, null, null, null);
            long id = health.insert(record); record.setId(id); record.setWeightKg(99f);
            assertNull(health.getById(id, 2));
            assertFalse(health.update(record, 2));
            assertFalse(health.delete(id, 2));
            assertEquals(65f, health.getById(id, 1).getWeightKg(), 0f);
            vn.edu.tdmu.vita.database.MedicineDao medicines = new vn.edu.tdmu.vita.database.MedicineDao(helper);
            long med = medicines.insert(new vn.edu.tdmu.vita.models.Medicine(0, 1, "Owner", "1", "08:00", 127, true));
            assertNull(medicines.getById(med, 2));
            assertFalse(medicines.toggleActive(med, 2, false));
            assertFalse(medicines.delete(med, 2));
            assertEquals(-1, medicines.logMedicineStatus(med, 2, "2026-10-19", "08:00", "TAKEN", "2026-10-19 08:01:00"));
            assertTrue(medicines.getById(med, 1).isActive());
            assertTrue(medicines.getHistory(med, 1).isEmpty());
        }
    }

    @Test public void detailRejectsAnotherProfilesRecord() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue(context.getPackageName().endsWith(".verification"));
        UserDao users = new UserDao(context);
        User owner = new User(); owner.setUsername("owner_" + System.nanoTime());
        User actor = new User(); actor.setUsername("actor_" + System.nanoTime());
        long ownerId = users.register(owner, "test-password");
        long actorId = users.register(actor, "test-password");
        long recordId = new HealthDao(context).insert(new HealthRecord(0, ownerId,
                "2026-10-19", 65f, null, null, null, null, null));
        SessionManager session = new SessionManager(context);
        session.saveSession(actorId, actor.getUsername(), "");
        try (ActivityScenario<DetailHealthActivity> screen = ActivityScenario.launch(
                new Intent(context, DetailHealthActivity.class)
                        .putExtra(DetailHealthActivity.EXTRA_RECORD_ID, recordId))) {
            assertEquals(Lifecycle.State.DESTROYED, screen.getState());
        } finally {
            users.deleteProfileWithPassword(ownerId, "test-password");
            users.deleteProfileWithPassword(actorId, "test-password");
            session.clearSession();
        }
    }
}

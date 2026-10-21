package vn.edu.tdmu.vita;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import vn.edu.tdmu.vita.database.DatabaseHelper;
import vn.edu.tdmu.vita.database.HealthDao;
import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.database.WaterDao;
import vn.edu.tdmu.vita.models.HealthRecord;
import vn.edu.tdmu.vita.models.Medicine;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.DateTimeUtils;

/**
 * Chạy trên thiết bị/máy ảo: Đăng ký -> Đăng nhập -> Thêm chỉ số ->
 * Đặt lịch thuốc -> Nạp nước.
 */
@RunWith(AndroidJUnit4.class)
public class VitaSystemIntegrationTest {

    private Context context;
    private UserDao userDao;
    private HealthDao healthDao;
    private MedicineDao medicineDao;
    private WaterDao waterDao;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(DatabaseHelper.DATABASE_NAME);
        userDao = new UserDao(context);
        healthDao = new HealthDao(context);
        medicineDao = new MedicineDao(context);
        waterDao = new WaterDao(context);
    }

    @Test
    public void fullUserJourney_endToEnd() {
        // 1. Đăng ký
        User user = new User();
        user.setUsername("vita_test");
        user.setFullname("Người Kiểm Thử");
        user.setHeightCm(170f);
        user.setBaseWeightKg(68f);
        long userId = userDao.register(user, "matkhau123");
        assertTrue(userId > 0);

        // 2. Đăng nhập
        User loggedIn = userDao.login("vita_test", "matkhau123");
        assertNotNull(loggedIn);
        assertEquals(userId, loggedIn.getId());

        String today = DateTimeUtils.getTodayDate();

        // 3. Thêm chỉ số sức khỏe (bỏ trống đường huyết -> NULL)
        HealthRecord record = new HealthRecord(0, userId, today, 68f, 120, 80, 72, null, null);
        assertTrue(healthDao.insert(record) > 0);
        List<HealthRecord> records = healthDao.getRecordsRange(userId, today, today);
        assertEquals(1, records.size());

        // 4. Đặt lịch thuốc
        Medicine medicine = new Medicine(0, userId, "Panadol", "1 viên sau ăn",
                "08:00", 127, true);
        long medId = medicineDao.insert(medicine);
        assertTrue(medId > 0);
        assertEquals(1, medicineDao.getAllByUserId(userId).size());
        long logId = medicineDao.logMedicineStatus(medId,userId, today, "08:00", "TAKEN",
                DateTimeUtils.getCurrentTimestamp());
        assertTrue(logId > 0);

        // 5. Nạp nước 2 lần 250ml
        assertTrue(waterDao.addWater(userId, today, 250, DateTimeUtils.getCurrentTimestamp()) > 0);
        assertTrue(waterDao.addWater(userId, today, 250, DateTimeUtils.getCurrentTimestamp()) > 0);
        assertEquals(500, waterDao.getDailyTotal(userId, today));

    }
}

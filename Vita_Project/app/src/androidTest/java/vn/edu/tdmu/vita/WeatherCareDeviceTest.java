package vn.edu.tdmu.vita;

import android.content.Context;
import android.content.Intent;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import vn.edu.tdmu.vita.activities.MainActivity;
import vn.edu.tdmu.vita.database.DatabaseHelper;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.fragments.HomeFragment;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.models.WeatherData;
import vn.edu.tdmu.vita.network.WeatherClient;
import vn.edu.tdmu.vita.receivers.WaterReminderReceiver;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.HydrationGoal;
import vn.edu.tdmu.vita.utils.SessionManager;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class WeatherCareDeviceTest {
    private Context context;
    private long owner;
    private int previousCity;

    @Before public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        assertTrue("Use isolated verification app", context.getPackageName().endsWith(".verification"));
        context.getSharedPreferences("vita_weather_cache", 0).edit().clear().commit();
        previousCity = context.getSharedPreferences("vita_home", 0).getInt("city_position", 0);
        context.getSharedPreferences("vita_home", 0).edit().putInt("city_position", 0).commit();
        User user = new User();
        user.setUsername("weather_" + System.nanoTime());
        user.setHeightCm(170); user.setBaseWeightKg(60); user.setWaterGoalMl(1500);
        owner = new UserDao(context).register(user, "test-password");
        assertTrue(owner > 0);
        new SessionManager(context).saveSession(owner, user.getUsername(), "Weather test");
    }

    @After public void cleanup() {
        if (owner <= 0) return;
        try (DatabaseHelper helper = new DatabaseHelper(context)) {
            helper.getWritableDatabase().delete("users", "id=?", new String[]{String.valueOf(owner)});
        }
        new SessionManager(context).clearSession();
        WaterReminderReceiver.cancel(context);
        context.getSharedPreferences("vita_home", 0).edit().putInt("city_position", previousCity).commit();
    }

    @Test public void hotOptOut_keepsGoalAndStillDescribesHeat() {
        try (ActivityScenario<MainActivity> activity = launch()) {
            activity.onActivity(screen -> {
                WeatherData hot = hot(50);
                render(screen, hot);
                String advice = advice(screen);
                assertTrue(advice, advice.contains("33"));
                assertFalse(advice, advice.contains("dễ chịu"));
                assertFalse(advice, advice.contains("300"));
                assertEquals(1500, HydrationGoal.current(context, new UserDao(context).getUserById(owner)));
                assertEquals(1500, new UserDao(context).getUserById(owner).getWaterGoalMl());
            });
        }
    }

    @Test public void heatAndPoorAir_areBothShown() {
        User user = new UserDao(context).getUserById(owner);
        assertTrue(new UserDao(context).updateWaterSettings(owner, 1500,
                user.getWaterStart(), user.getWaterEnd(), user.getWaterAmount(), true, ""));
        try (ActivityScenario<MainActivity> activity = launch()) {
            activity.onActivity(screen -> {
                render(screen, hot(120));
                String advice = advice(screen);
                assertTrue(advice, advice.contains("33"));
                assertTrue(advice, advice.contains("AQI 120"));
                assertTrue(advice, advice.contains("800"));
                assertFalse(advice, advice.contains("thanh lọc"));
            });
        }
    }

    @Test public void citySwitch_clearsPreviousAdviceWhileLoading() {
        try (ActivityScenario<MainActivity> activity = launch()) {
            activity.onActivity(screen -> {
                render(screen, hot(120));
                ((TextView) screen.findViewById(R.id.tv_home_advice_text)).setText("OLD_CITY_ADVICE");
                ((Spinner) screen.findViewById(R.id.sp_home_city)).setSelection(3);
                try {
                    HomeFragment home = (HomeFragment) screen.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    Method fetch = HomeFragment.class.getDeclaredMethod("fetchWeather", int.class);
                    fetch.setAccessible(true);
                    fetch.invoke(home, 3);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
                assertFalse(advice(screen), advice(screen).contains("OLD_CITY_ADVICE"));
                assertFalse(advice(screen), advice(screen).contains("AQI 120"));
            });
        }
    }

    // Run with network disabled so this covers WeatherClient's actual failure callback.
    @Test public void forecastFailureWithoutCache_showsUnknownWeather() throws Exception {
        try (ActivityScenario<MainActivity> activity = launch()) {
            AtomicReference<String> text = new AtomicReference<>("");
            long deadline = android.os.SystemClock.elapsedRealtime() + 15000;
            do {
                activity.onActivity(screen -> text.set(advice(screen)));
                if (text.get().contains("Chưa có dữ liệu thời tiết")) break;
                Thread.sleep(100);
            } while (android.os.SystemClock.elapsedRealtime() < deadline);
            assertTrue(text.get(), text.get().contains("Chưa có dữ liệu thời tiết"));
            assertFalse(text.get(), text.get().contains("dễ chịu"));
        }
    }

    private ActivityScenario<MainActivity> launch() {
        ActivityScenario<MainActivity> activity = ActivityScenario.launch(new Intent(context, MainActivity.class));
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        return activity;
    }

    private WeatherData hot(int aqi) {
        return new WeatherData(33f, 70, aqi, WeatherClient.CITY_NAMES[0], false,
                DateTimeUtils.getTodayDate() + " 10:00:00");
    }

    private void render(MainActivity screen, WeatherData data) {
        try {
            HomeFragment home = (HomeFragment) screen.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            Field request = HomeFragment.class.getDeclaredField("weatherRequest");
            request.setAccessible(true);
            request.setInt(home, request.getInt(home) + 1);
            Method render = HomeFragment.class.getDeclaredMethod("renderWeather", WeatherData.class);
            render.setAccessible(true);
            render.invoke(home, data);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private String advice(MainActivity screen) {
        return ((TextView) screen.findViewById(R.id.tv_home_advice_text)).getText().toString();
    }
}

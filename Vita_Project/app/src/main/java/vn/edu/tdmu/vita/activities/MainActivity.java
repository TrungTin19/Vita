package vn.edu.tdmu.vita.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.fragments.HealthFragment;
import vn.edu.tdmu.vita.fragments.HomeFragment;
import vn.edu.tdmu.vita.fragments.ReminderFragment;
import vn.edu.tdmu.vita.fragments.StatsFragment;

public class MainActivity extends AppCompatActivity implements vn.edu.tdmu.vita.utils.WaterJournalDialog.Host {

    public static final String EXTRA_TAB = "extra_tab";
    public static final String TAB_REMINDER = "reminder";

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private int activeTab = R.id.nav_home;
    private final android.content.BroadcastReceiver clockReceiver = new android.content.BroadcastReceiver() {
        @Override public void onReceive(android.content.Context context, Intent intent) {
            refreshVisible();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar_main);
        bottomNav = findViewById(R.id.bottom_nav_main);
        bottomNav.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(this, R.color.vita_surface)));
        bottomNav.setElevation(0);

        findViewById(R.id.btn_main_profile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        bottomNav.setOnItemSelectedListener(this::onTabSelected);
        bottomNav.setOnItemReselectedListener(item -> { /* đứng yên tab hiện tại */ });

        openTab(savedInstanceState == null ? getIntentTab() : savedInstanceState.getInt("active_tab", R.id.nav_home));
    }

    @Override protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putInt("active_tab", activeTab);
        super.onSaveInstanceState(state);
    }

    @Override protected void onStart() {
        super.onStart();
        android.content.IntentFilter filter = new android.content.IntentFilter(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        androidx.core.content.ContextCompat.registerReceiver(this, clockReceiver, filter,
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override protected void onStop() {
        unregisterReceiver(clockReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vn.edu.tdmu.vita.utils.SessionManager session = new vn.edu.tdmu.vita.utils.SessionManager(this);
        if (!session.isLoggedIn()
                || new vn.edu.tdmu.vita.database.UserDao(this).getUserById(session.getUserId()) == null) {
            session.clearSession();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        for (vn.edu.tdmu.vita.models.Medicine medicine :
                new vn.edu.tdmu.vita.database.MedicineDao(this).getActiveMedicines(session.getUserId())) {
            vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver.schedule(this, medicine);
        }
        vn.edu.tdmu.vita.receivers.WaterReminderReceiver.schedule(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        openTab(getIntentTab());
    }

    private int getIntentTab() {
        if (getIntent() != null && TAB_REMINDER.equals(getIntent().getStringExtra(EXTRA_TAB))) {
            return R.id.nav_reminder;
        }
        return R.id.nav_home;
    }

    private void openTab(int itemId) {
        if (bottomNav.getSelectedItemId() != itemId) {
            bottomNav.setSelectedItemId(itemId); // nếu đổi tab, hàm lắng nghe sẽ tự chạy
        } else {
            // setSelectedItemId không gọi hàm lắng nghe khi mục đó đã được chọn
            // -> gọi giao dịch hiển thị trực tiếp thay vì chờ hàm phản hồi sự kiện.
            onTabSelected(bottomNav.getMenu().findItem(itemId));
        }
    }

    @Override public void onWaterJournalChanged() {
        refreshVisible();
    }

    private void refreshVisible() {
        ((android.widget.TextView) findViewById(R.id.tv_main_date)).setText(
                new java.text.SimpleDateFormat("EEEE, d 'tháng' M", new java.util.Locale("vi", "VN")).format(new java.util.Date()));
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("tab:" + activeTab);
        if (fragment == null || fragment.getView() == null) return;
        if (fragment instanceof ReminderFragment && fragment.getView() != null)
            ((ReminderFragment) fragment).refreshToday();
        if (fragment instanceof HealthFragment) ((HealthFragment) fragment).loadData();
        if (fragment instanceof HomeFragment) ((HomeFragment) fragment).refreshDashboard();
        if (fragment instanceof StatsFragment) ((StatsFragment) fragment).loadAll();
    }

    private boolean onTabSelected(@NonNull MenuItem item) {
        Fragment fragment;
        int titleRes;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
            titleRes = R.string.nav_home;
        } else if (itemId == R.id.nav_health) {
            fragment = new HealthFragment();
            titleRes = R.string.nav_health;
        } else if (itemId == R.id.nav_reminder) {
            fragment = new ReminderFragment();
            titleRes = R.string.nav_reminder;
        } else if (itemId == R.id.nav_stats) {
            fragment = new StatsFragment();
            titleRes = R.string.nav_stats;
        } else {
            return false;
        }

        ((android.widget.TextView) findViewById(R.id.tv_main_title)).setText(titleRes);
        boolean home = itemId == R.id.nav_home;
        toolbar.setVisibility(home ? android.view.View.VISIBLE : android.view.View.GONE);
        android.widget.TextView date = findViewById(R.id.tv_main_date);
        date.setVisibility(home ? android.view.View.VISIBLE : android.view.View.GONE);
        date.setText(new java.text.SimpleDateFormat("EEEE, d 'tháng' M", new java.util.Locale("vi", "VN")).format(new java.util.Date()));
        activeTab = itemId;
        String tag = "tab:" + itemId;
        Fragment existing = getSupportFragmentManager().findFragmentByTag(tag);
        if (existing != null) fragment = existing;
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (Fragment other : getSupportFragmentManager().getFragments()) {
            if (other.getId() != R.id.fragment_container || other == fragment) continue;
            transaction.hide(other).setMaxLifecycle(other, androidx.lifecycle.Lifecycle.State.STARTED);
        }
        if (existing == null) transaction.add(R.id.fragment_container, fragment, tag);
        transaction.show(fragment).setMaxLifecycle(fragment, androidx.lifecycle.Lifecycle.State.RESUMED).commit();
        return true;
    }
}

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

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar_main);
        bottomNav = findViewById(R.id.bottom_nav_main);

        toolbar.setOnMenuItemClickListener(this::onToolbarItemClicked);
        bottomNav.setOnItemSelectedListener(this::onTabSelected);
        bottomNav.setOnItemReselectedListener(item -> { /* đứng yên tab hiện tại */ });

        if (savedInstanceState == null) {
            // ponytail: setSelectedItemId không bắn listener khi item đã checked sẵn
            // -> gọi transaction trực tiếp thay vì trông chờ callback.
            onTabSelected(bottomNav.getMenu().findItem(R.id.nav_home));
        }
    }

    private boolean onToolbarItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return false;
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

        toolbar.setTitle(titleRes);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        return true;
    }
}

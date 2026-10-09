package vn.edu.tdmu.vita.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.tdmu.vita.MainActivity;
import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Smooth brand fade-in animation
        View brandLayout = findViewById(R.id.ll_splash_brand);
        if (brandLayout != null) {
            brandLayout.setAlpha(0f);
            brandLayout.animate().alpha(1.0f).setDuration(600).start();
        }

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::checkSessionAndNavigate, SPLASH_DELAY_MS);
    }

    private void checkSessionAndNavigate() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}

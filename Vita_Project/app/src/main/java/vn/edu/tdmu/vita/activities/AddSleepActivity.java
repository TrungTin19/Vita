package vn.edu.tdmu.vita.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.SleepDao;
import vn.edu.tdmu.vita.models.SleepLog;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.HealthEvaluator;
import vn.edu.tdmu.vita.utils.SessionManager;
import vn.edu.tdmu.vita.utils.SleepEvaluator;

public class AddSleepActivity extends AppCompatActivity {

    public static final String EXTRA_SLEEP_LOG_ID = "extra_sleep_log_id";

    private MaterialToolbar toolbar;
    private TextInputLayout tilDate;
    private TextInputEditText etDate;
    private TextInputLayout tilSleepTime;
    private TextInputEditText etSleepTime;
    private TextInputLayout tilWakeTime;
    private TextInputEditText etWakeTime;
    private TextView tvDurationVal;
    private TextView tvBadge;
    private TextView tvAdvice;
    private MaterialButton btnSave;

    private SleepDao sleepDao;
    private SessionManager sessionManager;

    private long sleepLogId = -1;
    private String selectedDate;
    private String sleepTime = "23:00";
    private String wakeTime = "06:30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sleep);

        sessionManager = new SessionManager(this);
        if (sessionManager.getUserId() <= 0) {
            finish();
            return;
        }

        sleepDao = new SleepDao(this);

        initViews();
        setupListeners();
        loadIntentData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_add_sleep);
        tilDate = findViewById(R.id.til_sleep_date);
        etDate = findViewById(R.id.et_sleep_date);
        tilSleepTime = findViewById(R.id.til_sleep_time);
        etSleepTime = findViewById(R.id.et_sleep_time);
        tilWakeTime = findViewById(R.id.til_wake_time);
        etWakeTime = findViewById(R.id.et_wake_time);
        tvDurationVal = findViewById(R.id.tv_sleep_duration_val);
        tvBadge = findViewById(R.id.tv_sleep_badge);
        tvAdvice = findViewById(R.id.tv_sleep_advice);
        btnSave = findViewById(R.id.btn_sleep_save);
    }

    private void loadIntentData() {
        sleepLogId = getIntent().getLongExtra(EXTRA_SLEEP_LOG_ID, -1);

        if (sleepLogId > 0) {
            toolbar.setTitle(getString(R.string.add_sleep_title_edit));
            SleepLog log = sleepDao.getById(sleepLogId);
            if (log == null || log.getUserId() != sessionManager.getUserId()) {
                Toast.makeText(this, getString(R.string.add_sleep_error_update), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            selectedDate = log.getDate();
            sleepTime = log.getSleepTime();
            wakeTime = log.getWakeTime();
        } else {
            toolbar.setTitle(getString(R.string.add_sleep_title));
            selectedDate = DateTimeUtils.getTodayDate();
        }
        etDate.setText(DateTimeUtils.formatDisplayDate(selectedDate));
        etSleepTime.setText(sleepTime);
        etWakeTime.setText(wakeTime);
        updateDurationCard();
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        etDate.setOnClickListener(v -> showDatePicker());
        tilDate.setStartIconOnClickListener(v -> showDatePicker());
        tilDate.setOnClickListener(v -> showDatePicker());

        etSleepTime.setOnClickListener(v -> showTimePicker(true));
        tilSleepTime.setOnClickListener(v -> showTimePicker(true));

        etWakeTime.setOnClickListener(v -> showTimePicker(false));
        tilWakeTime.setOnClickListener(v -> showTimePicker(false));

        btnSave.setOnClickListener(v -> handleSave());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        if (selectedDate != null && selectedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = selectedDate.split("-");
            try {
                c.set(Calendar.YEAR, Integer.parseInt(parts[0]));
                c.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
            } catch (NumberFormatException ignored) {}
        }
        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            etDate.setText(DateTimeUtils.formatDisplayDate(selectedDate));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker(boolean isSleepTime) {
        String current = isSleepTime ? sleepTime : wakeTime;
        int hour = 23;
        int minute = 0;
        if (current != null && current.matches("\\d{1,2}:\\d{2}")) {
            String[] parts = current.split(":");
            try {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }
        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, m) -> {
            String value = String.format(Locale.US, "%02d:%02d", h, m);
            if (isSleepTime) {
                sleepTime = value;
                etSleepTime.setText(value);
            } else {
                wakeTime = value;
                etWakeTime.setText(value);
            }
            updateDurationCard();
        }, hour, minute, true);
        dialog.show();
    }

    private void updateDurationCard() {
        float hours = DateTimeUtils.calculateSleepDuration(sleepTime, wakeTime);
        tvDurationVal.setText(String.format(Locale.getDefault(),
                getString(R.string.add_sleep_duration_format), hours));

        SleepEvaluator.SleepResult result = SleepEvaluator.classify(hours);
        tvBadge.setText(result.label);

        int textColor;
        int badgeBg;
        int adviceRes;
        if (result.severity == HealthEvaluator.STATUS_GOOD) {
            textColor = ContextCompat.getColor(this, R.color.vita_status_good);
            badgeBg = R.drawable.bg_badge_good;
            adviceRes = R.string.add_sleep_advice_good;
        } else if (result.severity == SleepEvaluator.STATUS_OVERSLEEP) {
            textColor = ContextCompat.getColor(this, R.color.vita_sleep);
            badgeBg = R.drawable.bg_badge_rounded;
            adviceRes = R.string.add_sleep_advice_oversleep;
        } else if ("Thiếu ngủ".equals(result.label)) {
            textColor = ContextCompat.getColor(this, R.color.vita_status_warning);
            badgeBg = R.drawable.bg_badge_warning;
            adviceRes = R.string.add_sleep_advice_lack;
        } else {
            textColor = ContextCompat.getColor(this, R.color.vita_status_warning);
            badgeBg = R.drawable.bg_badge_warning;
            adviceRes = R.string.add_sleep_advice_fair;
        }
        tvBadge.setTextColor(textColor);
        tvBadge.setBackgroundResource(badgeBg);
        tvAdvice.setText(adviceRes);
    }

    private void handleSave() {
        if (sleepTime.equals(wakeTime)) {
            Toast.makeText(this, getString(R.string.add_sleep_error_same_time), Toast.LENGTH_SHORT).show();
            return;
        }
        float hours = DateTimeUtils.calculateSleepDuration(sleepTime, wakeTime);
        long userId = sessionManager.getUserId();

        if (sleepLogId > 0) {
            SleepLog log = sleepDao.getById(sleepLogId);
            if (log == null) {
                Toast.makeText(this, getString(R.string.add_sleep_error_update), Toast.LENGTH_SHORT).show();
                return;
            }
            log.setDate(selectedDate);
            log.setSleepTime(sleepTime);
            log.setWakeTime(wakeTime);
            log.setDurationH(hours);
            if (sleepDao.update(log)) {
                Toast.makeText(this, getString(R.string.add_sleep_success_edit), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.add_sleep_error_update), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        SleepLog log = new SleepLog(0, userId, selectedDate, sleepTime, wakeTime, hours);
        long id = sleepDao.insert(log);
        if (id > 0) {
            Toast.makeText(this, getString(R.string.add_sleep_success), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.add_sleep_error_save), Toast.LENGTH_SHORT).show();
        }
    }
}

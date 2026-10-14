package vn.edu.tdmu.vita.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.HealthDao;
import vn.edu.tdmu.vita.models.HealthRecord;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.InputValidator;
import vn.edu.tdmu.vita.utils.SessionManager;

public class AddHealthActivity extends AppCompatActivity {

    public static final String EXTRA_RECORD_ID = "extra_record_id";

    private MaterialToolbar toolbar;
    private TextInputLayout tilDate;
    private TextInputEditText etDate;

    private TextInputLayout tilSystolic;
    private TextInputEditText etSystolic;

    private TextInputLayout tilDiastolic;
    private TextInputEditText etDiastolic;

    private TextInputLayout tilHeartRate;
    private TextInputEditText etHeartRate;

    private TextInputLayout tilWeight;
    private TextInputEditText etWeight;

    private TextInputLayout tilSugar;
    private TextInputEditText etSugar;

    private TextInputLayout tilNote;
    private TextInputEditText etNote;

    private MaterialButton btnSave;

    private HealthDao healthDao;
    private SessionManager sessionManager;

    private long recordId = -1;
    private String selectedDate;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_health);

        sessionManager = new SessionManager(this);
        if (sessionManager.getUserId() <= 0) {
            finish();
            return;
        }

        healthDao = new HealthDao(this);
        calendar = Calendar.getInstance();

        initViews();
        setupListeners();
        loadIntentData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_add_health);
        tilDate = findViewById(R.id.til_add_health_date);
        etDate = findViewById(R.id.et_add_health_date);

        tilSystolic = findViewById(R.id.til_add_systolic);
        etSystolic = findViewById(R.id.et_add_systolic);

        tilDiastolic = findViewById(R.id.til_add_diastolic);
        etDiastolic = findViewById(R.id.et_add_diastolic);

        tilHeartRate = findViewById(R.id.til_add_heart_rate);
        etHeartRate = findViewById(R.id.et_add_heart_rate);

        tilWeight = findViewById(R.id.til_add_weight);
        etWeight = findViewById(R.id.et_add_weight);

        tilSugar = findViewById(R.id.til_add_sugar);
        etSugar = findViewById(R.id.et_add_sugar);

        tilNote = findViewById(R.id.til_add_note);
        etNote = findViewById(R.id.et_add_note);

        btnSave = findViewById(R.id.btn_add_health_save);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        etDate.setOnClickListener(v -> showDatePicker());
        tilDate.setStartIconOnClickListener(v -> showDatePicker());
        tilDate.setOnClickListener(v -> showDatePicker());

        etSystolic.addTextChangedListener(new SimpleTextWatcher(() -> tilSystolic.setError(null)));
        etDiastolic.addTextChangedListener(new SimpleTextWatcher(() -> tilDiastolic.setError(null)));
        etHeartRate.addTextChangedListener(new SimpleTextWatcher(() -> tilHeartRate.setError(null)));
        etWeight.addTextChangedListener(new SimpleTextWatcher(() -> tilWeight.setError(null)));
        etSugar.addTextChangedListener(new SimpleTextWatcher(() -> tilSugar.setError(null)));

        btnSave.setOnClickListener(v -> handleSave());
    }

    private void loadIntentData() {
        recordId = getIntent().getLongExtra(EXTRA_RECORD_ID, -1);

        if (recordId > 0) {
            // Edit Mode
            toolbar.setTitle(getString(R.string.add_health_title_edit));
            HealthRecord record = healthDao.getById(recordId);
            if (record != null) {
                selectedDate = record.getDate();
                etDate.setText(DateTimeUtils.formatDisplayDate(selectedDate));

                if (record.getSystolic() != null) {
                    etSystolic.setText(String.valueOf(record.getSystolic()));
                }
                if (record.getDiastolic() != null) {
                    etDiastolic.setText(String.valueOf(record.getDiastolic()));
                }
                if (record.getHeartRate() != null) {
                    etHeartRate.setText(String.valueOf(record.getHeartRate()));
                }
                if (record.getWeightKg() != null) {
                    etWeight.setText(String.valueOf(record.getWeightKg()));
                }
                if (record.getBloodSugar() != null) {
                    etSugar.setText(String.valueOf(record.getBloodSugar()));
                }
                if (record.getNote() != null) {
                    etNote.setText(record.getNote());
                }
            }
        } else {
            // Add Mode
            toolbar.setTitle(getString(R.string.add_health_title_add));
            selectedDate = DateTimeUtils.getTodayDate();
            etDate.setText(DateTimeUtils.formatDisplayDate(selectedDate));
        }
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (selectedDate != null && selectedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = selectedDate.split("-");
            try {
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]) - 1;
                day = Integer.parseInt(parts[2]);
            } catch (NumberFormatException ignored) {}
        }

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            calendar.set(y, m, d);
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            etDate.setText(DateTimeUtils.formatDisplayDate(selectedDate));
        }, year, month, day);

        dialog.show();
    }

    private void handleSave() {
        String systolicStr = getText(etSystolic);
        String diastolicStr = getText(etDiastolic);
        String heartRateStr = getText(etHeartRate);
        String weightStr = getText(etWeight);
        String sugarStr = getText(etSugar);
        String noteStr = getText(etNote);

        // Sanity check: must provide at least one measurement or note
        if (systolicStr.isEmpty() && diastolicStr.isEmpty() && heartRateStr.isEmpty()
                && weightStr.isEmpty() && sugarStr.isEmpty() && noteStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.add_health_error_no_input), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasError = false;

        Integer systolic = null;
        Integer diastolic = null;
        Integer heartRate = null;
        Float weight = null;
        Float bloodSugar = null;

        // Validate Systolic
        if (!systolicStr.isEmpty()) {
            try {
                systolic = Integer.parseInt(systolicStr);
                if (!InputValidator.isValidSystolic(systolic)) {
                    tilSystolic.setError(getString(R.string.add_health_error_systolic_range));
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilSystolic.setError(getString(R.string.add_health_error_invalid_number));
                hasError = true;
            }
        }

        // Validate Diastolic
        if (!diastolicStr.isEmpty()) {
            try {
                diastolic = Integer.parseInt(diastolicStr);
                if (!InputValidator.isValidDiastolic(diastolic)) {
                    tilDiastolic.setError(getString(R.string.add_health_error_diastolic_range));
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilDiastolic.setError(getString(R.string.add_health_error_invalid_number));
                hasError = true;
            }
        }

        // Validate Blood Pressure relation: Systolic must be greater than Diastolic
        if (systolic != null && diastolic != null && !hasError) {
            if (!InputValidator.isValidBloodPressure(systolic, diastolic)) {
                tilSystolic.setError(getString(R.string.add_health_error_bp_relation));
                hasError = true;
            }
        }

        // Validate Heart Rate
        if (!heartRateStr.isEmpty()) {
            try {
                heartRate = Integer.parseInt(heartRateStr);
                if (!InputValidator.isValidHeartRate(heartRate)) {
                    tilHeartRate.setError(getString(R.string.add_health_error_hr_range));
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilHeartRate.setError(getString(R.string.add_health_error_invalid_number));
                hasError = true;
            }
        }

        // Validate Weight
        if (!weightStr.isEmpty()) {
            try {
                weight = Float.parseFloat(weightStr);
                if (!InputValidator.isValidWeight(weight)) {
                    tilWeight.setError(getString(R.string.add_health_error_weight_range));
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilWeight.setError(getString(R.string.add_health_error_invalid_number));
                hasError = true;
            }
        }

        // Validate Blood Sugar
        if (!sugarStr.isEmpty()) {
            try {
                bloodSugar = Float.parseFloat(sugarStr);
                if (!InputValidator.isValidBloodSugar(bloodSugar)) {
                    tilSugar.setError(getString(R.string.add_health_error_sugar_range));
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                tilSugar.setError(getString(R.string.add_health_error_invalid_number));
                hasError = true;
            }
        }

        if (hasError) {
            return;
        }

        long userId = sessionManager.getUserId();
        String note = !noteStr.isEmpty() ? noteStr : null;

        if (recordId > 0) {
            HealthRecord record = healthDao.getById(recordId);
            if (record != null) {
                record.setDate(selectedDate);
                record.setSystolic(systolic);
                record.setDiastolic(diastolic);
                record.setHeartRate(heartRate);
                record.setWeightKg(weight);
                record.setBloodSugar(bloodSugar);
                record.setNote(note);

                boolean success = healthDao.update(record);
                if (success) {
                    Toast.makeText(this, getString(R.string.add_health_success_edit), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.add_health_error_update), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            HealthRecord record = new HealthRecord(0, userId, selectedDate, weight, systolic, diastolic, heartRate, bloodSugar, note);
            long id = healthDao.insert(record);
            if (id > 0) {
                Toast.makeText(this, getString(R.string.add_health_success_add), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.add_health_error_save), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onTextChanged;

        SimpleTextWatcher(Runnable onTextChanged) {
            this.onTextChanged = onTextChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (onTextChanged != null) {
                onTextChanged.run();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }
}

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
    private vn.edu.tdmu.vita.utils.UnsavedChangesGuard unsavedGuard;

    private long formUserId;

    public static final String EXTRA_RECORD_ID = "extra_record_id";
    public static final String EXTRA_METRIC_GROUP = "extra_metric_group";

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

    private TextInputEditText etMeasuredTime;
    private android.widget.Spinner mealContext;
    private String measuredTime;
    private com.google.android.material.chip.ChipGroup metricGroup;

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
        formUserId = savedInstanceState == null ? sessionManager.getUserId() : savedInstanceState.getLong("form_user_id", -1);
        if (!SessionManager.matchesUser(formUserId, sessionManager.getUserId())) {
            finish();
            return;
        }

        healthDao = new HealthDao(this);
        calendar = Calendar.getInstance();

        initViews();
        setupListeners();
        loadIntentData();
        if (savedInstanceState != null) {
            selectedDate = savedInstanceState.getString("record_date", selectedDate);
            measuredTime = savedInstanceState.getString("record_time");
            mealContext.setSelection(savedInstanceState.getInt("meal_context", 0));
            etDate.setText(DateTimeUtils.formatDisplayDate(selectedDate));
        }
        etMeasuredTime.setText(measuredTime == null ? "" : measuredTime);
        setupMetricGroups(savedInstanceState);
        unsavedGuard = vn.edu.tdmu.vita.utils.UnsavedChangesGuard.attach(this, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> unsavedGuard.requestExit());
    }

    private void setupMetricGroups(Bundle state) {
        com.google.android.material.chip.ChipGroup groupChips = new com.google.android.material.chip.ChipGroup(this);
        metricGroup = groupChips;
        groupChips.setId(R.id.sp_health_metric_group);
        groupChips.setContentDescription(getString(R.string.health_metric_group));
        groupChips.setTag("presentation_only");
        groupChips.setSingleSelection(true);
        groupChips.setSelectionRequired(true);
        groupChips.setChipSpacingHorizontal((int) (8 * getResources().getDisplayMetrics().density));
        groupChips.setChipSpacingVertical(0);
        String[] groupNames = getResources().getStringArray(R.array.figma_health_groups);
        android.content.res.ColorStateList chipColors = new android.content.res.ColorStateList(
                new int[][] {new int[] {android.R.attr.state_checked}, new int[] {}},
                new int[] {androidx.core.content.ContextCompat.getColor(this, R.color.vita_mint),
                        androidx.core.content.ContextCompat.getColor(this, R.color.vita_surface)});
        for (int i = 0; i < groupNames.length; i++) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setId(android.view.View.generateViewId());
            chip.setText(groupNames[i]);
            chip.setTextSize(12);
            chip.setChipCornerRadius(12 * getResources().getDisplayMetrics().density);
            chip.setChipMinHeight(36 * getResources().getDisplayMetrics().density);
            chip.setChipStartPadding(8 * getResources().getDisplayMetrics().density);
            chip.setChipEndPadding(8 * getResources().getDisplayMetrics().density);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColor(chipColors);
            chip.setChipStrokeWidth(0);
            chip.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.vita_teal));
            chip.setEnsureMinTouchTargetSize(true);
            int availableWidth = getResources().getDisplayMetrics().widthPixels - (int) (48 * getResources().getDisplayMetrics().density);
            int columns = i < 3 ? 3 : 2;
            chip.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    Math.min(availableWidth, Math.max(
                            (availableWidth - (int) ((columns - 1) * 8 * getResources().getDisplayMetrics().density)) / columns,
                            (int) Math.ceil(chip.getPaint().measureText(groupNames[i]) + 48 * getResources().getDisplayMetrics().density))),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
            chip.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
            groupChips.addView(chip);
        }
        ((android.view.ViewGroup) tilDate.getParent().getParent()).addView(groupChips, 1);
        groupChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) return;
                int position = group.indexOfChild(group.findViewById(group.getCheckedChipId()));
                tilSystolic.setVisibility(position == 0 || position == 1 ? android.view.View.VISIBLE : android.view.View.GONE);
                tilDiastolic.setVisibility(tilSystolic.getVisibility());
                ((android.view.View) tilSystolic.getParent()).setVisibility(tilSystolic.getVisibility());
                findViewById(R.id.tv_add_bp_section).setVisibility(tilSystolic.getVisibility());
                tilHeartRate.setVisibility(position == 0 || position == 2 ? android.view.View.VISIBLE : android.view.View.GONE);
                tilWeight.setVisibility(position == 0 || position == 3 ? android.view.View.VISIBLE : android.view.View.GONE);
                tilSugar.setVisibility(position == 0 || position == 4 ? android.view.View.VISIBLE : android.view.View.GONE);
                ((android.view.View) tilWeight.getParent()).setVisibility(position == 0 || position == 3 || position == 4 ? android.view.View.VISIBLE : android.view.View.GONE);
                ((android.view.View) mealContext.getParent()).setVisibility(tilSugar.getVisibility());
        });
        int initialGroup = state == null ? getIntent().getIntExtra(EXTRA_METRIC_GROUP, 0) : state.getInt("metric_group", 0);
        groupChips.check(groupChips.getChildAt(Math.max(0, Math.min(groupNames.length - 1, initialGroup))).getId());
    }

    @Override protected void onResume() {
        super.onResume();
        if (!SessionManager.matchesUser(formUserId, new SessionManager(this).getUserId())) finish();
    }

    private void initViews() {
        etMeasuredTime = findViewById(R.id.et_health_time);
        etMeasuredTime.setKeyListener(null);
        mealContext = findViewById(R.id.sp_meal_context);
        android.widget.ArrayAdapter<CharSequence> mealAdapter = android.widget.ArrayAdapter.createFromResource(
                this, R.array.meal_contexts, android.R.layout.simple_spinner_item);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealContext.setAdapter(mealAdapter);
        etMeasuredTime.setOnClickListener(v -> {
            String current = measuredTime == null ? DateTimeUtils.getCurrentTime() : measuredTime;
            new android.app.TimePickerDialog(this, (picker, hour, minute) -> {
                measuredTime = String.format(Locale.US, "%02d:%02d", hour, minute);
                etMeasuredTime.setText(measuredTime);
            }, Integer.parseInt(current.substring(0, 2)), Integer.parseInt(current.substring(3)), true).show();
        });
        toolbar = findViewById(R.id.toolbar_add_health);
        tilDate = findViewById(R.id.til_add_health_date);
        etDate = findViewById(R.id.et_add_health_date);
        etDate.setKeyListener(null);

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
        if (getResources().getConfiguration().fontScale > 1.3f) {
            for (android.widget.LinearLayout row : new android.widget.LinearLayout[]{
                    (android.widget.LinearLayout) tilDate.getParent(),
                    (android.widget.LinearLayout) tilSystolic.getParent()}) {
                row.setOrientation(android.widget.LinearLayout.VERTICAL);
                for (int index = 0; index < row.getChildCount(); index++) {
                    android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) row.getChildAt(index).getLayoutParams();
                    params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                    params.weight = 0;
                    params.setMarginStart(0); params.setMarginEnd(0);
                }
            }
        }
        alignMeasurementUnit(tilSystolic);
        alignMeasurementUnit(tilDiastolic);
        alignMeasurementUnit(tilHeartRate);
        alignMeasurementUnit(tilWeight);
        alignMeasurementUnit(tilSugar);
    }

    private void alignMeasurementUnit(TextInputLayout field) {
        android.widget.EditText input = field.getEditText();
        android.widget.TextView suffix = field.getSuffixTextView();
        Runnable align = () -> {
            int[] inputPosition = new int[2], suffixPosition = new int[2];
            input.getLocationInWindow(inputPosition);
            suffix.getLocationInWindow(suffixPosition);
            float textEnd = inputPosition[0] + input.getCompoundPaddingLeft()
                    + input.getPaint().measureText(input.getText().toString())
                    + 6 * getResources().getDisplayMetrics().density;
            // Figma places units beside the entered value; Material anchors suffixes at the field edge.
            suffix.setTranslationX(suffix.getTranslationX() + textEnd - suffixPosition[0] - suffix.getPaddingLeft());
        };
        input.addTextChangedListener(new SimpleTextWatcher(() -> input.post(align)));
        field.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> input.post(align));
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
            HealthRecord record = healthDao.getById(recordId, formUserId);
            if (record != null && record.getUserId() == sessionManager.getUserId()) {
                measuredTime = record.getMeasuredTime();
                mealContext.setSelection(record.getMealContext().ordinal());
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
            toolbar.setTitle(getString(R.string.figma_health_record));
            measuredTime = DateTimeUtils.getCurrentTime();
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
        if (!SessionManager.matchesUser(formUserId, sessionManager.getUserId())) { finish(); return; }
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
            metricGroup.check(metricGroup.getChildAt(0).getId());
            TextInputLayout[] fields = {tilSystolic, tilDiastolic, tilHeartRate, tilWeight, tilSugar};
            for (TextInputLayout field : fields) if (field.getError() != null) {
                field.setVisibility(android.view.View.VISIBLE);
                field.post(() -> {
                    field.getEditText().requestFocus();
                    field.getEditText().requestRectangleOnScreen(
                            new android.graphics.Rect(0, 0, field.getWidth(), field.getHeight()), false);
                });
                break;
            }
            return;
        }

        long userId = sessionManager.getUserId();
        String note = !noteStr.isEmpty() ? noteStr : null;

        if (recordId > 0) {
            HealthRecord record = healthDao.getById(recordId, formUserId);
            if (record != null) {
                record.setDate(selectedDate);
                record.setMeasuredTime(measuredTime);
                record.setMealContext(HealthRecord.MealContext.values()[mealContext.getSelectedItemPosition()].name());
                record.setSystolic(systolic);
                record.setDiastolic(diastolic);
                record.setHeartRate(heartRate);
                record.setWeightKg(weight);
                record.setBloodSugar(bloodSugar);
                record.setNote(note);

                boolean success = healthDao.update(record, formUserId);
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
            record.setMeasuredTime(measuredTime);
            record.setMealContext(HealthRecord.MealContext.values()[mealContext.getSelectedItemPosition()].name());
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

    @Override
    protected void onSaveInstanceState(Bundle state) {
        if (metricGroup != null) state.putInt("metric_group", metricGroup.indexOfChild(metricGroup.findViewById(metricGroup.getCheckedChipId())));
        state.putLong("form_user_id", formUserId);
        if (unsavedGuard != null) unsavedGuard.saveState(state);
        super.onSaveInstanceState(state);
        state.putString("record_date", selectedDate);
        state.putString("record_time", measuredTime);
        state.putInt("meal_context", mealContext.getSelectedItemPosition());
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

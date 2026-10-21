package vn.edu.tdmu.vita.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.HealthDao;
import vn.edu.tdmu.vita.models.HealthRecord;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.HealthEvaluator;

public class DetailHealthActivity extends AppCompatActivity {

    public static final String EXTRA_RECORD_ID = "extra_record_id";

    private MaterialToolbar toolbar;
    private TextView tvDate;
    private TextView tvBpValue;
    private TextView tvBpStatus;
    private TextView tvHeartRate;
    private TextView tvBpAdvice;
    private MaterialCardView cvBp;
    private LinearLayout layoutBpSection;
    private View dividerBp;

    private TextView tvWeight;
    private TextView tvSugar;
    private MaterialCardView cvBody;

    private TextView tvNote;
    private MaterialCardView cvNote;

    private MaterialButton btnEdit;

    private HealthDao healthDao;
    private long recordId = -1;
    private long ownerId;
    private HealthRecord currentRecord;

    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_health);

        ownerId = new vn.edu.tdmu.vita.utils.SessionManager(this).getUserId();
        healthDao = new HealthDao(this);
        recordId = getIntent().getLongExtra(EXTRA_RECORD_ID, -1);

        if (recordId <= 0) {
            Toast.makeText(this, getString(R.string.detail_health_invalid_record), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> loadAndDisplay() // Reload after edit
        );

        initViews();
        setupListeners();
        loadAndDisplay();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_detail_health);
        ((TextView) findViewById(R.id.tv_page_title)).setText(R.string.figma_health_detail_title);
        tvDate = findViewById(R.id.tv_detail_date);

        cvBp = findViewById(R.id.cv_detail_bp);
        layoutBpSection = findViewById(R.id.layout_detail_bp_section);
        tvBpValue = findViewById(R.id.tv_detail_bp_value);
        tvBpStatus = findViewById(R.id.tv_detail_bp_status);
        tvBpAdvice = findViewById(R.id.tv_detail_bp_advice);
        dividerBp = findViewById(R.id.divider_detail_bp);
        tvHeartRate = findViewById(R.id.tv_detail_heart_rate);

        cvBody = findViewById(R.id.cv_detail_body);
        tvWeight = findViewById(R.id.tv_detail_weight);
        tvSugar = findViewById(R.id.tv_detail_sugar);

        cvNote = findViewById(R.id.cv_detail_note);
        tvNote = findViewById(R.id.tv_detail_note);

        btnEdit = findViewById(R.id.btn_detail_edit);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        toolbar.inflateMenu(R.menu.context_menu_health);
        // Only keep the delete option in toolbar
        Menu menu = toolbar.getMenu();
        MenuItem editItem = menu.findItem(R.id.menu_health_edit);
        if (editItem != null) editItem.setVisible(false);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_health_delete) {
                showDeleteConfirmation();
                return true;
            }
            return false;
        });

        btnEdit.setOnClickListener(v -> openEdit());
    }

    @Override protected void onResume() {
        super.onResume();
        if (!vn.edu.tdmu.vita.utils.SessionManager.matchesUser(ownerId,
                new vn.edu.tdmu.vita.utils.SessionManager(this).getUserId())) finish();
    }

    private void loadAndDisplay() {
        currentRecord = healthDao.getById(recordId, ownerId);
        if (currentRecord == null) {
            Toast.makeText(this, getString(R.string.detail_health_record_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayRecord(currentRecord);
    }

    private void displayRecord(HealthRecord record) {
        // Date (no emoji)
        String displayDate = DateTimeUtils.formatDisplayDate(record.getDate()) + " · "
                + (record.getMeasuredTime() == null ? getString(R.string.measurement_time_unknown) : record.getMeasuredTime());
        tvDate.setText(displayDate);
        TextView context = findViewById(R.id.tv_detail_context);
        context.setVisibility(record.getBloodSugar() == null ? View.GONE : View.VISIBLE);
        context.setText(getString(R.string.figma_health_sugar_context, getResources().getStringArray(R.array.meal_contexts)[record.getMealContext().ordinal()]));

        // Blood Pressure & Heart Rate section
        boolean hasBp = record.getSystolic() != null || record.getDiastolic() != null;
        boolean completeBp = record.getSystolic() != null && record.getDiastolic() != null;
        boolean hasHr = record.getHeartRate() != null;
        tvBpStatus.setVisibility(View.GONE);
        tvBpAdvice.setVisibility(View.GONE);
        findViewById(R.id.card_detail_hr).setVisibility(hasHr ? View.VISIBLE : View.GONE);

        if (!hasBp && !hasHr) {
            cvBp.setVisibility(View.GONE);
        } else {
            cvBp.setVisibility(View.VISIBLE);

            if (hasBp) {
                ((View) layoutBpSection.getParent()).setVisibility(View.VISIBLE);
                tvBpValue.setText((record.getSystolic() == null ? "—" : record.getSystolic())
                        + " / " + (record.getDiastolic() == null ? "—" : record.getDiastolic()));

                tvBpValue.setContentDescription(tvBpValue.getText() + " mmHg");
                if (completeBp) {
                HealthEvaluator.BpResult bpResult = HealthEvaluator.classifyBloodPressure(
                        record.getSystolic(), record.getDiastolic());
                tvBpStatus.setText(bpResult.label);

                int statusColor;
                int badgeBgRes;
                switch (bpResult.severity) {
                    case HealthEvaluator.STATUS_WARNING:
                        statusColor = ContextCompat.getColor(this, R.color.vita_status_warning);
                        badgeBgRes = R.drawable.bg_badge_warning;
                        break;
                    case HealthEvaluator.STATUS_DANGER:
                        statusColor = ContextCompat.getColor(this, R.color.vita_status_danger);
                        badgeBgRes = R.drawable.bg_badge_danger;
                        break;
                    default:
                        statusColor = ContextCompat.getColor(this, R.color.vita_status_good);
                        badgeBgRes = R.drawable.bg_badge_good;
                        break;
                }
                tvBpStatus.setTextColor(statusColor);
                tvBpStatus.setBackgroundResource(badgeBgRes);

                tvBpAdvice.setText(getString(R.string.detail_health_advice_format, HealthEvaluator.getBpAdvice(bpResult)));
                tvBpAdvice.setVisibility(View.GONE);
                tvBpStatus.setClickable(true);
                tvBpStatus.setFocusable(true);
                tvBpStatus.setContentDescription(bpResult.label + ". " + getString(R.string.figma_health_advice_toggle));
                tvBpStatus.setOnClickListener(v -> tvBpAdvice.setVisibility(tvBpAdvice.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
                tvBpStatus.setVisibility(View.VISIBLE);
                } else {
                    tvBpStatus.setVisibility(View.GONE);
                    tvBpAdvice.setVisibility(View.GONE);
                }
            } else {
                ((View) layoutBpSection.getParent()).setVisibility(View.GONE);
            }

            if (hasHr) {
                findViewById(R.id.card_detail_hr).setVisibility(View.VISIBLE);
                String hrClassification = HealthEvaluator.classifyHeartRate(record.getHeartRate());
                tvHeartRate.setText(String.valueOf(record.getHeartRate()));
                tvHeartRate.setContentDescription(getString(R.string.figma_health_hr_detail, record.getHeartRate(), hrClassification));
            } else {
                findViewById(R.id.card_detail_hr).setVisibility(View.GONE);
            }

            dividerBp.setVisibility(hasBp && hasHr ? View.VISIBLE : View.GONE);
        }

        // Weight & Blood Sugar section (Hide if both null)
        boolean hasWeight = record.getWeightKg() != null;
        boolean hasSugar = record.getBloodSugar() != null;

        if (!hasWeight && !hasSugar && !hasHr) {
            cvBody.setVisibility(View.GONE);
        } else {
            cvBody.setVisibility(View.VISIBLE);
            if (hasWeight) {
                findViewById(R.id.card_detail_weight).setVisibility(View.VISIBLE);
                tvWeight.setText(String.format(Locale.getDefault(), "%.1f", record.getWeightKg()));
            } else {
                findViewById(R.id.card_detail_weight).setVisibility(View.GONE);
            }

            if (hasSugar) {
                findViewById(R.id.card_detail_sugar).setVisibility(View.VISIBLE);
                tvSugar.setText(String.format(Locale.getDefault(), "%.0f", record.getBloodSugar()));
            } else {
                findViewById(R.id.card_detail_sugar).setVisibility(View.GONE);
            }
        }

        // Note section (Hide if empty)
        boolean hasNote = record.getNote() != null && !record.getNote().trim().isEmpty();
        if (!hasNote) {
            cvNote.setVisibility(View.GONE);
        } else {
            cvNote.setVisibility(View.VISIBLE);
            tvNote.setText(record.getNote().trim());
        }
        displayPrevious(record);
    }

    private void displayPrevious(HealthRecord record) {
        for (vn.edu.tdmu.vita.utils.HealthChartData.Metric metric : vn.edu.tdmu.vita.utils.HealthChartData.Metric.values()) {
            Number value;
            String unit;
            int comparisonId;
            switch (metric) {
                case WEIGHT: value = record.getWeightKg(); unit = "kg"; comparisonId = R.id.tv_detail_weight_previous; break;
                case HEART_RATE: value = record.getHeartRate(); unit = "bpm"; comparisonId = R.id.tv_detail_hr_previous; break;
                case BLOOD_PRESSURE: value = record.getSystolic(); unit = "mmHg";
                    comparisonId = R.id.tv_detail_bp_previous;
                    if (record.getDiastolic() == null) continue; break;
                default: value = record.getBloodSugar(); unit = "mg/dL"; comparisonId = R.id.tv_detail_sugar_previous;
            }
            if (value == null) continue;
            HealthRecord previous = healthDao.getPreviousComparable(ownerId, record, metric);
            if (previous == null) {
                ((TextView) findViewById(comparisonId)).setText(R.string.no_comparable_measurement);
                continue;
            }
            Number prior;
            switch (metric) {
                case WEIGHT: prior = previous.getWeightKg(); break;
                case HEART_RATE: prior = previous.getHeartRate(); break;
                case BLOOD_PRESSURE: prior = previous.getSystolic(); break;
                default: prior = previous.getBloodSugar();
            }
            String priorValue = String.format(Locale.getDefault(), metric == vn.edu.tdmu.vita.utils.HealthChartData.Metric.WEIGHT ? "%.1f" : "%.0f", prior.floatValue());
            String difference = String.format(Locale.getDefault(), metric == vn.edu.tdmu.vita.utils.HealthChartData.Metric.WEIGHT ? "%+.1f" : "%+.0f", value.floatValue() - prior.floatValue());
            if (metric == vn.edu.tdmu.vita.utils.HealthChartData.Metric.BLOOD_PRESSURE) {
                priorValue += " / " + previous.getDiastolic();
                difference += " / " + String.format(Locale.getDefault(), "%+d", record.getDiastolic() - previous.getDiastolic());
            }
            ((TextView) findViewById(comparisonId)).setText(getString(R.string.figma_health_previous,
                    priorValue + " " + unit, vn.edu.tdmu.vita.utils.HealthDisplay.measurementLabel(this, previous), difference, unit));
        }
    }

    private void openEdit() {
        Intent intent = new Intent(this, AddHealthActivity.class);
        intent.putExtra(AddHealthActivity.EXTRA_RECORD_ID, recordId);
        editLauncher.launch(intent);
    }

    private void showDeleteConfirmation() {
        if (currentRecord == null) return;

        String dateDisplay = DateTimeUtils.formatDisplayDate(currentRecord.getDate());
        androidx.appcompat.app.AppCompatDialog dialog = vn.edu.tdmu.vita.utils.VitaDialog.create(this,
                getString(R.string.figma_delete_record_title), getString(R.string.figma_delete_record_message,
                        dateDisplay, currentRecord.getMeasuredTime() == null ? getString(R.string.measurement_time_unknown)
                                : currentRecord.getMeasuredTime()), R.string.figma_delete_record_action, true);
        dialog.findViewById(android.R.id.button1).setOnClickListener(v -> {
                    if (!vn.edu.tdmu.vita.utils.SessionManager.matchesUser(ownerId, new vn.edu.tdmu.vita.utils.SessionManager(this).getUserId())) { finish(); return; }
                    boolean deleted = healthDao.delete(recordId, ownerId);
                    if (deleted) {
                        Toast.makeText(this, getString(R.string.health_delete_success), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.health_delete_error), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                });
        dialog.show();
    }
}

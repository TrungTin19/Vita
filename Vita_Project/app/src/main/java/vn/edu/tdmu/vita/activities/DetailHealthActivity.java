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
import androidx.appcompat.app.AlertDialog;
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
    private TextView tvBpUnit;
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
    private HealthRecord currentRecord;

    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_health);

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
        tvDate = findViewById(R.id.tv_detail_date);

        cvBp = findViewById(R.id.cv_detail_bp);
        layoutBpSection = findViewById(R.id.layout_detail_bp_section);
        tvBpValue = findViewById(R.id.tv_detail_bp_value);
        tvBpStatus = findViewById(R.id.tv_detail_bp_status);
        tvBpUnit = findViewById(R.id.tv_detail_bp_unit);
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

    private void loadAndDisplay() {
        currentRecord = healthDao.getById(recordId);
        if (currentRecord == null) {
            Toast.makeText(this, getString(R.string.detail_health_record_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayRecord(currentRecord);
    }

    private void displayRecord(HealthRecord record) {
        // Date (no emoji)
        String displayDate = vn.edu.tdmu.vita.utils.HealthDisplay.measurementLabel(this, record);
        tvDate.setText(getString(R.string.detail_health_date_format, displayDate));

        // Blood Pressure & Heart Rate section
        boolean hasBp = record.getSystolic() != null && record.getDiastolic() != null;
        boolean hasHr = record.getHeartRate() != null;

        if (!hasBp && !hasHr) {
            cvBp.setVisibility(View.GONE);
        } else {
            cvBp.setVisibility(View.VISIBLE);

            if (hasBp) {
                layoutBpSection.setVisibility(View.VISIBLE);
                tvBpValue.setText(String.format(Locale.getDefault(), "%d / %d",
                        record.getSystolic(), record.getDiastolic()));

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
                tvBpAdvice.setVisibility(View.VISIBLE);
            } else {
                layoutBpSection.setVisibility(View.GONE);
            }

            if (hasHr) {
                tvHeartRate.setVisibility(View.VISIBLE);
                String hrClassification = HealthEvaluator.classifyHeartRate(record.getHeartRate());
                tvHeartRate.setText(getString(R.string.detail_health_hr_format, record.getHeartRate(), hrClassification));
            } else {
                tvHeartRate.setVisibility(View.GONE);
            }

            dividerBp.setVisibility(hasBp && hasHr ? View.VISIBLE : View.GONE);
        }

        // Weight & Blood Sugar section (Hide if both null)
        boolean hasWeight = record.getWeightKg() != null;
        boolean hasSugar = record.getBloodSugar() != null;

        if (!hasWeight && !hasSugar) {
            cvBody.setVisibility(View.GONE);
        } else {
            cvBody.setVisibility(View.VISIBLE);
            if (hasWeight) {
                tvWeight.setVisibility(View.VISIBLE);
                tvWeight.setText(getString(R.string.detail_health_weight_format, record.getWeightKg()));
            } else {
                tvWeight.setVisibility(View.GONE);
            }

            if (hasSugar) {
                tvSugar.setVisibility(View.VISIBLE);
                tvSugar.setText(getString(R.string.detail_health_sugar_format, record.getBloodSugar()));
            } else {
                tvSugar.setVisibility(View.GONE);
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
    }

    private void openEdit() {
        Intent intent = new Intent(this, AddHealthActivity.class);
        intent.putExtra(AddHealthActivity.EXTRA_RECORD_ID, recordId);
        editLauncher.launch(intent);
    }

    private void showDeleteConfirmation() {
        if (currentRecord == null) return;

        String dateDisplay = DateTimeUtils.formatDisplayDate(currentRecord.getDate());
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.health_delete_dialog_title))
                .setMessage(getString(R.string.health_delete_dialog_msg, dateDisplay))
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .setPositiveButton(getString(R.string.btn_delete), (dialog, which) -> {
                    boolean deleted = healthDao.delete(recordId);
                    if (deleted) {
                        Toast.makeText(this, getString(R.string.health_delete_success), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.health_delete_error), Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}

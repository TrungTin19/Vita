package vn.edu.tdmu.vita.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
            Toast.makeText(this, "Bản ghi không hợp lệ", Toast.LENGTH_SHORT).show();
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
        tvBpValue = findViewById(R.id.tv_detail_bp_value);
        tvBpStatus = findViewById(R.id.tv_detail_bp_status);
        tvBpUnit = findViewById(R.id.tv_detail_bp_unit);
        tvHeartRate = findViewById(R.id.tv_detail_heart_rate);
        tvBpAdvice = findViewById(R.id.tv_detail_bp_advice);

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
            Toast.makeText(this, "Không tìm thấy bản ghi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayRecord(currentRecord);
    }

    private void displayRecord(HealthRecord record) {
        // Date
        String displayDate = DateTimeUtils.formatDisplayDate(record.getDate());
        tvDate.setText("📅 Ngày đo: " + displayDate);

        // Blood Pressure
        if (record.getSystolic() != null && record.getDiastolic() != null) {
            tvBpValue.setText(String.format(Locale.getDefault(), "%d / %d",
                    record.getSystolic(), record.getDiastolic()));

            HealthEvaluator.BpResult bpResult = HealthEvaluator.classifyBloodPressure(
                    record.getSystolic(), record.getDiastolic());
            tvBpStatus.setText(bpResult.label);

            int statusColor;
            switch (bpResult.severity) {
                case HealthEvaluator.STATUS_WARNING: statusColor = getColor(R.color.vita_status_warning); break;
                case HealthEvaluator.STATUS_DANGER:  statusColor = getColor(R.color.vita_status_danger); break;
                default:                             statusColor = getColor(R.color.vita_status_good); break;
            }
            tvBpStatus.setTextColor(statusColor);

            String advice = "• Đánh giá: " + HealthEvaluator.getBpAdvice(bpResult);
            tvBpAdvice.setText(advice);
        } else {
            tvBpValue.setText("-- / --");
            tvBpStatus.setText("Không đo");
            tvBpStatus.setTextColor(getColor(R.color.vita_text_disabled));
            tvBpAdvice.setText("• Đánh giá: -- (Không đo trong lần này)");
        }

        // Heart Rate
        if (record.getHeartRate() != null) {
            String hrClassification = HealthEvaluator.classifyHeartRate(record.getHeartRate());
            tvHeartRate.setText(String.format(Locale.getDefault(),
                    "• Nhịp tim: %d bpm (%s)", record.getHeartRate(), hrClassification));
        } else {
            tvHeartRate.setText("• Nhịp tim: -- (Không đo trong lần này)");
        }

        // Weight
        if (record.getWeightKg() != null) {
            tvWeight.setText(String.format(Locale.getDefault(),
                    "• Cân nặng: %.1f kg", record.getWeightKg()));
        } else {
            tvWeight.setText("• Cân nặng: -- (Không đo trong lần này)");
        }

        // Blood Sugar
        if (record.getBloodSugar() != null) {
            tvSugar.setText(String.format(Locale.getDefault(),
                    "• Đường huyết: %.1f mg/dL", record.getBloodSugar()));
        } else {
            tvSugar.setText("• Đường huyết: -- (Không đo trong lần này)");
        }

        // Note
        if (record.getNote() != null && !record.getNote().trim().isEmpty()) {
            tvNote.setText(record.getNote());
        } else {
            tvNote.setText("Không có ghi chú");
            tvNote.setTextColor(getColor(R.color.vita_text_disabled));
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
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bản ghi đo ngày " + dateDisplay + " không? Thao tác này không thể hoàn tác.")
                .setNegativeButton("Hủy bỏ", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean deleted = healthDao.delete(recordId);
                    if (deleted) {
                        Toast.makeText(this, "Đã xóa bản ghi thành công", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Không thể xóa. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}

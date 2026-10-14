package vn.edu.tdmu.vita.adapters;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.models.HealthRecord;
import vn.edu.tdmu.vita.utils.DateTimeUtils;

public class HealthRecordAdapter extends RecyclerView.Adapter<HealthRecordAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(HealthRecord record, int position);
    }

    private final List<HealthRecord> records;
    private OnItemClickListener listener;
    private int contextMenuPosition = -1;

    public HealthRecordAdapter(List<HealthRecord> records) {
        this.records = records;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public int getContextMenuPosition() {
        return contextMenuPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_health_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthRecord record = records.get(position);
        holder.bind(record, position);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private final MaterialCardView cardView;
        private final TextView tvDate;
        private final TextView tvBpStatus;
        private final LinearLayout layoutBpRow;
        private final TextView tvBp;
        private final TextView tvWeight;
        private final TextView tvHeartRate;
        private final TextView tvSugar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cv_health_item);
            tvDate = itemView.findViewById(R.id.tv_item_date);
            tvBpStatus = itemView.findViewById(R.id.tv_item_bp_status);
            layoutBpRow = itemView.findViewById(R.id.layout_bp_row);
            tvBp = itemView.findViewById(R.id.tv_item_bp);
            tvWeight = itemView.findViewById(R.id.tv_item_weight);
            tvHeartRate = itemView.findViewById(R.id.tv_item_heart_rate);
            tvSugar = itemView.findViewById(R.id.tv_item_sugar);

            itemView.setOnCreateContextMenuListener(this);
        }

        void bind(HealthRecord record, int position) {
            // Date
            tvDate.setText(DateTimeUtils.formatDisplayDate(record.getDate()));

            // Blood Pressure
            if (record.getSystolic() != null && record.getDiastolic() != null) {
                layoutBpRow.setVisibility(View.VISIBLE);
                tvBp.setText(String.format(Locale.getDefault(), "%d/%d mmHg",
                        record.getSystolic(), record.getDiastolic()));

                // BP status badge
                String statusText = classifyBloodPressure(record.getSystolic(), record.getDiastolic());
                int statusColor = getBpStatusColor(record.getSystolic(), record.getDiastolic());
                tvBpStatus.setVisibility(View.VISIBLE);
                tvBpStatus.setText(statusText);
                tvBpStatus.setTextColor(statusColor);
            } else {
                layoutBpRow.setVisibility(View.GONE);
                tvBpStatus.setVisibility(View.GONE);
            }

            // Weight
            if (record.getWeightKg() != null) {
                tvWeight.setVisibility(View.VISIBLE);
                tvWeight.setText(String.format(Locale.getDefault(), "Cân nặng: %.1f kg", record.getWeightKg()));
            } else {
                tvWeight.setVisibility(View.GONE);
            }

            // Heart Rate
            if (record.getHeartRate() != null) {
                tvHeartRate.setVisibility(View.VISIBLE);
                tvHeartRate.setText(String.format(Locale.getDefault(), "Tim: %d bpm", record.getHeartRate()));
            } else {
                tvHeartRate.setVisibility(View.GONE);
            }

            // Blood Sugar
            if (record.getBloodSugar() != null) {
                tvSugar.setVisibility(View.VISIBLE);
                tvSugar.setText(String.format(Locale.getDefault(), "Đường huyết: %.0f mg/dL", record.getBloodSugar()));
            } else {
                tvSugar.setVisibility(View.GONE);
            }

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(record, position);
                }
            });

            // Long click sets context menu position
            cardView.setOnLongClickListener(v -> {
                contextMenuPosition = getBindingAdapterPosition();
                return false; // Return false to allow context menu to show
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                         ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Tùy chọn");
            menu.add(0, R.id.menu_health_edit, 0, "Sửa bản ghi này");
            menu.add(0, R.id.menu_health_delete, 1, "Xóa bản ghi này");
        }
    }

    /**
     * Classifies blood pressure according to Vietnamese medical standards.
     * Based on JNC 7 / Vietnamese Heart Association guidelines.
     */
    private String classifyBloodPressure(int systolic, int diastolic) {
        if (systolic < 120 && diastolic < 80) {
            return "Bình thường";
        } else if (systolic < 130 && diastolic < 85) {
            return "Bình thường cao";
        } else if (systolic < 140 || diastolic < 90) {
            return "Cần chú ý";
        } else {
            return "Tăng huyết áp";
        }
    }

    /**
     * Returns status color resource ID for BP classification.
     */
    private int getBpStatusColor(int systolic, int diastolic) {
        if (systolic < 120 && diastolic < 80) {
            return 0xFF2E7D32; // vita_status_good
        } else if (systolic < 130 && diastolic < 85) {
            return 0xFF2E7D32; // vita_status_good
        } else if (systolic < 140 || diastolic < 90) {
            return 0xFFF57C00; // vita_status_warning
        } else {
            return 0xFFD32F2F; // vita_status_danger
        }
    }
}

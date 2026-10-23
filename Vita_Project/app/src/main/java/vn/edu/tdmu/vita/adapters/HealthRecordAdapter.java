package vn.edu.tdmu.vita.adapters;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.models.HealthRecord;
import vn.edu.tdmu.vita.utils.HealthEvaluator;

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
        private final TextView tvNote;

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
            tvNote = itemView.findViewById(R.id.tv_item_health_note);

            itemView.setOnCreateContextMenuListener(this);
        }

        void bind(HealthRecord record, int position) {
            // Ngày đo
            tvDate.setText(vn.edu.tdmu.vita.utils.HealthDisplay.measurementLabel(itemView.getContext(), record));
            tvNote.setText(record.getNote());
            tvNote.setVisibility(record.getNote() == null || record.getNote().trim().isEmpty() ? View.GONE : View.VISIBLE);

            // Huyết áp
            if (record.getSystolic() != null && record.getDiastolic() != null) {
                layoutBpRow.setVisibility(View.VISIBLE);
                tvBp.setText(itemView.getContext().getString(R.string.health_bp_value_with_unit,
                        record.getSystolic().toString(), record.getDiastolic().toString()));

                // Nhãn trạng thái huyết áp — dùng HealthEvaluator làm nguồn phân loại thống nhất.
                HealthEvaluator.BpResult bpResult = HealthEvaluator.classifyBloodPressure(
                        record.getSystolic(), record.getDiastolic());
                tvBpStatus.setVisibility(View.VISIBLE);
                tvBpStatus.setText(bpResult.label);
                int statusColor;
                int badgeBgRes;
                switch (bpResult.severity) {
                    case HealthEvaluator.STATUS_WARNING:
                        statusColor = ContextCompat.getColor(itemView.getContext(), R.color.vita_status_warning);
                        badgeBgRes = R.drawable.bg_badge_warning;
                        break;
                    case HealthEvaluator.STATUS_DANGER:
                        statusColor = ContextCompat.getColor(itemView.getContext(), R.color.vita_status_danger);
                        badgeBgRes = R.drawable.bg_badge_danger;
                        break;
                    default:
                        statusColor = ContextCompat.getColor(itemView.getContext(), R.color.vita_status_good);
                        badgeBgRes = R.drawable.bg_badge_good;
                        break;
                }
                tvBpStatus.setTextColor(statusColor);
                tvBpStatus.setBackgroundResource(badgeBgRes);
            } else {
                boolean partial = record.getSystolic() != null || record.getDiastolic() != null;
                layoutBpRow.setVisibility(partial ? View.VISIBLE : View.GONE);
                tvBp.setText(itemView.getContext().getString(R.string.health_bp_value_with_unit,
                        record.getSystolic() == null ? "—" : record.getSystolic().toString(),
                        record.getDiastolic() == null ? "—" : record.getDiastolic().toString()));
                tvBpStatus.setVisibility(View.GONE);
            }

            // Cân nặng
            if (record.getWeightKg() != null) {
                tvWeight.setVisibility(View.VISIBLE);
                tvWeight.setText(itemView.getContext().getString(R.string.figma_health_weight_value, record.getWeightKg()));
                tvWeight.setTextSize(layoutBpRow.getVisibility() == View.VISIBLE ? 14 : 28);
                tvWeight.setTypeface(null, (layoutBpRow.getVisibility() != View.VISIBLE) ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
                tvWeight.setTextColor(ContextCompat.getColor(itemView.getContext(), (layoutBpRow.getVisibility() != View.VISIBLE) ? R.color.vita_text_primary : R.color.vita_text_secondary));
            } else {
                tvWeight.setVisibility(View.GONE);
            }

            // Nhịp tim
            if (record.getHeartRate() != null) {
                tvHeartRate.setVisibility(View.VISIBLE);
                tvHeartRate.setText(itemView.getContext().getString(R.string.figma_health_hr_value, record.getHeartRate()));
                tvHeartRate.setTextSize(layoutBpRow.getVisibility() == View.VISIBLE || record.getWeightKg() != null ? 14 : 28);
                tvHeartRate.setTypeface(null, (layoutBpRow.getVisibility() != View.VISIBLE && record.getWeightKg() == null) ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
                tvHeartRate.setTextColor(ContextCompat.getColor(itemView.getContext(), (layoutBpRow.getVisibility() != View.VISIBLE && record.getWeightKg() == null) ? R.color.vita_text_primary : R.color.vita_text_secondary));
            } else {
                tvHeartRate.setVisibility(View.GONE);
            }

            // Đường huyết
            if (record.getBloodSugar() != null) {
                tvSugar.setVisibility(View.VISIBLE);
                tvSugar.setText(itemView.getContext().getString(R.string.figma_health_sugar_value, record.getBloodSugar()));
                tvSugar.setTextSize(layoutBpRow.getVisibility() == View.VISIBLE || record.getWeightKg() != null || record.getHeartRate() != null ? 14 : 28);
                tvSugar.setTypeface(null, (layoutBpRow.getVisibility() != View.VISIBLE && record.getWeightKg() == null && record.getHeartRate() == null) ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
                tvSugar.setTextColor(ContextCompat.getColor(itemView.getContext(), (layoutBpRow.getVisibility() != View.VISIBLE && record.getWeightKg() == null && record.getHeartRate() == null) ? R.color.vita_text_primary : R.color.vita_text_secondary));
            } else {
                tvSugar.setVisibility(View.GONE);
            }

            // Bộ xử lý khi chạm vào mục
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(record, position);
                }
            });

            // Nhấn giữ để xác định vị trí hiển thị menu ngữ cảnh
            cardView.setOnLongClickListener(v -> {
                contextMenuPosition = getBindingAdapterPosition();
                return false; // Trả về false để menu ngữ cảnh có thể hiển thị
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                         ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(itemView.getContext().getString(R.string.health_context_menu_title));
            menu.add(0, R.id.menu_health_edit, 0, itemView.getContext().getString(R.string.health_context_menu_edit));
            menu.add(0, R.id.menu_health_delete, 1, itemView.getContext().getString(R.string.health_context_menu_delete));
        }
    }
}

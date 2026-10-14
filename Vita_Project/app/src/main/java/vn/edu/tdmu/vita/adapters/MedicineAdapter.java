package vn.edu.tdmu.vita.adapters;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.models.Medicine;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private static final String[] DAY_LABELS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    public interface OnSwitchChangeListener {
        void onSwitchChanged(Medicine medicine, int position, boolean isActive);
    }

    private final List<Medicine> medicines;
    private java.util.Map<Long, vn.edu.tdmu.vita.models.MedicineLog> todayLogs;
    private OnSwitchChangeListener switchListener;
    private int contextMenuPosition = -1;

    public MedicineAdapter(List<Medicine> medicines) {
        this.medicines = medicines;
    }

    public void setTodayLogs(java.util.Map<Long, vn.edu.tdmu.vita.models.MedicineLog> todayLogs) {
        this.todayLogs = todayLogs;
    }

    public void setOnSwitchChangeListener(OnSwitchChangeListener listener) {
        this.switchListener = listener;
    }

    public int getContextMenuPosition() {
        return contextMenuPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(medicines.get(position), position);
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    public static String formatDaysMask(android.content.Context context, int daysMask) {
        if (daysMask == 127) {
            return context.getString(R.string.reminder_med_days_daily);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DAY_LABELS.length; i++) {
            if ((daysMask & (1 << i)) != 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(DAY_LABELS[i]);
            }
        }
        return sb.toString();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private final MaterialCardView cardView;
        private final TextView tvName;
        private final MaterialSwitch swActive;
        private final TextView tvTime;
        private final TextView tvDays;
        private final TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cv_medicine_item);
            tvName = itemView.findViewById(R.id.tv_med_item_name);
            swActive = itemView.findViewById(R.id.sw_med_active);
            tvTime = itemView.findViewById(R.id.tv_med_item_time);
            tvDays = itemView.findViewById(R.id.tv_med_item_days);
            tvStatus = itemView.findViewById(R.id.tv_med_item_status);

            itemView.setOnCreateContextMenuListener(this);
        }

        void bind(Medicine medicine, int position) {
            tvName.setText(medicine.getName());

            String dosage = medicine.getDosage() == null || medicine.getDosage().trim().isEmpty()
                    ? "" : medicine.getDosage();
            String timeText = medicine.getTime() + (dosage.isEmpty() ? "" : " • " + dosage);
            tvTime.setText(timeText);

            tvDays.setText(itemView.getContext().getString(R.string.reminder_med_days_prefix,
                    formatDaysMask(itemView.getContext(), medicine.getDaysMask())));

            bindTodayStatus(medicine);

            swActive.setOnCheckedChangeListener(null);
            swActive.setChecked(medicine.isActive());
            swActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (switchListener != null) {
                    switchListener.onSwitchChanged(medicine, getBindingAdapterPosition(), isChecked);
                }
            });

            cardView.setOnLongClickListener(v -> {
                contextMenuPosition = getBindingAdapterPosition();
                return false;
            });
        }

        void bindTodayStatus(Medicine medicine) {
            android.content.Context context = itemView.getContext();
            vn.edu.tdmu.vita.models.MedicineLog log =
                    todayLogs == null ? null : todayLogs.get(medicine.getId());
            if (log == null) {
                tvStatus.setText(context.getString(R.string.reminder_med_status_none));
                tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context,
                        R.color.vita_text_secondary));
            } else if ("TAKEN".equals(log.getStatus())) {
                String time = log.getLoggedAt() != null && log.getLoggedAt().length() >= 16
                        ? log.getLoggedAt().substring(11, 16) : "";
                tvStatus.setText(context.getString(R.string.reminder_med_status_taken, time));
                tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context,
                        R.color.vita_status_good));
            } else {
                tvStatus.setText(context.getString(R.string.reminder_med_status_skipped));
                tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context,
                        R.color.vita_status_danger));
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                         ContextMenu.ContextMenuInfo menuInfo) {
            contextMenuPosition = getBindingAdapterPosition();
            menu.setHeaderTitle(itemView.getContext().getString(R.string.reminder_context_menu_title));
            menu.add(0, R.id.menu_medicine_edit, 0, itemView.getContext().getString(R.string.reminder_context_menu_edit));
            menu.add(0, R.id.menu_medicine_delete, 1, itemView.getContext().getString(R.string.reminder_context_menu_delete));
        }
    }
}

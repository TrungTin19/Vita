package vn.edu.tdmu.vita.adapters;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.models.Medicine;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private static final String[] DAY_LABELS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    public interface OnSwitchChangeListener {
        void onSwitchChanged(Medicine medicine, int position, boolean isActive);
    }

    private final List<Medicine> medicines;
    private OnSwitchChangeListener switchListener;
    private java.util.function.Consumer<Medicine> historyListener;

    public void setHistoryListener(java.util.function.Consumer<Medicine> listener) { historyListener = listener; }
    private int contextMenuPosition = -1;

    public MedicineAdapter(List<Medicine> medicines) {
        this.medicines = medicines;
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
        holder.bind(medicines.get(position));
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

        private final TextView tvName;
        private final com.google.android.material.button.MaterialButton swActive;
        private final TextView tvTime;
        private final TextView tvDays;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_med_item_name);
            swActive = itemView.findViewById(R.id.sw_med_active);
            tvTime = itemView.findViewById(R.id.tv_med_item_time);
            tvDays = itemView.findViewById(R.id.tv_med_item_days);

            itemView.setOnCreateContextMenuListener(this);
        }

        void bind(Medicine medicine) {
            tvName.setText(medicine.getName());

            String dosage = medicine.getDosage() == null || medicine.getDosage().trim().isEmpty()
                    ? "" : medicine.getDosage();
            ((TextView) itemView.findViewById(R.id.tv_med_item_dosage)).setText(dosage);
            String timeText = medicine.getTime().replace(",", " · ");
            tvTime.setText(timeText);

            tvDays.setText(formatDaysMask(itemView.getContext(), medicine.getDaysMask()));

            itemView.findViewById(R.id.btn_med_history).setOnClickListener(v -> {
                if (historyListener != null) historyListener.accept(medicine);
            });

            swActive.clearOnCheckedChangeListeners();
            swActive.setChecked(medicine.isActive());
            swActive.setText(medicine.isActive() ? R.string.figma_reminder_on : R.string.figma_reminder_off);
            swActive.addOnCheckedChangeListener((buttonView, isChecked) -> {
                swActive.setText(isChecked ? R.string.figma_reminder_on : R.string.figma_reminder_off);
                if (switchListener != null) {
                    switchListener.onSwitchChanged(medicine, getBindingAdapterPosition(), isChecked);
                }
            });

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

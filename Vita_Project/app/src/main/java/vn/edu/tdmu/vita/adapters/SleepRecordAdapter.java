package vn.edu.tdmu.vita.adapters;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.models.SleepLog;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.HealthEvaluator;
import vn.edu.tdmu.vita.utils.SleepEvaluator;

public class SleepRecordAdapter extends RecyclerView.Adapter<SleepRecordAdapter.ViewHolder> {

    private final List<SleepLog> logs;
    private int contextMenuPosition = -1;

    public SleepRecordAdapter(List<SleepLog> logs) {
        this.logs = logs;
    }

    public int getContextMenuPosition() {
        return contextMenuPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sleep_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(logs.get(position));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private final TextView tvDate;
        private final TextView tvTime;
        private final TextView tvBadge;
        private final com.google.android.material.card.MaterialCardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_sleep_item_date);
            tvTime = itemView.findViewById(R.id.tv_sleep_item_time);
            tvBadge = itemView.findViewById(R.id.tv_sleep_item_badge);
            cardView = itemView.findViewById(R.id.cv_sleep_item);

            itemView.setOnCreateContextMenuListener(this);
        }

        void bind(SleepLog log) {
            String wakeDisplay = DateTimeUtils.formatDisplayDate(log.getDate());
            tvDate.setText(itemView.getContext().getString(R.string.stats_sleep_date_format,
                    nightBefore(log.getDate()), wakeDisplay));
            tvTime.setText(itemView.getContext().getString(R.string.stats_sleep_item_format,
                    log.getSleepTime(), log.getWakeTime(), log.getDurationH()));

            SleepEvaluator.SleepResult result = SleepEvaluator.classify(log.getDurationH());
            tvBadge.setText(result.label);
            int textColor;
            int badgeBg;
            if (result.severity == HealthEvaluator.STATUS_GOOD) {
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.vita_status_good);
                badgeBg = R.drawable.bg_badge_good;
            } else if (result.severity == SleepEvaluator.STATUS_OVERSLEEP) {
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.vita_sleep);
                badgeBg = R.drawable.bg_badge_rounded;
            } else {
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.vita_status_warning);
                badgeBg = R.drawable.bg_badge_warning;
            }
            tvBadge.setTextColor(textColor);
            tvBadge.setBackgroundResource(badgeBg);

            cardView.setOnLongClickListener(v -> {
                contextMenuPosition = getBindingAdapterPosition();
                return false;
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                         ContextMenu.ContextMenuInfo menuInfo) {
            contextMenuPosition = getBindingAdapterPosition();
            menu.setHeaderTitle(itemView.getContext().getString(R.string.stats_sleep_menu_title));
            menu.add(0, R.id.menu_sleep_edit, 0, itemView.getContext().getString(R.string.stats_sleep_menu_edit));
            menu.add(0, R.id.menu_sleep_delete, 1, itemView.getContext().getString(R.string.stats_sleep_menu_delete));
        }

        private String nightBefore(String wakeDate) {
            if (wakeDate == null || !wakeDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return DateTimeUtils.formatDisplayDate(wakeDate);
            }
            String[] parts = wakeDate.split("-");
            try {
                Calendar c = Calendar.getInstance();
                c.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[2]));
                c.add(Calendar.DAY_OF_YEAR, -1);
                String night = String.format(Locale.US, "%04d-%02d-%02d",
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
                        c.get(Calendar.DAY_OF_MONTH));
                return DateTimeUtils.formatDisplayDate(night);
            } catch (NumberFormatException e) {
                return DateTimeUtils.formatDisplayDate(wakeDate);
            }
        }
    }
}

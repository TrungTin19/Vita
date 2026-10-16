package vn.edu.tdmu.vita.utils;

import android.content.Context;
import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.models.HealthRecord;

public final class HealthDisplay {
    private HealthDisplay() {}

    public static String measurementLabel(Context context, HealthRecord record) {
        String label = DateTimeUtils.formatDisplayDate(record.getDate()) + " · "
                + (record.getMeasuredTime() == null ? context.getString(R.string.measurement_time_unknown)
                : record.getMeasuredTime());
        if (record.getBloodSugar() != null) {
            label += " · " + context.getResources().getStringArray(R.array.meal_contexts)[record.getMealContext().ordinal()];
        }
        return label;
    }
}

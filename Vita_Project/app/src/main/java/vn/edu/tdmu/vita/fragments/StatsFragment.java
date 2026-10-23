package vn.edu.tdmu.vita.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.HealthDao;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.database.WaterDao;
import vn.edu.tdmu.vita.models.HealthRecord;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.HealthChartData;
import vn.edu.tdmu.vita.utils.HydrationGoal;
import vn.edu.tdmu.vita.utils.SessionManager;

public class StatsFragment extends Fragment {

    private LineChart chartBp;
    private LineChart chartWeight;
    private LineChart chartHeartRate;
    private LineChart chartGlucose;
    private BarChart chartWater;
    private TextView tvWaterGoal;
    private MaterialButtonToggleGroup toggleRange;
    private androidx.appcompat.app.AppCompatDialog dataScreen;

    private HealthDao healthDao;
    private WaterDao waterDao;
    private UserDao userDao;
    private SessionManager sessionManager;

    private int rangeDays = 7;
    private int selectedMetric, selectedContext;
    private android.widget.Spinner metricSelector, contextSelector;
    private final List<HealthRecord> displayedRecords = new ArrayList<>();
    private List<String> displayedDays = new ArrayList<>();
    private Map<String, Integer> displayedWater = java.util.Collections.emptyMap();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_stats_profile).setOnClickListener(v -> startActivity(
                new android.content.Intent(requireContext(), vn.edu.tdmu.vita.activities.ProfileActivity.class)));

        sessionManager = new SessionManager(requireContext());
        healthDao = new HealthDao(requireContext());
        waterDao = new WaterDao(requireContext());
        userDao = new UserDao(requireContext());

        chartBp = view.findViewById(R.id.chart_bp);
        chartWeight = view.findViewById(R.id.chart_weight);
        chartHeartRate = view.findViewById(R.id.chart_heart_rate);
        chartGlucose = view.findViewById(R.id.chart_glucose);
        chartWater = view.findViewById(R.id.chart_water);
        tvWaterGoal = view.findViewById(R.id.tv_water_goal);
        toggleRange = view.findViewById(R.id.toggle_time_range);
        if (savedInstanceState != null) {
            selectedMetric = savedInstanceState.getInt("metric", 0);
            selectedContext = savedInstanceState.getInt("context", 0);
        }
        metricSelector = view.findViewById(R.id.sp_stats_metric);
        contextSelector = view.findViewById(R.id.sp_stats_context);
        metricSelector.setAdapter(android.widget.ArrayAdapter.createFromResource(requireContext(), R.array.stats_metrics, android.R.layout.simple_spinner_dropdown_item));
        contextSelector.setAdapter(android.widget.ArrayAdapter.createFromResource(requireContext(), R.array.stats_contexts, android.R.layout.simple_spinner_dropdown_item));
        metricSelector.setSelection(selectedMetric);
        contextSelector.setSelection(selectedContext);
        android.widget.AdapterView.OnItemSelectedListener selected = new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View item, int position, long id) {
                selectedMetric = metricSelector.getSelectedItemPosition();
                selectedContext = contextSelector.getSelectedItemPosition();
                loadAll();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        };
        metricSelector.setOnItemSelectedListener(selected);
        contextSelector.setOnItemSelectedListener(selected);
        view.findViewById(R.id.btn_stats_data).setOnClickListener(v -> showData());
        view.findViewById(R.id.btn_stats_first_measurement).setOnClickListener(v -> {
            if (selectedMetric == 4) {
                vn.edu.tdmu.vita.utils.WaterJournalDialog.showAddAmount(requireActivity(), sessionManager.getUserId(), this::loadAll);
            } else {
                int[] groups = {3, 1, 2, 4};
                startActivity(new android.content.Intent(requireContext(), vn.edu.tdmu.vita.activities.AddHealthActivity.class)
                        .putExtra(vn.edu.tdmu.vita.activities.AddHealthActivity.EXTRA_METRIC_GROUP, groups[selectedMetric]));
            }
        });
        if (savedInstanceState != null) rangeDays = savedInstanceState.getInt("range_days", 7);
        toggleRange.check(rangeDays == 30 ? R.id.btn_range_30 : R.id.btn_range_7);

        toggleRange.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            rangeDays = (checkedId == R.id.btn_range_30) ? 30 : 7;
            loadCharts();
        });
        loadAll();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAll();
    }

    public void loadAll() {
        if (getView() == null || sessionManager == null) return;
        if (sessionManager.getUserId() <= 0) return;
        loadCharts();
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        state.putInt("range_days", rangeDays);
        state.putInt("metric", selectedMetric);
        state.putInt("context", selectedContext);
        super.onSaveInstanceState(state);
    }

    private List<String> lastDaysDb(int n) {
        List<String> days = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -(n - 1));
        for (int i = 0; i < n; i++) {
            days.add(String.format(Locale.US, "%04d-%02d-%02d",
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
                    c.get(Calendar.DAY_OF_MONTH)));
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        return days;
    }

    private void loadCharts() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) return;

        List<String> days = lastDaysDb(rangeDays);
        String start = days.get(0);
        String end = days.get(days.size() - 1);

        List<HealthRecord> records = healthDao.getRecordsRange(userId, start, end);
        displayedDays = days;
        displayedWater = selectedMetric == 4 ? waterDao.getDailyTotalsRange(userId, start, end)
                : java.util.Collections.emptyMap();
        displayedRecords.clear();
        for (HealthRecord record : records) if (matchesSelectedMetric(record)) displayedRecords.add(record);
        View[] cards = { (View) chartWeight.getParent().getParent(), (View) chartBp.getParent().getParent(),
                (View) chartHeartRate.getParent().getParent(), (View) chartGlucose.getParent().getParent(), (View) chartWater.getParent().getParent() };
        for (int i = 0; i < cards.length; i++) cards[i].setVisibility(i == selectedMetric ? View.VISIBLE : View.GONE);
        ((View) contextSelector.getParent()).setVisibility(selectedMetric == 3 ? View.VISIBLE : View.GONE);
        switch (selectedMetric) {
            case 0: drawMetricChart(chartWeight, displayedRecords, days, HealthChartData.Metric.WEIGHT); break;
            case 1: drawBpChart(displayedRecords, days); break;
            case 2: drawMetricChart(chartHeartRate, displayedRecords, days, HealthChartData.Metric.HEART_RATE); break;
            case 3: drawMetricChart(chartGlucose, displayedRecords, days, HealthChartData.Metric.BLOOD_SUGAR); break;
            case 4: drawWaterChart(userId, days); break;
        }
        String summary;
        TextView latestValue = requireView().findViewById(R.id.tv_stats_latest_value);
        latestValue.setText("—");
        TextView countBadge = requireView().findViewById(R.id.tv_stats_count);
        countBadge.setVisibility(selectedMetric == 0 && !displayedRecords.isEmpty() ? View.VISIBLE : View.GONE);
        countBadge.setText(getString(R.string.figma_stats_record_count, displayedRecords.size()));
        TextView chartNote = requireView().findViewById(R.id.tv_stats_chart_note);
        chartNote.setVisibility(selectedMetric == 1 || selectedMetric == 2 || selectedMetric == 4 ? View.VISIBLE : View.GONE);
        chartNote.setText(selectedMetric == 1 ? getString(R.string.figma_stats_bp_note)
                : selectedMetric == 4 ? tvWaterGoal.getText() : getString(R.string.figma_stats_point_note, displayedRecords.size()));
        tvWaterGoal.setVisibility(View.GONE);
        if (selectedMetric == 4) {
            summary = getString(R.string.health_result_count, displayedWater.size());
            for (int i = days.size() - 1; i >= 0; i--) if (displayedWater.containsKey(days.get(i))) {
                summary += "\n" + DateTimeUtils.formatDisplayDate(days.get(i));
                latestValue.setText(getString(R.string.water_amount_value, displayedWater.get(days.get(i))));
                break;
            }
        } else {
            int count = displayedRecords.size();
            if (count > 0) latestValue.setText(metricValue(displayedRecords.get(count - 1)));
            summary = count == 0 ? getString(R.string.stats_no_records) : vn.edu.tdmu.vita.utils.HealthDisplay.measurementLabel(requireContext(), displayedRecords.get(count - 1));
            if (selectedMetric == 1 || selectedMetric == 2) summary += "\n" + getString(R.string.figma_stats_record_count, count);
            if (count == 1 && selectedMetric != 0) summary += "\n" + getString(R.string.stats_one_record);
        }
        String latest = latestValue.getText().toString();
        int unitStart = latest.lastIndexOf(' ');
        ((TextView) requireView().findViewById(R.id.tv_stats_latest_unit)).setText(unitStart < 0 ? "" : latest.substring(unitStart + 1));
        if (unitStart >= 0) latestValue.setText(latest.substring(0, unitStart));
        ((TextView) requireView().findViewById(R.id.tv_stats_summary)).setText(summary);
        boolean empty = selectedMetric == 4 ? displayedWater.isEmpty() : displayedRecords.isEmpty();
        requireView().findViewById(R.id.layout_stats_empty).setVisibility(empty ? View.VISIBLE : View.GONE);
        requireView().findViewById(R.id.card_stats_latest).setVisibility(empty ? View.GONE : View.VISIBLE);
        requireView().findViewById(R.id.btn_stats_data).setVisibility(empty ? View.GONE : View.VISIBLE);
        ((TextView) requireView().findViewById(R.id.tv_stats_empty)).setText(selectedMetric == 0
                ? R.string.figma_stats_empty_weight : R.string.figma_stats_empty_metric);
        ((TextView) requireView().findViewById(R.id.btn_stats_first_measurement)).setText(selectedMetric == 0
                ? R.string.figma_stats_first_weight : selectedMetric == 4 ? R.string.figma_stats_first_water : R.string.figma_health_record);
        if (empty) {
            for (View card : cards) card.setVisibility(View.GONE);
            chartNote.setVisibility(View.GONE);
        }
        boolean oneWeight = selectedMetric == 0 && displayedRecords.size() == 1;
        ((TextView) requireView().findViewById(R.id.tv_stats_weight_chart_title)).setText(oneWeight
                ? R.string.figma_stats_one_title : R.string.figma_chart_weight);
        requireView().findViewById(R.id.tv_stats_weight_note).setVisibility(oneWeight ? View.VISIBLE : View.GONE);
    }

    private boolean matchesSelectedMetric(HealthRecord r) {
        switch (selectedMetric) {
            case 0: return r.getWeightKg() != null;
            case 1: return r.getSystolic() != null && r.getDiastolic() != null;
            case 2: return r.getHeartRate() != null;
            case 3: return r.getBloodSugar() != null && (selectedContext == 0
                    || r.getMealContext().ordinal() == (selectedContext == 4 ? 0 : selectedContext));
            default: return false;
        }
    }

    private String metricValue(HealthRecord r) {
        String value;
        switch (selectedMetric) {
            case 0: value = String.format(Locale.getDefault(), "%.1f kg", r.getWeightKg()); break;
            case 1: value = r.getSystolic() + " / " + r.getDiastolic() + " mmHg"; break;
            case 2: value = r.getHeartRate() + " bpm"; break;
            default: value = String.format(Locale.getDefault(), "%.0f mg/dL", r.getBloodSugar());
        }
        return value;
    }

    private CharSequence dataRow(String date, String value) {
        android.text.SpannableString row = new android.text.SpannableString(date + "\n" + value);
        row.setSpan(new android.text.style.AbsoluteSizeSpan(Math.round(android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()))), 0, date.length(), 0);
        row.setSpan(new android.text.style.ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.vita_text_secondary)), 0, date.length(), 0);
        row.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), date.length() + 1, row.length(), 0);
        return row;
    }

    private void showData() {
        List<HealthRecord> dataRecords = new ArrayList<>(displayedRecords);
        java.util.Collections.reverse(dataRecords);
        List<String> dataDays = new ArrayList<>(displayedDays);
        java.util.Collections.reverse(dataDays);
        List<CharSequence> labels = new ArrayList<>();
        if (selectedMetric == 4) {
            for (String day : dataDays) labels.add(dataRow(DateTimeUtils.formatDisplayDate(day),
                    (displayedWater.containsKey(day) ? displayedWater.get(day) + " ml" : getString(R.string.stats_unrecorded))
                            + (day.equals(DateTimeUtils.getTodayDate()) ? " · " + getString(R.string.stats_today_ongoing) : "")));
        } else for (HealthRecord r : dataRecords) labels.add(dataRow(
                vn.edu.tdmu.vita.utils.HealthDisplay.measurementLabel(requireContext(), r).replace(DateTimeUtils.formatDisplayDate(r.getDate()), shortDate(r.getDate())), metricValue(r)));
        if (labels.isEmpty()) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setMessage(R.string.stats_no_records)
                    .setPositiveButton(R.string.action_close_short, null).show();
            return;
        }
        dataScreen = new androidx.appcompat.app.AppCompatDialog(requireContext(), R.style.Theme_Vita);
        dataScreen.setContentView(R.layout.dialog_stats_data);
        com.google.android.material.appbar.MaterialToolbar dataToolbar = dataScreen.findViewById(R.id.toolbar_stats_data);
        ((TextView) dataScreen.findViewById(R.id.tv_page_title)).setText(getString(R.string.figma_stats_data_title,
                metricSelector.getSelectedItem().toString().toLowerCase(Locale.getDefault())));
        dataToolbar.setNavigationOnClickListener(v -> dataScreen.dismiss());
        ((TextView) dataScreen.findViewById(R.id.tv_stats_data_period)).setText(getString(selectedMetric == 4
                        ? R.string.figma_stats_water_data_period : R.string.figma_stats_data_period,
                shortDate(displayedDays.get(0)),
                DateTimeUtils.formatDisplayDate(displayedDays.get(displayedDays.size() - 1)), labels.size()));
        android.widget.ListView list = dataScreen.findViewById(R.id.list_stats_data);
        list.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), R.layout.item_stats_data,
                R.id.tv_stats_data_item, labels));
        list.setOnItemClickListener((parent, row, position, id) -> {
                    dataScreen.dismiss();
                    if (selectedMetric == 4) vn.edu.tdmu.vita.utils.WaterJournalDialog.show(requireActivity(), sessionManager.getUserId(), dataDays.get(position), this::loadAll);
                    else startActivity(new android.content.Intent(requireContext(), vn.edu.tdmu.vita.activities.DetailHealthActivity.class)
                            .putExtra(vn.edu.tdmu.vita.activities.DetailHealthActivity.EXTRA_RECORD_ID, dataRecords.get(position).getId()));
                });
        dataScreen.show();
        dataScreen.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override public void onDestroyView() {
        if (dataScreen != null) dataScreen.dismiss();
        dataScreen = null;
        super.onDestroyView();
    }

    private String shortDate(String dbDate) {
        String display = DateTimeUtils.formatDisplayDate(dbDate);
        return display.length() >= 5 ? display.substring(0, 5) : display;
    }

    private List<String> dayLabels(List<String> daysDb) {
        List<String> labels = new ArrayList<>();
        for (String d : daysDb) {
            labels.add(shortDate(d));
        }
        return labels;
    }

    private void drawMetricChart(LineChart chart, List<HealthRecord> records,
                                 List<String> days, HealthChartData.Metric metric) {
        styleLineChart(requireContext(), chart);
        chart.setNoDataText(getString(R.string.stats_no_records));
        List<HealthChartData.Point> points = HealthChartData.metricPoints(records, days, metric);
        List<Entry> entries = new ArrayList<>();
        for (HealthChartData.Point point : points) {
            entries.add(new Entry(point.x, point.value,
                    vn.edu.tdmu.vita.utils.HealthDisplay.measurementLabel(requireContext(), point.record)));
        }
        if (entries.isEmpty()) {
            chart.clear();
            return;
        }
        boolean weight = metric == HealthChartData.Metric.WEIGHT;
        boolean glucose = metric == HealthChartData.Metric.BLOOD_SUGAR;
        String unit = weight ? " kg" : glucose ? " mg/dL" : " bpm";
        LineDataSet data = new LineDataSet(entries,
                getString(weight ? R.string.stats_weight_title : glucose ? R.string.stats_glucose_title : R.string.stats_heart_rate_title));
        int color = ContextCompat.getColor(requireContext(),
                weight ? R.color.vita_teal : R.color.vita_status_danger);
        data.setColor(color);
        data.setCircleColor(color);
        data.setLineWidth(2f);
        data.setCircleRadius(4f);
        data.setDrawValues(false);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dayLabels(days)));
        chart.getXAxis().setAxisMinimum(0f);
        chart.getXAxis().setAxisMaximum(days.size());
        chart.getXAxis().setDrawLabels(entries.size() != 1);
        if (entries.size() == 1) {
            chart.getXAxis().setAxisMinimum(entries.get(0).getX() - 1f);
            chart.getXAxis().setAxisMaximum(entries.get(0).getX() + 1f);
        }
        chart.setMarker(new ChartMarker(requireContext(), dayLabels(days), unit));
        chart.setData(new LineData(data));
        if (glucose) {
            List<com.github.mikephil.charting.interfaces.datasets.ILineDataSet> groups = new ArrayList<>();
            int[] colors = {R.color.vita_text_secondary, R.color.vita_water, R.color.vita_status_warning, R.color.vita_teal};
            for (HealthRecord.MealContext context : HealthRecord.MealContext.values()) {
                List<Entry> values = new ArrayList<>();
                for (HealthChartData.Point point : points)
                    if (point.record.getMealContext() == context) values.add(new Entry(point.x, point.value,
                            vn.edu.tdmu.vita.utils.HealthDisplay.measurementLabel(requireContext(), point.record)));
                if (values.isEmpty()) continue;
                LineDataSet group = new LineDataSet(values, getResources().getStringArray(R.array.meal_contexts)[context.ordinal()]);
                int groupColor = ContextCompat.getColor(requireContext(), colors[context.ordinal()]);
                group.setColor(groupColor); group.setCircleColor(groupColor); group.setCircleRadius(4f); group.setDrawValues(false);
                group.setLineWidth(2f);
                if (context == HealthRecord.MealContext.UNSPECIFIED) group.setColor(Color.TRANSPARENT);
                else if (context == HealthRecord.MealContext.BEFORE_MEAL) group.enableDashedLine(8f, 5f, 0f);
                else if (context == HealthRecord.MealContext.AFTER_MEAL) group.enableDashedLine(2f, 5f, 0f);
                groups.add(group);
            }
            chart.setData(new LineData(groups));
            chart.getLegend().setEnabled(true);
        }
        chart.invalidate();
    }

    private void drawBpChart(List<HealthRecord> records, List<String> days) {
        Context context = requireContext();
        List<Entry> sysEntries = new ArrayList<>();
        List<Entry> diaEntries = new ArrayList<>();
        List<String> labels = dayLabels(days);
        for (HealthRecord r : records) {
            if (r.getSystolic() == null || r.getDiastolic() == null) continue;
            int x = days.indexOf(r.getDate());
            if (x < 0) continue;
            float position = HealthChartData.measurementX(r, x);
            String label = vn.edu.tdmu.vita.utils.HealthDisplay.measurementLabel(context, r);
            sysEntries.add(new Entry(position, r.getSystolic(), label));
            diaEntries.add(new Entry(position, r.getDiastolic(), label));
        }

        styleLineChart(context, chartBp);
        chartBp.setNoDataText(getString(R.string.stats_no_records));
        if (sysEntries.isEmpty()) {
            chartBp.clear();
            chartBp.invalidate();
            return;
        }

        int sysColor = ContextCompat.getColor(context, R.color.vita_teal);
        int diaColor = ContextCompat.getColor(context, R.color.vita_water);

        LineDataSet sysSet = new LineDataSet(sysEntries, getString(R.string.stats_bp_sys_label));
        sysSet.setColor(sysColor);
        sysSet.setForm(com.github.mikephil.charting.components.Legend.LegendForm.LINE);
        sysSet.setCircleColor(sysColor);
        sysSet.setCircleRadius(4f);
        sysSet.setLineWidth(2.5f);
        sysSet.setDrawValues(false);
        sysSet.setMode(LineDataSet.Mode.LINEAR);

        LineDataSet diaSet = new LineDataSet(diaEntries, getString(R.string.stats_bp_dia_label));
        diaSet.setColor(diaColor);
        diaSet.setCircleColor(diaColor);
        diaSet.setCircleRadius(5f);
        diaSet.setLineWidth(2f);
        diaSet.setDrawValues(false);
        diaSet.setForm(com.github.mikephil.charting.components.Legend.LegendForm.LINE);
        diaSet.enableDashedLine(10f, 5f, 0f);
        diaSet.setFormLineDashEffect(diaSet.getDashPathEffect()); // nét đứt cho người mù màu
        diaSet.setMode(LineDataSet.Mode.LINEAR);

        chartBp.getXAxis().setAxisMinimum(0f);
        chartBp.getXAxis().setAxisMaximum(days.size());
        chartBp.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartBp.setMarker(new ChartMarker(context, labels, " mmHg"));
        chartBp.getLegend().setEnabled(true);
        chartBp.getLegend().setWordWrapEnabled(true);
        chartBp.setData(new LineData(sysSet, diaSet));
        chartBp.animateY(800);
        chartBp.invalidate();
    }

    private void drawWaterChart(long userId, List<String> days) {
        Context context = requireContext();
        Map<String, Integer> totals = displayedWater;

        User user = userDao.getUserById(userId);
        int goal = HydrationGoal.forUserGoal(user != null ? user.getWaterGoalMl() : 0);
        tvWaterGoal.setText(String.format(Locale.getDefault(),
                getString(R.string.stats_water_reference), goal));

        List<BarEntry> entries = new ArrayList<>();
        boolean hasData = false;
        for (int i = 0; i < days.size(); i++) {
            if (!totals.containsKey(days.get(i))) continue;
            int total = totals.get(days.get(i));
            if (total > 0) hasData = true;
            entries.add(new BarEntry(i, total));
        }

        styleChart(context, chartWater);
        chartWater.getAxisLeft().setAxisMinimum(0f);
        chartWater.setNoDataText(getString(R.string.stats_not_enough_data));
        if (!hasData) {
            chartWater.clear();
            chartWater.invalidate();
            return;
        }

        BarDataSet set = new BarDataSet(entries, "ml");
        set.setColor(ContextCompat.getColor(context, R.color.vita_water));
        set.setDrawValues(false);

        LimitLine goalLine = new LimitLine(goal);
        goalLine.setLineColor(ContextCompat.getColor(context, R.color.vita_teal));
        goalLine.enableDashedLine(8f, 6f, 0f);
        chartWater.getAxisLeft().removeAllLimitLines();
        chartWater.getAxisLeft().addLimitLine(goalLine);

        chartWater.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dayLabels(days)));
        chartWater.setMarker(new ChartMarker(context, dayLabels(days), " ml"));
        chartWater.setData(new BarData(set));
        chartWater.animateY(800);
        chartWater.invalidate();
    }

    private void styleLineChart(Context context, LineChart chart) {
        styleChart(context, chart);
        chart.getLegend().setTextColor(ContextCompat.getColor(context, R.color.vita_text_primary));
        chart.getLegend().setTextSize(12f * getResources().getConfiguration().fontScale);
        chart.getLegend().setWordWrapEnabled(true);
        chart.getAxisLeft().setLabelCount(3, false);
        chart.getXAxis().setLabelCount(3, false);
    }

    private void styleChart(Context context, BarLineChartBase<?> chart) {
        chart.getDescription().setEnabled(false);
        chart.getLayoutParams().height = Math.round(180 * getResources().getDisplayMetrics().density * getResources().getConfiguration().fontScale);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setNoDataTextColor(ContextCompat.getColor(context, R.color.vita_text_secondary));
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(context, R.color.vita_text_secondary));
        chart.getAxisLeft().setTextSize(12f * getResources().getConfiguration().fontScale);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setGridColor(ContextCompat.getColor(context, R.color.vita_divider));
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(context, R.color.vita_text_secondary));
        xAxis.setTextSize(12f * getResources().getConfiguration().fontScale);
        chart.getLegend().setEnabled(false);
    }

    static class ChartMarker extends MarkerView {
        private final TextView tvText;
        private final List<String> labels;
        private final String unit;

        ChartMarker(Context context, List<String> labels, String unit) {
            super(context, R.layout.marker_chart);
            this.labels = labels;
            this.unit = unit;
            tvText = findViewById(R.id.tv_marker_text);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            int x = (int) e.getX();
            String label = (x >= 0 && x < labels.size()) ? labels.get(x) : "";
            if (e.getData() instanceof String) label = (String) e.getData();
            tvText.setText(getContext().getString(R.string.chart_marker_value, label, String.valueOf(e.getY()), unit));
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2f), -getHeight());
        }
    }
}

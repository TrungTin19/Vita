package vn.edu.tdmu.vita.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.activities.AddSleepActivity;
import vn.edu.tdmu.vita.adapters.SleepRecordAdapter;
import vn.edu.tdmu.vita.database.HealthDao;
import vn.edu.tdmu.vita.database.SleepDao;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.database.WaterDao;
import vn.edu.tdmu.vita.models.HealthRecord;
import vn.edu.tdmu.vita.models.SleepLog;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.SessionManager;

public class StatsFragment extends Fragment {

    private LineChart chartBp;
    private BarChart chartWater;
    private BarChart chartSleep;
    private TextView tvWaterGoal;
    private TextView tvSleepEmpty;
    private RecyclerView rvSleepRecords;
    private FloatingActionButton fabAddSleep;
    private MaterialButtonToggleGroup toggleRange;

    private HealthDao healthDao;
    private WaterDao waterDao;
    private SleepDao sleepDao;
    private UserDao userDao;
    private SessionManager sessionManager;

    private List<SleepLog> sleepLogs;
    private SleepRecordAdapter sleepAdapter;

    private int rangeDays = 7;

    private ActivityResultLauncher<Intent> addSleepLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSleepLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> loadAll()
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        healthDao = new HealthDao(requireContext());
        waterDao = new WaterDao(requireContext());
        sleepDao = new SleepDao(requireContext());
        userDao = new UserDao(requireContext());

        chartBp = view.findViewById(R.id.chart_bp);
        chartWater = view.findViewById(R.id.chart_water);
        chartSleep = view.findViewById(R.id.chart_sleep);
        tvWaterGoal = view.findViewById(R.id.tv_water_goal);
        tvSleepEmpty = view.findViewById(R.id.tv_sleep_empty);
        rvSleepRecords = view.findViewById(R.id.rv_sleep_records);
        fabAddSleep = view.findViewById(R.id.fab_add_sleep);
        toggleRange = view.findViewById(R.id.toggle_time_range);

        sleepLogs = new ArrayList<>();
        sleepAdapter = new SleepRecordAdapter(sleepLogs);
        rvSleepRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSleepRecords.setAdapter(sleepAdapter);

        toggleRange.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            rangeDays = (checkedId == R.id.btn_range_30) ? 30 : 7;
            loadCharts();
        });
        fabAddSleep.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddSleepActivity.class);
            addSleepLauncher.launch(intent);
        });

        loadAll();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAll();
    }

    private void loadAll() {
        if (sessionManager.getUserId() <= 0) return;
        loadCharts();
        loadSleepList();
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

        drawBpChart(userId, start, end);
        drawWaterChart(userId, days);
        drawSleepChart(userId, days);
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

    private void drawBpChart(long userId, String start, String end) {
        Context context = requireContext();
        List<HealthRecord> records = healthDao.getRecordsRange(userId, start, end);

        List<Entry> sysEntries = new ArrayList<>();
        List<Entry> diaEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (HealthRecord r : records) {
            if (r.getSystolic() == null || r.getDiastolic() == null) continue;
            int x = labels.size();
            sysEntries.add(new Entry(x, r.getSystolic()));
            diaEntries.add(new Entry(x, r.getDiastolic()));
            labels.add(shortDate(r.getDate()));
        }

        styleLineChart(context, chartBp);
        chartBp.setNoDataText(getString(R.string.stats_not_enough_data));
        if (sysEntries.size() < 2) {
            chartBp.clear();
            chartBp.invalidate();
            return;
        }

        int sysColor = ContextCompat.getColor(context, R.color.vita_status_danger);
        int diaColor = ContextCompat.getColor(context, R.color.vita_water);

        LineDataSet sysSet = new LineDataSet(sysEntries, getString(R.string.stats_bp_sys_label));
        sysSet.setColor(sysColor);
        sysSet.setCircleColor(sysColor);
        sysSet.setCircleRadius(4f);
        sysSet.setLineWidth(2.5f);
        sysSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet diaSet = new LineDataSet(diaEntries, getString(R.string.stats_bp_dia_label));
        diaSet.setColor(diaColor);
        diaSet.setCircleColor(diaColor);
        diaSet.setCircleRadius(5f);
        diaSet.setLineWidth(2f);
        diaSet.enableDashedLine(10f, 5f, 0f); // nét đứt cho người mù màu
        diaSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LimitLine sysLimit = new LimitLine(120f);
        sysLimit.setLineColor(sysColor);
        sysLimit.enableDashedLine(8f, 6f, 0f);
        LimitLine diaLimit = new LimitLine(80f);
        diaLimit.setLineColor(diaColor);
        diaLimit.enableDashedLine(8f, 6f, 0f);
        chartBp.getAxisLeft().removeAllLimitLines();
        chartBp.getAxisLeft().addLimitLine(sysLimit);
        chartBp.getAxisLeft().addLimitLine(diaLimit);

        chartBp.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartBp.setMarker(new ChartMarker(context, labels, " mmHg"));
        chartBp.setData(new LineData(sysSet, diaSet));
        chartBp.animateY(800);
        chartBp.invalidate();
    }

    private void drawWaterChart(long userId, List<String> days) {
        Context context = requireContext();
        Map<String, Integer> totals = waterDao.getDailyTotalsRange(userId, days.get(0), days.get(days.size() - 1));

        User user = userDao.getUserById(userId);
        int goal = (user != null && user.getWaterGoalMl() > 0) ? user.getWaterGoalMl() : 2000;
        tvWaterGoal.setText(String.format(Locale.getDefault(),
                getString(R.string.stats_water_goal_format), goal));

        List<BarEntry> entries = new ArrayList<>();
        boolean hasData = false;
        for (int i = 0; i < days.size(); i++) {
            int total = totals.containsKey(days.get(i)) ? totals.get(days.get(i)) : 0;
            if (total > 0) hasData = true;
            entries.add(new BarEntry(i, total));
        }

        styleBarChart(context, chartWater);
        chartWater.setNoDataText(getString(R.string.stats_not_enough_data));
        if (!hasData) {
            chartWater.clear();
            chartWater.invalidate();
            return;
        }

        BarDataSet set = new BarDataSet(entries, "ml");
        set.setColor(ContextCompat.getColor(context, R.color.vita_water));

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

    private void drawSleepChart(long userId, List<String> days) {
        Context context = requireContext();
        List<SleepLog> logs = sleepDao.getLogsRange(userId, days.get(0), days.get(days.size() - 1));
        Map<String, Float> byDate = new HashMap<>();
        for (SleepLog log : logs) {
            byDate.put(log.getDate(), log.getDurationH());
        }

        List<BarEntry> entries = new ArrayList<>();
        boolean hasData = false;
        for (int i = 0; i < days.size(); i++) {
            float h = byDate.containsKey(days.get(i)) ? byDate.get(days.get(i)) : 0f;
            if (h > 0) hasData = true;
            entries.add(new BarEntry(i, h));
        }

        styleBarChart(context, chartSleep);
        chartSleep.setNoDataText(getString(R.string.stats_not_enough_data));
        if (!hasData) {
            chartSleep.clear();
            chartSleep.invalidate();
            return;
        }

        BarDataSet set = new BarDataSet(entries, getString(R.string.stats_sleep_chart_title));
        set.setColor(ContextCompat.getColor(context, R.color.vita_sleep));

        chartSleep.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dayLabels(days)));
        chartSleep.setMarker(new ChartMarker(context, dayLabels(days), " giờ"));
        chartSleep.setData(new BarData(set));
        chartSleep.animateY(800);
        chartSleep.invalidate();
    }

    private void styleLineChart(Context context, LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(context, R.color.vita_text_secondary));
        chart.getAxisLeft().setTextSize(10f);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(context, R.color.vita_text_secondary));
        xAxis.setTextSize(10f);
        chart.getLegend().setTextColor(ContextCompat.getColor(context, R.color.vita_text_primary));
    }

    private void styleBarChart(Context context, BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(context, R.color.vita_text_secondary));
        chart.getAxisLeft().setTextSize(10f);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(context, R.color.vita_text_secondary));
        xAxis.setTextSize(10f);
        chart.getLegend().setEnabled(false);
    }

    private void loadSleepList() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) return;

        sleepLogs.clear();
        sleepLogs.addAll(sleepDao.getLatest7Days(userId));
        sleepAdapter.notifyDataSetChanged();

        if (sleepLogs.isEmpty()) {
            rvSleepRecords.setVisibility(View.GONE);
            tvSleepEmpty.setVisibility(View.VISIBLE);
        } else {
            rvSleepRecords.setVisibility(View.VISIBLE);
            tvSleepEmpty.setVisibility(View.GONE);
        }
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
            tvText.setText(label + ": " + e.getY() + unit);
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2f), -getHeight());
        }
    }
}

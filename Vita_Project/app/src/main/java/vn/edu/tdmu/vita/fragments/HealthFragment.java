package vn.edu.tdmu.vita.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import com.google.android.material.datepicker.MaterialDatePicker;

import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.activities.AddHealthActivity;
import vn.edu.tdmu.vita.activities.DetailHealthActivity;
import vn.edu.tdmu.vita.adapters.HealthRecordAdapter;
import vn.edu.tdmu.vita.database.HealthDao;
import vn.edu.tdmu.vita.models.HealthRecord;
import vn.edu.tdmu.vita.utils.DateTimeUtils;
import vn.edu.tdmu.vita.utils.SessionManager;

public class HealthFragment extends Fragment {

    private RecyclerView rvHealthRecords;
    private LinearLayout layoutHealthEmpty;
    private MaterialButton btnFilterDate;
    private MaterialButton btnClearFilter;
    private FloatingActionButton fabAddHealth;

    private HealthDao healthDao;
    private SessionManager sessionManager;

    private List<HealthRecord> records;
    private HealthRecordAdapter adapter;

    private String startDate;
    private String endDate;

    private ActivityResultLauncher<Intent> addHealthLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addHealthLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Reload data when returning from Add/Edit
                    loadData();
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        healthDao = new HealthDao(requireContext());

        initViews(view);
        setupRecyclerView();
        setupListeners();
        if (savedInstanceState != null) {
            startDate = savedInstanceState.getString("filter_start");
            endDate = savedInstanceState.getString("filter_end");
        }
        renderFilter();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews(View view) {
        rvHealthRecords = view.findViewById(R.id.rv_health_records);
        layoutHealthEmpty = view.findViewById(R.id.layout_health_empty);
        btnFilterDate = view.findViewById(R.id.btn_health_filter_date);
        btnClearFilter = view.findViewById(R.id.btn_health_clear_filter);
        fabAddHealth = view.findViewById(R.id.fab_add_health);
    }

    private void setupRecyclerView() {
        records = new ArrayList<>();
        adapter = new HealthRecordAdapter(records);

        adapter.setOnItemClickListener((record, position) -> {
            Intent intent = new Intent(requireContext(), DetailHealthActivity.class);
            intent.putExtra(DetailHealthActivity.EXTRA_RECORD_ID, record.getId());
            addHealthLauncher.launch(intent);
        });

        rvHealthRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHealthRecords.setAdapter(adapter);

        registerForContextMenu(rvHealthRecords);
    }

    private void setupListeners() {
        btnFilterDate.setOnClickListener(v -> showDateFilterPicker());

        btnClearFilter.setOnClickListener(v -> {
            startDate = endDate = null;
            renderFilter();
            loadData();
        });

        fabAddHealth.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddHealthActivity.class);
            addHealthLauncher.launch(intent);
        });
    }

    private void showDateFilterPicker() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText(R.string.health_filter_range_title).build();
        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            startDate = format.format(new Date(selection.first));
            endDate = format.format(new Date(selection.second));
            renderFilter();
            loadData();
        });
        picker.show(getParentFragmentManager(), "health_date_range");
    }

    private void renderFilter() {
        boolean filtered = startDate != null && endDate != null;
        btnFilterDate.setText(filtered
                ? getString(R.string.health_filter_range_format,
                        DateTimeUtils.formatDisplayDate(startDate), DateTimeUtils.formatDisplayDate(endDate))
                : getString(R.string.health_filter_all_dates));
        btnClearFilter.setVisibility(filtered ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("filter_start", startDate);
        outState.putString("filter_end", endDate);
    }

    private void loadData() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) return;

        records.clear();
        if (startDate != null && endDate != null) {
            records.addAll(healthDao.getRecordsRange(userId, startDate, endDate));
            Collections.reverse(records);
        } else {
            records.addAll(healthDao.getAllByUserId(userId, null));
        }
        adapter.notifyDataSetChanged();

        updateEmptyState();
    }

    private void updateEmptyState() {
        if (records.isEmpty()) {
            rvHealthRecords.setVisibility(View.GONE);
            layoutHealthEmpty.setVisibility(View.VISIBLE);
        } else {
            rvHealthRecords.setVisibility(View.VISIBLE);
            layoutHealthEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = adapter.getContextMenuPosition();
        if (position < 0 || position >= records.size()) {
            return super.onContextItemSelected(item);
        }

        HealthRecord record = records.get(position);
        int itemId = item.getItemId();

        if (itemId == R.id.menu_health_edit) {
            // Open AddHealthActivity in edit mode
            Intent intent = new Intent(requireContext(), AddHealthActivity.class);
            intent.putExtra(AddHealthActivity.EXTRA_RECORD_ID, record.getId());
            addHealthLauncher.launch(intent);
            return true;
        } else if (itemId == R.id.menu_health_delete) {
            showDeleteConfirmation(record, position);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void showDeleteConfirmation(HealthRecord record, int position) {
        String dateDisplay = DateTimeUtils.formatDisplayDate(record.getDate());
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.health_delete_dialog_title))
                .setMessage(getString(R.string.health_delete_dialog_msg, dateDisplay))
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .setPositiveButton(getString(R.string.btn_delete), (dialog, which) -> {
                    boolean deleted = healthDao.delete(record.getId());
                    if (deleted) {
                        records.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, records.size() - position);
                        updateEmptyState();
                        Toast.makeText(requireContext(), getString(R.string.health_delete_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.health_delete_error), Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}

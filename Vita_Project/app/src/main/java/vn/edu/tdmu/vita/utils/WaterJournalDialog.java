package vn.edu.tdmu.vita.utils;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.List;
import java.util.Locale;
import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.database.WaterDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.models.WaterLog;
import vn.edu.tdmu.vita.receivers.WaterReminderReceiver;

/** Dùng chung nhật ký và biểu mẫu nhập; tham số cùng nội dung đang nhập được giữ khi Activity tạo lại. */
public class WaterJournalDialog extends DialogFragment {
    public interface Host { void onWaterJournalChanged(); }
    private Runnable callback;
    private TextInputEditText amountField;
    private String selectedTime;

    public static void show(FragmentActivity activity, long userId, String date, Runnable changed) {
        WaterJournalDialog dialog = create(userId, date, 0, 0, null);
        dialog.callback = changed;
        dialog.show(activity.getSupportFragmentManager(), "water_journal");
    }

    public static void showAddAmount(FragmentActivity activity, long userId, Runnable changed) {
        User user = new UserDao(activity).getUserById(userId);
        WaterJournalDialog dialog = create(userId, DateTimeUtils.getTodayDate(), -1,
                user == null ? 250 : user.getWaterAmount(), DateTimeUtils.getCurrentTime());
        dialog.callback = changed;
        dialog.show(activity.getSupportFragmentManager(), "water_entry");
    }

    private static WaterJournalDialog create(long user, String date, long id, int amount, String time) {
        WaterJournalDialog dialog = new WaterJournalDialog();
        Bundle args = new Bundle(); args.putLong("user", user); args.putString("date", date);
        args.putLong("id", id); args.putInt("amount", amount); args.putString("time", time);
        dialog.setArguments(args); return dialog;
    }

    private boolean ownsSession() {
        return SessionManager.matchesUser(requireArguments().getLong("user"), new SessionManager(requireContext()).getUserId());
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        Bundle args = requireArguments();
        if (!ownsSession()) return new AlertDialog.Builder(requireContext()).setMessage(R.string.water_entry_error)
                .setPositiveButton(R.string.action_close_short, null).create();
        if (args.getLong("id") == 0) return journal();
        return entry(state);
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private android.widget.TextView text(String value, int size, int color, boolean bold) {
        android.widget.TextView view = new android.widget.TextView(requireContext());
        view.setText(value); view.setTextSize(size); view.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), color));
        view.setIncludeFontPadding(false);
        androidx.core.widget.TextViewCompat.setLineHeight(view, Math.round(view.getTextSize() * 1.4f));
        view.setMinHeight(Math.round(view.getTextSize() * 1.4f));
        view.setGravity(android.view.Gravity.CENTER_VERTICAL);
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private void rounded(android.view.View view, int color, int radius) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor(androidx.core.content.ContextCompat.getColor(requireContext(), color)); shape.setCornerRadius(dp(radius));
        view.setBackground(shape);
    }

    private LinearLayout page(String title) {
        LinearLayout root = new LinearLayout(requireContext()); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vita_bg));
        com.google.android.material.appbar.MaterialToolbar toolbar = (com.google.android.material.appbar.MaterialToolbar)
                getLayoutInflater().inflate(R.layout.view_page_toolbar, root, false);
        ((android.widget.TextView) toolbar.findViewById(R.id.tv_page_title)).setText(title);
        toolbar.setNavigationOnClickListener(v -> dismiss()); root.addView(toolbar);
        android.widget.ScrollView scroll = new android.widget.ScrollView(requireContext());
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout body = new LinearLayout(requireContext()); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(24), dp(8), dp(24), dp(24));
        scroll.addView(body); root.setTag(body); return root;
    }

    private Dialog fullPage(LinearLayout root) {
        Dialog dialog = new Dialog(requireContext()); dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE); dialog.setContentView(root);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vita_bg));
            dialog.getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vita_surface));
        }
        return dialog;
    }

    private com.google.android.material.button.MaterialButton button(String label, boolean primary) {
        com.google.android.material.button.MaterialButton button = new com.google.android.material.button.MaterialButton(requireContext());
        button.setText(label); button.setTextSize(16); button.setAllCaps(false); button.setCornerRadius(dp(12));
        button.setInsetTop(0); button.setInsetBottom(0);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), primary ? R.color.vita_teal : R.color.vita_mint)));
        button.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), primary ? R.color.white : R.color.vita_teal));
        button.setMinHeight(dp(52));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2); params.topMargin = dp(16); button.setLayoutParams(params); return button;
    }

    private Dialog journal() { return fullPage(journalContent()); }

    private LinearLayout journalContent() {
        long user = requireArguments().getLong("user"); String date = requireArguments().getString("date");
        WaterDao dao = new WaterDao(requireContext()); List<WaterLog> logs = dao.getLogsByDate(user, date);
        User profile = new UserDao(requireContext()).getUserById(user);
        int goal = HydrationGoal.forUserGoal(profile == null ? 0 : profile.getWaterGoalMl()); int total = dao.getDailyTotal(user, date);
        LinearLayout root = page(getString(R.string.figma_water_journal)); LinearLayout body = (LinearLayout) root.getTag();
        android.widget.TextView dateLabel = text((DateTimeUtils.getTodayDate().equals(date) ? getString(R.string.figma_today_prefix) : "") + DateTimeUtils.formatDisplayDate(date), 18, R.color.vita_text_primary, true);
        body.addView(dateLabel);
        LinearLayout card = new LinearLayout(requireContext()); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(20), dp(20), dp(20), dp(20)); rounded(card, R.color.vita_water_bg, 20);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2); cardParams.topMargin = dp(16); body.addView(card, cardParams);
        LinearLayout titleRow = new LinearLayout(requireContext()); titleRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        android.widget.ImageView titleIcon = new android.widget.ImageView(requireContext()); titleIcon.setImageResource(R.drawable.figma_ic_water);
        LinearLayout.LayoutParams iconSize = new LinearLayout.LayoutParams(dp(24), dp(24)); iconSize.rightMargin = dp(10); titleRow.addView(titleIcon, iconSize);
        titleRow.addView(text(getString(R.string.home_water_title), 18, R.color.vita_text_primary, true)); card.addView(titleRow);
        boolean largeText = getResources().getConfiguration().fontScale > 1.3f;
        LinearLayout totals = new LinearLayout(requireContext()); totals.setOrientation(largeText ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL); totals.setGravity(android.view.Gravity.CENTER_VERTICAL); totals.setPadding(0, dp(8), 0, dp(16)); card.addView(totals);
        totals.addView(text(String.format(Locale.getDefault(), "%,d", total), 40, R.color.vita_water, true), new LinearLayout.LayoutParams(largeText ? -1 : 0, -2, largeText ? 0 : 16));
        totals.addView(text(getString(R.string.figma_home_goal, goal, Math.max(0, goal - total)), 14, R.color.vita_text_secondary, false), new LinearLayout.LayoutParams(largeText ? -1 : 0, -2, largeText ? 0 : 11));
        android.widget.ProgressBar progress = new android.widget.ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(goal); progress.setProgress(Math.min(total, goal)); progress.setProgressDrawable(androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.progress_water)); card.addView(progress, new LinearLayout.LayoutParams(-1, dp(8)));
        android.widget.TextView percent = text(getString(R.string.figma_water_percent, Math.round(total * 100f / goal)), 12, R.color.vita_water, false); percent.setPadding(0, dp(12), 0, 0); card.addView(percent);
        if (DateTimeUtils.getTodayDate().equals(date)) {
            LinearLayout actions = new LinearLayout(requireContext()); actions.setOrientation(largeText ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL); card.addView(actions);
            com.google.android.material.button.MaterialButton quick = button(getString(R.string.figma_home_quick_water, profile == null ? 250 : profile.getWaterAmount()), true);
            com.google.android.material.button.MaterialButton other = button(getString(R.string.figma_home_custom), false);
            LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(largeText ? -1 : 0, -2, largeText ? 0 : 1); left.topMargin = dp(12); left.rightMargin = largeText ? 0 : dp(8); actions.addView(quick, left);
            LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(largeText ? -1 : 0, -2, largeText ? 0 : 1); right.topMargin = dp(12); actions.addView(other, right);
            quick.setOnClickListener(v -> addQuickWater(requireActivity(), user, callback));
            other.setOnClickListener(v -> showAddAmount(requireActivity(), user, callback));
        }
        LinearLayout countRow = new LinearLayout(requireContext()); countRow.setGravity(android.view.Gravity.CENTER_VERTICAL); countRow.setPadding(0, dp(20), 0, dp(16)); body.addView(countRow);
        countRow.addView(text(getString(R.string.figma_water_count, logs.size()), 18, R.color.vita_text_primary, true), new LinearLayout.LayoutParams(0, -2, 1));
        countRow.addView(text(String.format(Locale.getDefault(), "%,d ml", total), 13, R.color.vita_teal, false));
        if (logs.isEmpty()) body.addView(text(getString(R.string.water_journal_empty), 14, R.color.vita_text_secondary, false));
        for (WaterLog log : logs) {
            LinearLayout row = new LinearLayout(requireContext()); row.setGravity(android.view.Gravity.CENTER_VERTICAL); row.setPadding(dp(20), 0, dp(20), 0); rounded(row, R.color.vita_surface, 20);
            android.widget.ImageView icon = new android.widget.ImageView(requireContext()); icon.setImageResource(R.drawable.figma_ic_water); icon.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vita_water)); row.addView(icon, new LinearLayout.LayoutParams(dp(22), dp(22)));
            android.widget.TextView time = text(clock(log.getLoggedAt()), 14, R.color.vita_text_primary, true); time.setPadding(dp(16), 0, 0, 0); row.addView(time, new LinearLayout.LayoutParams(0, -2, 1));
            row.addView(text(getString(R.string.figma_water_row_amount, log.getAmountMl()), 16, R.color.vita_water, true));
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, dp(64)); rowParams.bottomMargin = dp(16); body.addView(row, rowParams);
            row.setFocusable(true); row.setOnClickListener(v -> { FragmentActivity activity = requireActivity(); WaterJournalDialog edit = create(user, date, log.getId(), log.getAmountMl(), clock(log.getLoggedAt())); edit.callback = callback; edit.show(activity.getSupportFragmentManager(), "water_entry"); });
        }
        return root;
    }

    private void styleInput(TextInputLayout input) {
        input.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE); input.setBoxBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vita_surface));
        input.setBackground(null);
        input.setBoxStrokeWidth(dp(1)); input.setBoxStrokeWidthFocused(dp(2));
        input.setExpandedHintEnabled(false); input.setBoxCornerRadii(dp(12), dp(12), dp(12), dp(12));
        input.setBoxStrokeColorStateList(androidx.appcompat.content.res.AppCompatResources.getColorStateList(requireContext(), R.color.vita_input_stroke));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2); params.topMargin = dp(16); params.bottomMargin = dp(4); input.setLayoutParams(params);
    }

    private Dialog entry(Bundle state) {
        Bundle args = requireArguments();
        LinearLayout root = page(getString(args.getLong("id") > 0 ? R.string.water_edit_entry : R.string.figma_water_add_title));
        LinearLayout form = (LinearLayout) root.getTag();
        int pad = dp(20);
        android.widget.TextView intro = new android.widget.TextView(requireContext());
        intro.setText(R.string.figma_water_entry_intro);
        intro.setTextSize(24);
        intro.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vita_text_primary));
        intro.setTypeface(null, android.graphics.Typeface.BOLD);
        intro.setPadding(pad, pad, pad, pad);
        intro.setBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vita_water_bg));
        if (args.getLong("id") < 0) {
            LinearLayout hero = new LinearLayout(requireContext()); hero.setOrientation(android.widget.LinearLayout.VERTICAL); hero.setPadding(pad, pad, pad, pad); rounded(hero, R.color.vita_water_bg, 20);
            android.widget.ImageView drop = new android.widget.ImageView(requireContext()); drop.setImageResource(R.drawable.figma_ic_water); hero.addView(drop, new LinearLayout.LayoutParams(dp(48), dp(48)));
            intro.setPadding(0, dp(16), 0, 0); hero.addView(intro); form.addView(hero);
        }
        else { android.widget.TextView fixed = text(getString(R.string.figma_water_fixed_date, DateTimeUtils.formatDisplayDate(args.getString("date"))), 11, R.color.vita_water, true); fixed.setPadding(dp(8), dp(12), dp(8), dp(12)); form.addView(fixed); }
        TextInputLayout amountLayout = new TextInputLayout(requireContext()); amountLayout.setHint(R.string.figma_water_entry_amount_label);
        if (args.getLong("id") < 0) amountLayout.setHelperText(getString(R.string.figma_water_amount_help));
        amountField = new TextInputEditText(amountLayout.getContext()); amountField.setInputType(InputType.TYPE_CLASS_NUMBER);
        amountField.setText(state == null ? String.valueOf(args.getInt("amount")) : state.getString("draft_amount"));
        amountLayout.addView(amountField); styleInput(amountLayout); form.addView(amountLayout);
        TextInputLayout timeLayout = new TextInputLayout(requireContext()); timeLayout.setHint(R.string.water_entry_time);
        TextInputEditText time = new TextInputEditText(timeLayout.getContext()); time.setKeyListener(null);
        selectedTime = state == null ? args.getString("time") : state.getString("draft_time");
        if (!validClock(selectedTime)) selectedTime = DateTimeUtils.getCurrentTime();
        time.setText(selectedTime); time.setMinHeight(getResources().getDimensionPixelSize(R.dimen.min_touch_target));
        time.setOnClickListener(v -> new TimePickerDialog(requireContext(), (picker, h, m) -> {
            selectedTime = String.format(Locale.US, "%02d:%02d", h, m); time.setText(selectedTime);
        }, Integer.parseInt(selectedTime.substring(0, 2)), Integer.parseInt(selectedTime.substring(3)), true).show());
        timeLayout.addView(time); styleInput(timeLayout); if (args.getLong("id") > 0) form.addView(timeLayout);
        android.widget.TextView help = text(getString(args.getLong("id") > 0 ? R.string.figma_water_edit_help : R.string.figma_water_add_help), 14, R.color.vita_text_secondary, false);
        help.setPadding(pad, pad, pad, pad); rounded(help, R.color.vita_soft, 20); form.addView(help);
        com.google.android.material.button.MaterialButton save = button(getString(args.getLong("id") > 0 ? R.string.figma_save_changes : R.string.action_save_short), true); save.setId(android.R.id.button1); form.addView(save);
        if (args.getLong("id") < 0) {
            save.setText(getString(R.string.figma_record_water, amountField.getText()));
            amountField.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence value, int start, int count, int after) {}
                public void onTextChanged(CharSequence value, int start, int before, int count) { save.setText(getString(R.string.figma_record_water, value)); }
                public void afterTextChanged(android.text.Editable value) {}
            });
        }
        Dialog dialog = fullPage(root);
        save.setOnClickListener(v -> {
            if (!ownsSession()) { dismiss(); return; }
            int amount;
            try { amount = Integer.parseInt(amountField.getText().toString()); } catch (NumberFormatException invalid) { amount = 0; }
            if (amount < 50 || amount > 2000) { amountLayout.setError(getString(R.string.water_entry_invalid)); amountField.requestFocus(); return; }
            String stamp = args.getLong("id") > 0
                    ? args.getString("date") + " " + selectedTime + ":00"
                    : DateTimeUtils.getCurrentTimestamp();
            String date = stamp.substring(0, 10);
            long user = args.getLong("user");
            try {
                WaterDao dao = new WaterDao(requireContext());
                boolean saved = args.getLong("id") > 0 ? dao.updateLog(args.getLong("id"), user, amount, stamp)
                        : dao.addWater(user, date, amount, stamp) > 0;
                if (!saved) { amountLayout.setError(getString(R.string.water_entry_error)); return; }
                save.setEnabled(false);
                FragmentActivity activity = requireActivity(); notifyChanged(activity, user, callback); dismiss();
            } catch (android.database.sqlite.SQLiteException error) { amountLayout.setError(getString(R.string.water_entry_error)); }
        });
        if (args.getLong("id") > 0) {
            com.google.android.material.button.MaterialButton delete = button(getString(R.string.figma_water_delete), false);
            delete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vita_status_danger_bg)));
            delete.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vita_status_danger)); delete.setId(android.R.id.button3); form.addView(delete);
            delete.setOnClickListener(v -> {
                androidx.appcompat.app.AppCompatDialog confirm = VitaDialog.create(requireContext(),
                        getString(R.string.figma_water_delete_title), getString(
                        R.string.figma_water_delete_message, args.getInt("amount"), clock(args.getString("time")),
                        DateTimeUtils.formatDisplayDate(args.getString("date"))), R.string.figma_water_delete_action, true);
                confirm.findViewById(android.R.id.button1).setOnClickListener(accepted -> {
                        if (!ownsSession()) return;
                        try { if (new WaterDao(requireContext()).deleteLog(args.getLong("id"), args.getLong("user"))) { notifyChanged(requireActivity(), args.getLong("user"), callback); dismiss(); }
                        else Toast.makeText(requireContext(), R.string.water_entry_changed, Toast.LENGTH_SHORT).show();
                        } catch (android.database.sqlite.SQLiteException error) { Toast.makeText(requireContext(), R.string.water_entry_error, Toast.LENGTH_SHORT).show(); }
                        confirm.dismiss();
                });
                confirm.show();
            });
        }
        return dialog;
    }

    @Override public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.color.vita_bg);
            getDialog().getWindow().getDecorView().setPadding(0, 0, 0, 0);
            getDialog().getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        if (amountField != null) state.putString("draft_amount", amountField.getText().toString());
        state.putString("draft_time", selectedTime);
    }

    private static boolean validClock(String time) { return time != null && time.matches("([01]\\d|2[0-3]):[0-5]\\d"); }
    private static String clock(String stamp) {
        if (stamp != null && stamp.matches("\\d{4}-\\d{2}-\\d{2} .*")) stamp = stamp.substring(11);
        if (stamp != null && stamp.length() >= 5 && validClock(stamp.substring(0, 5))) return stamp.substring(0, 5);
        return "—";
    }

    private static void notifyChanged(FragmentActivity activity, long user, Runnable callback) {
        if (!SessionManager.matchesUser(user, new SessionManager(activity).getUserId())) return;
        WaterReminderReceiver.cancel(activity); WaterReminderReceiver.schedule(activity);
        User profile = new UserDao(activity).getUserById(user);
        if (profile != null && new WaterDao(activity).getDailyTotal(user, DateTimeUtils.getTodayDate()) >= HydrationGoal.forUserGoal(profile.getWaterGoalMl()))
            androidx.core.app.NotificationManagerCompat.from(activity).cancel(9001);
        if (callback != null) callback.run();
        else if (activity instanceof Host) ((Host) activity).onWaterJournalChanged();
        androidx.fragment.app.Fragment journal = activity.getSupportFragmentManager().findFragmentByTag("water_journal");
        if (journal instanceof WaterJournalDialog) {
            WaterJournalDialog page = (WaterJournalDialog) journal;
            if (page.getDialog() != null && page.ownsSession()) page.getDialog().setContentView(page.journalContent());
        }
    }

    private static android.view.View snackbarAnchor(FragmentActivity activity) {
        androidx.fragment.app.Fragment journal = activity.getSupportFragmentManager().findFragmentByTag("water_journal");
        if (journal instanceof WaterJournalDialog) {
            Dialog dialog = ((WaterJournalDialog) journal).getDialog();
            if (dialog != null && dialog.isShowing()) return dialog.findViewById(android.R.id.content);
        }
        return activity.findViewById(android.R.id.content);
    }

    public static void addQuickWater(FragmentActivity activity, long user, Runnable callback) {
        if (!SessionManager.matchesUser(user, new SessionManager(activity).getUserId())) return;
        User profile = new UserDao(activity).getUserById(user); if (profile == null) return;
        int amount = profile.getWaterAmount(); String stamp = DateTimeUtils.getCurrentTimestamp();
        try {
            WaterDao dao = new WaterDao(activity);
            long id = dao.addWater(user, stamp.substring(0, 10), amount, stamp);
            if (id <= 0) { Toast.makeText(activity, R.string.water_entry_error, Toast.LENGTH_SHORT).show(); return; }
            notifyChanged(activity, user, callback);
            Snackbar.make(snackbarAnchor(activity), activity.getString(R.string.water_added_undo, amount), Snackbar.LENGTH_LONG)
                    .setAction(R.string.reminder_undo, v -> {
                        if (!SessionManager.matchesUser(user, new SessionManager(activity).getUserId())) return;
                        try {
                            if (dao.undoUnchangedLog(id, user, amount, stamp)) notifyChanged(activity, user, callback);
                            else Toast.makeText(activity, R.string.water_entry_changed, Toast.LENGTH_SHORT).show();
                        } catch (android.database.sqlite.SQLiteException error) { Toast.makeText(activity, R.string.water_entry_error, Toast.LENGTH_SHORT).show(); }
                    }).show();
        } catch (android.database.sqlite.SQLiteException error) { Toast.makeText(activity, R.string.water_entry_error, Toast.LENGTH_SHORT).show(); }
    }
}

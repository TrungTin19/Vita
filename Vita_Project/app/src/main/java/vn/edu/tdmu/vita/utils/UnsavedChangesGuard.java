package vn.edu.tdmu.vita.utils;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.app.AppCompatActivity;
import vn.edu.tdmu.vita.R;

/** Compare user-editable values, not validation messages, focus or scroll position. */
public final class UnsavedChangesGuard {
    private final AppCompatActivity activity;
    private final String baseline;
    private AppCompatDialog prompt;

    private UnsavedChangesGuard(AppCompatActivity activity, Bundle state) {
        this.activity = activity;
        baseline = state == null ? snapshot() : state.getString("unsaved_baseline", snapshot());
        activity.getOnBackPressedDispatcher().addCallback(activity, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { requestExit(); }
        });
        activity.getLifecycle().addObserver(new androidx.lifecycle.DefaultLifecycleObserver() {
            @Override public void onDestroy(androidx.lifecycle.LifecycleOwner owner) {
                if (prompt != null) prompt.dismiss();
            }
        });
    }

    public static UnsavedChangesGuard attach(AppCompatActivity activity, Bundle state) {
        return new UnsavedChangesGuard(activity, state);
    }

    public void saveState(Bundle state) { state.putString("unsaved_baseline", baseline); }

    public void requestExit() {
        if (baseline.equals(snapshot())) { activity.finish(); return; }
        if (prompt != null && prompt.isShowing()) return;
        prompt = VitaDialog.create(activity, activity.getString(R.string.unsaved_title),
                activity.getString(R.string.unsaved_message), R.string.unsaved_continue, false);
        prompt.findViewById(android.R.id.button1).setOnClickListener(v -> prompt.dismiss());
        ((TextView) prompt.findViewById(android.R.id.button2)).setText(R.string.unsaved_discard);
        prompt.findViewById(android.R.id.button2).setOnClickListener(v -> { prompt.dismiss(); activity.finish(); });
        prompt.show();
    }

    private String snapshot() {
        StringBuilder values = new StringBuilder();
        capture(activity.findViewById(android.R.id.content), values);
        return values.toString();
    }

    private static void value(StringBuilder out, String value) {
        out.append(value.length()).append(':').append(value).append(';');
    }

    private static void capture(View view, StringBuilder out) {
        if ("presentation_only".equals(view.getTag())) return;
        if (view instanceof EditText) {
            EditText field = (EditText) view;
            String text = field.getText() == null ? "" : field.getText().toString().trim();
            if ((field.getInputType() & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER && !text.isEmpty()) {
                try { text = new java.math.BigDecimal(text).stripTrailingZeros().toPlainString(); }
                catch (NumberFormatException ignored) { /* Invalid input is still a draft worth keeping. */ }
            }
            value(out, text);
        } else if (view instanceof CompoundButton) {
            CompoundButton button = (CompoundButton) view;
            value(out, button.getText() + ":" + button.isChecked());
        } else if (view instanceof Spinner) {
            value(out, String.valueOf(((Spinner) view).getSelectedItemPosition()));
            return;
        } else if (view instanceof TextView && view.hasOnClickListeners() && view.isSelected()) {
            value(out, "selected:" + view.getId());
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) capture(group.getChildAt(i), out);
        }
    }
}

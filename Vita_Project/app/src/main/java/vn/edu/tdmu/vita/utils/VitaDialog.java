package vn.edu.tdmu.vita.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import vn.edu.tdmu.vita.R;

/** Shared Figma dialog shell; callers retain validation and action handling. */
public final class VitaDialog {
    private VitaDialog() {}

    public static AppCompatDialog create(Context context, CharSequence title, CharSequence message,
                                         int actionLabel, boolean destructive) {
        AppCompatDialog dialog = new AppCompatDialog(context);
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_profile_action, null);
        ((TextView) content.findViewById(R.id.tv_account_dialog_title)).setText(title);
        ((TextView) content.findViewById(R.id.tv_account_dialog_message)).setText(message);
        MaterialButton action = content.findViewById(android.R.id.button1);
        action.setText(actionLabel);
        if (destructive) {
            action.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.vita_status_danger_bg)));
            action.setTextColor(ContextCompat.getColor(context, R.color.vita_status_danger));
        }
        content.findViewById(android.R.id.button2).setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(content);
        dialog.setOnShowListener(ignored -> {
            if (dialog.getWindow() == null) return;
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            int margin = Math.round(48 * context.getResources().getDisplayMetrics().density);
            dialog.getWindow().setLayout(context.getResources().getDisplayMetrics().widthPixels - margin,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        });
        return dialog;
    }
}

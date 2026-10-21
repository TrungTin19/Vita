package vn.edu.tdmu.vita.utils;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import vn.edu.tdmu.vita.R;
import vn.edu.tdmu.vita.activities.LoginActivity;
import vn.edu.tdmu.vita.database.UserDao;
import vn.edu.tdmu.vita.database.MedicineDao;
import vn.edu.tdmu.vita.models.User;
import vn.edu.tdmu.vita.models.Medicine;

public final class ProfileAccountActions {
    private final AppCompatActivity activity;
    private final User currentUser;
    private final UserDao userDao;
    private final SessionManager sessionManager;

    public ProfileAccountActions(AppCompatActivity activity, User user) {
        this.activity = activity;
        currentUser = user;
        userDao = new UserDao(activity);
        sessionManager = new SessionManager(activity);
    }

    public void showLogoutConfirmDialog() {
        AppCompatDialog dialog = VitaDialog.create(activity, activity.getString(R.string.figma_logout_title),
                activity.getString(R.string.figma_logout_message), R.string.edit_profile_btn_logout, false);
        dialog.findViewById(android.R.id.button1).setOnClickListener(v -> {
            if (!ownsSession()) { activity.finish(); return; }
            dialog.dismiss();
            finishSession(new MedicineDao(activity).getAllByUserId(currentUser.getId()), R.string.edit_profile_logout_toast);
        });
        dialog.show();
    }
    private boolean ownsSession() {
        return currentUser != null && sessionManager.getUserId() == currentUser.getId();
    }

    public void showDeleteConfirmDialog() {
        if (!ownsSession()) { activity.finish(); return; }
        android.view.View form = activity.getLayoutInflater().inflate(R.layout.dialog_delete_profile, null);
        TextInputLayout passwordLayout = form.findViewById(R.id.til_delete_profile_password);
        TextInputEditText password = form.findViewById(R.id.et_delete_profile_password);
        AppCompatDialog dialog = VitaDialog.create(activity, activity.getString(R.string.figma_delete_profile_title),
                activity.getString(R.string.figma_delete_profile_message), R.string.profile_delete_confirm, true);
        ((android.widget.FrameLayout) dialog.findViewById(R.id.account_dialog_form)).addView(form);
        dialog.setOnDismissListener(ignored -> password.setText(null));
        dialog.findViewById(android.R.id.button1).setOnClickListener(v -> {
            if (!ownsSession()) { dialog.dismiss(); activity.finish(); return; }
            String entered = password.getText() == null ? "" : password.getText().toString();
            try {
                List<Medicine> medicines = new MedicineDao(activity).getAllByUserId(currentUser.getId());
                if (!userDao.deleteProfileWithPassword(currentUser.getId(), entered)) {
                    passwordLayout.setError(activity.getString(R.string.profile_delete_password_error));
                    password.requestFocus();
                    return;
                }
                dialog.dismiss();
                finishSession(medicines, R.string.profile_delete_success);
            } catch (SQLiteException error) {
                Toast.makeText(activity, R.string.profile_delete_error, Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }

    private void finishSession(List<Medicine> medicines, int message) {
        for (Medicine medicine : medicines) {
            vn.edu.tdmu.vita.receivers.MedicineAlarmReceiver.cancel(activity, medicine);
        }
        vn.edu.tdmu.vita.receivers.WaterReminderReceiver.cancel(activity);
        androidx.core.app.NotificationManagerCompat.from(activity).cancelAll();
        sessionManager.clearSession();
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        activity.startActivity(new Intent(activity, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        activity.finish();
    }

}

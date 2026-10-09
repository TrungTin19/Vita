package vn.edu.tdmu.vita.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    public static final String PREF_NAME = "vita_user_session";
    public static final String KEY_USER_ID = "session_user_id";
    public static final String KEY_USERNAME = "session_username";
    public static final String KEY_FULLNAME = "session_fullname";
    public static final String KEY_IS_LOGGED_IN = "session_is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = prefs.edit();
    }

    public void saveSession(long userId, String username, String fullname) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULLNAME, fullname);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getFullname() {
        return prefs.getString(KEY_FULLNAME, "");
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getUserId() > 0;
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}

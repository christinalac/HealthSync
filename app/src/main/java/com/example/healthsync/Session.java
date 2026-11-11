package com.example.healthsync;

import android.content.Context;
import android.content.SharedPreferences;

// Remembers which user is logged in, so any screen can look up their profile.
//
// >>> Call Session.setEmail(context, email) right after a successful login / sign-up.
// >>> If your app already tracks the logged-in user another way, change getEmail() to use that.
public final class Session {
    private static final String PREFS = "session";
    private static final String KEY_EMAIL = "email";

    private Session() {}

    public static void setEmail(Context context, String email) {
        prefs(context).edit().putString(KEY_EMAIL, email).apply();
    }

    // null if nobody is logged in
    public static String getEmail(Context context) {
        return prefs(context).getString(KEY_EMAIL, null);
    }

    public static void clear(Context context) {
        prefs(context).edit().remove(KEY_EMAIL).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}

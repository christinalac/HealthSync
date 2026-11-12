package com.example.healthsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

// Saves the user's profile on the device. The password is never stored as plain text:
// only a random salt and a SHA-256 hash of (salt + password).
public class AccountStore {

    private final SharedPreferences prefs;

    public AccountStore(Context context) {
        prefs = context.getSharedPreferences("account_prefs", Context.MODE_PRIVATE);
    }

    public String getUsername() {
        return prefs.getString("username", "First Last");
    }

    public String getEmail() {
        return prefs.getString("email", "user@email.com");
    }

    public void setUsername(String username) {
        prefs.edit().putString("username", username).apply();
    }

    public void setEmail(String email) {
        prefs.edit().putString("email", email).apply();
    }

    // False until a password has been saved (e.g. by the sign-up screen)
    public boolean hasPassword() {
        return prefs.getString("pw_hash", null) != null;
    }

    public boolean checkPassword(String password) {
        String salt = prefs.getString("pw_salt", null);
        String hash = prefs.getString("pw_hash", null);
        if (salt == null || hash == null) return false;
        return hash.equals(hash(salt, password));
    }

    public void setPassword(String password) {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        String salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP);
        prefs.edit()
                .putString("pw_salt", salt)
                .putString("pw_hash", hash(salt, password))
                .apply();
    }

    private String hash(String salt, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((salt + password).getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(digest, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // SHA-256 is always available on Android
        }
    }
}
//******************************************
// Program Name: DatabaseHelper.java
// Developer: Christina LaCombe
// Date Created: 10/12/2026
// Version: 3.0
// Purpose: This stores the data a user has when
// signing up so they can log in again, and stores
// their personal profile information including
// gender, activity level, and pregnancy status
// used by PersonalizedCalculations.
//******************************************
package com.example.healthsync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String databaseName = "SignLog.db";

    public DatabaseHelper(@Nullable Context context) {
        // Version bumped to 3 so onUpgrade runs and adds new profile columns
        super(context, "SignLog.db", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Login credentials table
        db.execSQL("CREATE TABLE users (email TEXT PRIMARY KEY, password TEXT)");

        // Personal profile table — linked to users by email
        db.execSQL("CREATE TABLE user_profile (" +
                "email              TEXT PRIMARY KEY, " +
                "name               TEXT, " +
                "age                INTEGER, " +
                "gender             TEXT, " +
                "heightFt           INTEGER, " +
                "heightIn           INTEGER, " +
                "weightLbs          INTEGER, " +
                "goal               TEXT, " +
                "activityLevel      TEXT, " +
                "isPregnant         INTEGER DEFAULT 0, " +
                "isBreastfeeding    INTEGER DEFAULT 0, " +
                "FOREIGN KEY(email) REFERENCES users(email))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS user_profile");
        onCreate(db);
    }

    // ── Auth methods ─────────────────────────────────────────

    public Boolean insertData(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    public Boolean checkEmail(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ?", new String[]{email});
        return cursor.getCount() > 0;
    }

    public Boolean checkEmailPassword(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ? AND password = ?",
                new String[]{email, password});
        return cursor.getCount() > 0;
    }

    // ── Profile methods ──────────────────────────────────────

    public Boolean insertUserProfile(String email, String name, int age,
                                     String gender, int heightFt, int heightIn,
                                     int weightLbs, String goal,
                                     String activityLevel,
                                     boolean isPregnant, boolean isBreastfeeding) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email",           email);
        values.put("name",            name);
        values.put("age",             age);
        values.put("gender",          gender);
        values.put("heightFt",        heightFt);
        values.put("heightIn",        heightIn);
        values.put("weightLbs",       weightLbs);
        values.put("goal",            goal);
        values.put("activityLevel",   activityLevel);
        values.put("isPregnant",      isPregnant ? 1 : 0);
        values.put("isBreastfeeding", isBreastfeeding ? 1 : 0);
        long result = db.insert("user_profile", null, values);
        return result != -1;
    }

    /** Returns the saved profile for this email, or null if there isn't one. */
    @Nullable
    // Used by the Account screen to show the profile and run the calculations
    public UserProfile getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM user_profile WHERE email = ?",
                new String[]{email})) {
            if (!c.moveToFirst()) return null;
            // Copy each column into a UserProfile (booleans are stored as 0/1)
            return new UserProfile(
                    c.getString(c.getColumnIndexOrThrow("email")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getInt(c.getColumnIndexOrThrow("age")),
                    c.getString(c.getColumnIndexOrThrow("gender")),
                    c.getInt(c.getColumnIndexOrThrow("heightFt")),
                    c.getInt(c.getColumnIndexOrThrow("heightIn")),
                    c.getInt(c.getColumnIndexOrThrow("weightLbs")),
                    c.getString(c.getColumnIndexOrThrow("goal")),
                    c.getString(c.getColumnIndexOrThrow("activityLevel")),
                    c.getInt(c.getColumnIndexOrThrow("isPregnant")) == 1,
                    c.getInt(c.getColumnIndexOrThrow("isBreastfeeding")) == 1);
        }
    }

    // Changes the name shown on the Account screen. Returns false if no profile row matched.
    public boolean updateName(String email, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        return db.update("user_profile", values, "email = ?", new String[]{email}) > 0;
    }

    // Changes the login password. Returns false if no user row matched.
    public boolean updatePassword(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", password);
        return db.update("users", values, "email = ?", new String[]{email}) > 0;
    }

    /** Changes the login email in both tables. Returns false if the new email is taken. */
    public boolean updateEmail(String oldEmail, String newEmail) {
        // Refuse if another account already uses the new email
        if (!oldEmail.equalsIgnoreCase(newEmail) && checkEmail(newEmail)) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        // Update both tables together so they never end up with different emails
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("email", newEmail);
            int users = db.update("users", values, "email = ?", new String[]{oldEmail});
            db.update("user_profile", values, "email = ?", new String[]{oldEmail});
            // No login row matched, so skip the commit and undo the changes
            if (users == 0) return false;
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }
}

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

    public UserProfile getLatestUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM user_profile ORDER BY rowid DESC LIMIT 1", null);
        if (cursor != null && cursor.moveToFirst()) {
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            int age = cursor.getInt(cursor.getColumnIndexOrThrow("age"));
            String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
            int heightFt = cursor.getInt(cursor.getColumnIndexOrThrow("heightFt"));
            int heightIn = cursor.getInt(cursor.getColumnIndexOrThrow("heightIn"));
            int weightLbs = cursor.getInt(cursor.getColumnIndexOrThrow("weightLbs"));
            String goal = cursor.getString(cursor.getColumnIndexOrThrow("goal"));
            String activityLevel = cursor.getString(cursor.getColumnIndexOrThrow("activityLevel"));
            boolean isPregnant = cursor.getInt(cursor.getColumnIndexOrThrow("isPregnant")) == 1;
            boolean isBreastfeeding = cursor.getInt(cursor.getColumnIndexOrThrow("isBreastfeeding")) == 1;
            cursor.close();
            return new UserProfile(email, name, age, gender, heightFt, heightIn, weightLbs, goal, activityLevel, isPregnant, isBreastfeeding);
        }
        if (cursor != null) {
            cursor.close();
        }
        return new UserProfile("default@healthsync.com", "User", 25, "Female", 5, 6, 150,
                PersonalizedCalculations.GOAL_MAINTAIN_WEIGHT,
                PersonalizedCalculations.ACTIVITY_MODERATELY_ACTIVE, false, false);
    }
}

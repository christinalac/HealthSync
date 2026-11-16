//******************************************
// Program Name: ProgressHistory.java
// Developer: Anthony Ramirez and Christina LaCombe
// Date Created: 11/15/2026
// Version: 1.1
// Purpose: Keeps one record per day of the calories eaten, water drunk,
// and hours slept so the Progress page can draw the weekly and monthly
// graphs from the user's real data instead of sample numbers.
//******************************************
package com.example.healthsync;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProgressHistory {

    // ── Storage keys ──────────────────────────────────────────
    // Each day is saved under its date ("2026-11-15") as a small JSON object:
    // {"kcal": 1850, "water": 2.5, "sleep": 7.5}
    private static final String PREFS = "progress_history";
    private static final String KEY_KCAL  = "kcal";
    private static final String KEY_WATER = "water";
    private static final String KEY_SLEEP = "sleep";

    private final SharedPreferences prefs;

    public ProgressHistory(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ─────────────────────────────────────────────────────────
    // SAVING (called by the Nutrition and Sleep pages)
    // ─────────────────────────────────────────────────────────

    /**
     * Saves today's calories and water. Called every time the Nutrition page saves,
     * so it always holds the latest totals for the day.
     */
    public void recordNutrition(int calories, double waterLitres) {
        JSONObject day = loadDay(new Date());
        try {
            day.put(KEY_KCAL, calories);
            day.put(KEY_WATER, waterLitres);
        } catch (JSONException e) {
            return; // can't happen for numbers, but don't save a broken day
        } // end of try/catch
        saveDay(new Date(), day);
    } // end of recordNutrition

    /**
     * Saves the hours slept for today (the morning the user woke up).
     */
    public void recordSleep(double hours) {
        JSONObject day = loadDay(new Date());
        try {
            day.put(KEY_SLEEP, hours);
        } catch (JSONException e) {
            return;
        } // end of try/catch
        saveDay(new Date(), day);
    } // end of recordSleep

    // ─────────────────────────────────────────────────────────
    // READING (called by the Progress page)
    // ─────────────────────────────────────────────────────────

    /** Calories eaten on that day, or null if nothing was logged. */
    public Double getCalories(Date date) {
        return getValue(date, KEY_KCAL);
    } // end of getCalories

    /** Litres of water on that day, or null if nothing was logged. */
    public Double getWater(Date date) {
        return getValue(date, KEY_WATER);
    } // end of getWater

    /** Hours slept on that day, or null if nothing was logged. */
    public Double getSleep(Date date) {
        return getValue(date, KEY_SLEEP);
    } // end of getSleep

    /**
     * Average per day for a range of days in the current month, counting only
     * the days that have data. Returns null if none of them have data.
     *
     * @param year     e.g. 2026
     * @param month    Calendar.JANUARY ... Calendar.DECEMBER
     * @param firstDay first day of the range (1 = the 1st of the month)
     * @param lastDay  last day of the range, included
     * @param type     "kcal", "water", or "sleep"
     */
    public Double getAverage(int year, int month, int firstDay, int lastDay, String type) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month, 1);

        double total = 0;
        int daysWithData = 0;
        for (int d = firstDay; d <= lastDay; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            Double value = getValue(cal.getTime(), type);
            if (value != null) {
                total += value;
                daysWithData++;
            } // end of has-data check
        } // end of for loop over the days

        return daysWithData == 0 ? null : total / daysWithData;
    } // end of getAverage

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    // The type names the Progress page passes into getAverage
    public static final String TYPE_CALORIES = KEY_KCAL;
    public static final String TYPE_WATER    = KEY_WATER;
    public static final String TYPE_SLEEP    = KEY_SLEEP;

    private Double getValue(Date date, String key) {
        JSONObject day = loadDay(date);
        return day.has(key) ? day.optDouble(key) : null;
    } // end of getValue

    // Loads one day's record. A missing or broken record comes back empty
    private JSONObject loadDay(Date date) {
        String json = prefs.getString(dateKey(date), null);
        if (json == null) return new JSONObject();
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return new JSONObject();
        } // end of try/catch
    } // end of loadDay

    private void saveDay(Date date, JSONObject day) {
        prefs.edit().putString(dateKey(date), day.toString()).apply();
    } // end of saveDay

    private static String dateKey(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
    } // end of dateKey

} // end of class ProgressHistory

//******************************************
// Program Name: SleepStore.java
// Developer: Anthony Ramirez and Christina LaCombe
// Date Created: 11/11/2026
// Version: 1.1
// Purpose: Saves the Sleep schedule cards and last night's sleep on the device (SharedPreferences, stored as JSON).
//******************************************
package com.example.healthsync;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SleepStore {

    private final SharedPreferences prefs;

    public SleepStore(Context context) {
        prefs = context.getSharedPreferences("sleep_prefs", Context.MODE_PRIVATE);
    }

    public List<SleepEntry> loadAll() {
        List<SleepEntry> list = new ArrayList<>();
        String json = prefs.getString("entries", null);
        JSONArray entriesArray = null;
        JSONObject entryObject = null;
        JSONArray daysArray = null;
        Set<Integer> days = null;
        SleepEntry entry = null;
        int bed = -1;
        int wake = -1;
        if (json == null) return list;

        try {
            entriesArray = new JSONArray(json);
            for (int i = 0; i < entriesArray.length(); i++) {
                entryObject = entriesArray.getJSONObject(i);

                daysArray = entryObject.getJSONArray("days");
                days = new HashSet<>();
                for (int j = 0; j < daysArray.length(); j++) {
                    days.add(daysArray.getInt(j));
                }

                entry = new SleepEntry(entryObject.getLong("id"), days);
                bed = entryObject.optInt("bed", -1);
                wake = entryObject.optInt("wake", -1);
                if (bed >= 0) {
                    entry.bedMinutes = Integer.valueOf(bed);
                } else {
                    entry.bedMinutes = null;
                }
                if (wake >= 0) {
                    entry.wakeMinutes = Integer.valueOf(wake);
                } else {
                    entry.wakeMinutes = null;
                }
                entry.bedAlarm = entryObject.optBoolean("bedAlarm", false);
                entry.wakeAlarm = entryObject.optBoolean("wakeAlarm", false);
                list.add(entry);
            }
        } catch (JSONException exception) {
            // Corrupt data: return whatever loaded successfully
        }
        return list;
    }

    public void saveAll(List<SleepEntry> entries) {
        JSONArray entriesArray = new JSONArray();
        JSONObject entryObject = null;
        int bedValue = -1;
        int wakeValue = -1;
        try {
            for (SleepEntry entry : entries) {
                entryObject = new JSONObject();
                if (entry.bedMinutes != null) {
                    bedValue = entry.bedMinutes;
                } else {
                    bedValue = -1;
                }
                if (entry.wakeMinutes != null) {
                    wakeValue = entry.wakeMinutes;
                } else {
                    wakeValue = -1;
                }
                entryObject.put("id", entry.id);
                entryObject.put("days", new JSONArray(entry.days));
                entryObject.put("bed", bedValue);
                entryObject.put("wake", wakeValue);
                entryObject.put("bedAlarm", entry.bedAlarm);
                entryObject.put("wakeAlarm", entry.wakeAlarm);
                entriesArray.put(entryObject);
            }
        } catch (JSONException exception) {
            // don't overwrite good data with a half-built list
            return;
        }
        prefs.edit().putString("entries", entriesArray.toString()).apply();
    }

    public void saveLastNight(int bedMinutes, int wakeMinutes) {
        prefs.edit()
                .putInt("last_bed", bedMinutes)
                .putInt("last_wake", wakeMinutes)
                .apply();
    }

    // Returns {bedMinutes, wakeMinutes}, or null if nothing has been logged yet
    public int[] loadLastNight() {
        int bed = prefs.getInt("last_bed", -1);
        int wake = prefs.getInt("last_wake", -1);
        if (bed < 0 || wake < 0) return null;
        return new int[]{bed, wake};
    }
}
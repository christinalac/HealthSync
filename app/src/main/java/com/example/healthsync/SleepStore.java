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
        if (json == null) return list;

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);

                JSONArray daysArr = o.getJSONArray("days");
                Set<Integer> days = new HashSet<>();
                for (int j = 0; j < daysArr.length(); j++) {
                    days.add(daysArr.getInt(j));
                }

                SleepEntry e = new SleepEntry(o.getLong("id"), days);
                int bed = o.optInt("bed", -1);
                int wake = o.optInt("wake", -1);
                e.bedMinutes = bed >= 0 ? Integer.valueOf(bed) : null;
                e.wakeMinutes = wake >= 0 ? Integer.valueOf(wake) : null;
                e.bedAlarm = o.optBoolean("bedAlarm", false);
                e.wakeAlarm = o.optBoolean("wakeAlarm", false);
                list.add(e);
            }
        } catch (JSONException ex) {
            // Corrupt data: return whatever loaded successfully
        }
        return list;
    }

    public void saveAll(List<SleepEntry> entries) {
        JSONArray arr = new JSONArray();
        try {
            for (SleepEntry e : entries) {
                JSONObject o = new JSONObject();
                o.put("id", e.id);
                o.put("days", new JSONArray(e.days));
                o.put("bed", e.bedMinutes != null ? e.bedMinutes : -1);
                o.put("wake", e.wakeMinutes != null ? e.wakeMinutes : -1);
                o.put("bedAlarm", e.bedAlarm);
                o.put("wakeAlarm", e.wakeAlarm);
                arr.put(o);
            }
        } catch (JSONException ex) {
            return; // don't overwrite good data with a half-built list
        }
        prefs.edit().putString("entries", arr.toString()).apply();
    }
}
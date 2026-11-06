package com.example.healthsync;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class SleepStore {

    private final SharedPreferences prefs;

    public SleepStore(Context context) {
        this.prefs = context.getSharedPreferences("sleep_prefs", Context.MODE_PRIVATE);
    }

    public List<SleepDay> load() {
        List<SleepDay> out = new ArrayList<>();
        for (SleepDay.Entry e : SleepDay.WEEK) {
            int dow = e.dayOfWeek;
            int bed = prefs.getInt(dow + "-bed", -1);
            int wake = prefs.getInt(dow + "-wake", -1);
            out.add(new SleepDay(
                    dow,
                    e.name,
                    bed >= 0 ? bed : null,
                    wake >= 0 ? wake : null,
                    prefs.getBoolean(dow + "-bedAlarm", false),
                    prefs.getBoolean(dow + "-wakeAlarm", false)
            ));
        }
        return out;
    }

    public void save(SleepDay day) {
        prefs.edit()
                .putInt(day.getDayOfWeek() + "-bed", day.getBedMinutes() != null ? day.getBedMinutes() : -1)
                .putInt(day.getDayOfWeek() + "-wake", day.getWakeMinutes() != null ? day.getWakeMinutes() : -1)
                .putBoolean(day.getDayOfWeek() + "-bedAlarm", day.isBedAlarm())
                .putBoolean(day.getDayOfWeek() + "-wakeAlarm", day.isWakeAlarm())
                .apply();
    }
}

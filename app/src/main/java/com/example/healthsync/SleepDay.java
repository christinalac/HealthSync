package com.example.healthsync;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class SleepDay {

    private final int dayOfWeek;          // Calendar.MONDAY etc.
    private final String name;
    @Nullable private Integer bedMinutes;  // minutes since midnight, null = not set
    @Nullable private Integer wakeMinutes;
    private boolean bedAlarm;
    private boolean wakeAlarm;

    public SleepDay(int dayOfWeek, String name) {
        this(dayOfWeek, name, null, null, false, false);
    }

    public SleepDay(int dayOfWeek, String name, @Nullable Integer bedMinutes,
                    @Nullable Integer wakeMinutes, boolean bedAlarm, boolean wakeAlarm) {
        this.dayOfWeek = dayOfWeek;
        this.name = name;
        this.bedMinutes = bedMinutes;
        this.wakeMinutes = wakeMinutes;
        this.bedAlarm = bedAlarm;
        this.wakeAlarm = wakeAlarm;
    }

    public int getDayOfWeek() { return dayOfWeek; }
    public String getName() { return name; }
    @Nullable public Integer getBedMinutes() { return bedMinutes; }
    @Nullable public Integer getWakeMinutes() { return wakeMinutes; }
    public boolean isBedAlarm() { return bedAlarm; }
    public boolean isWakeAlarm() { return wakeAlarm; }

    public void setBedMinutes(@Nullable Integer bedMinutes) { this.bedMinutes = bedMinutes; }
    public void setWakeMinutes(@Nullable Integer wakeMinutes) { this.wakeMinutes = wakeMinutes; }
    public void setBedAlarm(boolean bedAlarm) { this.bedAlarm = bedAlarm; }
    public void setWakeAlarm(boolean wakeAlarm) { this.wakeAlarm = wakeAlarm; }

    // One entry in the week: a Calendar day-of-week constant paired with its display name.
    public static final class Entry {
        public final int dayOfWeek;
        public final String name;

        Entry(int dayOfWeek, String name) {
            this.dayOfWeek = dayOfWeek;
            this.name = name;
        }
    }

    public static final List<Entry> WEEK;

    static {
        List<Entry> week = new ArrayList<>();
        week.add(new Entry(Calendar.MONDAY, "Monday"));
        week.add(new Entry(Calendar.TUESDAY, "Tuesday"));
        week.add(new Entry(Calendar.WEDNESDAY, "Wednesday"));
        week.add(new Entry(Calendar.THURSDAY, "Thursday"));
        week.add(new Entry(Calendar.FRIDAY, "Friday"));
        week.add(new Entry(Calendar.SATURDAY, "Saturday"));
        week.add(new Entry(Calendar.SUNDAY, "Sunday"));
        WEEK = Collections.unmodifiableList(week);
    }
}

package com.example.healthsync;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

// One card on the screen: a set of days that share a bedtime and/or wake-up time.
public class SleepEntry {

    public static final int[] WEEK_DAYS = {
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
            Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
    };
    public static final String[] WEEK_NAMES = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    public final long id;
    public Set<Integer> days;      // Calendar.MONDAY etc.
    public Integer bedMinutes;     // minutes since midnight, null = not set
    public Integer wakeMinutes;
    public boolean bedAlarm;
    public boolean wakeAlarm;

    public SleepEntry(long id, Set<Integer> days) {
        this.id = id;
        this.days = days;
    }

    public Integer getMinutes(boolean isWake) {
        return isWake ? wakeMinutes : bedMinutes;
    }

    public void setMinutes(boolean isWake, Integer minutes) {
        if (isWake) wakeMinutes = minutes; else bedMinutes = minutes;
    }

    public boolean isAlarmOn(boolean isWake) {
        return isWake ? wakeAlarm : bedAlarm;
    }

    public void setAlarmOn(boolean isWake, boolean on) {
        if (isWake) wakeAlarm = on; else bedAlarm = on;
    }

    // Readable title: "Weekdays", "Mon, Wed, Fri", ...
    public String label() {
        Set<Integer> weekdays = new HashSet<>(Arrays.asList(
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY));
        Set<Integer> weekend = new HashSet<>(Arrays.asList(
                Calendar.SATURDAY, Calendar.SUNDAY));

        if (days.size() == 7) return "Every day";
        if (days.equals(weekdays)) return "Weekdays";
        if (days.equals(weekend)) return "Weekends";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < WEEK_DAYS.length; i++) {
            if (days.contains(WEEK_DAYS[i])) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(WEEK_NAMES[i].substring(0, 3));
            }
        }
        return sb.toString();
    }
}
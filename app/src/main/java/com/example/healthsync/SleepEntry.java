//******************************************
// Program Name: SleepEntry.java
// Developer: Anthony Ramirez and Christina LaCombe
// Date Created: 11/11/2026
// Version: 1.1
// Purpose: Data for one Sleep schedule card: a set of days sharing a bedtime and/or wake-up time and their alarm settings.
//******************************************
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

    // Minutes since midnight must fall in this range to be valid
    private static final int MIN_MINUTES = 0;
    private static final int MAX_MINUTES = 1439;

    public final long id;
    // Calendar.MONDAY etc.
    public Set<Integer> days = new HashSet<>();
    // minutes since midnight, null = not set
    public Integer bedMinutes = null;
    public Integer wakeMinutes = null;
    public boolean bedAlarm = false;
    public boolean wakeAlarm = false;

    public SleepEntry(long id, Set<Integer> days) {
        if (id >= 0) {
            this.id = id;
        } else {
            this.id = -1;
        }
        if (days != null) {
            this.days = days;
        } else {
            this.days = new HashSet<>();
        }
    }

    public Integer getMinutes(boolean isWake) {
        Integer result;
        if (isWake) {
            result = wakeMinutes;
        } else {
            result = bedMinutes;
        }
        return result;
    }

    public void setMinutes(boolean isWake, Integer minutes) {
        Integer validMinutes = null;
        if (minutes != null && minutes >= MIN_MINUTES && minutes <= MAX_MINUTES) {
            validMinutes = minutes;
        } else {
            validMinutes = null;
        }
        if (isWake) {
            wakeMinutes = validMinutes;
        } else {
            bedMinutes = validMinutes;
        }
    }

    public boolean isAlarmOn(boolean isWake) {
        boolean result;
        if (isWake) {
            result = wakeAlarm;
        } else {
            result = bedAlarm;
        }
        return result;
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
        StringBuilder labelBuilder = new StringBuilder();

        if (days.size() == 7) return "Every day";
        if (days.equals(weekdays)) return "Weekdays";
        if (days.equals(weekend)) return "Weekends";

        for (int i = 0; i < WEEK_DAYS.length; i++) {
            if (days.contains(WEEK_DAYS[i])) {
                if (labelBuilder.length() > 0) labelBuilder.append(", ");
                labelBuilder.append(WEEK_NAMES[i].substring(0, 3));
            }
        }
        return labelBuilder.toString();
    }
}

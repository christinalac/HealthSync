package com.example.healthsync;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public final class AlarmScheduler {

    private AlarmScheduler() { }

    // Unique ID per day + type, so each alarm can be cancelled independently
    private static int requestCode(int dayOfWeek, boolean isWake) {
        return dayOfWeek * 2 + (isWake ? 1 : 0);
    }

    private static PendingIntent pendingIntent(Context context, int dayOfWeek, int minutes, boolean isWake) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("dayOfWeek", dayOfWeek);
        intent.putExtra("minutes", minutes);
        intent.putExtra("isWake", isWake);
        return PendingIntent.getBroadcast(
                context,
                requestCode(dayOfWeek, isWake),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    public static long nextTrigger(int dayOfWeek, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
        cal.set(Calendar.MINUTE, minutes % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // If that moment already passed this week, use next week
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }
        return cal.getTimeInMillis();
    }

    /** Returns false if exact alarms aren't allowed (and opens the settings screen). */
    public static boolean schedule(Context context, int dayOfWeek, int minutes, boolean isWake) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long trigger = nextTrigger(dayOfWeek, minutes);
        PendingIntent pi = pendingIntent(context, dayOfWeek, minutes, isWake);

        boolean canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || alarmManager.canScheduleExactAlarms();

        try {
            if (canExact && isWake) {
                PendingIntent showIntent = PendingIntent.getActivity(
                        context, 0, new Intent(context, SleepActivity.class), PendingIntent.FLAG_IMMUTABLE);
                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(trigger, showIntent), pi);
            } else if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
            } else {
                // No exact permission: fires close to the time, but Android may delay it
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
            }
        } catch (SecurityException e) {
            Log.e("SleepAlarm", "Alarm not allowed", e);
            return false;
        }
        return true;
    }

    public static void cancel(Context context, int dayOfWeek, int minutes, boolean isWake) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent(context, dayOfWeek, minutes, isWake));
    }
}

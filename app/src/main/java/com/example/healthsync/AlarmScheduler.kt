package com.example.healthsync
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.util.Calendar
import android.util.Log
object AlarmScheduler {

    // Unique ID per day + type, so each alarm can be cancelled independently
    private fun requestCode(dayOfWeek: Int, isWake: Boolean) =
        dayOfWeek * 2 + if (isWake) 1 else 0

    private fun pendingIntent(
        context: Context, dayOfWeek: Int, minutes: Int, isWake: Boolean
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("dayOfWeek", dayOfWeek)
            putExtra("minutes", minutes)
            putExtra("isWake", isWake)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(dayOfWeek, isWake),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun nextTrigger(dayOfWeek: Int, minutes: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // If that moment already passed this week, use next week
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    /** Returns false if exact alarms aren't allowed (and opens the settings screen). */
    fun schedule(context: Context, dayOfWeek: Int, minutes: Int, isWake: Boolean): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val trigger = nextTrigger(dayOfWeek, minutes)
        val pi = pendingIntent(context, dayOfWeek, minutes, isWake)

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()

        try {
            if (canExact && isWake) {
                val showIntent = PendingIntent.getActivity(
                    context, 0, Intent(context, SleepActivity::class.java), PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, showIntent), pi)
            } else if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
            } else {
                // No exact permission: fires close to the time, but Android may delay it
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
            }
        } catch (e: SecurityException) {
            Log.e("SleepAlarm", "Alarm not allowed", e)
            return false
        }
        return true
    }

    fun cancel(context: Context, dayOfWeek: Int, minutes: Int, isWake: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, dayOfWeek, minutes, isWake))
    }
}
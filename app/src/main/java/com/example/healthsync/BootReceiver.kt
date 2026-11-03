package com.example.healthsync
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        SleepStore(context).load().forEach { day ->
            day.bedMinutes?.let { if (day.bedAlarm) AlarmScheduler.schedule(context, day.dayOfWeek, it, false) }
            day.wakeMinutes?.let { if (day.wakeAlarm) AlarmScheduler.schedule(context, day.dayOfWeek, it, true) }
        }
    }
}
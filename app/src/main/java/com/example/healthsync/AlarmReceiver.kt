package com.example.healthsync
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val day = intent.getIntExtra("dayOfWeek", -1)
        val minutes = intent.getIntExtra("minutes", -1)
        val isWake = intent.getBooleanExtra("isWake", false)
        if (day == -1 || minutes == -1) return

        Log.d("SleepAlarm", "Fired: day=$day minutes=$minutes isWake=$isWake")

        if (isWake) {
            ContextCompat.startForegroundService(context, Intent(context, AlarmService::class.java))
        } else {
            NotificationHelper.show(context, day, false)
        }

        // Repeat next week
        AlarmScheduler.schedule(context, day, minutes, isWake)
    }
}
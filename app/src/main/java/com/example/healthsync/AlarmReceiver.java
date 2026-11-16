//******************************************
// Program Name: AlarmReceiver.java
// Developer: Anthony Ramirez and Christina LaCombe
// Date Created: 11/11/2026
// Version: 1.1
// Purpose: Receives a fired alarm: starts the ringing service for wake-ups or shows a reminder for bedtime, then schedules next week.
//******************************************
package com.example.healthsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int day = intent.getIntExtra("dayOfWeek", -1);
        int minutes = intent.getIntExtra("minutes", -1);
        boolean isWake = intent.getBooleanExtra("isWake", false);
        if (day == -1 || minutes == -1) return;

        Log.d("SleepAlarm", "Fired: day=" + day + " minutes=" + minutes + " isWake=" + isWake);

        if (isWake) {
            // Android 12+ can refuse to start a foreground service from the background
            try {
                ContextCompat.startForegroundService(context, new Intent(context, AlarmService.class));
            } catch (RuntimeException e) {
                // Android refused to start the service from the background; still alert the user
                Log.e("SleepAlarm", "Could not start alarm service", e);
                NotificationHelper.show(context, day, true);
            }
        } else {
            NotificationHelper.show(context, day, false);
        }

        // Repeat next week
        AlarmScheduler.schedule(context, day, minutes, isWake);
    }
}

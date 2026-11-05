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
            ContextCompat.startForegroundService(context, new Intent(context, AlarmService.class));
        } else {
            NotificationHelper.show(context, day, false);
        }

        // Repeat next week
        AlarmScheduler.schedule(context, day, minutes, isWake);
    }
}

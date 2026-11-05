package com.example.healthsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        for (SleepDay day : new SleepStore(context).load()) {
            if (day.getBedMinutes() != null && day.isBedAlarm()) {
                AlarmScheduler.schedule(context, day.getDayOfWeek(), day.getBedMinutes(), false);
            }
            if (day.getWakeMinutes() != null && day.isWakeAlarm()) {
                AlarmScheduler.schedule(context, day.getDayOfWeek(), day.getWakeMinutes(), true);
            }
        }
    }
}

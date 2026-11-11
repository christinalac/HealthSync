package com.example.healthsync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        for (SleepEntry entry : new SleepStore(context).loadAll()) {
            if (entry.bedMinutes != null && entry.bedAlarm) {
                for (int day : entry.days) {
                    AlarmScheduler.schedule(context, day, entry.bedMinutes, false);
                }
            }
            if (entry.wakeMinutes != null && entry.wakeAlarm) {
                for (int day : entry.days) {
                    AlarmScheduler.schedule(context, day, entry.wakeMinutes, true);
                }
            }
        }
    }
}
package com.example.healthsync;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

public final class NotificationHelper {

    private NotificationHelper() { }

    private static final String BED_CHANNEL = "bedtime_reminder";
    private static final String WAKE_CHANNEL = "wake_alarm";

    // Channels are where Android stores sound/vibration/importance settings.
    // Creating one that already exists does nothing, so this is safe to call repeatedly.
    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);

        // Bedtime: normal notification sound
        NotificationChannel bed = new NotificationChannel(
                BED_CHANNEL, "Bedtime reminders", NotificationManager.IMPORTANCE_DEFAULT);
        bed.setDescription("Reminders to start winding down");

        // Wake-up: loud, uses the ALARM volume stream, vibrates
        AudioAttributes alarmSound = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel wake = new NotificationChannel(
                WAKE_CHANNEL, "Wake-up alarms", NotificationManager.IMPORTANCE_HIGH);
        wake.setDescription("Alarms for your wake-up time");
        wake.setSound(Settings.System.DEFAULT_ALARM_ALERT_URI, alarmSound);
        wake.enableVibration(true);
        wake.setVibrationPattern(new long[]{0, 500, 300, 500, 300, 500});

        manager.createNotificationChannels(Arrays.asList(bed, wake));
    }

    @SuppressLint("MissingPermission") // we check the permission manually below
    public static void show(Context context, int dayOfWeek, boolean isWake) {
        // Android 13+ requires the user to have allowed notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createChannels(context);

        // Tapping the notification opens the Sleep screen
        Intent openIntent = new Intent(context, SleepActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openApp = PendingIntent.getActivity(
                context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE);

        android.app.Notification notification =
                new NotificationCompat.Builder(context, isWake ? WAKE_CHANNEL : BED_CHANNEL)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // placeholder; swap for your own icon
                        .setContentTitle(isWake ? "Good morning!" : "Time for bed")
                        .setContentText(isWake
                                ? "Your wake-up alarm is going off"
                                : "Start winding down for a good night's sleep")
                        .setPriority(isWake ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(isWake ? NotificationCompat.CATEGORY_ALARM : NotificationCompat.CATEGORY_REMINDER)
                        .setContentIntent(openApp)
                        .setAutoCancel(true) // disappears when tapped
                        .build();

        // Same number scheme as the alarm request codes, so each day/type has its own notification
        int id = dayOfWeek * 2 + (isWake ? 1 : 0);
        NotificationManagerCompat.from(context).notify(id, notification);
    }
}

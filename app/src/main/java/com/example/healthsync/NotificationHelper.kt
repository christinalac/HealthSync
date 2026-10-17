package com.example.healthsync

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {

    private const val BED_CHANNEL = "bedtime_reminder"
    private const val WAKE_CHANNEL = "wake_alarm"

    // Channels are where Android stores sound/vibration/importance settings.
    // Creating one that already exists does nothing, so this is safe to call repeatedly.
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)

        // Bedtime: normal notification sound
        val bed = NotificationChannel(
            BED_CHANNEL, "Bedtime reminders", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders to start winding down"
        }

        // Wake-up: loud, uses the ALARM volume stream, vibrates
        val alarmSound = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val wake = NotificationChannel(
            WAKE_CHANNEL, "Wake-up alarms", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarms for your wake-up time"
            setSound(Settings.System.DEFAULT_ALARM_ALERT_URI, alarmSound)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 300, 500, 300, 500)
        }

        manager.createNotificationChannels(listOf(bed, wake))
    }

    @SuppressLint("MissingPermission") // we check the permission manually below
    fun show(context: Context, dayOfWeek: Int, isWake: Boolean) {
        // Android 13+ requires the user to have allowed notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        createChannels(context)

        // Tapping the notification opens the Sleep screen
        val openIntent = Intent(context, SleepActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openApp = PendingIntent.getActivity(
            context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, if (isWake) WAKE_CHANNEL else BED_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // placeholder; swap for your own icon
            .setContentTitle(if (isWake) "Good morning!" else "Time for bed")
            .setContentText(if (isWake) "Your wake-up alarm is going off" else "Start winding down for a good night's sleep")
            .setPriority(if (isWake) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(if (isWake) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openApp)
            .setAutoCancel(true) // disappears when tapped
            .build()

        // Same number scheme as the alarm request codes, so each day/type has its own notification
        val id = dayOfWeek * 2 + if (isWake) 1 else 0
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
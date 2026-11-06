package com.example.healthsync;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

public class AlarmService extends Service {

    public static final String ACTION_STOP = "com.example.healthsync.STOP_ALARM";
    private static final String CHANNEL_ID = "alarm_ringing";
    private static final int NOTIF_ID = 1001;
    private static final long AUTO_STOP_MS = 5 * 60 * 1000L; // stop after 5 minutes

    @Nullable private MediaPlayer player;
    @Nullable private Vibrator vibrator;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoStop = this::stopAlarm;

    // Not a bound service, so there's nothing to return here
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The Stop button on the notification sends us here with ACTION_STOP
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopAlarm();
            return START_NOT_STICKY;
        }

        createChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                    this, NOTIF_ID, buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIF_ID, buildNotification());
        }

        startSound();
        startVibration();
        handler.removeCallbacks(autoStop);
        handler.postDelayed(autoStop, AUTO_STOP_MS);

        return START_NOT_STICKY;
    }

    private void startSound() {
        stopSound(); // in case an alarm is already ringing
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

            player = new MediaPlayer();
            player.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM) // plays on the alarm volume
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build());
            player.setDataSource(this, uri);
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (Exception e) {
            Log.e("SleepAlarm", "Could not play alarm sound", e);
        }
    }

    @SuppressWarnings("deprecation")
    private void startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibrator = getSystemService(VibratorManager.class).getDefaultVibrator();
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        long[] pattern = {0, 600, 400};
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        } else {
            vibrator.vibrate(pattern, 0); // 0 = repeat from the start
        }
    }

    private void stopSound() {
        if (player != null) {
            if (player.isPlaying()) player.stop();
            player.release();
        }
        player = null;
    }

    private void stopAlarm() {
        handler.removeCallbacks(autoStop);
        stopSound();
        if (vibrator != null) vibrator.cancel();
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(autoStop);
        stopSound();
        if (vibrator != null) vibrator.cancel();
        super.onDestroy();
    }

    // Silent channel: the service plays the sound itself, so the notification must not also beep
    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return; // channels don't exist before Android 8

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Alarm ringing", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Shown while your wake-up alarm is ringing");
        channel.setSound(null, null);
        channel.enableVibration(false);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        Intent openIntent = new Intent(this, SleepActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openApp = PendingIntent.getActivity(
                this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE);

        PendingIntent stop = PendingIntent.getService(
                this, 1,
                new Intent(this, AlarmService.class).setAction(ACTION_STOP),
                PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // swap for your own icon
                .setContentTitle("Wake up!")
                .setContentText("Your alarm is ringing")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setContentIntent(openApp)
                .addAction(0, "Stop", stop)
                .build();
    }
}

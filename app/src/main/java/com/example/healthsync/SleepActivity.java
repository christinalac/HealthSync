package com.example.healthsync;

import android.Manifest;
import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// BottomNavigationView is the Material component that renders the tab bar at the bottom
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

// SleepActivity is the Sleep screen — Tab 4.
// Shows a placeholder sleep score, last night's stats, an "Add Sleep Entry" button,
// and a weekly bedtime/wake-up schedule with optional alarms.
public class SleepActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> { /* granted or not */ });

    // Weekly schedule pieces
    private SleepStore store;
    private SleepAdapter adapter;
    private List<SleepDay> days;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Link to the XML layout for this screen
        setContentView(R.layout.sleep_activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
        // ---- Weekly schedule setup ----
        store = new SleepStore(this);
        days = store.load();
        adapter = new SleepAdapter(days, this::pickTime, this::toggleAlarm);

        RecyclerView sleepList = findViewById(R.id.sleepList);
        sleepList.setLayoutManager(new LinearLayoutManager(this));
        sleepList.setAdapter(adapter);

        // ---- Bottom navigation ----
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Highlight the Sleep tab so the user knows which screen they're on
        bottomNav.setSelectedItemId(R.id.nav_sleep);

        // Listen for tab taps and navigate to the matching Activity
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ProgressActivity.class));
                return true;
            } else if (id == R.id.nav_nutrition) {
                startActivity(new Intent(this, NutritionActivity.class));
                return true;
            } else if (id == R.id.nav_workout) {
                startActivity(new Intent(this, WorkoutActivity.class));
                return true;
            } else if (id == R.id.nav_sleep) {
                // Already on this screen — do nothing
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return false;
        });
    }

    // Called by the adapter when the user taps a bedtime or wake-up button.
    // Shows a clock dialog, saves the chosen time, and moves the alarm if it was on.
    private void pickTime(final SleepDay day, final boolean isWake) {
        Integer existing = isWake ? day.getWakeMinutes() : day.getBedMinutes();
        int current = existing != null ? existing : (22 * 60);

        new TimePickerDialog(this, (view, hour, minute) -> {
            int total = hour * 60 + minute;
            if (isWake) day.setWakeMinutes(total); else day.setBedMinutes(total);
            store.save(day);

            // If the alarm was already on, move it to the new time
            boolean alarmOn = isWake ? day.isWakeAlarm() : day.isBedAlarm();
            if (alarmOn) AlarmScheduler.schedule(this, day.getDayOfWeek(), total, isWake);

            adapter.notifyItemChanged(days.indexOf(day));
        }, current / 60, current % 60, false).show();
    }

    // Called by the adapter when the user flips an alarm switch.
    private void toggleAlarm(SleepDay day, boolean isWake, boolean enabled) {
        Integer minutesBoxed = isWake ? day.getWakeMinutes() : day.getBedMinutes();
        if (minutesBoxed == null) return;
        int minutes = minutesBoxed;

        // ask for exact-alarm permission before turning on a wake-up alarm
        if (enabled && isWake && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!am.canScheduleExactAlarms()) {
                startActivity(new Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName())));
                adapter.notifyItemChanged(days.indexOf(day)); // put the switch back to off
                return;
            }
        }
        if (enabled) {
            boolean ok = AlarmScheduler.schedule(this, day.getDayOfWeek(), minutes, isWake);
            if (!ok) {
                // Exact-alarm permission missing: redraw the row so the switch goes back to off
                adapter.notifyItemChanged(days.indexOf(day));
                return;
            }
        } else {
            AlarmScheduler.cancel(this, day.getDayOfWeek(), minutes, isWake);
        }

        if (isWake) day.setWakeAlarm(enabled); else day.setBedAlarm(enabled);
        store.save(day);
    }
}

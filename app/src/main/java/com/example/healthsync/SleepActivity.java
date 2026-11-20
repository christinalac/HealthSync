//******************************************
// Program Name: SleepActivity.java
// Developer: Anthony Ramirez and Christina LaCombe
// Date Created: 11/11/2026
// Version: 1.2
// Purpose: Sleep screen (Tab 4): last night's sleep stats, plus a weekly schedule of bedtime / wake-up times with optional alarms.
//******************************************
package com.example.healthsync;

import android.Manifest;
import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// BottomNavigationView is the Material component that renders the tab bar at the bottom
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.function.IntConsumer;

// Sleep screen (Tab 4): placeholder score/stats on top, then cards of days with
// optional bedtime / wake-up times and alarms.
public class SleepActivity extends AppCompatActivity implements SleepAdapter.Actions {

    // Dialogs on this screen use ThemeOverlay.HealthSync.SleepDialog (green accents, dark green buttons)
    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    private SleepStore store;
    private SleepAdapter adapter;
    private List<SleepEntry> entries;
    private RecyclerView sleepList;
    private TextView emptyText;
    // sleep goal
    private static final int GOAL_MINUTES = 8 * 60;
    private TextView textBedtime, textWakeTime, textSleepDuration, textSleepGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sleep_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        // ---- Schedule list ----
        store = new SleepStore(this);
        entries = store.loadAll();
        adapter = new SleepAdapter(entries, this);

        sleepList = findViewById(R.id.sleepList);
        emptyText = findViewById(R.id.textEmpty);
        sleepList.setLayoutManager(new LinearLayoutManager(this));
        sleepList.setAdapter(adapter);

        Button addDay = findViewById(R.id.buttonAddDay);
        addDay.setOnClickListener(v -> showDayPicker(null));
        updateEmptyState();
        textBedtime = findViewById(R.id.textBedtime);
        textWakeTime = findViewById(R.id.textWakeTime);
        textSleepDuration = findViewById(R.id.textSleepDuration);
        textSleepGoal = findViewById(R.id.textSleepGoal);

        findViewById(R.id.buttonAddSleep).setOnClickListener(v -> showSleepEntryDialog());
        // restore the saved entry, if any
        showLastNight(store.loadLastNight());
        // ---- Bottom navigation (unchanged) ----
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_sleep);
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
                // already here
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return false;
        });
    }

    // ---------- Adding, editing and removing days ----------

    // existing == null means "add a new card"; otherwise "edit this card's days"
    private void showDayPicker(SleepEntry existing) {
        Set<Integer> usedElsewhere = new HashSet<>();
        List<Integer> availIds = new ArrayList<>();
        List<String> availNames = new ArrayList<>();
        String dialogTitle;
        if (existing == null) {
            dialogTitle = "Add days";
        } else {
            dialogTitle = "Edit days";
        }

        // Days already used by OTHER cards can't be picked again
        for (SleepEntry otherEntry : entries) {
            if (existing == null || otherEntry.id != existing.id) usedElsewhere.addAll(otherEntry.days);
        }

        for (int i = 0; i < SleepEntry.WEEK_DAYS.length; i++) {
            if (!usedElsewhere.contains(SleepEntry.WEEK_DAYS[i])) {
                availIds.add(SleepEntry.WEEK_DAYS[i]);
                availNames.add(SleepEntry.WEEK_NAMES[i]);
            }
        }

        if (availIds.isEmpty()) {
            Toast.makeText(this, "All days are already added", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = availNames.toArray(new String[0]);
        boolean[] checked = new boolean[names.length];
        for (int i = 0; i < checked.length; i++) {
            checked[i] = existing != null && existing.days.contains(availIds.get(i));
        }

        new AlertDialog.Builder(this, R.style.ThemeOverlay_HealthSync_SleepDialog)
                .setTitle(dialogTitle)
                .setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("OK", (dialog, which) -> {
                    Set<Integer> picked = new HashSet<>();
                    for (int i = 0; i < checked.length; i++) {
                        if (checked[i]) picked.add(availIds.get(i));
                    }
                    if (picked.isEmpty()) {
                        Toast.makeText(this, "Pick at least one day", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (existing == null) addEntry(picked); else changeDays(existing, picked);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addEntry(Set<Integer> days) {
        entries.add(new SleepEntry(System.currentTimeMillis(), days));
        store.saveAll(entries);
        adapter.notifyItemInserted(entries.size() - 1);
        sleepList.scrollToPosition(entries.size() - 1);
        updateEmptyState();
    }

    private void changeDays(SleepEntry entry, Set<Integer> newDays) {
        // cancel using the OLD days
        cancelAlarm(entry, false);
        cancelAlarm(entry, true);
        entry.days = newDays;
        // re-schedule for the NEW days (only if the alarm is on)
        scheduleAlarm(entry, false);
        scheduleAlarm(entry, true);
        store.saveAll(entries);
        adapter.notifyItemChanged(entries.indexOf(entry));
    }

    @Override
    public void onEditDays(SleepEntry entry) {
        showDayPicker(entry);
    }

    @Override
    public void onRemove(SleepEntry entry) {
        new AlertDialog.Builder(this, R.style.ThemeOverlay_HealthSync_SleepDialog)
                .setTitle("Remove " + entry.label() + "?")
                .setMessage("This deletes its times and cancels its alarms.")
                .setPositiveButton("Remove", (dialog, which) -> {
                    cancelAlarm(entry, false);
                    cancelAlarm(entry, true);
                    int index = entries.indexOf(entry);
                    entries.remove(index);
                    store.saveAll(entries);
                    adapter.notifyItemRemoved(index);
                    updateEmptyState();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------- Times and alarms ----------

    @Override
    public void onPickTime(SleepEntry entry, boolean isWake) {
        Integer saved = entry.getMinutes(isWake);
        int current;
        if (saved != null) {
            current = saved;
        } else if (isWake) {
            current = 7 * 60;
        } else {
            current = 22 * 60;
        }

        new TimePickerDialog(timePickerContext(), (view, hour, minute) -> {
            // remove alarms at the old time
            cancelAlarm(entry, isWake);
            entry.setMinutes(isWake, hour * 60 + minute);
            // does nothing unless the alarm switch is on
            scheduleAlarm(entry, isWake);
            store.saveAll(entries);
            adapter.notifyItemChanged(entries.indexOf(entry));
        }, current / 60, current % 60, false).show();
    }

    @Override
    public void onClearTime(SleepEntry entry, boolean isWake) {
        cancelAlarm(entry, isWake);
        entry.setMinutes(isWake, null);
        entry.setAlarmOn(isWake, false);
        store.saveAll(entries);
        adapter.notifyItemChanged(entries.indexOf(entry));
    }

    @Override
    public void onToggleAlarm(SleepEntry entry, boolean isWake, boolean enabled) {
        if (entry.getMinutes(isWake) == null) return;

        // Wake-up alarms need the exact-alarm permission; send the user to grant it
        if (enabled && isWake && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // Explain the extra step, since the settings screen does not say what to do
                Toast.makeText(this, "Allow \"Alarms & reminders\" for HealthSync, then turn the alarm on again",
                        Toast.LENGTH_LONG).show();
                // Open the exact-alarm settings; some devices lack that screen, so fall back to app info
                try {
                    startActivity(new Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:" + getPackageName())));
                } catch (RuntimeException exception) {
                    // Some devices have no such settings screen; fall back to the app's info page
                    startActivity(new Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + getPackageName())));
                }
                // Switch back to off. Posted because we're inside the switch's own listener.
                int index = entries.indexOf(entry);
                sleepList.post(() -> adapter.notifyItemChanged(index));
                return;
            }
        }

        if (enabled) {
            entry.setAlarmOn(isWake, true);
            if (!scheduleAlarm(entry, isWake)) {
                cancelAlarm(entry, isWake);
                entry.setAlarmOn(isWake, false);
                int index = entries.indexOf(entry);
                sleepList.post(() -> adapter.notifyItemChanged(index));
                return;
            }
        } else {
            cancelAlarm(entry, isWake);
            entry.setAlarmOn(isWake, false);
        }
        store.saveAll(entries);
    }

    // ---------- Helpers ----------

    // One alarm per day in the card. Returns false if any of them was refused.
    private boolean scheduleAlarm(SleepEntry entry, boolean isWake) {
        Integer minutes = entry.getMinutes(isWake);
        boolean allOk = true;
        if (minutes == null || !entry.isAlarmOn(isWake)) return true;

        for (int day : entry.days) {
            if (!AlarmScheduler.schedule(this, day, minutes, isWake)) allOk = false;
        }
        return allOk;
    }

    private void cancelAlarm(SleepEntry entry, boolean isWake) {
        Integer minutes = entry.getMinutes(isWake);
        if (minutes == null) return;
        for (int day : entry.days) {
            AlarmScheduler.cancel(this, day, minutes, isWake);
        }
    }

    // Context whose time pickers use the app's green accent instead of the default light blue
    private ContextThemeWrapper timePickerContext() {
        return new ContextThemeWrapper(this, R.style.ThemeOverlay_HealthSync_SleepTimePicker);
    }

    private void updateEmptyState() {
        int visibility;
        if (entries.isEmpty()) {
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }
        emptyText.setVisibility(visibility);
    }

    // ---------- Logging last night's sleep ----------

    private void showSleepEntryDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_sleep_entry, null);
        Button bedBtn = view.findViewById(R.id.dialogBedButton);
        Button wakeBtn = view.findViewById(R.id.dialogWakeButton);
        TextView preview = view.findViewById(R.id.dialogPreview);

        // {bed, wake}; -1 means "not chosen yet". Start from the saved entry if there is one.
        int[] saved = store.loadLastNight();
        int[] times;
        if (saved != null) {
            times = saved.clone();
        } else {
            times = new int[]{-1, -1};
        }

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_HealthSync_SleepDialog)
                .setTitle("Last night's sleep")
                .setView(view)
                .setPositiveButton("Save", (dialogInterface, which) -> {
                    store.saveLastNight(times[0], times[1]);
                    // Record the hours slept for today so the Progress page sleep graph fills in
                    new ProgressHistory(this).recordSleep(sleepDuration(times[0], times[1]) / 60.0);
                    showLastNight(times);
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();

        Runnable refresh = () -> {
            String bedText;
            String wakeText;
            if (times[0] < 0) {
                bedText = "Tap to set";
            } else {
                bedText = formatTime(times[0]);
            }
            if (times[1] < 0) {
                wakeText = "Tap to set";
            } else {
                wakeText = formatTime(times[1]);
            }
            bedBtn.setText(bedText);
            wakeBtn.setText(wakeText);

            boolean ready = times[0] >= 0 && times[1] >= 0 && times[0] != times[1];
            if (ready) {
                preview.setText("You slept " + formatDuration(sleepDuration(times[0], times[1])));
            } else if (times[0] >= 0 && times[0] == times[1]) {
                preview.setText("Bedtime and wake-up can't be the same");
            } else {
                // Save stays disabled until both times are set; this tells the user why
                preview.setText("Tap both buttons to set your times");
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(ready);
        };

        bedBtn.setOnClickListener(v -> pickSleepTime(times[0], 22 * 60 + 30, pickedMinutes -> {
            times[0] = pickedMinutes;
            refresh.run();
        }));
        wakeBtn.setOnClickListener(v -> pickSleepTime(times[1], 7 * 60, pickedMinutes -> {
            times[1] = pickedMinutes;
            refresh.run();
        }));
        refresh.run();
    }

    private void pickSleepTime(int current, int fallback, IntConsumer onPicked) {
        int start;
        if (current >= 0) {
            start = current;
        } else {
            start = fallback;
        }
        new TimePickerDialog(timePickerContext(), (view, hour, minute) -> onPicked.accept(hour * 60 + minute),
                start / 60, start % 60, false).show();
    }

    // Minutes slept. If the wake time is earlier on the clock than bedtime, the sleep
    // crossed midnight, so add a day (e.g. 10:30 PM -> 6:30 AM = 8h).
    private int sleepDuration(int bed, int wake) {
        int diff = wake - bed;
        if (diff <= 0) diff += 24 * 60;
        return diff;
    }

    private void showLastNight(int[] times) {
        textSleepGoal.setText("Goal:  " + formatDuration(GOAL_MINUTES));
        if (times == null) {
            textBedtime.setText("Bedtime:    --:--");
            textWakeTime.setText("Wake Time:  --:--");
            textSleepDuration.setText("Slept:  --");
            return;
        }

        int slept = sleepDuration(times[0], times[1]);
        textBedtime.setText("Bedtime:    " + formatTime(times[0]));
        textWakeTime.setText("Wake Time:  " + formatTime(times[1]));
        textSleepDuration.setText("Slept:  " + formatDuration(slept));

        if (slept >= GOAL_MINUTES) {
            textSleepGoal.setText("Goal:  " + formatDuration(GOAL_MINUTES) + "  -  reached!");
        } else {
            textSleepGoal.setText("Goal:  " + formatDuration(GOAL_MINUTES) + "  -  "
                    + formatDuration(GOAL_MINUTES - slept) + " short");
        }
    }

    private String formatTime(int minutes) {
        int hour24 = minutes / 60;
        int minuteOfHour = minutes % 60;
        String amPm;
        int hour12;
        if (hour24 < 12) {
            amPm = "AM";
        } else {
            amPm = "PM";
        }
        if (hour24 % 12 == 0) {
            hour12 = 12;
        } else {
            hour12 = hour24 % 12;
        }
        return String.format(Locale.getDefault(), "%d:%02d %s", hour12, minuteOfHour, amPm);
    }

    private String formatDuration(int minutes) {
        int hours = minutes / 60;
        int minutesPart = minutes % 60;
        if (minutesPart == 0) return hours + "h";
        if (hours == 0) return minutesPart + "m";
        return hours + "h " + minutesPart + "m";
    }
}
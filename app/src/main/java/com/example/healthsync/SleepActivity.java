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

// Sleep screen (Tab 4): placeholder score/stats on top, then cards of days with
// optional bedtime / wake-up times and alarms.
public class SleepActivity extends AppCompatActivity implements SleepAdapter.Actions {

    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    private SleepStore store;
    private SleepAdapter adapter;
    private List<SleepEntry> entries;
    private RecyclerView sleepList;
    private TextView emptyText;

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
                return true; // already here
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
        // Days already used by OTHER cards can't be picked again
        Set<Integer> usedElsewhere = new HashSet<>();
        for (SleepEntry e : entries) {
            if (existing == null || e.id != existing.id) usedElsewhere.addAll(e.days);
        }

        List<Integer> availIds = new ArrayList<>();
        List<String> availNames = new ArrayList<>();
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

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Add days" : "Edit days")
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
        cancelAlarm(entry, false);   // cancel using the OLD days
        cancelAlarm(entry, true);
        entry.days = newDays;
        scheduleAlarm(entry, false); // re-schedule for the NEW days (only if the alarm is on)
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
        new AlertDialog.Builder(this)
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
        int current = saved != null ? saved : (isWake ? 7 * 60 : 22 * 60);

        new TimePickerDialog(this, (view, hour, minute) -> {
            cancelAlarm(entry, isWake); // remove alarms at the old time
            entry.setMinutes(isWake, hour * 60 + minute);
            scheduleAlarm(entry, isWake); // does nothing unless the alarm switch is on
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
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!am.canScheduleExactAlarms()) {
                startActivity(new Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName())));
                adapter.notifyItemChanged(entries.indexOf(entry)); // switch back to off
                return;
            }
        }

        if (enabled) {
            entry.setAlarmOn(isWake, true);
            if (!scheduleAlarm(entry, isWake)) {
                cancelAlarm(entry, isWake);
                entry.setAlarmOn(isWake, false);
                adapter.notifyItemChanged(entries.indexOf(entry));
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
    private boolean scheduleAlarm(SleepEntry e, boolean isWake) {
        Integer minutes = e.getMinutes(isWake);
        if (minutes == null || !e.isAlarmOn(isWake)) return true;

        boolean allOk = true;
        for (int day : e.days) {
            if (!AlarmScheduler.schedule(this, day, minutes, isWake)) allOk = false;
        }
        return allOk;
    }

    private void cancelAlarm(SleepEntry e, boolean isWake) {
        Integer minutes = e.getMinutes(isWake);
        if (minutes == null) return;
        for (int day : e.days) {
            AlarmScheduler.cancel(this, day, minutes, isWake);
        }
    }

    private void updateEmptyState() {
        emptyText.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
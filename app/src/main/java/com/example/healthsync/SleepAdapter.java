//******************************************
// Program Name: SleepAdapter.java
// Developer: Anthony Ramirez and Christina LaCombe
// Date Created: 11/11/2026
// Version: 1.1
// Purpose: RecyclerView adapter that draws one card per SleepEntry on the Sleep screen.
//******************************************
package com.example.healthsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthsync.databinding.ItemSleepEntryBinding;

import java.util.List;
import java.util.Locale;

public class SleepAdapter extends RecyclerView.Adapter<SleepAdapter.VH> {

    // Everything a card can ask the screen to do. SleepActivity implements this.
    public interface Actions {
        void onPickTime(SleepEntry entry, boolean isWake);
        void onClearTime(SleepEntry entry, boolean isWake);
        void onToggleAlarm(SleepEntry entry, boolean isWake, boolean enabled);
        void onEditDays(SleepEntry entry);
        void onRemove(SleepEntry entry);
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemSleepEntryBinding b;

        VH(ItemSleepEntryBinding itemBinding) {
            super(itemBinding.getRoot());
            this.b = itemBinding;
        }
    }

    private final List<SleepEntry> entries;
    private final Actions actions;

    public SleepAdapter(List<SleepEntry> entries, Actions actions) {
        this.entries = entries;
        this.actions = actions;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemSleepEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SleepEntry entry = entries.get(position);
        ItemSleepEntryBinding binding = holder.b;

        binding.dayLabel.setText(entry.label());
        binding.editDaysButton.setOnClickListener(v -> actions.onEditDays(entry));
        binding.removeButton.setOnClickListener(v -> actions.onRemove(entry));

        bindTime(binding.bedTimeButton, binding.bedAlarmSwitch, binding.bedClearButton, entry, false);
        bindTime(binding.wakeTimeButton, binding.wakeAlarmSwitch, binding.wakeClearButton, entry, true);
    }

    // Same setup for bedtime and wake-up, so it's written once
    private void bindTime(Button timeButton, SwitchCompat alarmSwitch, View clearButton,
                          SleepEntry entry, boolean isWake) {
        Integer minutes = entry.getMinutes(isWake);
        String placeholder;
        int clearVisibility;

        if (isWake) {
            placeholder = "Add wake-up";
        } else {
            placeholder = "Add bedtime";
        }
        if (minutes == null) {
            clearVisibility = View.GONE;
        } else {
            clearVisibility = View.VISIBLE;
        }

        timeButton.setText(formatTime(minutes, placeholder));
        timeButton.setOnClickListener(v -> actions.onPickTime(entry, isWake));

        clearButton.setVisibility(clearVisibility);
        clearButton.setOnClickListener(v -> actions.onClearTime(entry, isWake));

        // Clear the old listener before setChecked, or reused rows fire false events
        alarmSwitch.setOnCheckedChangeListener(null);
        alarmSwitch.setChecked(entry.isAlarmOn(isWake));
        alarmSwitch.setEnabled(minutes != null);
        alarmSwitch.setOnCheckedChangeListener((button, checked) ->
                actions.onToggleAlarm(entry, isWake, checked));
    }

    private String formatTime(Integer minutes, String placeholder) {
        int hour24 = 0;
        int minuteOfHour = 0;
        String amPm;
        int hour12;

        if (minutes == null) return placeholder;
        hour24 = minutes / 60;
        minuteOfHour = minutes % 60;
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
}
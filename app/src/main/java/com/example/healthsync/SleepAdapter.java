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

        VH(ItemSleepEntryBinding b) {
            super(b.getRoot());
            this.b = b;
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
        ItemSleepEntryBinding b = holder.b;

        b.dayLabel.setText(entry.label());
        b.editDaysButton.setOnClickListener(v -> actions.onEditDays(entry));
        b.removeButton.setOnClickListener(v -> actions.onRemove(entry));

        bindTime(b.bedTimeButton, b.bedAlarmSwitch, b.bedClearButton, entry, false);
        bindTime(b.wakeTimeButton, b.wakeAlarmSwitch, b.wakeClearButton, entry, true);
    }

    // Same setup for bedtime and wake-up, so it's written once
    private void bindTime(Button timeButton, SwitchCompat alarmSwitch, View clearButton,
                          SleepEntry entry, boolean isWake) {
        Integer minutes = entry.getMinutes(isWake);

        timeButton.setText(formatTime(minutes, isWake ? "Add wake-up" : "Add bedtime"));
        timeButton.setOnClickListener(v -> actions.onPickTime(entry, isWake));

        clearButton.setVisibility(minutes == null ? View.GONE : View.VISIBLE);
        clearButton.setOnClickListener(v -> actions.onClearTime(entry, isWake));

        // Clear the old listener before setChecked, or reused rows fire false events
        alarmSwitch.setOnCheckedChangeListener(null);
        alarmSwitch.setChecked(entry.isAlarmOn(isWake));
        alarmSwitch.setEnabled(minutes != null);
        alarmSwitch.setOnCheckedChangeListener((button, checked) ->
                actions.onToggleAlarm(entry, isWake, checked));
    }

    private String formatTime(Integer minutes, String placeholder) {
        if (minutes == null) return placeholder;
        int h = minutes / 60;
        int m = minutes % 60;
        String amPm = h < 12 ? "AM" : "PM";
        int h12 = (h % 12 == 0) ? 12 : h % 12;
        return String.format(Locale.getDefault(), "%d:%02d %s", h12, m, amPm);
    }
}
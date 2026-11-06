package com.example.healthsync;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthsync.databinding.ItemSleepDayBinding;

import java.util.List;
import java.util.Locale;

public class SleepAdapter extends RecyclerView.Adapter<SleepAdapter.VH> {

    // (day, isWake)
    public interface OnPickTime {
        void onPickTime(SleepDay day, boolean isWake);
    }

    // (day, isWake, enabled)
    public interface OnToggleAlarm {
        void onToggleAlarm(SleepDay day, boolean isWake, boolean enabled);
    }

    private final List<SleepDay> days;
    private final OnPickTime onPickTime;
    private final OnToggleAlarm onToggleAlarm;

    public SleepAdapter(List<SleepDay> days, OnPickTime onPickTime, OnToggleAlarm onToggleAlarm) {
        this.days = days;
        this.onPickTime = onPickTime;
        this.onToggleAlarm = onToggleAlarm;
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemSleepDayBinding b;

        VH(ItemSleepDayBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemSleepDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final SleepDay day = days.get(position);
        ItemSleepDayBinding b = holder.b;

        b.dayName.setText(day.getName());
        b.bedTimeButton.setText(formatTime(day.getBedMinutes(), "Set bedtime"));
        b.wakeTimeButton.setText(formatTime(day.getWakeMinutes(), "Set wake-up"));

        b.bedTimeButton.setOnClickListener(v -> onPickTime.onPickTime(day, false));
        b.wakeTimeButton.setOnClickListener(v -> onPickTime.onPickTime(day, true));

        // Clear old listeners before setting isChecked, or rows being reused fire false events
        b.bedAlarmSwitch.setOnCheckedChangeListener(null);
        b.bedAlarmSwitch.setChecked(day.isBedAlarm());
        b.bedAlarmSwitch.setEnabled(day.getBedMinutes() != null);
        b.bedAlarmSwitch.setOnCheckedChangeListener(
                (buttonView, checked) -> onToggleAlarm.onToggleAlarm(day, false, checked));

        b.wakeAlarmSwitch.setOnCheckedChangeListener(null);
        b.wakeAlarmSwitch.setChecked(day.isWakeAlarm());
        b.wakeAlarmSwitch.setEnabled(day.getWakeMinutes() != null);
        b.wakeAlarmSwitch.setOnCheckedChangeListener(
                (buttonView, checked) -> onToggleAlarm.onToggleAlarm(day, true, checked));
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

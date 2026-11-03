package com.example.healthsync
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import com.example.healthsync.databinding.ItemSleepDayBinding  // change to your package

class SleepAdapter(
    private val days: List<SleepDay>,
    private val onPickTime: (SleepDay, Boolean) -> Unit,            // (day, isWake)
    private val onToggleAlarm: (SleepDay, Boolean, Boolean) -> Unit // (day, isWake, enabled)
) : RecyclerView.Adapter<SleepAdapter.VH>() {

    class VH(val b: ItemSleepDayBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSleepDayBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val day = days[position]
        val b = holder.b

        b.dayName.text = day.name
        b.bedTimeButton.text = formatTime(day.bedMinutes, "Set bedtime")
        b.wakeTimeButton.text = formatTime(day.wakeMinutes, "Set wake-up")

        b.bedTimeButton.setOnClickListener { onPickTime(day, false) }
        b.wakeTimeButton.setOnClickListener { onPickTime(day, true) }

        // Clear old listeners before setting isChecked, or rows being reused fire false events
        b.bedAlarmSwitch.setOnCheckedChangeListener(null)
        b.bedAlarmSwitch.isChecked = day.bedAlarm
        b.bedAlarmSwitch.isEnabled = day.bedMinutes != null
        b.bedAlarmSwitch.setOnCheckedChangeListener { _, checked ->
            onToggleAlarm(day, false, checked)
        }

        b.wakeAlarmSwitch.setOnCheckedChangeListener(null)
        b.wakeAlarmSwitch.isChecked = day.wakeAlarm
        b.wakeAlarmSwitch.isEnabled = day.wakeMinutes != null
        b.wakeAlarmSwitch.setOnCheckedChangeListener { _, checked ->
            onToggleAlarm(day, true, checked)
        }
    }

    private fun formatTime(minutes: Int?, placeholder: String): String {
        if (minutes == null) return placeholder
        val h = minutes / 60
        val m = minutes % 60
        val amPm = if (h < 12) "AM" else "PM"
        val h12 = if (h % 12 == 0) 12 else h % 12
        return String.format(Locale.getDefault(), "%d:%02d %s", h12, m, amPm)
    }
}
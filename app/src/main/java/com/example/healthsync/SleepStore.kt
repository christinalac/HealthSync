package com.example.healthsync
import android.content.Context

class SleepStore(context: Context) {
    private val prefs = context.getSharedPreferences("sleep_prefs", Context.MODE_PRIVATE)

    fun load(): List<SleepDay> = WEEK.map { (dow, name) ->
        SleepDay(
            dayOfWeek = dow,
            name = name,
            bedMinutes = prefs.getInt("$dow-bed", -1).takeIf { it >= 0 },
            wakeMinutes = prefs.getInt("$dow-wake", -1).takeIf { it >= 0 },
            bedAlarm = prefs.getBoolean("$dow-bedAlarm", false),
            wakeAlarm = prefs.getBoolean("$dow-wakeAlarm", false)
        )
    }

    fun save(day: SleepDay) {
        prefs.edit()
            .putInt("${day.dayOfWeek}-bed", day.bedMinutes ?: -1)
            .putInt("${day.dayOfWeek}-wake", day.wakeMinutes ?: -1)
            .putBoolean("${day.dayOfWeek}-bedAlarm", day.bedAlarm)
            .putBoolean("${day.dayOfWeek}-wakeAlarm", day.wakeAlarm)
            .apply()
    }
}
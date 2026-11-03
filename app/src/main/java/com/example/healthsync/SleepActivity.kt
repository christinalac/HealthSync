package com.example.healthsync

import android.app.AlarmManager
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// BottomNavigationView is the Material component that renders the tab bar at the bottom
import com.google.android.material.bottomnavigation.BottomNavigationView

// SleepActivity is the Sleep screen — Tab 4.
// Shows a placeholder sleep score, last night's stats, an "Add Sleep Entry" button,
// and a weekly bedtime/wake-up schedule with optional alarms.
class SleepActivity : AppCompatActivity() {
    private val notifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or not */ }
    // Weekly schedule pieces
    private lateinit var store: SleepStore
    private lateinit var adapter: SleepAdapter
    private lateinit var days: List<SleepDay>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Link to the XML layout for this screen
        setContentView(R.layout.sleep_activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // ---- Weekly schedule setup ----
        store = SleepStore(this)
        days = store.load()
        adapter = SleepAdapter(days, ::pickTime, ::toggleAlarm)

        val sleepList = findViewById<RecyclerView>(R.id.sleepList)
        sleepList.layoutManager = LinearLayoutManager(this)
        sleepList.adapter = adapter

        // ---- Bottom navigation (unchanged) ----
        // Find the BottomNavigationView in the XML layout
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Highlight the Sleep tab so the user knows which screen they're on
        bottomNav.selectedItemId = R.id.nav_sleep

        // Listen for tab taps and navigate to the matching Activity
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, ProgressActivity::class.java))
                    true
                }
                R.id.nav_nutrition -> {
                    startActivity(Intent(this, NutritionActivity::class.java))
                    true
                }
                R.id.nav_workout -> {
                    startActivity(Intent(this, WorkoutActivity::class.java))
                    true
                }
                R.id.nav_sleep -> {
                    // Already on this screen — do nothing
                    true
                }
                R.id.nav_account -> {
                    startActivity(Intent(this, AccountActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // Called by the adapter when the user taps a bedtime or wake-up button.
    // Shows a clock dialog, saves the chosen time, and moves the alarm if it was on.
    private fun pickTime(day: SleepDay, isWake: Boolean) {
        val current = (if (isWake) day.wakeMinutes else day.bedMinutes) ?: (22 * 60)

        TimePickerDialog(this, { _, hour, minute ->
            val total = hour * 60 + minute
            if (isWake) day.wakeMinutes = total else day.bedMinutes = total
            store.save(day)

            // If the alarm was already on, move it to the new time
            val alarmOn = if (isWake) day.wakeAlarm else day.bedAlarm
            if (alarmOn) AlarmScheduler.schedule(this, day.dayOfWeek, total, isWake)

            adapter.notifyItemChanged(days.indexOf(day))
        }, current / 60, current % 60, false).show()
    }

    // Called by the adapter when the user flips an alarm switch.
    private fun toggleAlarm(day: SleepDay, isWake: Boolean, enabled: Boolean) {
        val minutes = (if (isWake) day.wakeMinutes else day.bedMinutes) ?: return
        // NEW: ask for exact-alarm permission before turning on a wake-up alarm
        if (enabled && isWake && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:$packageName")
                    )
                )
                adapter.notifyItemChanged(days.indexOf(day)) // put the switch back to off
                return
            }
        }
        if (enabled) {
            val ok = AlarmScheduler.schedule(this, day.dayOfWeek, minutes, isWake)
            if (!ok) {
                // Exact-alarm permission missing: redraw the row so the switch goes back to off
                adapter.notifyItemChanged(days.indexOf(day))
                return
            }
        } else {
            AlarmScheduler.cancel(this, day.dayOfWeek, minutes, isWake)
        }

        if (isWake) day.wakeAlarm = enabled else day.bedAlarm = enabled
        store.save(day)
    }
}
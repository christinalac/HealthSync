package com.example.healthsync

// Intent lets us launch other Activities (screens)
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
// BottomNavigationView is the Material component that renders the tab bar at the bottom
import com.google.android.material.bottomnavigation.BottomNavigationView

// WorkoutActivity is the Workout screen — Tab 3.
// Shows placeholder exercise entries for today's plan.
// The "+" FAB is visible in the layout but not yet wired to any action.
class WorkoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Link to the XML layout for this screen
        setContentView(R.layout.workout_activity)

        // Find the BottomNavigationView in the XML layout
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Highlight the Workout tab so the user knows which screen they're on
        bottomNav.selectedItemId = R.id.nav_workout

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
                    // Already on this screen — do nothing
                    true
                }
                R.id.nav_sleep -> {
                    startActivity(Intent(this, SleepActivity::class.java))
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
}

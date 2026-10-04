package com.example.healthsync

// Intent lets us launch other Activities (screens)
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
// BottomNavigationView is the Material component that renders the tab bar at the bottom
import com.google.android.material.bottomnavigation.BottomNavigationView

// SleepActivity is the Sleep screen — Tab 4.
// Shows a placeholder sleep score, last night's stats, and an "Add Sleep Entry" button.
// The button is visible but not yet wired to any action.
class SleepActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Link to the XML layout for this screen
        setContentView(R.layout.sleep_activity)

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
}

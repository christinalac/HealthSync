package com.example.healthsync

// Intent lets us launch other Activities (screens)
import android.content.Intent
import android.os.Bundle
// Button is the View class for clickable buttons defined in XML layouts
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
// BottomNavigationView is the Material component that renders the tab bar at the bottom
import com.google.android.material.bottomnavigation.BottomNavigationView

// AccountActivity is the Account / Profile screen — Tab 5 (last tab).
// Shows a placeholder profile header, account settings rows, and a working Log Out button.
// Log Out is the only button fully wired up — it clears the back stack and returns to Splash.
class AccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Link to the XML layout for this screen
        setContentView(R.layout.account_activity)

        // Find the BottomNavigationView in the XML layout
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Highlight the Account tab so the user knows which screen they're on
        bottomNav.selectedItemId = R.id.nav_account

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
                    startActivity(Intent(this, SleepActivity::class.java))
                    true
                }
                R.id.nav_account -> {
                    // Already on this screen — do nothing
                    true
                }
                else -> false
            }
        }

        // Grab the Log Out button from the XML layout
        val logoutButton = findViewById<Button>(R.id.buttonLogout)

        // When Log Out is tapped:
        //   FLAG_ACTIVITY_NEW_TASK     — start a fresh task
        //   FLAG_ACTIVITY_CLEAR_TASK  — wipe all screens off the back stack
        // This means the user can't press Back to get back in after logging out
        logoutButton.setOnClickListener {
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}

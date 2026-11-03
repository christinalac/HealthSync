package com.example.healthsync

// Intent lets us launch other Activities (screens)
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// BottomNavigationView is the Material component that renders the tab bar at the bottom
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// WorkoutActivity is the Workout screen — Tab 3.
// Shows placeholder exercise entries for today's plan.
class WorkoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIX: Replaced layout binding with lowercase resource ID reference
        setContentView(R.layout.workout_activity)
// Inside your onCreate function, after setContentView(R.layout.workout_activity)
        val rvExercises = findViewById<RecyclerView>(R.id.rvExercises)

// 1. Tell it how to layout items vertically
        rvExercises.layoutManager = LinearLayoutManager(this)

// 2. Set up your custom adapter with your dummy workout item data list
        val exerciseList = listOf(
            Exercise("Bench Press", "Chest", "4x10 · 135 lbs"),
            Exercise("Pull-Ups", "Back & Biceps", "3x8 "),
            Exercise("Squat", "Legs", "4x12 · 185 lbs"),
            Exercise("Plank Hold", "Core", "3x60 sec")
        )
        val adapter = ExerciseAdapter(exerciseList) // Use whatever your adapter name is
        rvExercises.adapter = adapter

        // 1. Find the "+" FAB Button by its XML ID and make it functional
        val fabAddWorkout = findViewById<FloatingActionButton>(R.id.fabAddWorkout)
        fabAddWorkout.setOnClickListener {
            // This safely shows a small temporary bubble message instead of taking you Home
            Toast.makeText(this, "Add Workout Button Pressed!", Toast.LENGTH_SHORT).show()
        }

        // 2. Find the BottomNavigationView in the XML layout
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
                    // Already on this screen — safely do nothing
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

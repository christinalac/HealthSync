package com.example.healthsync;

// Intent lets us launch other Activities (screens)
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// BottomNavigationView is the Material component that renders the tab bar at the bottom
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// WorkoutActivity is the Workout screen — Tab 3.
// Shows placeholder exercise entries for today's plan.
public class WorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_activity);

        // 1. Find the "+" FAB Button by its XML ID and make it functional
        FloatingActionButton fabAddWorkout = findViewById(R.id.fabAddWorkout);
        fabAddWorkout.setOnClickListener(v ->
                // This safely shows a small temporary bubble message instead of taking you Home
                Toast.makeText(this, "Add Workout Button Pressed!", Toast.LENGTH_SHORT).show());

        // 2. Find the BottomNavigationView in the XML layout
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Highlight the Workout tab so the user knows which screen they're on
        bottomNav.setSelectedItemId(R.id.nav_workout);

        // Listen for tab taps and navigate to the matching Activity
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ProgressActivity.class));
                return true;
            } else if (id == R.id.nav_nutrition) {
                startActivity(new Intent(this, NutritionActivity.class));
                return true;
            } else if (id == R.id.nav_workout) {
                // Already on this screen — safely do nothing
                return true;
            } else if (id == R.id.nav_sleep) {
                startActivity(new Intent(this, SleepActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return false;
        });
    }
}

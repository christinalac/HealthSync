//******************************************
// Program Name: AccountActivity.java
// Developer: Christina LaCombe
// Date Created: 10/12/2026
// Version: 1.0
// Purpose: AccountActivity is the Account / Profile screen — Tab 5 (last tab).
// Shows a placeholder profile header, account settings rows, and a working Log Out button.
// Log Out clears the back stack and returns to SplashActivity.
//******************************************
package com.example.healthsync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);

        // Find the BottomNavigationView in the XML layout
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Highlight the Account tab so the user knows which screen they're on
        bottomNav.setSelectedItemId(R.id.nav_account);

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
                startActivity(new Intent(this, WorkoutActivity.class));
                return true;
            } else if (id == R.id.nav_sleep) {
                startActivity(new Intent(this, SleepActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                // Already on this screen — do nothing
                return true;
            }
            return false;
        });

        // Grab the Log Out button from the XML layout
        Button logoutButton = findViewById(R.id.buttonLogout);

        // When Log Out is tapped:
        //   FLAG_ACTIVITY_NEW_TASK    — start a fresh task
        //   FLAG_ACTIVITY_CLEAR_TASK  — wipe all screens off the back stack
        // This means the user can't press Back to get back in after logging out
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountActivity.this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}

//******************************************
// Program Name: AccountActivity.java
// Developer: Anthony Ramirez
// Date Created: 10/12/2026
// Version: 1.2
// Purpose: AccountActivity is the Account / Profile screen — Tab 5 (last tab).
// Shows the logged-in user's profile from the database, their personalized daily
// targets (PersonalizedCalculations), lets them change their username, email and
// password, and has a working Log Out button that returns to SplashActivity.
//******************************************
package com.example.healthsync;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;

public class AccountActivity extends AppCompatActivity {

    private static final int USERNAME = 0;
    private static final int EMAIL = 1;
    private static final int PASSWORD = 2;

    private AccountStore store;
    // Database access: the profile and login info are read from and saved here
    private DatabaseHelper db;
    // Email of the logged-in user; it is the key used to look up their rows
    private String currentEmail; // who is logged in; the key into the database

    private TextView textProfileName;
    private TextView textProfileEmail;
    private TextView textStatsRow;
    private TextView textGoalValue;
    private TextView textCaloriesValue;
    private TextView textWaterValue;
    private TextView textSleepValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);

        store = new AccountStore(this);
        db = new DatabaseHelper(this);
        // Saved at login/signup; null means nobody is logged in
        currentEmail = store.getCurrentEmail();

        textProfileName = findViewById(R.id.textProfileName);
        textProfileEmail = findViewById(R.id.textProfileEmail);
        textStatsRow = findViewById(R.id.textStatsRow);
        textGoalValue = findViewById(R.id.textGoalValue);
        textCaloriesValue = findViewById(R.id.textCaloriesValue);
        textWaterValue = findViewById(R.id.textWaterValue);
        textSleepValue = findViewById(R.id.textSleepValue);
        showProfile();

        // ---- Settings rows ----
        findViewById(R.id.rowChangeUsername).setOnClickListener(v -> showEditDialog(USERNAME));
        findViewById(R.id.rowChangeEmail).setOnClickListener(v -> showEditDialog(EMAIL));
        findViewById(R.id.rowChangePassword).setOnClickListener(v -> showEditDialog(PASSWORD));

        // ---- Bottom navigation (unchanged) ----
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_account);
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
                return true; // already here
            }
            return false;
        });

        // ---- Log out ----
        // NEW_TASK + CLEAR_TASK wipes the back stack so Back can't return to the app
        Button logoutButton = findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(view -> {
            // Forget who was logged in so the next login starts clean
            store.clearCurrentEmail();
            Intent intent = new Intent(AccountActivity.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // Fills the screen from the database and runs the personalized calculations on it
    private void showProfile() {
        // Load the saved profile; null if nobody is logged in or no profile exists
        UserProfile p = currentEmail == null ? null : db.getUserProfile(currentEmail);
        if (p == null) {
            textProfileName.setText("Not signed in");
            textProfileEmail.setText(currentEmail == null ? "Log in to see your profile" : currentEmail);
            textStatsRow.setText("Age: --  |  Height: --  |  Weight: --  |  Level: --");
            textGoalValue.setText("Goal: --");
            textCaloriesValue.setText("Calories: --");
            textWaterValue.setText("Water: --");
            textSleepValue.setText("Sleep: --");
            return;
        }

        textProfileName.setText(p.name);
        textProfileEmail.setText(p.email);
        textStatsRow.setText("Age: " + p.age
                + "  |  Height: " + p.heightFt + "'" + p.heightIn + "\""
                + "  |  Weight: " + p.weightLbs + " lbs"
                + "  |  Level: " + p.activityLevel);

        // Daily targets use the same formulas as the rest of the app
        // false = treat today as an active day (there is no rest-day setting yet)
        int calories = PersonalizedCalculations.calculateDailyCalories(
                p.gender, p.weightLbs, p.heightFt, p.heightIn, p.age,
                p.activityLevel, p.goal, false);
        double litres = PersonalizedCalculations.calculateDailyWaterLitres(
                p.gender, p.weightLbs, p.activityLevel, p.goal,
                p.isPregnant, p.isBreastfeeding);

        textGoalValue.setText("Goal: " + p.goal);
        textCaloriesValue.setText("Calories: " + calories + " kcal per day");
        textWaterValue.setText("Water: " + litres + " L (about "
                + PersonalizedCalculations.litresToCups(litres) + " cups) per day");
        textSleepValue.setText("Sleep: " + PersonalizedCalculations.getSleepSuggestion(p.age)
                + " per night");
    }

    // One popup for all three changes; only the needed fields are shown
    private void showEditDialog(int type) {
        View view = getLayoutInflater().inflate(R.layout.dialog_account_edit, null);
        TextInputLayout tilCurrent = view.findViewById(R.id.tilCurrent);
        TextInputLayout tilNew = view.findViewById(R.id.tilNew);
        TextInputLayout tilConfirm = view.findViewById(R.id.tilConfirm);
        EditText etCurrent = tilCurrent.getEditText();
        EditText etNew = tilNew.getEditText();
        EditText etConfirm = tilConfirm.getEditText();

        String title;
        if (type == USERNAME) {
            title = "Change username";
            tilNew.setHint("New username");
        } else if (type == EMAIL) {
            title = "Change email";
            tilNew.setHint("New email");
            etNew.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else {
            title = "Change password";
            tilNew.setHint("New password (8+ characters)");
            tilNew.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            etNew.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            tilConfirm.setVisibility(View.VISIBLE);
            tilCurrent.setVisibility(View.VISIBLE);
        }

        // Sleep dialog theme gives these dialogs green accents and dark green buttons
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_HealthSync_SleepDialog)
                .setTitle(title)
                .setView(view)
                .setPositiveButton("Save", null) // set below so errors don't close the dialog
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilCurrent.setError(null);
            tilNew.setError(null);
            tilConfirm.setError(null);

            // Without a logged-in user there is nothing to update
            if (currentEmail == null) {
                tilNew.setError("Please log in again first");
                return;
            }
            String value = etNew.getText().toString().trim();

            if (type == USERNAME) {
                if (value.length() < 3) {
                    tilNew.setError("Use at least 3 characters");
                    return;
                }
                // Username is saved as the name on the profile row
                if (!db.updateName(currentEmail, value)) {
                    tilNew.setError("Could not save. Please log in again.");
                    return;
                }

            } else if (type == EMAIL) {
                if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                    tilNew.setError("Enter a valid email address");
                    return;
                }
                // Email is the login, so it changes in both tables; fails if already taken
                if (!db.updateEmail(currentEmail, value)) {
                    tilNew.setError("That email is already in use");
                    return;
                }
                // Keep the session pointing at the new email
                currentEmail = value;
                store.setCurrentEmail(value);

            } else {
                String password = etNew.getText().toString(); // don't trim passwords
                // Must type the current password before it can be changed
                if (!db.checkEmailPassword(currentEmail, etCurrent.getText().toString())) {
                    tilCurrent.setError("Current password is incorrect");
                    return;
                }
                if (password.length() < 8) {
                    tilNew.setError("Use at least 8 characters");
                    return;
                }
                if (!password.equals(etConfirm.getText().toString())) {
                    tilConfirm.setError("Passwords don't match");
                    return;
                }
                // Login checks this same stored password, so the new one works next login
                if (!db.updatePassword(currentEmail, password)) {
                    tilNew.setError("Could not save. Please log in again.");
                    return;
                }
            }

            showProfile();
            dialog.dismiss();
        });
    }
}

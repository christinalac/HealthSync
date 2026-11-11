//******************************************
// Program Name: SignupInformationActivity.java
// Developer: Christina LaCombe
// Date Created: 11/2/2026
// Version: 3.0
// Purpose: Collects personal details (name, age, height, weight,
// goal, activity level) after a successful signup and saves them
// to the database.  When the user selects "Female" a dialog asks
// whether they are pregnant and/or breastfeeding so that water
// intake calculations in PersonalizedCalculations can be adjusted.
// Redirects to ProgressActivity on completion.
//******************************************
package com.example.healthsync;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupInformationActivity extends AppCompatActivity {

    private EditText userNameEnter;
    private Button finishRedirectButton;
    private Spinner ageSelectorSpinner;
    private Spinner goalOutcomeSpinner;
    private Spinner genderSpinner;
    private Spinner activityLevelSpinner;
    private NumberPicker userHeightInFeetPicker;
    private NumberPicker userHeightInInchesPicker;
    private NumberPicker userWeightPicker;
    private DatabaseHelper databaseHelper;

    // Email passed in from SignupActivity
    private String userEmail;

    // Pregnancy / breastfeeding state — captured via dialog when Female is selected
    private boolean isPregnant      = false;
    private boolean isBreastfeeding = false;

    // Track whether the popup has already been shown for the current Female selection
    // so it doesn't re-fire on every spinner re-draw
    private boolean pregnancyDialogShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_information_activity);

        databaseHelper = new DatabaseHelper(this);

        // Retrieve the email that was passed from SignupActivity
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        // Wire up views
        userNameEnter            = findViewById(R.id.nameTextInput);
        ageSelectorSpinner       = findViewById(R.id.ageSpinner);
        genderSpinner            = findViewById(R.id.genderSpinner);
        activityLevelSpinner     = findViewById(R.id.activityLevelSpinner);
        goalOutcomeSpinner       = findViewById(R.id.goalOutcome);
        userHeightInFeetPicker   = findViewById(R.id.pickerFeet);
        userHeightInInchesPicker = findViewById(R.id.pickerInches);
        userWeightPicker         = findViewById(R.id.weightPicker);
        finishRedirectButton     = findViewById(R.id.finishInformationSignUpButton);

        // ── Age spinner: 13 – 100 ──────────────────────────────
        String[] ages = new String[88];
        for (int i = 0; i < 88; i++) {
            ages[i] = String.valueOf(i + 13);
        }
        ArrayAdapter<String> ageAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, ages);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ageSelectorSpinner.setAdapter(ageAdapter);

        // ── Gender spinner ─────────────────────────────────────
        // Note: limited to Male / Female for accuracy of health calculations
        String[] genders = { "Male", "Female" };
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Show pregnancy/breastfeeding popup whenever Female is selected
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if ("Female".equals(selected)) {
                    // Reset and re-ask every time the user actively picks Female
                    isPregnant      = false;
                    isBreastfeeding = false;
                    pregnancyDialogShown = false;
                    showPregnancyDialog();
                } else {
                    // Male selected — clear pregnancy flags silently
                    isPregnant      = false;
                    isBreastfeeding = false;
                    pregnancyDialogShown = false;
                }
            } // end of onItemSelected

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }
        }); // end of genderSpinner listener

        // ── Activity level spinner ─────────────────────────────
        String[] activityLevels = {
                PersonalizedCalculations.ACTIVITY_SEDENTARY,
                PersonalizedCalculations.ACTIVITY_LIGHTLY_ACTIVE,
                PersonalizedCalculations.ACTIVITY_MODERATELY_ACTIVE,
                PersonalizedCalculations.ACTIVITY_VERY_ACTIVE,
                PersonalizedCalculations.ACTIVITY_EXTRA_ACTIVE
        };
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, activityLevels);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityLevelSpinner.setAdapter(activityAdapter);

        // ── Goal spinner ───────────────────────────────────────
        String[] goals = {
                PersonalizedCalculations.GOAL_LOSE_WEIGHT,
                PersonalizedCalculations.GOAL_GAIN_MUSCLE,
                PersonalizedCalculations.GOAL_MAINTAIN_WEIGHT,
                PersonalizedCalculations.GOAL_IMPROVE_ENDURANCE,
                PersonalizedCalculations.GOAL_IMPROVE_OVERALL_HEALTH
        };
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, goals);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalOutcomeSpinner.setAdapter(goalAdapter);

        //******************************************
        // Height pickers: 4–7 ft, 0–11 in
        // Height is scaled based on the average
        // American body height
        // Also 1ft = 12in, so inches only need to
        // go to 11
        //******************************************
        userHeightInFeetPicker.setMinValue(4);
        userHeightInFeetPicker.setMaxValue(7);
        userHeightInFeetPicker.setValue(5);

        userHeightInInchesPicker.setMinValue(0);
        userHeightInInchesPicker.setMaxValue(11);
        userHeightInInchesPicker.setValue(6);

        //******************************************
        //  Weight picker: 50–400 lbs
        //  Weight is scaled based on the average
        //  American body weight
        //******************************************
        userWeightPicker.setMinValue(50);
        userWeightPicker.setMaxValue(400);
        userWeightPicker.setValue(150);

        // ── Done button ────────────────────────────────────────
        // Validate, save to DB, then navigate to ProgressActivity
        finishRedirectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = userNameEnter.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(SignupInformationActivity.this,
                            "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                } // end of name-empty check

                // Read selected values
                int    age           = Integer.parseInt(ageSelectorSpinner.getSelectedItem().toString());
                int    heightFt      = userHeightInFeetPicker.getValue();
                int    heightIn      = userHeightInInchesPicker.getValue();
                int    weight        = userWeightPicker.getValue();
                String goal          = goalOutcomeSpinner.getSelectedItem().toString();
                String gender        = genderSpinner.getSelectedItem().toString();
                String activityLevel = activityLevelSpinner.getSelectedItem().toString();

                // Save profile to the database
                Boolean saved = databaseHelper.insertUserProfile(
                        userEmail, name, age, gender, heightFt, heightIn, weight,
                        goal, activityLevel, isPregnant, isBreastfeeding);

                if (saved) {
                    Session.setEmail(SignupInformationActivity.this, userEmail);

                    Toast.makeText(SignupInformationActivity.this,
                            "Profile saved!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupInformationActivity.this, ProgressActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignupInformationActivity.this,
                            "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show();
                } // end of saved if/else
            } // end of onClick
        }); // end of finishRedirectButton listener
    } // end of onCreate

    // ─────────────────────────────────────────────────────────
    // Pregnancy / Breastfeeding Dialog
    // ─────────────────────────────────────────────────────────

    /**
     * Shows an AlertDialog with two checkboxes asking whether the female
     * user is currently pregnant and/or breastfeeding.  The results are
     * stored in {@link #isPregnant} and {@link #isBreastfeeding} and are
     * later passed to PersonalizedCalculations for water intake adjustment.
     */
    private void showPregnancyDialog() {
        if (pregnancyDialogShown) return;
        pregnancyDialogShown = true;

        // Inflate a simple two-checkbox layout inline
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, 0);

        final CheckBox cbPregnant      = new CheckBox(this);
        final CheckBox cbBreastfeeding = new CheckBox(this);
        cbPregnant.setText("I am currently pregnant");
        cbBreastfeeding.setText("I am currently breastfeeding");

        layout.addView(cbPregnant);
        layout.addView(cbBreastfeeding);

        new AlertDialog.Builder(this)
                .setTitle("Additional Health Information")
                .setMessage("This helps us personalise your daily water intake goal.")
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    isPregnant      = cbPregnant.isChecked();
                    isBreastfeeding = cbBreastfeeding.isChecked();
                })
                .setNegativeButton("Skip", (dialog, which) -> {
                    isPregnant      = false;
                    isBreastfeeding = false;
                })
                .show();
    } // end of showPregnancyDialog

} // end of class

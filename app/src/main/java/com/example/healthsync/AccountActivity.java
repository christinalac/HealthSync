//******************************************
// Program Name: AccountActivity.java
// Developer:
// Date Created: 10/12/2026
// Version: 2.0
// Purpose: Account / Profile screen — Tab 5. Shows the logged-in user's profile from
// the SQLite database, lets them change their name, email and password, and logs out.
//******************************************
package com.example.healthsync;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
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

    private DatabaseHelper db;
    private String email; // the logged-in user's email (the key in both tables)
    private TextView textProfileName, textProfileEmail, textStatsRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);

        db = new DatabaseHelper(this);
        email = Session.getEmail(this);
        if (email == null) { // nobody is logged in
            goToLogin();
            return;
        }

        textProfileName = findViewById(R.id.textProfileName);
        textProfileEmail = findViewById(R.id.textProfileEmail);
        textStatsRow = findViewById(R.id.textStatsRow);
        showProfile();

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

        // ---- Log out: forget the user, then wipe the back stack ----
        findViewById(R.id.buttonLogout).setOnClickListener(v -> {
            Session.clear(this);
            goToLogin();
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showProfile() {
        String[] profile = db.getProfileDisplay(email);
        textProfileName.setText(profile != null ? profile[0] : "Your name");
        textProfileEmail.setText(email);
        if (profile != null) textStatsRow.setText(profile[1]);
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
            title = "Change name";
            tilNew.setHint("New name");
        } else if (type == EMAIL) {
            title = "Change email";
            tilNew.setHint("New email");
            etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else {
            title = "Change password";
            tilNew.setHint("New password");
            tilNew.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            tilCurrent.setVisibility(View.VISIBLE);
            tilConfirm.setVisibility(View.VISIBLE);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
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
            // Trimmed, to match how signup and login treat these fields
            String value = etNew.getText().toString().trim();

            if (type == USERNAME) {
                if (value.isEmpty()) {
                    tilNew.setError("Enter a name");
                    return;
                }
                if (!db.updateName(email, value)) {
                    tilNew.setError("Couldn't save. Please try again");
                    return;
                }

            } else if (type == EMAIL) {
                if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                    tilNew.setError("Enter a valid email address");
                    return;
                }
                if (!db.updateEmail(email, value)) {
                    tilNew.setError("That email is already in use");
                    return;
                }
                // The email is the user's key, so the saved login must follow it
                email = value;
                Session.setEmail(this, email);

            } else {
                if (!db.checkEmailPassword(email, etCurrent.getText().toString().trim())) {
                    tilCurrent.setError("Current password is incorrect");
                    return;
                }
                if (value.isEmpty()) {
                    tilNew.setError("Enter a new password");
                    return;
                }
                if (!value.equals(etConfirm.getText().toString().trim())) {
                    tilConfirm.setError("Passwords don't match");
                    return;
                }
                if (!db.updatePassword(email, value)) {
                    tilNew.setError("Couldn't save. Please try again");
                    return;
                }
            }

            showProfile();
            dialog.dismiss();
        });
    }
}
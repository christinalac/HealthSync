//******************************************
// Program Name: AccountActivity.java
// Developer: Anthony Ramirez
// Date Created: 10/12/2026
// Version: 1.1
// Purpose: AccountActivity is the Account / Profile screen — Tab 5 (last tab).
// Shows the user's profile at the top, lets them change their username, email and
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
    private TextView textProfileName;
    private TextView textProfileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);

        store = new AccountStore(this);
        textProfileName = findViewById(R.id.textProfileName);
        textProfileEmail = findViewById(R.id.textProfileEmail);
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
            Intent intent = new Intent(AccountActivity.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void showProfile() {
        textProfileName.setText(store.getUsername());
        textProfileEmail.setText(store.getEmail());
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
            // Only ask for the current password if one has been saved
            if (store.hasPassword()) tilCurrent.setVisibility(View.VISIBLE);
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
            String value = etNew.getText().toString().trim();

            if (type == USERNAME) {
                if (value.length() < 3) {
                    tilNew.setError("Use at least 3 characters");
                    return;
                }
                store.setUsername(value);

            } else if (type == EMAIL) {
                if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                    tilNew.setError("Enter a valid email address");
                    return;
                }
                store.setEmail(value);

            } else {
                String password = etNew.getText().toString(); // don't trim passwords
                if (store.hasPassword()
                        && !store.checkPassword(etCurrent.getText().toString())) {
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
                store.setPassword(password);
            }

            showProfile();
            dialog.dismiss();
        });
    }
}
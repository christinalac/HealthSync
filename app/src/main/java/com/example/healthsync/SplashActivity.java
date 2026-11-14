//******************************************
// Program Name: SplashActivity.java
// Developer: Christina LaCombe
// Date Created: 10/12/2026
// Version: 1.1 (accessibility: field errors, headings)
// Purpose: SplashActivity is the first screen the user sees when the app launches.
// It is set as the LAUNCHER activity in AndroidManifest.xml.
// It shows a login form and validates credentials against the local SQLite database.
//******************************************
package com.example.healthsync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.google.android.material.textfield.TextInputLayout;

public class SplashActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button loginButton;
    private Button signupRedirectButton;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        databaseHelper = new DatabaseHelper(this);

        // Grab the views from login_screen.xml by their IDs
        email                = findViewById(R.id.email);   // field is used for email
        password             = findViewById(R.id.password);
        loginButton          = findViewById(R.id.buttonLogin);
        signupRedirectButton = findViewById(R.id.signupText);

        emailLayout    = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        // Mark the app name and "Login" title as headings so TalkBack users
        // can jump between sections (compat call works below API 28)
        ViewCompat.setAccessibilityHeading(findViewById(R.id.textAppName), true);
        ViewCompat.setAccessibilityHeading(findViewById(R.id.loginText), true);

        // Login: check credentials against the SQLite database
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredEmail = email.getText().toString().trim();
                String enteredPass  = password.getText().toString().trim();

                // Clear any previous errors
                emailLayout.setError(null);
                passwordLayout.setError(null);

                // Field errors stay on screen and are announced by screen readers,
                // unlike a Toast which disappears quickly
                if (enteredEmail.isEmpty()) {
                    emailLayout.setError(getString(R.string.login_error_email_required));
                    email.requestFocus();
                    return;
                }
                if (enteredPass.isEmpty()) {
                    passwordLayout.setError(getString(R.string.login_error_password_required));
                    password.requestFocus();
                    return;
                }

                Boolean valid = databaseHelper.checkEmailPassword(enteredEmail, enteredPass);
                if (valid) {
                    Session.setEmail(SplashActivity.this, enteredEmail);
                    Toast.makeText(SplashActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SplashActivity.this, ProgressActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    passwordLayout.setError(getString(R.string.login_error_invalid));
                    password.requestFocus();
                }
            }
        });

        // Navigate to SignupActivity when the user taps "Sign Up"
        signupRedirectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}

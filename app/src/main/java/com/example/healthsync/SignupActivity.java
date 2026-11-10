//******************************************
// Program Name: SignupActicvity.java
// Developer: Christina LaCombe
// Date Created: 10/12/2026
// Version: 1.1 (accessibility: field errors, headings)
// Purpose: This collects the data of all information
// when the user signs up for their account
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

public class SignupActivity extends AppCompatActivity {

    private EditText username, emailAddress, password, confirmPassword;
    public Button signupButton;
    public Button loginRedirectText;
    private TextInputLayout usernameLayout, emailLayout, passwordLayout, confirmLayout;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        databaseHelper = new DatabaseHelper(this);

        // Wire up views using the IDs defined in signup_activity.xml
        username        = findViewById(R.id.username);
        password        = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.signup_confirm);
        emailAddress    = findViewById(R.id.emailaddress);
        // the Sign Up button
        signupButton    = findViewById(R.id.buttonLogin);
        // "Already have an account? Login"
        loginRedirectText = findViewById(R.id.signupText);

        usernameLayout = findViewById(R.id.usernameLayout);
        emailLayout    = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmLayout  = findViewById(R.id.confirmLayout);

        // Mark the app name and "Sign up" title as headings for TalkBack
        ViewCompat.setAccessibilityHeading(findViewById(R.id.textAppName), true);
        ViewCompat.setAccessibilityHeading(findViewById(R.id.loginText), true);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredUsername = username.getText().toString().trim();
                String enteredEmail = emailAddress.getText().toString().trim();
                String enteredPassword = password.getText().toString().trim();
                String enteredConfirm = confirmPassword.getText().toString().trim();

                // Clear any previous errors
                usernameLayout.setError(null);
                emailLayout.setError(null);
                passwordLayout.setError(null);
                confirmLayout.setError(null);

                // Field errors stay on screen and are announced by screen readers,
                // unlike a Toast which disappears quickly
                if (enteredUsername.isEmpty()) {
                    usernameLayout.setError(getString(R.string.signup_error_username_required));
                    username.requestFocus();
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                    emailLayout.setError(getString(R.string.signup_error_email_invalid));
                    emailAddress.requestFocus();
                } else if (enteredPassword.isEmpty()) {
                    passwordLayout.setError(getString(R.string.signup_error_password_required));
                    password.requestFocus();
                } else if (enteredConfirm.isEmpty()) {
                    confirmLayout.setError(getString(R.string.signup_error_confirm_required));
                    confirmPassword.requestFocus();
                } else if (!enteredPassword.equals(enteredConfirm)) {
                    confirmLayout.setError(getString(R.string.signup_error_mismatch));
                    confirmPassword.requestFocus();
                } else {
                    Boolean userExists = databaseHelper.checkEmail(enteredEmail);
                    if (userExists) {
                        emailLayout.setError(getString(R.string.signup_error_email_exists));
                        emailAddress.requestFocus();
                    } else {
                        Boolean inserted = databaseHelper.insertData(enteredEmail, enteredPassword);
                        if (inserted) {
                            Toast.makeText(SignupActivity.this, R.string.signup_success, Toast.LENGTH_SHORT).show();
                            // Pass the email along so SignupInformationActivity can save the profile
                            Intent intent = new Intent(SignupActivity.this, SignupInformationActivity.class);
                            intent.putExtra("USER_EMAIL", enteredEmail);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, R.string.signup_error_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        // "Already have an account? Login" → back to login screen
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, SplashActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

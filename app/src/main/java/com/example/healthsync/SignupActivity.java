//******************************************
// Program Name: SignupActicvity.java
// Developer: Christina LaCombe
// Date Created: 10/12/2026
// Version: 1.0
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

public class SignupActivity extends AppCompatActivity {

    private EditText username, emailAddress, password, confirmPassword;
    public Button signupButton;
    public Button loginRedirectText;
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

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredUsername = username.getText().toString().trim();
                String enteredEmail = emailAddress.getText().toString().trim();
                String enteredPassword = password.getText().toString().trim();
                String enteredConfirm = confirmPassword.getText().toString().trim();

                if (enteredUsername.isEmpty() || enteredEmail.isEmpty() || enteredPassword.isEmpty() || enteredConfirm.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                    Toast.makeText(SignupActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                } else if (!enteredPassword.equals(enteredConfirm)) {
                    Toast.makeText(SignupActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                } else {
                    Boolean userExists = databaseHelper.checkEmail(enteredEmail);
                    if (userExists) {
                        Toast.makeText(SignupActivity.this, "Email already registered! Please login", Toast.LENGTH_SHORT).show();
                    } else {
                        Boolean inserted = databaseHelper.insertData(enteredEmail, enteredPassword);
                        if (inserted) {
                            Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                            // Pass the email along so SignupInformationActivity can save the profile
                            Intent intent = new Intent(SignupActivity.this, SignupInformationActivity.class);
                            intent.putExtra("USER_EMAIL", enteredEmail);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Signup failed. Try again.", Toast.LENGTH_SHORT).show();
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

//******************************************
// Program Name: SplashActivity.java
// Developer: Christina LaCombe
// Date Created: 10/12/2026
// Version: 1.0
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

public class SplashActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button loginButton;
    private Button signupRedirectButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        databaseHelper = new DatabaseHelper(this);

        // Grab the views from login_screen.xml by their IDs
        username             = findViewById(R.id.username);   // field is used for email
        password             = findViewById(R.id.password);
        loginButton          = findViewById(R.id.buttonLogin);
        signupRedirectButton = findViewById(R.id.signupText);

        // Login: check credentials against the SQLite database
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredEmail = username.getText().toString().trim();
                String enteredPass  = password.getText().toString().trim();

                if (enteredEmail.isEmpty() || enteredPass.isEmpty()) {
                    Toast.makeText(SplashActivity.this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                Boolean valid = databaseHelper.checkEmailPassword(enteredEmail, enteredPass);
                if (valid) {
                    Toast.makeText(SplashActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SplashActivity.this, ProgressActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SplashActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
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

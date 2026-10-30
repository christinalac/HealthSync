package com.example.healthsync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// SplashActivity is the first screen the user sees when the app launches.
// It is set as the LAUNCHER activity in AndroidManifest.xml.
// It shows a login form and validates credentials against the local SQLite database.
class SplashActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        databaseHelper = DatabaseHelper(this)

        // Grab the views from login_screen.xml by their IDs
        username    = findViewById(R.id.username)
        password    = findViewById(R.id.password)
        loginButton = findViewById(R.id.buttonLogin)
        signupButton = findViewById(R.id.signupText)  // "Not yet registered? SignUp Now"

        // Login: check credentials against the SQLite database
        loginButton.setOnClickListener {
            val enteredUser = username.text.toString().trim()
            val enteredPass = password.text.toString().trim()

            if (enteredUser.isEmpty() || enteredPass.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valid = databaseHelper.checkEmailPassword(enteredUser, enteredPass)
            if (valid) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ProgressActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to the SignupActivity when the user taps "Sign Up"
        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}

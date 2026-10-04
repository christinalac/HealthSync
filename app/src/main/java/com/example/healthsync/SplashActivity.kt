package com.example.healthsync

// Intent lets us launch other Activities (screens)
import android.content.Intent
import android.os.Bundle
// Button is the View class for clickable buttons defined in XML layouts
import android.widget.Button
// AppCompatActivity is the base class for all Activities in this project —
// it provides backwards-compatible support for modern Android features
import androidx.appcompat.app.AppCompatActivity

// SplashActivity is the first screen the user sees when the app launches.
// It's set as the LAUNCHER activity in AndroidManifest.xml.
// All it does right now is show the app name and wire up the Login button.
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tell Android which XML layout file to display for this screen
        setContentView(R.layout.splash_activity)

        // Grab the Login button from the XML layout by its ID
        val loginButton = findViewById<Button>(R.id.buttonLogin)

        // When the user taps Login, navigate to the Home/Progress screen
        loginButton.setOnClickListener {
            val intent = Intent(this, ProgressActivity::class.java)
            startActivity(intent)
        }
    }
}

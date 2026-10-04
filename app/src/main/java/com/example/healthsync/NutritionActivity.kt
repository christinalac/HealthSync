package com.example.healthsync

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NutritionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.nutrition_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bottom Navigagtion
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        // Highlight the current tab so the user knows where they are
        bottomNav.selectedItemId = R.id.nav_nutrition

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_nutrition -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }

                R.id.nav_workout -> {
                    startActivity(Intent(this, NutritionActivity::class.java))
                    true
                }

                R.id.nav_sleep -> {
                    startActivity(Intent(this, NutritionActivity::class.java))
                    true
                }

                R.id.nav_account -> {
                    startActivity(Intent(this, NutritionActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }
}
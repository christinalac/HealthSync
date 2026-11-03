package com.example.healthsync

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup

class ProgressActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progress_activity)

        // Find bottom nav view
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_nutrition -> {
                    startActivity(Intent(this, NutritionActivity::class.java))
                    true
                }
                R.id.nav_workout -> {
                    startActivity(Intent(this, WorkoutActivity::class.java))
                    true
                }
                R.id.nav_sleep -> {
                    startActivity(Intent(this, SleepActivity::class.java))
                    true
                }
                R.id.nav_account -> {
                    startActivity(Intent(this, AccountActivity::class.java))
                    true
                }
                else -> false
            }
        }

        setupToggleListeners()
    }

    private fun setupToggleListeners() {
        // Calorie Graph Toggle
        val toggleCaloriesGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleCaloriesGroup)
        val layoutCaloriesWeekly = findViewById<LinearLayout>(R.id.layoutCaloriesWeekly)
        val layoutCaloriesMonthly = findViewById<LinearLayout>(R.id.layoutCaloriesMonthly)
        val tvCaloriesSubtitle = findViewById<TextView>(R.id.tvCaloriesSubtitle)

        toggleCaloriesGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btnCaloriesWeekly) {
                    layoutCaloriesWeekly.visibility = View.VISIBLE
                    layoutCaloriesMonthly.visibility = View.GONE
                    tvCaloriesSubtitle.text = "Avg: 2,135 kcal / day"
                } else if (checkedId == R.id.btnCaloriesMonthly) {
                    layoutCaloriesWeekly.visibility = View.GONE
                    layoutCaloriesMonthly.visibility = View.VISIBLE
                    tvCaloriesSubtitle.text = "Avg: 2,135 kcal / week"
                }
            }
        }

        // Water Graph Toggle
        val toggleWaterGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleWaterGroup)
        val layoutWaterWeekly = findViewById<LinearLayout>(R.id.layoutWaterWeekly)
        val layoutWaterMonthly = findViewById<LinearLayout>(R.id.layoutWaterMonthly)
        val tvWaterSubtitle = findViewById<TextView>(R.id.tvWaterSubtitle)

        toggleWaterGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btnWaterWeekly) {
                    layoutWaterWeekly.visibility = View.VISIBLE
                    layoutWaterMonthly.visibility = View.GONE
                    tvWaterSubtitle.text = "Avg: 2.6 L / day"
                } else if (checkedId == R.id.btnWaterMonthly) {
                    layoutWaterWeekly.visibility = View.GONE
                    layoutWaterMonthly.visibility = View.VISIBLE
                    tvWaterSubtitle.text = "Avg: 2.6 L / week"
                }
            }
        }

        // Sleep Graph Toggle
        val toggleSleepGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleSleepGroup)
        val layoutSleepWeekly = findViewById<LinearLayout>(R.id.layoutSleepWeekly)
        val layoutSleepMonthly = findViewById<LinearLayout>(R.id.layoutSleepMonthly)
        val tvSleepSubtitle = findViewById<TextView>(R.id.tvSleepSubtitle)

        toggleSleepGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btnSleepWeekly) {
                    layoutSleepWeekly.visibility = View.VISIBLE
                    layoutSleepMonthly.visibility = View.GONE
                    tvSleepSubtitle.text = "Avg: 7.6 hrs / night"
                } else if (checkedId == R.id.btnSleepMonthly) {
                    layoutSleepWeekly.visibility = View.GONE
                    layoutSleepMonthly.visibility = View.VISIBLE
                    tvSleepSubtitle.text = "Avg: 7.6 hrs / week"
                }
            }
        }
    }
}

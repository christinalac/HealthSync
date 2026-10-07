package com.example.healthsync

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import java.util.Locale

// Nutrition screen — Tab 2.
// Shows the calorie summary and hydration tracker.
// The "+" FAB and the meals list are placeholders for the meal-tracking feature.
class NutritionActivity : AppCompatActivity() {

    private val viewModel: NutritionViewModel by viewModels()

    private lateinit var tvHeaderKcal: TextView
    private lateinit var tvGoal: TextView
    private lateinit var tvEaten: TextView
    private lateinit var tvLeft: TextView
    private lateinit var calorieProgress: ProgressBar
    private lateinit var tvCarbs: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvFat: TextView
    private lateinit var tvHydrationTitle: TextView
    private lateinit var waterBar: WaterProgressBar
    private lateinit var tvGlassCount: TextView
    private lateinit var hydrationCard: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nutrition_activity)

        bindViews()
        setupHydrationTaps()
        setupBottomNav()

        // Redraw whenever the data changes
        viewModel.state.observe(this) { render(it) }

        //Test calories
        viewModel.addFood(220, 105, 32, 28)
    }

    private fun bindViews() {
        tvHeaderKcal = findViewById(R.id.tvHeaderKcal)
        tvGoal = findViewById(R.id.tvGoal)
        tvEaten = findViewById(R.id.tvEaten)
        tvLeft = findViewById(R.id.tvLeft)
        calorieProgress = findViewById(R.id.calorieProgress)
        tvCarbs = findViewById(R.id.tvCarbs)
        tvProtein = findViewById(R.id.tvProtein)
        tvFat = findViewById(R.id.tvFat)
        tvHydrationTitle = findViewById(R.id.tvHydrationTitle)
        waterBar = findViewById(R.id.waterBar)
        tvGlassCount = findViewById(R.id.tvGlassCount)
        hydrationCard = findViewById(R.id.hydrationCard)
    }

    // Tap anywhere on the hydration card = +1 glass. Long-press = undo a glass.
    private fun setupHydrationTaps() {
        hydrationCard.setOnClickListener { viewModel.addGlass() }
        hydrationCard.setOnLongClickListener {
            viewModel.removeGlass()
            true
        }
    }

    private fun render(s: NutritionState) {
        // Header pill + calorie card
        tvHeaderKcal.text = getString(R.string.kcal_progress, s.caloriesEaten, s.calorieGoal)
        tvGoal.text = s.calorieGoal.toString()
        tvEaten.text = s.caloriesEaten.toString()
        tvLeft.text = s.caloriesLeft.toString()
        calorieProgress.progress = s.calorieProgressPercent
        tvCarbs.text = "${s.carbsG}g"
        tvProtein.text = "${s.proteinG}g"
        tvFat.text = "${s.fatG}g"

        // Hydration card
        tvHydrationTitle.text = String.format(
            Locale.US, "HYDRATION – %.1fL / %.1fL", s.hydrationLiters, s.hydrationGoalLiters
        )
        waterBar.setProgress(s.hydrationProgress)
        waterBar.contentDescription = "Hydration ${s.hydrationPercent} percent of daily goal"
        tvGlassCount.text = "${s.glassesDrunk} / ${s.totalGlasses} glasses · ${s.hydrationPercent}%"
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_nutrition

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, ProgressActivity::class.java)); true }
                R.id.nav_nutrition -> true
                R.id.nav_workout -> { startActivity(Intent(this, WorkoutActivity::class.java)); true }
                R.id.nav_sleep -> { startActivity(Intent(this, SleepActivity::class.java)); true }
                R.id.nav_account -> { startActivity(Intent(this, AccountActivity::class.java)); true }
                else -> false
            }
        }
    }
}
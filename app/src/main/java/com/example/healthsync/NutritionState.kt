package com.example.healthsync

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// One snapshot of everything the Nutrition screen displays.
// Calories and macros are now calculated from today's meals.
data class NutritionState(
    val calorieGoal: Int = 2000,
    val meals: List<LoggedMeal> = emptyList(),
    val hydrationMl: Int = 0,
    val hydrationGoalMl: Int = 2500,
    val glassMl: Int = 250
) {
    // ----- Calories & macros (sum of meals x servings) -----
    val caloriesEaten: Int get() = meals.sumOf { it.totalKcal }.roundToInt()
    val carbsG: Int get() = meals.sumOf { it.totalCarbs }.roundToInt()
    val proteinG: Int get() = meals.sumOf { it.totalProtein }.roundToInt()
    val fatG: Int get() = meals.sumOf { it.totalFat }.roundToInt()

    val caloriesLeft: Int get() = max(calorieGoal - caloriesEaten, 0)

    // 0..100 for the ProgressBar
    val calorieProgressPercent: Int
        get() = if (calorieGoal == 0) 0 else min(caloriesEaten * 100 / calorieGoal, 100)

    // ----- Hydration -----
    val totalGlasses: Int get() = ceil(hydrationGoalMl / glassMl.toDouble()).toInt()
    val glassesDrunk: Int get() = min(hydrationMl / glassMl, totalGlasses)

    // 0f..1f for the water bar
    val hydrationProgress: Float
        get() = if (hydrationGoalMl == 0) 0f else min(hydrationMl.toFloat() / hydrationGoalMl, 1f)
    val hydrationPercent: Int get() = (hydrationProgress * 100).toInt()
    val hydrationLiters: Double get() = hydrationMl / 1000.0
    val hydrationGoalLiters: Double get() = hydrationGoalMl / 1000.0
}
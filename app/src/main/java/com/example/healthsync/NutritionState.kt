package com.example.healthsync

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

// One snapshot of everything the Nutrition screen displays.
// The UI only ever reads this; it never calculates anything itself.
data class NutritionState(
    val calorieGoal: Int = 2000,
    val caloriesEaten: Int = 0,
    val carbsG: Int = 0,
    val proteinG: Int = 0,
    val fatG: Int = 0,
    val hydrationMl: Int = 0,
    val hydrationGoalMl: Int = 2500,
    val glassMl: Int = 250
) {
    val caloriesLeft: Int get() = max(calorieGoal - caloriesEaten, 0)

    // 0..100 for the ProgressBar
    val calorieProgressPercent: Int
        get() = if (calorieGoal == 0) 0 else min(caloriesEaten * 100 / calorieGoal, 100)

    val totalGlasses: Int get() = ceil(hydrationGoalMl / glassMl.toDouble()).toInt()
    val glassesDrunk: Int get() = min(hydrationMl / glassMl, totalGlasses)
    // 0f..1f for the water bar
    val hydrationProgress: Float
        get() = if (hydrationGoalMl == 0) 0f else min(hydrationMl.toFloat() / hydrationGoalMl, 1f)
    val hydrationPercent: Int get() = (hydrationProgress * 100).toInt()
    val hydrationLiters: Double get() = hydrationMl / 1000.0
    val hydrationGoalLiters: Double get() = hydrationGoalMl / 1000.0
}
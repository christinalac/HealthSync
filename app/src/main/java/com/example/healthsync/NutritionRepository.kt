package com.example.healthsync

import android.content.Context
import java.time.LocalDate

// Saves today's nutrition numbers so they survive the app closing.
// Data resets automatically when the date changes.
// Later you can swap SharedPreferences for Room without touching the UI or ViewModel.
class NutritionRepository(context: Context) {

    private val prefs = context.getSharedPreferences("nutrition_prefs", Context.MODE_PRIVATE)

    fun load(): NutritionState {
        resetIfNewDay()
        return NutritionState(
            caloriesEaten = prefs.getInt(KEY_CALORIES, 0),
            carbsG = prefs.getInt(KEY_CARBS, 0),
            proteinG = prefs.getInt(KEY_PROTEIN, 0),
            fatG = prefs.getInt(KEY_FAT, 0),
            hydrationMl = prefs.getInt(KEY_WATER, 0)
        )
    }

    fun save(state: NutritionState) {
        prefs.edit()
            .putString(KEY_DATE, LocalDate.now().toString())
            .putInt(KEY_CALORIES, state.caloriesEaten)
            .putInt(KEY_CARBS, state.carbsG)
            .putInt(KEY_PROTEIN, state.proteinG)
            .putInt(KEY_FAT, state.fatG)
            .putInt(KEY_WATER, state.hydrationMl)
            .apply()
    }

    private fun resetIfNewDay() {
        val today = LocalDate.now().toString()
        if (prefs.getString(KEY_DATE, today) != today) prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_DATE = "date"
        const val KEY_CALORIES = "calories"
        const val KEY_CARBS = "carbs"
        const val KEY_PROTEIN = "protein"
        const val KEY_FAT = "fat"
        const val KEY_WATER = "water_ml"
    }
}

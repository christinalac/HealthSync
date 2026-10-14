package com.example.healthsync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// Holds the screen's data and the actions that change it.
// LiveData (rather than Flow) keeps this easy to port to Java.
class NutritionViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = NutritionRepository(app)
    private val _state = MutableLiveData(repo.load())
    val state: LiveData<NutritionState> = _state

    private val current: NutritionState get() = _state.value ?: NutritionState()

    // ----- Hydration -----
    fun addGlass() = update { it.copy(hydrationMl = it.hydrationMl + it.glassMl) }

    fun removeGlass() = update { it.copy(hydrationMl = (it.hydrationMl - it.glassMl).coerceAtLeast(0)) }

    // ----- Calories -----
    // Not called by the UI yet. Your future meal-logging code will call this
    // when the user logs a meal, and the summary card updates automatically.
    fun addFood(calories: Int, carbsG: Int = 0, proteinG: Int = 0, fatG: Int = 0) = update {
        it.copy(
            caloriesEaten = it.caloriesEaten + calories,
            carbsG = it.carbsG + carbsG,
            proteinG = it.proteinG + proteinG,
            fatG = it.fatG + fatG
        )
    }

    private fun update(change: (NutritionState) -> NutritionState) {
        val next = change(current)
        _state.value = next
        repo.save(next)
    }
}

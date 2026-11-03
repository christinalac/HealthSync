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

    // ----- Meals -----
    // Adding a food that's already in today's list bumps its servings instead of duplicating it.
    fun addMeal(food: FoodItem) = update { s ->
        val i = s.meals.indexOfFirst { it.code.isNotBlank() && it.code == food.code }
        val meals = if (i >= 0) {
            s.meals.toMutableList().also { it[i] = it[i].copy(servings = it[i].servings + 1.0) }
        } else {
            s.meals + food.toMeal()
        }
        s.copy(meals = meals)
    }

    // delta is +0.5 / -0.5 from the stepper buttons; never goes below 0.5 (use delete to remove)
    fun changeServings(id: String, delta: Double) = update { s ->
        s.copy(meals = s.meals.map {
            if (it.id == id) it.copy(servings = (it.servings + delta).coerceAtLeast(0.5)) else it
        })
    }

    fun removeMeal(id: String) = update { s -> s.copy(meals = s.meals.filterNot { it.id == id }) }

    // For testing: wipes today's meals and water
    fun resetToday() = update { it.copy(meals = emptyList(), hydrationMl = 0) }

    private fun update(change: (NutritionState) -> NutritionState) {
        val next = change(current)
        _state.value = next
        repo.save(next)
    }
}
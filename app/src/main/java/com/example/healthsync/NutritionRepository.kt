package com.example.healthsync

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate

// Saves today's meals and water so they survive the app closing.
// Data resets automatically when the date changes.
// Later you can swap SharedPreferences for Room without touching the UI or ViewModel.
class NutritionRepository(context: Context) {

    private val prefs = context.getSharedPreferences("nutrition_prefs", Context.MODE_PRIVATE)

    fun load(): NutritionState {
        resetIfNewDay()
        return NutritionState(
            meals = mealsFromJson(prefs.getString(KEY_MEALS, null)),
            hydrationMl = prefs.getInt(KEY_WATER, 0)
        )
    }

    fun save(state: NutritionState) {
        prefs.edit()
            .putString(KEY_DATE, LocalDate.now().toString())
            .putString(KEY_MEALS, mealsToJson(state.meals))
            .putInt(KEY_WATER, state.hydrationMl)
            .apply()
    }

    private fun resetIfNewDay() {
        val today = LocalDate.now().toString()
        if (prefs.getString(KEY_DATE, today) != today) prefs.edit().clear().apply()
    }

    // ----- Meals <-> JSON -----
    private fun mealsToJson(meals: List<LoggedMeal>): String {
        val arr = JSONArray()
        for (m in meals) {
            arr.put(
                JSONObject()
                    .put("id", m.id).put("code", m.code).put("name", m.name).put("brand", m.brand)
                    .put("imageUrl", m.imageUrl ?: JSONObject.NULL)
                    .put("servingLabel", m.servingLabel)
                    .put("kcal", m.kcalPerServing).put("carbs", m.carbsPerServing)
                    .put("protein", m.proteinPerServing).put("fat", m.fatPerServing)
                    .put("servings", m.servings)
            )
        }
        return arr.toString()
    }

    private fun mealsFromJson(json: String?): List<LoggedMeal> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                LoggedMeal(
                    id = o.getString("id"), code = o.optString("code"),
                    name = o.getString("name"), brand = o.optString("brand"),
                    imageUrl = if (o.isNull("imageUrl")) null else o.getString("imageUrl"),
                    servingLabel = o.optString("servingLabel", "100 g"),
                    kcalPerServing = o.optDouble("kcal", 0.0),
                    carbsPerServing = o.optDouble("carbs", 0.0),
                    proteinPerServing = o.optDouble("protein", 0.0),
                    fatPerServing = o.optDouble("fat", 0.0),
                    servings = o.optDouble("servings", 1.0)
                )
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    private companion object {
        const val KEY_DATE = "date"
        const val KEY_MEALS = "meals_json"
        const val KEY_WATER = "water_ml"
    }
}
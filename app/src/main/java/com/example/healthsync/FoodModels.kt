package com.example.healthsync

import java.util.Locale
import java.util.UUID
import kotlin.math.floor
import kotlin.math.roundToInt

// A food returned by the Open Food Facts search.
// All nutrition values are PER SERVING (see OpenFoodFactsApi for how a serving is worked out).
data class FoodItem(
    val code: String,            // barcode
    val name: String,
    val brand: String,
    val imageUrl: String?,
    val servingLabel: String,    // e.g. "30 g" (falls back to "100 g")
    val kcal: Double,
    val carbsG: Double,
    val proteinG: Double,
    val fatG: Double
) {
    fun toMeal() = LoggedMeal(
        id = UUID.randomUUID().toString(),
        code = code, name = name, brand = brand, imageUrl = imageUrl,
        servingLabel = servingLabel,
        kcalPerServing = kcal, carbsPerServing = carbsG,
        proteinPerServing = proteinG, fatPerServing = fatG,
        servings = 1.0
    )
}

// One entry in "Today's Meals". Stores per-serving values + how many servings were eaten.
data class LoggedMeal(
    val id: String,
    val code: String,
    val name: String,
    val brand: String,
    val imageUrl: String?,
    val servingLabel: String,
    val kcalPerServing: Double,
    val carbsPerServing: Double,
    val proteinPerServing: Double,
    val fatPerServing: Double,
    val servings: Double
) {
    val totalKcal: Double get() = kcalPerServing * servings
    val totalCarbs: Double get() = carbsPerServing * servings
    val totalProtein: Double get() = proteinPerServing * servings
    val totalFat: Double get() = fatPerServing * servings
}

// 1.0 -> "1", 1.5 -> "1.5"
fun Double.fmt1(): String =
    if (this == floor(this)) toInt().toString() else String.format(Locale.US, "%.1f", this)

// "120 kcal · C 20g · P 3g · F 4g" (same order as the macros on the summary card)
fun nutrientLine(kcal: Double, carbs: Double, protein: Double, fat: Double): String =
    "${kcal.roundToInt()} kcal · C ${carbs.fmt1()}g · P ${protein.fmt1()}g · F ${fat.fmt1()}g"

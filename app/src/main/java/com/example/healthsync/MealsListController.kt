package com.example.healthsync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide

// Draws "Today's Meals" as plain rows inside a vertical LinearLayout.
// Because the rows are normal children (not a RecyclerView), the card grows by one row for every
// meal, and the page's ScrollView scrolls when the list gets long.
// Rows are reused between updates, so tapping +/- doesn't make photos flicker or reload.
class MealsListController(
    private val container: LinearLayout,
    private val onIncrease: (LoggedMeal) -> Unit,
    private val onDecrease: (LoggedMeal) -> Unit,
    private val onDelete: (LoggedMeal) -> Unit
) {

    private class Row(val view: View) {
        val image: ImageView = view.findViewById(R.id.ivMeal)
        val name: TextView = view.findViewById(R.id.tvMealName)
        val serving: TextView = view.findViewById(R.id.tvMealServing)
        val nutrients: TextView = view.findViewById(R.id.tvMealNutrients)
        val servings: TextView = view.findViewById(R.id.tvServings)
        var meal: LoggedMeal? = null          // always the latest data for this row
        var imageLoaded = false
        var imageUrl: String? = null
    }

    private val rows = LinkedHashMap<String, Row>()   // meal id -> its row

    fun submit(meals: List<LoggedMeal>) {
        // 1. Remove rows for meals that were deleted
        val keep = meals.map { it.id }.toSet()
        val it = rows.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.key !in keep) {
                container.removeView(entry.value.view)
                it.remove()
            }
        }

        // 2. Create/reuse a row for each meal and make sure it sits in the right position
        meals.forEachIndexed { index, meal ->
            val row = rows.getOrPut(meal.id) { createRow() }
            if (container.getChildAt(index) !== row.view) {
                (row.view.parent as? ViewGroup)?.removeView(row.view)
                container.addView(row.view, index)
            }
            bind(row, meal)
        }
    }

    private fun createRow(): Row {
        val view = LayoutInflater.from(container.context).inflate(R.layout.item_meal, container, false)
        val row = Row(view)
        // Listeners read row.meal so they always act on the latest data
        view.findViewById<View>(R.id.btnPlus).setOnClickListener { row.meal?.let(onIncrease) }
        view.findViewById<View>(R.id.btnMinus).setOnClickListener { row.meal?.let(onDecrease) }
        view.findViewById<View>(R.id.btnDelete).setOnClickListener { row.meal?.let(onDelete) }
        return row
    }

    private fun bind(row: Row, meal: LoggedMeal) {
        row.meal = meal
        row.name.text = meal.name
        row.serving.text = "${meal.servings.fmt1()} × ${meal.servingLabel}"
        // Totals already include the number of servings
        row.nutrients.text = nutrientLine(meal.totalKcal, meal.totalCarbs, meal.totalProtein, meal.totalFat)
        row.servings.text = meal.servings.fmt1()

        // Only (re)load the photo if it changed
        if (!row.imageLoaded || row.imageUrl != meal.imageUrl) {
            row.imageLoaded = true
            row.imageUrl = meal.imageUrl
            Glide.with(row.image)
                .load(meal.imageUrl)
                .placeholder(R.drawable.bg_food_placeholder)
                .error(R.drawable.bg_food_placeholder)
                .centerCrop()
                .into(row.image)
        }
    }
}

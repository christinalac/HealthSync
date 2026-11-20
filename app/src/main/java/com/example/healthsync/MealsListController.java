//******************************************
// Program Name: MealsListController.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 2.1
// Purpose: Builds and updates the rows in Today's Meals so the card grows with each meal. Handles the servings stepper and delete button.
//******************************************

package com.example.healthsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Draws "Today's Meals" as plain rows inside a vertical LinearLayout.
// Because the rows are normal children (not a RecyclerView), the card grows by one row for every
// meal, and the page's ScrollView scrolls when the list gets long.
// Rows are reused between updates, so tapping +/- doesn't make photos flicker or reload.
public class MealsListController {

    public interface Listener {
        void onIncrease(LoggedMeal meal);
        void onDecrease(LoggedMeal meal);
        void onDelete(LoggedMeal meal);
    }

    private static class Row {
        final View view;
        final ImageView image;
        final TextView name, serving, nutrients, servings;
        // always the latest data for this row
        LoggedMeal meal = null;
        boolean imageLoaded = false;
        String imageUrl = null;

        Row(View rowView) {
            view = rowView;
            image = rowView.findViewById(R.id.ivMeal);
            name = rowView.findViewById(R.id.tvMealName);
            serving = rowView.findViewById(R.id.tvMealServing);
            nutrients = rowView.findViewById(R.id.tvMealNutrients);
            servings = rowView.findViewById(R.id.tvServings);
        }
    }

    private final LinearLayout container;
    private final Listener listener;
    // meal id -> its row
    private final Map<String, Row> rows = new LinkedHashMap<>();

    public MealsListController(LinearLayout container, Listener listener) {
        this.container = container;
        this.listener = listener;
    }

    public void submit(List<LoggedMeal> meals) {
        Set<String> keep = new HashSet<>();
        Iterator<Map.Entry<String, Row>> rowIterator = rows.entrySet().iterator();
        LoggedMeal meal = null;
        Row row = null;

        // 1. Remove rows for meals that were deleted
        for (LoggedMeal loggedMeal : meals) keep.add(loggedMeal.id);
        while (rowIterator.hasNext()) {
            Map.Entry<String, Row> entry = rowIterator.next();
            if (!keep.contains(entry.getKey())) {
                container.removeView(entry.getValue().view);
                rowIterator.remove();
            }
        }

        // 2. Create/reuse a row for each meal and make sure it sits in the right position
        for (int i = 0; i < meals.size(); i++) {
            meal = meals.get(i);
            row = rows.get(meal.id);
            if (row == null) {
                row = createRow();
                rows.put(meal.id, row);
            }
            if (container.getChildAt(i) != row.view) {
                ViewGroup parent = (ViewGroup) row.view.getParent();
                if (parent != null) parent.removeView(row.view);
                container.addView(row.view, i);
            }
            bind(row, meal);
        }
    }

    private Row createRow() {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_meal, container, false);
        final Row row = new Row(view);
        // Listeners read row.meal so they always act on the latest data
        view.findViewById(R.id.btnPlus).setOnClickListener(clickedView -> {
            if (row.meal != null) listener.onIncrease(row.meal);
        });
        view.findViewById(R.id.btnMinus).setOnClickListener(clickedView -> {
            if (row.meal != null) listener.onDecrease(row.meal);
        });
        view.findViewById(R.id.btnDelete).setOnClickListener(clickedView -> {
            if (row.meal != null) listener.onDelete(row.meal);
        });
        return row;
    }

    private void bind(Row row, LoggedMeal meal) {
        boolean urlChanged = false;
        if (row.imageUrl == null) {
            urlChanged = meal.imageUrl != null;
        } else {
            urlChanged = !row.imageUrl.equals(meal.imageUrl);
        }
        row.meal = meal;
        row.name.setText(meal.name);
        row.serving.setText(Format.fmt1(meal.servings) + " × " + meal.servingLabel);
        // Totals already include the number of servings
        row.nutrients.setText(Format.nutrientLine(
                meal.totalKcal(), meal.totalCarbs(), meal.totalProtein(), meal.totalFat()));
        row.servings.setText(Format.fmt1(meal.servings));

        // Only (re)load the photo if it changed
        if (!row.imageLoaded || urlChanged) {
            row.imageLoaded = true;
            row.imageUrl = meal.imageUrl;
            Glide.with(row.image)
                    .load(meal.imageUrl)
                    .placeholder(R.drawable.bg_food_placeholder)
                    .error(R.drawable.bg_food_placeholder)
                    .centerCrop()
                    .into(row.image);
        }
    }
}

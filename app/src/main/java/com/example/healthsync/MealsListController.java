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
        LoggedMeal meal;            // always the latest data for this row
        boolean imageLoaded = false;
        String imageUrl = null;

        Row(View v) {
            view = v;
            image = v.findViewById(R.id.ivMeal);
            name = v.findViewById(R.id.tvMealName);
            serving = v.findViewById(R.id.tvMealServing);
            nutrients = v.findViewById(R.id.tvMealNutrients);
            servings = v.findViewById(R.id.tvServings);
        }
    }

    private final LinearLayout container;
    private final Listener listener;
    private final Map<String, Row> rows = new LinkedHashMap<>();   // meal id -> its row

    public MealsListController(LinearLayout container, Listener listener) {
        this.container = container;
        this.listener = listener;
    }

    public void submit(List<LoggedMeal> meals) {
        // 1. Remove rows for meals that were deleted
        Set<String> keep = new HashSet<>();
        for (LoggedMeal m : meals) keep.add(m.id);
        Iterator<Map.Entry<String, Row>> it = rows.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Row> entry = it.next();
            if (!keep.contains(entry.getKey())) {
                container.removeView(entry.getValue().view);
                it.remove();
            }
        }

        // 2. Create/reuse a row for each meal and make sure it sits in the right position
        for (int i = 0; i < meals.size(); i++) {
            LoggedMeal meal = meals.get(i);
            Row row = rows.get(meal.id);
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
        view.findViewById(R.id.btnPlus).setOnClickListener(v -> {
            if (row.meal != null) listener.onIncrease(row.meal);
        });
        view.findViewById(R.id.btnMinus).setOnClickListener(v -> {
            if (row.meal != null) listener.onDecrease(row.meal);
        });
        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            if (row.meal != null) listener.onDelete(row.meal);
        });
        return row;
    }

    private void bind(Row row, LoggedMeal meal) {
        row.meal = meal;
        row.name.setText(meal.name);
        row.serving.setText(Format.fmt1(meal.servings) + " × " + meal.servingLabel);
        // Totals already include the number of servings
        row.nutrients.setText(Format.nutrientLine(
                meal.totalKcal(), meal.totalCarbs(), meal.totalProtein(), meal.totalFat()));
        row.servings.setText(Format.fmt1(meal.servings));

        // Only (re)load the photo if it changed
        boolean urlChanged = row.imageUrl == null ? meal.imageUrl != null : !row.imageUrl.equals(meal.imageUrl);
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

package com.example.healthsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// Draws "Today's Meals" as plain rows inside a vertical LinearLayout.
// Because the rows are normal children (not a RecyclerView), the card grows by one row for every
// meal, and the page's ScrollView scrolls when the list gets long.
// Rows are reused between updates, so tapping +/- doesn't make photos flicker or reload.
public class MealsListController {

    public interface MealAction {
        void run(LoggedMeal meal);
    }

    private final LinearLayout container;
    private final MealAction onIncrease;
    private final MealAction onDecrease;
    private final MealAction onDelete;

    private static final class Row {
        final View view;
        final ImageView image;
        final TextView name;
        final TextView serving;
        final TextView nutrients;
        final TextView servings;
        @Nullable LoggedMeal meal;          // always the latest data for this row
        boolean imageLoaded = false;
        @Nullable String imageUrl = null;

        Row(View view) {
            this.view = view;
            this.image = view.findViewById(R.id.ivMeal);
            this.name = view.findViewById(R.id.tvMealName);
            this.serving = view.findViewById(R.id.tvMealServing);
            this.nutrients = view.findViewById(R.id.tvMealNutrients);
            this.servings = view.findViewById(R.id.tvServings);
        }
    }

    private final LinkedHashMap<String, Row> rows = new LinkedHashMap<>();   // meal id -> its row

    public MealsListController(LinearLayout container, MealAction onIncrease,
                               MealAction onDecrease, MealAction onDelete) {
        this.container = container;
        this.onIncrease = onIncrease;
        this.onDecrease = onDecrease;
        this.onDelete = onDelete;
    }

    public void submit(List<LoggedMeal> meals) {
        // 1. Remove rows for meals that were deleted
        Set<String> keep = new HashSet<>();
        for (LoggedMeal m : meals) keep.add(m.getId());
        Iterator<Map.Entry<String, Row>> it = rows.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Row> entry = it.next();
            if (!keep.contains(entry.getKey())) {
                container.removeView(entry.getValue().view);
                it.remove();
            }
        }

        // 2. Create/reuse a row for each meal and make sure it sits in the right position
        for (int index = 0; index < meals.size(); index++) {
            LoggedMeal meal = meals.get(index);
            Row row = rows.get(meal.getId());
            if (row == null) {
                row = createRow();
                rows.put(meal.getId(), row);
            }
            if (container.getChildAt(index) != row.view) {
                if (row.view.getParent() instanceof ViewGroup) {
                    ((ViewGroup) row.view.getParent()).removeView(row.view);
                }
                container.addView(row.view, index);
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
            if (row.meal != null) onIncrease.run(row.meal);
        });
        view.findViewById(R.id.btnMinus).setOnClickListener(v -> {
            if (row.meal != null) onDecrease.run(row.meal);
        });
        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            if (row.meal != null) onDelete.run(row.meal);
        });
        return row;
    }

    private void bind(Row row, LoggedMeal meal) {
        row.meal = meal;
        row.name.setText(meal.getName());
        row.serving.setText(NutritionFormat.fmt1(meal.getServings()) + " × " + meal.getServingLabel());
        // Totals already include the number of servings
        row.nutrients.setText(NutritionFormat.nutrientLine(
                meal.getTotalKcal(), meal.getTotalCarbs(), meal.getTotalProtein(), meal.getTotalFat()));
        row.servings.setText(NutritionFormat.fmt1(meal.getServings()));

        // Only (re)load the photo if it changed
        if (!row.imageLoaded || !Objects.equals(row.imageUrl, meal.getImageUrl())) {
            row.imageLoaded = true;
            row.imageUrl = meal.getImageUrl();
            Glide.with(row.image)
                    .load(meal.getImageUrl())
                    .placeholder(R.drawable.bg_food_placeholder)
                    .error(R.drawable.bg_food_placeholder)
                    .centerCrop()
                    .into(row.image);
        }
    }
}

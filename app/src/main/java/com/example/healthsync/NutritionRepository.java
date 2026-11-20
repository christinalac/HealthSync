//******************************************
// Program Name: NutritionRepository.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 3.1
// Purpose: Saves and loads today's meals and water intake on the device, and resets them when a new day starts.
//******************************************

package com.example.healthsync;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Saves today's meals and water so they survive the app closing.
// Data resets automatically when the date changes.
// Later you could swap SharedPreferences for a table in DatabaseHelper without touching the UI.
public class NutritionRepository {

    private static final String PREFS = "nutrition_prefs";
    private static final String KEY_DATE = "date";
    private static final String KEY_MEALS = "meals_json";
    private static final String KEY_WATER_CUPS = "water_cups";
    // from earlier versions
    private static final String KEY_WATER_ML_OLD = "water_ml";

    private final SharedPreferences prefs;
    // day-by-day totals for the Progress page
    private final ProgressHistory history;

    public NutritionRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        history = new ProgressHistory(context);
    }

    public NutritionState load() {
        resetIfNewDay();
        List<LoggedMeal> meals = mealsFromJson(prefs.getString(KEY_MEALS, null));
        // Falls back to the old millilitre value (250 ml = 1 cup)
        int cups = prefs.getInt(KEY_WATER_CUPS, prefs.getInt(KEY_WATER_ML_OLD, 0) / 250);
        return NutritionState.initial().withMeals(meals).withHydrationCups(cups);
    }

    public void save(NutritionState state) {
        prefs.edit()
                .putString(KEY_DATE, today())
                .putString(KEY_MEALS, mealsToJson(state.meals))
                .putInt(KEY_WATER_CUPS, state.hydrationCups)
                .apply();

        // Also record today's totals in the history so the Progress page graphs fill in
        // (1 cup = 0.25 L, same as the 250 ml conversion above)
        history.recordNutrition(state.getCaloriesEaten(), state.hydrationCups * 0.25);
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private void resetIfNewDay() {
        String today = today();
        if (!today.equals(prefs.getString(KEY_DATE, today))) prefs.edit().clear().apply();
    }

    // ----- Meals <-> JSON -----
    private static String mealsToJson(List<LoggedMeal> meals) {
        JSONArray mealsArray = new JSONArray();
        JSONObject mealObject = null;
        Object imageUrlValue = null;
        try {
            for (LoggedMeal meal : meals) {
                mealObject = new JSONObject();
                if (meal.imageUrl == null) {
                    imageUrlValue = JSONObject.NULL;
                } else {
                    imageUrlValue = meal.imageUrl;
                }
                mealObject.put("id", meal.id);
                mealObject.put("code", meal.code);
                mealObject.put("name", meal.name);
                mealObject.put("brand", meal.brand);
                mealObject.put("imageUrl", imageUrlValue);
                mealObject.put("servingLabel", meal.servingLabel);
                mealObject.put("kcal", meal.kcalPerServing);
                mealObject.put("carbs", meal.carbsPerServing);
                mealObject.put("protein", meal.proteinPerServing);
                mealObject.put("fat", meal.fatPerServing);
                mealObject.put("servings", meal.servings);
                mealsArray.put(mealObject);
            }
        } catch (JSONException ignored) {
            // can't happen for these value types
        }
        return mealsArray.toString();
    }

    private static List<LoggedMeal> mealsFromJson(String json) {
        List<LoggedMeal> loadedMeals = new ArrayList<>();
        JSONArray mealsArray = null;
        JSONObject mealObject = null;
        String imageUrl = null;
        if (json == null || json.isEmpty()) return loadedMeals;
        try {
            mealsArray = new JSONArray(json);
            for (int i = 0; i < mealsArray.length(); i++) {
                mealObject = mealsArray.getJSONObject(i);
                if (mealObject.isNull("imageUrl")) {
                    imageUrl = null;
                } else {
                    imageUrl = mealObject.getString("imageUrl");
                }
                loadedMeals.add(new LoggedMeal(
                        mealObject.getString("id"),
                        mealObject.optString("code"),
                        mealObject.getString("name"),
                        mealObject.optString("brand"),
                        imageUrl,
                        mealObject.optString("servingLabel", "100 g"),
                        mealObject.optDouble("kcal", 0.0),
                        mealObject.optDouble("carbs", 0.0),
                        mealObject.optDouble("protein", 0.0),
                        mealObject.optDouble("fat", 0.0),
                        mealObject.optDouble("servings", 1.0)));
            }
        } catch (JSONException exception) {
            return new ArrayList<>();
        }
        return loadedMeals;
    }
}

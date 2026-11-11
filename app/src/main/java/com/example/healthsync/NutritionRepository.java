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
    private static final String KEY_WATER_ML_OLD = "water_ml";   // from earlier versions

    private final SharedPreferences prefs;

    public NutritionRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
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
        JSONArray arr = new JSONArray();
        try {
            for (LoggedMeal m : meals) {
                JSONObject o = new JSONObject();
                o.put("id", m.id);
                o.put("code", m.code);
                o.put("name", m.name);
                o.put("brand", m.brand);
                o.put("imageUrl", m.imageUrl == null ? JSONObject.NULL : m.imageUrl);
                o.put("servingLabel", m.servingLabel);
                o.put("kcal", m.kcalPerServing);
                o.put("carbs", m.carbsPerServing);
                o.put("protein", m.proteinPerServing);
                o.put("fat", m.fatPerServing);
                o.put("servings", m.servings);
                arr.put(o);
            }
        } catch (JSONException ignored) {
            // can't happen for these value types
        }
        return arr.toString();
    }

    private static List<LoggedMeal> mealsFromJson(String json) {
        List<LoggedMeal> out = new ArrayList<>();
        if (json == null || json.isEmpty()) return out;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                out.add(new LoggedMeal(
                        o.getString("id"),
                        o.optString("code"),
                        o.getString("name"),
                        o.optString("brand"),
                        o.isNull("imageUrl") ? null : o.getString("imageUrl"),
                        o.optString("servingLabel", "100 g"),
                        o.optDouble("kcal", 0.0),
                        o.optDouble("carbs", 0.0),
                        o.optDouble("protein", 0.0),
                        o.optDouble("fat", 0.0),
                        o.optDouble("servings", 1.0)));
            }
        } catch (JSONException e) {
            return new ArrayList<>();
        }
        return out;
    }
}

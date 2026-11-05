package com.example.healthsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Saves today's meals and water so they survive the app closing.
// Data resets automatically when the date changes.
// Later you can swap SharedPreferences for Room without touching the UI or ViewModel.
public class NutritionRepository {

    private static final String KEY_DATE = "date";
    private static final String KEY_MEALS = "meals_json";
    private static final String KEY_WATER = "water_ml";

    private final SharedPreferences prefs;

    public NutritionRepository(Context context) {
        this.prefs = context.getSharedPreferences("nutrition_prefs", Context.MODE_PRIVATE);
    }

    public NutritionState load() {
        resetIfNewDay();
        return new NutritionState(
                2000,
                mealsFromJson(prefs.getString(KEY_MEALS, null)),
                prefs.getInt(KEY_WATER, 0),
                2500,
                250
        );
    }

    public void save(NutritionState state) {
        prefs.edit()
                .putString(KEY_DATE, LocalDate.now().toString())
                .putString(KEY_MEALS, mealsToJson(state.getMeals()))
                .putInt(KEY_WATER, state.getHydrationMl())
                .apply();
    }

    private void resetIfNewDay() {
        String today = LocalDate.now().toString();
        if (!today.equals(prefs.getString(KEY_DATE, today))) {
            prefs.edit().clear().apply();
        }
    }

    // ----- Meals <-> JSON -----
    private String mealsToJson(List<LoggedMeal> meals) {
        JSONArray arr = new JSONArray();
        try {
            for (LoggedMeal m : meals) {
                arr.put(new JSONObject()
                        .put("id", m.getId()).put("code", m.getCode())
                        .put("name", m.getName()).put("brand", m.getBrand())
                        .put("imageUrl", m.getImageUrl() != null ? m.getImageUrl() : JSONObject.NULL)
                        .put("servingLabel", m.getServingLabel())
                        .put("kcal", m.getKcalPerServing()).put("carbs", m.getCarbsPerServing())
                        .put("protein", m.getProteinPerServing()).put("fat", m.getFatPerServing())
                        .put("servings", m.getServings()));
            }
        } catch (JSONException e) {
            return "[]";
        }
        return arr.toString();
    }

    private List<LoggedMeal> mealsFromJson(String json) {
        List<LoggedMeal> out = new ArrayList<>();
        if (TextUtils.isEmpty(json)) return out;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                out.add(new LoggedMeal(
                        o.getString("id"), o.optString("code"),
                        o.getString("name"), o.optString("brand"),
                        o.isNull("imageUrl") ? null : o.getString("imageUrl"),
                        o.optString("servingLabel", "100 g"),
                        o.optDouble("kcal", 0.0),
                        o.optDouble("carbs", 0.0),
                        o.optDouble("protein", 0.0),
                        o.optDouble("fat", 0.0),
                        o.optDouble("servings", 1.0)
                ));
            }
        } catch (JSONException e) {
            return new ArrayList<>();
        }
        return out;
    }
}

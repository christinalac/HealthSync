package com.example.healthsync;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

// Holds the screen's data and the actions that change it.
// LiveData (rather than Flow) keeps this easy to port to Java.
public class NutritionViewModel extends AndroidViewModel {

    private final NutritionRepository repo;
    private final MutableLiveData<NutritionState> state;

    public NutritionViewModel(@NonNull Application app) {
        super(app);
        repo = new NutritionRepository(app);
        state = new MutableLiveData<>(repo.load());
    }

    public LiveData<NutritionState> getState() {
        return state;
    }

    private NutritionState current() {
        NutritionState value = state.getValue();
        return value != null ? value : new NutritionState();
    }

    // ----- Hydration -----
    public void addGlass() {
        NutritionState s = current();
        apply(s.withHydrationMl(s.getHydrationMl() + s.getGlassMl()));
    }

    public void removeGlass() {
        NutritionState s = current();
        apply(s.withHydrationMl(Math.max(s.getHydrationMl() - s.getGlassMl(), 0)));
    }

    // ----- Meals -----
    // Adding a food that's already in today's list bumps its servings instead of duplicating it.
    public void addMeal(FoodItem food) {
        NutritionState s = current();
        List<LoggedMeal> meals = new ArrayList<>(s.getMeals());
        int i = -1;
        for (int idx = 0; idx < meals.size(); idx++) {
            LoggedMeal m = meals.get(idx);
            if (m.getCode() != null && !m.getCode().isEmpty() && m.getCode().equals(food.getCode())) {
                i = idx;
                break;
            }
        }
        if (i >= 0) {
            LoggedMeal existing = meals.get(i);
            meals.set(i, existing.withServings(existing.getServings() + 1.0));
        } else {
            meals.add(food.toMeal());
        }
        apply(s.withMeals(meals));
    }

    // delta is +0.5 / -0.5 from the stepper buttons; never goes below 0.5 (use delete to remove)
    public void changeServings(String id, double delta) {
        NutritionState s = current();
        List<LoggedMeal> meals = new ArrayList<>(s.getMeals().size());
        for (LoggedMeal m : s.getMeals()) {
            if (m.getId().equals(id)) {
                meals.add(m.withServings(Math.max(m.getServings() + delta, 0.5)));
            } else {
                meals.add(m);
            }
        }
        apply(s.withMeals(meals));
    }

    public void removeMeal(String id) {
        NutritionState s = current();
        List<LoggedMeal> meals = new ArrayList<>();
        for (LoggedMeal m : s.getMeals()) {
            if (!m.getId().equals(id)) meals.add(m);
        }
        apply(s.withMeals(meals));
    }

    // For testing: wipes today's meals and water
    public void resetToday() {
        NutritionState s = current();
        apply(s.withMealsAndHydration(new ArrayList<LoggedMeal>(), 0));
    }

    private void apply(NutritionState next) {
        state.setValue(next);
        repo.save(next);
    }
}

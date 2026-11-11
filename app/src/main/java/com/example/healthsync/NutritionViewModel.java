//******************************************
// Program Name: NutritionViewModel.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 3.0
// Purpose: Holds the Nutrition screen's data and handles actions: adding cups of water, adding and removing meals, changing servings, and refreshing goals from the user's profile.
//******************************************

package com.example.healthsync;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

// Holds the screen's data and the actions that change it.
public class NutritionViewModel extends AndroidViewModel {

    private final NutritionRepository repo;
    private final DatabaseHelper db;
    private final MutableLiveData<NutritionState> state = new MutableLiveData<>();

    public NutritionViewModel(@NonNull Application app) {
        super(app);
        repo = new NutritionRepository(app);
        db = new DatabaseHelper(app);
        state.setValue(withGoals(repo.load()));
    }

    public LiveData<NutritionState> getState() {
        return state;
    }

    private NutritionState current() {
        NutritionState s = state.getValue();
        return s != null ? s : NutritionState.initial();
    }

    // ----- Goals -----
    // Recalculates the calorie and water goals from the logged-in user's profile.
    // Call when the screen comes back into view, in case the profile was edited.
    public void refreshGoals() {
        NutritionState cur = current();
        NutritionState next = withGoals(cur);
        if (next.calorieGoal != cur.calorieGoal || next.hydrationGoalCups != cur.hydrationGoalCups) {
            state.setValue(next);
        }
    }

    private NutritionState withGoals(NutritionState s) {
        UserProfile profile = null;
        String email = Session.getEmail(getApplication());
        if (email != null) profile = db.getUserProfile(email);
        DailyGoals.Goals goals = DailyGoals.forProfile(profile);
        return s.withGoals(goals.calories, goals.waterCups);
    }

    // ----- Hydration (1 tap = 1 cup) -----
    public void addCup() {
        NutritionState s = current();
        update(s.withHydrationCups(s.hydrationCups + 1));
    }

    public void removeCup() {
        NutritionState s = current();
        update(s.withHydrationCups(Math.max(s.hydrationCups - 1, 0)));
    }

    // ----- Meals -----
    // Adding a food that's already in today's list bumps its servings instead of duplicating it.
    public void addMeal(FoodItem food) {
        NutritionState s = current();
        List<LoggedMeal> meals = new ArrayList<>(s.meals);
        int existing = -1;
        for (int i = 0; i < meals.size(); i++) {
            if (!food.code.isEmpty() && food.code.equals(meals.get(i).code)) {
                existing = i;
                break;
            }
        }
        if (existing >= 0) {
            LoggedMeal m = meals.get(existing);
            meals.set(existing, m.withServings(m.servings + 1.0));
        } else {
            meals.add(food.toMeal());
        }
        update(s.withMeals(meals));
    }

    // delta is +0.5 / -0.5 from the stepper buttons; never goes below 0.5 (use delete to remove)
    public void changeServings(String id, double delta) {
        NutritionState s = current();
        List<LoggedMeal> meals = new ArrayList<>();
        for (LoggedMeal m : s.meals) {
            meals.add(m.id.equals(id) ? m.withServings(Math.max(m.servings + delta, 0.5)) : m);
        }
        update(s.withMeals(meals));
    }

    public void removeMeal(String id) {
        NutritionState s = current();
        List<LoggedMeal> meals = new ArrayList<>();
        for (LoggedMeal m : s.meals) {
            if (!m.id.equals(id)) meals.add(m);
        }
        update(s.withMeals(meals));
    }

    // For testing: wipes today's meals and water
    public void resetToday() {
        update(current().withMeals(new ArrayList<LoggedMeal>()).withHydrationCups(0));
    }

    private void update(NutritionState next) {
        state.setValue(next);
        repo.save(next);
    }
}

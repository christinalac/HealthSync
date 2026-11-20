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

    public NutritionViewModel(@NonNull Application application) {
        super(application);
        repo = new NutritionRepository(application);
        db = new DatabaseHelper(application);
        state.setValue(withGoals(repo.load()));
    }

    public LiveData<NutritionState> getState() {
        return state;
    }

    private NutritionState current() {
        NutritionState currentState = state.getValue();
        if (currentState == null) {
            currentState = NutritionState.initial();
        }
        return currentState;
    }

    // ----- Goals -----
    // Recalculates the calorie and water goals from the logged-in user's profile.
    // Call when the screen comes back into view, in case the profile was edited.
    public void refreshGoals() {
        NutritionState currentState = current();
        NutritionState nextState = withGoals(currentState);
        if (nextState.calorieGoal != currentState.calorieGoal
                || nextState.hydrationGoalCups != currentState.hydrationGoalCups) {
            state.setValue(nextState);
        }
    }

    private NutritionState withGoals(NutritionState baseState) {
        UserProfile profile = null;
        String email = Session.getEmail(getApplication());
        DailyGoals.Goals goals = null;
        if (email != null) profile = db.getUserProfile(email);
        goals = DailyGoals.forProfile(profile);
        return baseState.withGoals(goals.calories, goals.waterCups);
    }

    // ----- Hydration (1 tap = 1 cup) -----
    public void addCup() {
        NutritionState currentState = current();
        update(currentState.withHydrationCups(currentState.hydrationCups + 1));
    }

    public void removeCup() {
        NutritionState currentState = current();
        update(currentState.withHydrationCups(Math.max(currentState.hydrationCups - 1, 0)));
    }

    // ----- Meals -----
    // Adding a food that's already in today's list bumps its servings instead of duplicating it.
    public void addMeal(FoodItem food) {
        NutritionState currentState = current();
        List<LoggedMeal> meals = new ArrayList<>(currentState.meals);
        int existing = -1;
        LoggedMeal existingMeal = null;
        for (int i = 0; i < meals.size(); i++) {
            if (!food.code.isEmpty() && food.code.equals(meals.get(i).code)) {
                existing = i;
                break;
            }
        }
        if (existing >= 0) {
            existingMeal = meals.get(existing);
            meals.set(existing, existingMeal.withServings(existingMeal.servings + 1.0));
        } else {
            meals.add(food.toMeal());
        }
        update(currentState.withMeals(meals));
    }

    // delta is +0.5 / -0.5 from the stepper buttons; never goes below 0.5 (use delete to remove)
    public void changeServings(String id, double delta) {
        NutritionState currentState = current();
        List<LoggedMeal> meals = new ArrayList<>();
        for (LoggedMeal meal : currentState.meals) {
            if (meal.id.equals(id)) {
                meals.add(meal.withServings(Math.max(meal.servings + delta, 0.5)));
            } else {
                meals.add(meal);
            }
        }
        update(currentState.withMeals(meals));
    }

    public void removeMeal(String id) {
        NutritionState currentState = current();
        List<LoggedMeal> meals = new ArrayList<>();
        for (LoggedMeal meal : currentState.meals) {
            if (!meal.id.equals(id)) meals.add(meal);
        }
        update(currentState.withMeals(meals));
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

package com.example.healthsync;

import java.util.ArrayList;
import java.util.List;

// One snapshot of everything the Nutrition screen displays. Immutable.
// Goals come from the user's profile; calories/macros are summed from today's meals.
// Hydration is tracked in cups (1 cup = 250 ml, matching PersonalizedCalculations.litresToCups).
public class NutritionState {
    public final int calorieGoal;
    public final List<LoggedMeal> meals;
    public final int hydrationCups;
    public final double hydrationGoalCups;

    public NutritionState(int calorieGoal, List<LoggedMeal> meals, int hydrationCups, double hydrationGoalCups) {
        this.calorieGoal = calorieGoal;
        this.meals = meals;
        this.hydrationCups = hydrationCups;
        this.hydrationGoalCups = hydrationGoalCups;
    }

    public static NutritionState initial() {
        return new NutritionState(DailyGoals.DEFAULT_CALORIES, new ArrayList<LoggedMeal>(), 0,
                DailyGoals.DEFAULT_WATER_CUPS);
    }

    // ----- "Copy with one change" helpers -----
    public NutritionState withMeals(List<LoggedMeal> newMeals) {
        return new NutritionState(calorieGoal, newMeals, hydrationCups, hydrationGoalCups);
    }

    public NutritionState withHydrationCups(int cups) {
        return new NutritionState(calorieGoal, meals, cups, hydrationGoalCups);
    }

    public NutritionState withGoals(int newCalorieGoal, double newWaterGoalCups) {
        return new NutritionState(newCalorieGoal, meals, hydrationCups, newWaterGoalCups);
    }

    // ----- Calories & macros (sum of meals x servings) -----
    public int getCaloriesEaten() {
        double sum = 0;
        for (LoggedMeal m : meals) sum += m.totalKcal();
        return (int) Math.round(sum);
    }

    public int getCarbsG() {
        double sum = 0;
        for (LoggedMeal m : meals) sum += m.totalCarbs();
        return (int) Math.round(sum);
    }

    public int getProteinG() {
        double sum = 0;
        for (LoggedMeal m : meals) sum += m.totalProtein();
        return (int) Math.round(sum);
    }

    public int getFatG() {
        double sum = 0;
        for (LoggedMeal m : meals) sum += m.totalFat();
        return (int) Math.round(sum);
    }

    public int getCaloriesLeft() {
        return Math.max(calorieGoal - getCaloriesEaten(), 0);
    }

    // 0..100 for the ProgressBar
    public int getCalorieProgressPercent() {
        if (calorieGoal == 0) return 0;
        return Math.min(getCaloriesEaten() * 100 / calorieGoal, 100);
    }

    // ----- Hydration (cups) -----
    // 0f..1f for the water bar
    public float getHydrationProgress() {
        if (hydrationGoalCups <= 0.0) return 0f;
        return Math.min((float) (hydrationCups / hydrationGoalCups), 1f);
    }

    public int getHydrationPercent() {
        return (int) (getHydrationProgress() * 100);
    }

    public double getCupsRemaining() {
        return Math.max(hydrationGoalCups - hydrationCups, 0.0);
    }
}

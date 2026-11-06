package com.example.healthsync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// One snapshot of everything the Nutrition screen displays.
// Calories and macros are calculated from today's meals.
public final class NutritionState {

    private final int calorieGoal;
    private final List<LoggedMeal> meals;
    private final int hydrationMl;
    private final int hydrationGoalMl;
    private final int glassMl;

    public NutritionState() {
        this(2000, Collections.<LoggedMeal>emptyList(), 0, 2500, 250);
    }

    public NutritionState(int calorieGoal, List<LoggedMeal> meals, int hydrationMl,
                          int hydrationGoalMl, int glassMl) {
        this.calorieGoal = calorieGoal;
        this.meals = Collections.unmodifiableList(new ArrayList<>(meals));
        this.hydrationMl = hydrationMl;
        this.hydrationGoalMl = hydrationGoalMl;
        this.glassMl = glassMl;
    }

    public int getCalorieGoal() { return calorieGoal; }
    public List<LoggedMeal> getMeals() { return meals; }
    public int getHydrationMl() { return hydrationMl; }
    public int getHydrationGoalMl() { return hydrationGoalMl; }
    public int getGlassMl() { return glassMl; }

    // ----- copy helpers (replace Kotlin data-class copy) -----
    public NutritionState withMeals(List<LoggedMeal> newMeals) {
        return new NutritionState(calorieGoal, newMeals, hydrationMl, hydrationGoalMl, glassMl);
    }

    public NutritionState withHydrationMl(int newHydrationMl) {
        return new NutritionState(calorieGoal, meals, newHydrationMl, hydrationGoalMl, glassMl);
    }

    public NutritionState withMealsAndHydration(List<LoggedMeal> newMeals, int newHydrationMl) {
        return new NutritionState(calorieGoal, newMeals, newHydrationMl, hydrationGoalMl, glassMl);
    }

    // ----- Calories & macros (sum of meals x servings) -----
    public int getCaloriesEaten() {
        double sum = 0.0;
        for (LoggedMeal m : meals) sum += m.getTotalKcal();
        return (int) Math.round(sum);
    }

    public int getCarbsG() {
        double sum = 0.0;
        for (LoggedMeal m : meals) sum += m.getTotalCarbs();
        return (int) Math.round(sum);
    }

    public int getProteinG() {
        double sum = 0.0;
        for (LoggedMeal m : meals) sum += m.getTotalProtein();
        return (int) Math.round(sum);
    }

    public int getFatG() {
        double sum = 0.0;
        for (LoggedMeal m : meals) sum += m.getTotalFat();
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

    // ----- Hydration -----
    public int getTotalGlasses() {
        return (int) Math.ceil(hydrationGoalMl / (double) glassMl);
    }

    public int getGlassesDrunk() {
        return Math.min(hydrationMl / glassMl, getTotalGlasses());
    }

    // 0f..1f for the water bar
    public float getHydrationProgress() {
        if (hydrationGoalMl == 0) return 0f;
        return Math.min((float) hydrationMl / hydrationGoalMl, 1f);
    }

    public int getHydrationPercent() {
        return (int) (getHydrationProgress() * 100);
    }

    public double getHydrationLiters() {
        return hydrationMl / 1000.0;
    }

    public double getHydrationGoalLiters() {
        return hydrationGoalMl / 1000.0;
    }
}

//******************************************
// Program Name: DailyGoals.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 3.0
// Purpose: Calculates the daily calorie goal and water goal (in cups) from a user's profile using PersonalizedCalculations.
//******************************************

package com.example.healthsync;

// Turns a UserProfile into today's calorie and water goals using PersonalizedCalculations.
public final class DailyGoals {
    private DailyGoals() {}

    // Used until the user has a complete profile
    public static final int DEFAULT_CALORIES = 2000;
    public static final double DEFAULT_WATER_CUPS = 10.0;

    public static class Goals {
        public final int calories;
        public final double waterCups;

        Goals(int calories, double waterCups) {
            this.calories = calories;
            this.waterCups = waterCups;
        }
    }

    public static Goals forProfile(UserProfile p) {
        if (p == null || !p.isComplete()) return new Goals(DEFAULT_CALORIES, DEFAULT_WATER_CUPS);

        int calories = PersonalizedCalculations.calculateDailyCalories(
                p.gender, p.weightLbs, p.heightFt, p.heightIn, p.age,
                p.activityLevel, p.goal, p.isRestDay);
        double litres = PersonalizedCalculations.calculateDailyWaterLitres(
                p.gender, p.weightLbs, p.activityLevel, p.goal, p.isPregnant, p.isBreastfeeding);
        double cups = PersonalizedCalculations.litresToCups(litres);

        // Guard against nonsense input producing a zero/negative goal
        return new Goals(Math.max(calories, 1), Math.max(cups, 1.0));
    }
}

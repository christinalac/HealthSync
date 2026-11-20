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
            // Only accept sensible values; otherwise use 0 as the "invalid" marker
            if (calories >= 0 && calories <= 100000) {
                this.calories = calories;
            } else {
                this.calories = 0;
            }
            if (waterCups >= 0 && waterCups <= 10000) {
                this.waterCups = waterCups;
            } else {
                this.waterCups = 0;
            }
        }
    }

    public static Goals forProfile(UserProfile profile) {
        int calories = 0;
        double litres = 0;
        double cups = 0;

        if (profile == null || !profile.isComplete()) return new Goals(DEFAULT_CALORIES, DEFAULT_WATER_CUPS);

        calories = PersonalizedCalculations.calculateDailyCalories(
                profile.gender, profile.weightLbs, profile.heightFt, profile.heightIn, profile.age,
                profile.activityLevel, profile.goal, profile.isRestDay);
        litres = PersonalizedCalculations.calculateDailyWaterLitres(
                profile.gender, profile.weightLbs, profile.activityLevel, profile.goal,
                profile.isPregnant, profile.isBreastfeeding);
        cups = PersonalizedCalculations.litresToCups(litres);

        // Guard against nonsense input producing a zero/negative goal
        return new Goals(Math.max(calories, 1), Math.max(cups, 1.0));
    }
}

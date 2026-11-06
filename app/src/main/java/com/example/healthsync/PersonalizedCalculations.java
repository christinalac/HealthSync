//******************************************
// Program Name: PersonalizedCalculations.java
// Author: Christina LaCombe
// Date Created: 11/4/2026
// Version: 1.0
// Purpose: Calculate personalized daily calorie goal,
// water intake goal, and sleep suggestion based on the
// user's profile data (gender, age, height, weight,
// activity level, body goal, and pregnancy status).
//******************************************
package com.example.healthsync;

public class PersonalizedCalculations {

    // ── Activity Level Constants ──────────────────────────────

    public static final String ACTIVITY_SEDENTARY         = "Sedentary";
    public static final String ACTIVITY_LIGHTLY_ACTIVE    = "Lightly Active";
    public static final String ACTIVITY_MODERATELY_ACTIVE = "Moderately Active";
    public static final String ACTIVITY_VERY_ACTIVE       = "Very Active";
    public static final String ACTIVITY_EXTRA_ACTIVE      = "Extra Active";

    // ── Goal Constants ────────────────────────────────────────

    public static final String GOAL_LOSE_WEIGHT            = "Lose Weight";
    public static final String GOAL_GAIN_MUSCLE            = "Gain Muscle";
    public static final String GOAL_MAINTAIN_WEIGHT        = "Maintain Weight";
    public static final String GOAL_IMPROVE_ENDURANCE      = "Improve Endurance";
    public static final String GOAL_IMPROVE_OVERALL_HEALTH = "Improve Overall Health";

    // ─────────────────────────────────────────────────────────
    // CALORIE CALCULATIONS (Mifflin-St Jeor BMR + activity + goal)
    // ─────────────────────────────────────────────────────────

    /**
     * Calculates the recommended daily calorie intake.
     *
     * @param gender        "Male" or "Female"
     * @param weightLbs     body weight in pounds
     * @param heightFt      height feet component
     * @param heightIn      height inches component
     * @param age           age in years
     * @param activityLevel one of the ACTIVITY_* constants
     * @param goal          one of the GOAL_* constants
     * @param isRestDay     only relevant for Improve Endurance goal;
     *                      true = rest day (+200 kcal), false = active day (+300 kcal)
     * @return recommended daily calories (kcal), rounded to nearest integer
     */
    public static int calculateDailyCalories(String gender, int weightLbs, int heightFt,
                                             int heightIn, int age, String activityLevel,
                                             String goal, boolean isRestDay) {
        // Convert imperial to metric
        double weightKg = lbsToKg(weightLbs);
        double heightCm = feetInchesToCm(heightFt, heightIn);

        // Step 1 — Mifflin-St Jeor BMR
        double bmr;
        if ("Male".equalsIgnoreCase(gender)) {
            // BMR = 10 × weight(kg) + 6.25 × height(cm) − 5 × age + 5
            bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5;
        } else {
            // BMR = 10 × weight(kg) + 6.25 × height(cm) − 5 × age − 161
            bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161;
        }

        // Step 2 — Apply activity multiplier (TDEE)
        double tdee = bmr * getActivityMultiplier(activityLevel);

        // Step 3 — Apply goal adjustment
        double goalAdjustment = getCalorieGoalAdjustment(goal, isRestDay);

        double totalCalories = tdee + goalAdjustment;

        return (int) Math.round(totalCalories);
    } // end of calculateDailyCalories

    /**
     * Returns the activity multiplier for TDEE calculation.
     */
    private static double getActivityMultiplier(String activityLevel) {
        if (activityLevel == null) return 1.2;
        switch (activityLevel) {
            case ACTIVITY_LIGHTLY_ACTIVE:    return 1.375;
            case ACTIVITY_MODERATELY_ACTIVE: return 1.55;
            case ACTIVITY_VERY_ACTIVE:       return 1.725;
            case ACTIVITY_EXTRA_ACTIVE:      return 1.9;
            case ACTIVITY_SEDENTARY:
            default:                         return 1.2;
        }
    } // end of getActivityMultiplier

    /**
     * Returns the kcal adjustment based on body goal.
     * For "Improve Endurance": +300 on active days, +200 on rest days.
     */
    private static double getCalorieGoalAdjustment(String goal, boolean isRestDay) {
        if (goal == null) return 0;
        switch (goal) {
            case GOAL_LOSE_WEIGHT:       return -400;
            case GOAL_GAIN_MUSCLE:       return  250;
            case GOAL_IMPROVE_ENDURANCE: return isRestDay ? 200 : 300;
            case GOAL_MAINTAIN_WEIGHT:
            case GOAL_IMPROVE_OVERALL_HEALTH:
            default:                     return 0;
        }
    } // end of getCalorieGoalAdjustment

    // ─────────────────────────────────────────────────────────
    // WATER INTAKE CALCULATIONS
    // ─────────────────────────────────────────────────────────

    /**
     * Calculates the recommended daily water intake in litres.
     *
     * @param gender            "Male" or "Female"
     * @param weightLbs         body weight in pounds
     * @param activityLevel     one of the ACTIVITY_* constants
     * @param goal              one of the GOAL_* constants
     * @param isPregnant        true if the user is pregnant (female only)
     * @param isBreastfeeding   true if the user is breastfeeding (female only)
     * @return daily water intake in litres, rounded to two decimal places
     */
    public static double calculateDailyWaterLitres(String gender, int weightLbs,
                                                    String activityLevel, String goal,
                                                    boolean isPregnant, boolean isBreastfeeding) {
        double weightKg = lbsToKg(weightLbs);

        // Step 1 — Base calculation
        // Base (L/day) = weight (kg) × 0.033
        double base = weightKg * 0.033;

        // Women subtract 0.3 L from base
        if ("Female".equalsIgnoreCase(gender)) {
            base -= 0.3;
        }

        // Step 2 — Activity adjustment
        base += getWaterActivityAdjustment(activityLevel);

        // Step 3 — Goal adjustment
        base += getWaterGoalAdjustment(goal);

        // Step 4 — Pregnancy / breastfeeding adjustments (female only)
        if ("Female".equalsIgnoreCase(gender)) {
            if (isPregnant)      base += 0.4;
            if (isBreastfeeding) base += 0.8;
        }

        // Round to 2 decimal places
        return Math.round(base * 100.0) / 100.0;
    } // end of calculateDailyWaterLitres

    /**
     * Converts the daily water intake from litres to cups.
     * 1 L = 4 cups
     */
    public static double litresToCups(double litres) {
        return Math.round(litres * 4.0 * 10.0) / 10.0;
    } // end of litresToCups

    /**
     * Returns the water adjustment (in L) for activity level.
     * Note: activity levels are mapped to the closest documented tier:
     *   Sedentary          → +0.0 L
     *   Lightly Active     → +0.35 L  (moderate, 3–4 sessions/week)
     *   Moderately Active  → +0.35 L  (moderate, 3–4 sessions/week)
     *   Very Active        → +0.6 L   (active, daily exercise)
     *   Extra Active       → +1.0 L   (very active, intense training)
     */
    private static double getWaterActivityAdjustment(String activityLevel) {
        if (activityLevel == null) return 0;
        switch (activityLevel) {
            case ACTIVITY_LIGHTLY_ACTIVE:
            case ACTIVITY_MODERATELY_ACTIVE: return 0.35;
            case ACTIVITY_VERY_ACTIVE:       return 0.6;
            case ACTIVITY_EXTRA_ACTIVE:      return 1.0;
            case ACTIVITY_SEDENTARY:
            default:                         return 0.0;
        }
    } // end of getWaterActivityAdjustment

    /**
     * Returns the water goal adjustment in litres.
     */
    private static double getWaterGoalAdjustment(String goal) {
        if (goal == null) return 0;
        switch (goal) {
            case GOAL_LOSE_WEIGHT:
            case GOAL_GAIN_MUSCLE:
            case GOAL_IMPROVE_ENDURANCE: return 0.3;
            case GOAL_MAINTAIN_WEIGHT:
            case GOAL_IMPROVE_OVERALL_HEALTH:
            default:                     return 0.0;
        }
    } // end of getWaterGoalAdjustment

    // ─────────────────────────────────────────────────────────
    // SLEEP SUGGESTION
    // ─────────────────────────────────────────────────────────

    /**
     * Returns the recommended sleep range as a human-readable string,
     * based solely on age (suggestion only — not a medical prescription).
     *
     * Age ranges:
     *   14–17  → "8 to 10 hours"
     *   18–25  → "7 to 9 hours"
     *   26–64  → "7 to 9 hours"
     *   65+    → "7 to 8 hours"
     *
     * @param age age in years
     * @return sleep suggestion string
     */
    public static String getSleepSuggestion(int age) {
        if (age >= 14 && age <= 17) {
            return "8 to 10 hours";
        } else if (age >= 18 && age <= 25) {
            return "7 to 9 hours";
        } else if (age >= 26 && age <= 64) {
            return "7 to 9 hours";
        } else {
            // 65+
            return "7 to 8 hours";
        }
    } // end of getSleepSuggestion

    // ─────────────────────────────────────────────────────────
    // UNIT CONVERSION HELPERS
    // ─────────────────────────────────────────────────────────

    /**
     * Converts pounds to kilograms.
     * kg = lbs ÷ 2.2046
     */
    public static double lbsToKg(int lbs) {
        return lbs / 2.2046;
    } // end of lbsToKg

    /**
     * Converts feet + inches to centimetres.
     * 1 ft = 30.48 cm, 1 in = 2.54 cm
     */
    public static double feetInchesToCm(int feet, int inches) {
        return (feet * 30.48) + (inches * 2.54);
    } // end of feetInchesToCm

} // end of class PersonalizedCalculations

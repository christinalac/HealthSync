//******************************************
// Program Name: UserProfile.java
// Developer: Maximiliano Salinas, Anthony Ramirez and Christina LaCombe
// Date Created: 10/12/2026
// Version: 3.1
// Purpose: Model holding the user's profile (name, email, gender, age, height, weight,
// activity level, and goal) used by the Account screen and to personalize calculations.
//******************************************
package com.example.healthsync;

// One row of the user_profile table, loaded by DatabaseHelper.getUserProfile().
// Used by the Account screen and by PersonalizedCalculations / DailyGoals.
public class UserProfile {
    public final String email;
    public final String name;
    public final int age;
    // "Male" or "Female"
    public final String gender;
    public final int heightFt;
    public final int heightIn;
    public final int weightLbs;
    // one of PersonalizedCalculations.GOAL_*
    public final String goal;
    // one of PersonalizedCalculations.ACTIVITY_*
    public final String activityLevel;
    public final boolean isPregnant;
    public final boolean isBreastfeeding;

    // Only matters for the "Improve Endurance" goal. Not stored in the database yet;
    // meant to come from the Workout tab later.
    public final boolean isRestDay;

    public UserProfile(String email, String name, int age, String gender, int heightFt,
                       int heightIn, int weightLbs, String goal, String activityLevel,
                       boolean isPregnant, boolean isBreastfeeding, boolean isRestDay) {
        // Valid ranges are generous; anything outside becomes -1 (strings: null becomes "")
        if (email != null) {
            this.email = email;
        } else {
            this.email = "";
        }
        if (name != null) {
            this.name = name;
        } else {
            this.name = "";
        }
        if (age >= 1 && age <= 120) {
            this.age = age;
        } else {
            this.age = -1;
        }
        if (gender != null) {
            this.gender = gender;
        } else {
            this.gender = "";
        }
        if (heightFt >= 0 && heightFt <= 9) {
            this.heightFt = heightFt;
        } else {
            this.heightFt = -1;
        }
        if (heightIn >= 0 && heightIn <= 12) {
            this.heightIn = heightIn;
        } else {
            this.heightIn = -1;
        }
        if (weightLbs >= 0 && weightLbs <= 1500) {
            this.weightLbs = weightLbs;
        } else {
            this.weightLbs = -1;
        }
        if (goal != null) {
            this.goal = goal;
        } else {
            this.goal = "";
        }
        if (activityLevel != null) {
            this.activityLevel = activityLevel;
        } else {
            this.activityLevel = "";
        }
        this.isPregnant = isPregnant;
        this.isBreastfeeding = isBreastfeeding;
        this.isRestDay = isRestDay;
    }

    // Enough info to run the formulas?
    public boolean isComplete() {
        return gender != null && !gender.trim().isEmpty() && weightLbs > 0 && heightFt > 0 && age > 0;
    }

    public String getGender() {
        return gender;
    }

    public int getWeightLbs() {
        return weightLbs;
    }

    public int getHeightFt() {
        return heightFt;
    }

    public int getHeightIn() {
        return heightIn;
    }

    public int getAge() {
        return age;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public String getGoal() {
        return goal;
    }

    public boolean isPregnant() {
        return isPregnant;
    }

    public boolean isBreastfeeding() {
        return isBreastfeeding;
    }

    public boolean isRestDay() {
        return isRestDay;
    }
}

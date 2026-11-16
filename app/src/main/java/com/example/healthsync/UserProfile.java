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
    public final String gender;          // "Male" or "Female"
    public final int heightFt;
    public final int heightIn;
    public final int weightLbs;
    public final String goal;            // one of PersonalizedCalculations.GOAL_*
    public final String activityLevel;   // one of PersonalizedCalculations.ACTIVITY_*
    public final boolean isPregnant;
    public final boolean isBreastfeeding;

    // Only matters for the "Improve Endurance" goal. Not stored in the database yet;
    // meant to come from the Workout tab later.
    public final boolean isRestDay;

    public UserProfile(String email, String name, int age, String gender, int heightFt,
                       int heightIn, int weightLbs, String goal, String activityLevel,
                       boolean isPregnant, boolean isBreastfeeding, boolean isRestDay) {
        this.email = email;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.heightFt = heightFt;
        this.heightIn = heightIn;
        this.weightLbs = weightLbs;
        this.goal = goal;
        this.activityLevel = activityLevel;
        this.isPregnant = isPregnant;
        this.isBreastfeeding = isBreastfeeding;
        this.isRestDay = isRestDay;
    }

    // Enough info to run the formulas?
    public boolean isComplete() {
        return gender != null && !gender.trim().isEmpty() && weightLbs > 0 && heightFt > 0 && age > 0;
    }
}

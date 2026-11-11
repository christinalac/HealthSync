package com.example.healthsync;

// Everything PersonalizedCalculations needs to know about the user.
// Loaded from the user_profile table by DatabaseHelper.getUserProfile().
public class UserProfile {
    public final String gender;          // "Male" or "Female"
    public final int weightLbs;
    public final int heightFt;
    public final int heightIn;
    public final int age;
    public final String activityLevel;   // one of PersonalizedCalculations.ACTIVITY_*
    public final String goal;            // one of PersonalizedCalculations.GOAL_*
    public final boolean isPregnant;
    public final boolean isBreastfeeding;
    // Only matters for the "Improve Endurance" goal. Not stored in the database yet;
    // meant to come from the Workout tab later.
    public final boolean isRestDay;

    public UserProfile(String gender, int weightLbs, int heightFt, int heightIn, int age,
                       String activityLevel, String goal, boolean isPregnant,
                       boolean isBreastfeeding, boolean isRestDay) {
        this.gender = gender;
        this.weightLbs = weightLbs;
        this.heightFt = heightFt;
        this.heightIn = heightIn;
        this.age = age;
        this.activityLevel = activityLevel;
        this.goal = goal;
        this.isPregnant = isPregnant;
        this.isBreastfeeding = isBreastfeeding;
        this.isRestDay = isRestDay;
    }

    // Enough info to run the formulas?
    public boolean isComplete() {
        return gender != null && !gender.trim().isEmpty() && weightLbs > 0 && heightFt > 0 && age > 0;
    }
}

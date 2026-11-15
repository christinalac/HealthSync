//******************************************
// Program Name: UserProfile.java
// Developer: Anthony Ramirez and Christina LaCombe
// Date Created: 11/11/2026
// Version: 1.1
// Purpose: Plain data holder for one row of the user_profile table, as read by DatabaseHelper.
//******************************************
package com.example.healthsync;

// One row of the user_profile table, as read by DatabaseHelper.getUserProfile()
public class UserProfile {
    public final String email;
    public final String name;
    public final int age;
    public final String gender;
    public final int heightFt;
    public final int heightIn;
    public final int weightLbs;
    public final String goal;
    public final String activityLevel;
    public final boolean isPregnant;
    public final boolean isBreastfeeding;

    public UserProfile(String email, String name, int age, String gender, int heightFt,
                       int heightIn, int weightLbs, String goal, String activityLevel,
                       boolean isPregnant, boolean isBreastfeeding) {
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
    }
}

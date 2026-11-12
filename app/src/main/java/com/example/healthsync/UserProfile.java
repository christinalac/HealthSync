//******************************************
// Program Name: UserProfile.java
// Developer: Christina LaCombe
// Date Created: 11/4/2026
// Version: 1.0
// Purpose: Represents the user profile data stored in the database
// used for personalized health calculations.
//******************************************
package com.example.healthsync;

public class UserProfile {
    private String email;
    private String name;
    private int age;
    private String gender;
    private int heightFt;
    private int heightIn;
    private int weightLbs;
    private String goal;
    private String activityLevel;
    private boolean isPregnant;
    private boolean isBreastfeeding;

    public UserProfile(String email, String name, int age, String gender, int heightFt, int heightIn,
                       int weightLbs, String goal, String activityLevel, boolean isPregnant, boolean isBreastfeeding) {
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

    public String getEmail() { return email; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public int getHeightFt() { return heightFt; }
    public int getHeightIn() { return heightIn; }
    public int getWeightLbs() { return weightLbs; }
    public String getGoal() { return goal; }
    public String getActivityLevel() { return activityLevel; }
    public boolean isPregnant() { return isPregnant; }
    public boolean isBreastfeeding() { return isBreastfeeding; }
}

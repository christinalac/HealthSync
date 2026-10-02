/*
 * Date: October 2. 2026
 * Version: 1.0
 * Purpose: This program is the progress page for the application.
 */

package myjavapackage;

public class Progress{

    // variables
    private int weeklyCaloriesIntake = 0;
    private int monthlyCaloriesIntake = 0;
    private float weeklyWaterIntake = 0.0;
    private float monthlyWaterIntake = 0.0;
    private int weeklyWorkoutTimes = 0;
    private int monthlyWorkoutTimes = 0;
    private float weeklyAmountOfSleep = 0.0;
    private float monthlyAmountOfSleep = 0.0;

    //constructor
    Progress(){

    }
    //getter methods
    public int getWeeklyCaloriesIntake() {
        return weeklyCaloriesIntake;
    }
    public int getMonthlyCaloriesIntake() {
        return monthlyCaloriesIntake;
    }
    public float getWeeklyWaterIntake() {
        return weeklyWaterIntake;
    }
    public float getMonthlyWaterIntake() {
        return monthlyWaterIntake;
    }
    public int getWeeklyWorkoutTimes() {
        return weeklyWorkoutTimes;
    }
    public int getMonthlyWorkoutTimes() {
        return monthlyWorkoutTimes;
    }
    public float getWeeklyAmountOfSleep() {
        return weeklyAmountOfSleep;
    }
    public float getMonthlyAmountOfSleep() {
        return monthlyAmountOfSleep;
    }
    //setter methods
    public void setWeeklyCaloriesIntake(int weeklyCaloriesIntake) {
        this.weeklyCaloriesIntake = weeklyCaloriesIntake;
    }
    public void setMonthlyCaloriesIntake(int monthlyCaloriesIntake) {
        this.monthlyCaloriesIntake = monthlyCaloriesIntake;
    }
    public void setWeeklyWaterIntake(float weeklyWaterIntake) {
        this.weeklyWaterIntake = weeklyWaterIntake;
    }
    public void setMonthlyWaterIntake(float monthlyWaterIntake) {
        this.monthlyWaterIntake = monthlyWaterIntake;
    }
    public void setWeeklyWorkoutTimes(int weeklyWorkoutTimes) {
        this.weeklyWorkoutTimes = weeklyWorkoutTimes;
    }
    public void setMonthlyWorkoutTimes(int monthlyWorkoutTimes) {
        this.monthlyWorkoutTimes = monthlyWorkoutTimes;
    }
    public void setWeeklyAmountOfSleep(float weeklyAmountOfSleep) {
        this.weeklyAmountOfSleep = weeklyAmountOfSleep;
    }
    public void setMonthlyAmountOfSleep(float monthlyAmountOfSleep) {
        this.monthlyAmountOfSleep = monthlyAmountOfSleep;
    }

    public void displayWeeklyChart(){}
    public void displayMonthlyChart(){}
} //end of progress class

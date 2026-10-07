/*
 * Date: October 2. 2026
 * Version: 1.0
 * Purpose: This program is the progress page for the application.
 */

package myjavapackage;

public class Progress{

    // variables
    private int weeklyCaloriesIntake;
    private int monthlyCaloriesIntake;
    private float weeklyWaterIntake;
    private float monthlyWaterIntake;
    private int weeklyWorkoutTimes;
    private int monthlyWorkoutTimes;
    private float weeklyAmountOfSleep;
    private float monthlyAmountOfSleep;

    //constructor

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

    // Setter Methods
    // Note: Most likely to be used to set to 0

    public int setWeeklyCaloriesIntake(int caloriesInput)
    {
        if (caloriesInput >= 0)
        {
            weeklyCaloriesIntake = caloriesInput;
        }
    }

    public int setMonthlyCaloriesIntake(int caloriesInput)
    {
        if (caloriesInput >= 0)
        {
            monthlyCaloriesIntake = caloriesInput;
        }
    }

    public float setWeeklyWaterIntake(int waterInput)
    {
        if (waterInput >= 0)
        {
            weeklyWaterIntake = waterInput;
        }
    }

    public float setMonthlyWaterInput(int waterInput)
    {
        if (waterInput >= 0)
        {
            weeklyWaterIntake = waterInput;
        }
    }

    public int setWeeklyWorkoutTimes(int workoutInput)
    {
        if (workoutInput >= 0)
        {
            weeklyWorkoutTimes = workoutInput;
        }
    }

    public int setMonthlyWorkoutTimes(int workoutInput)
    {
        if (workoutInput >= 0)
        {
            monthlyWorkoutTimes = workoutInput;
        }
    }

    public float setWeeklyAmountOfSleep(int sleepInput)
    {
        if (sleepInput >= 0)
        {
            weeklyAmountOfSleep = sleepInput;
        }
    }

    public float setMonthlyAmountOfSleep(int sleepInput)
    {
        if (sleepInput >= 0)
        {
            monthlyAmountOfSleep = sleepInput;
        }
    }


    // Utility Methods
    public void displayWeeklyChart(){}
    public void displayMonthlyChart(){}
} //end of progress class
/*
 * Date: October 2. 2026
 * Version: 1.0
 * Purpose: This program is the nutrition page for the application.
 */

package myjavapackage;

public class Nutrition{

    //variables
    private int calories;
    private float waterIntake;
    private int calorieGoal;

    //getter methods
    public int getCalorieGoal() {
        return calorieGoal;
    }
    public int getCalories() {
        return calories;
    }
    public float getWaterIntake() {
        return waterIntake;
    }

    //setter methods
    public void setCalorieGoal(int calorieGoal) {
        this.calorieGoal = calorieGoal;
    }
    public void setCalories(int calories) {
        this.calories = calories;
    }
    public void setWaterIntake(float waterIntake) {
        this.waterIntake = waterIntake;
    }

    //function for a graph on calorie goals, will subtract actual calories with wanted calories
    public int calorieSummaryGraph(int calories){}

    //function for a graph on hydration goals, will subtract hydration
    //intake with wanted water intake
    public float hydrationSummaryGraph(float waterIntake){}

    //this function allows users to add meals to their daily plan and add meals they did eat aleady
    public void mealLogger(){}

} //end of nutrition class
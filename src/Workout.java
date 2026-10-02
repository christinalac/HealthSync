/*
 * Date: October 2. 2026
 * Version: 1.0
 * Purpose: This program is the workout page for the application.
 */

package myjavapackage;
import java.util.ArrayList;

public class Workout{

    //variables
    private String workoutName = null;
    private String muscleGroup = null;
    private String workoutDate = null;
    private ArrayList<String> workoutPlan;

    //constructor methods
    Workout(ArrayList<String> workoutPlan){
        this.workoutPlan = workoutPlan;
    }
    Workout(){

    }
        
    //getter methods
    public ArrayList<String> getWorkoutPlan() {
        return workoutPlan;
    }
    public String getWorkoutName() {
        return workoutName;
    }
    public String getMuscleGroup() {
        return muscleGroup;
    }

    //setter methods
    public void setDailyPlan(Array<String> workoutPlan){

    }
    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }
    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    //function allowing users to go through workout list via filters
    public ArrayList<String> filterWorkouts(String muscleGroup){}
    //function allowing users to add workouts to their daily plan
    public void addWorkoutToPlan(String workoutName, String date){}
    //function allowing users to remove workouts from their daily plan
    public void removeWorkoutFromPlan(String workoutName){}
} //end of workout class

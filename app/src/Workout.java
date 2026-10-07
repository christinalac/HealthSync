/*
 * Date: October 2. 2026
 * Version: 1.0
 * Purpose: This program is the workout page for the application.
 */

package myjavapackage;

public class Workout{

    //variables
    private String workoutName;
    private String muscleGroup;
    private String workoutDate;
    private ArrayList<String> workoutPlan;

    public class Workout(String workoutName, String muscleGroup, String workoutDate, boolean isCompleted){
        this.workoutName=workoutName;
        this.muscleGroup=muscleGroup;
        this.workoutDate-workouDate;
        this.isCompleted = isCompleted;
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
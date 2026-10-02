/*
 * Date: October 2. 2026
 * Version: 1.0
 * Purpose: This program is the sleep page for the application.
 */

package myjavapackage;

public class Sleep{

    //variables
    private int bedTime = 0;
    private int wakeTime = 0;
    private String alarmSound = null;
    private boolean alarmEnabled = false;

    // constructor method
    Sleep(){

    }
    
    // getter methods
    public int getBedTime() {
        return bedTime;
    }
    public int getWakeTime() {
        return wakeTime;
    }
    public String getAlarmSound() {
        return alarmSound;
    }
    public boolean isAlarmEnabled() {
        return alarmEnabled;
    }
    public String getSleepRecommendation(){

    }

    // setter methods
    public void setBedTime(int bedTime) {
        this.bedTime = bedTime;
    }
    public void setWakeTime(int wakeTime) {
        this.wakeTime = wakeTime;
    }
    public void setAlarmSound(String alarmSound) {
        this.alarmSound = alarmSound;
    }
    public void setAlarmEnabled(boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
    }

    //function allows users to enter sleep data
    public void addSleepEntry(String day, int bedTime, int wakeTime){

    }

    //function produces the alarm sound
    public void playAlarm(){

    }

} //end of sleep class

/*
 * Author: Zoey Lira
 * Date: November 16, 2026
 * Class: COSC 3324
 * File: Exercise.java
 * Description: Data model representing a single exercise in the HealthSync workout
 *              plan, storing its name, category, sets/reps, completion status,
 *              targeted muscle tags, and form tips.
 */
package com.example.healthsync;

// One exercise row in the workout plan (Java port of the Kotlin data class).
public class Exercise {
    private final String name;
    private final String category;
    private final String setsReps;
    private boolean checked;
    // Add these fields inside your Exercise class:
    private String[] muscleTags;
    private String tips;

    // Add these getters and setters inside your Exercise class:
    public String[] getMuscleTags() {
        return muscleTags;
    }

    public void setMuscleTags(String[] muscleTags) {
        this.muscleTags = muscleTags;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public Exercise(String name, String category, String setsReps) {
        this.name = name;
        this.category = category;
        this.setsReps = setsReps;
        this.checked = false;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getSetsReps() { return setsReps; }
    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}

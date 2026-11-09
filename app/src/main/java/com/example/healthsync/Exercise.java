package com.example.healthsync;

// One exercise row in the workout plan (Java port of the Kotlin data class).
public class Exercise {
    private final String name;
    private final String category;
    private final String setsReps;
    private boolean checked;

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

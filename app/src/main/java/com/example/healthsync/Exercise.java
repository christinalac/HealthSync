package com.example.healthsync;

// One exercise row in the workout plan (Java port of the Kotlin data class).
public class Exercise {
    private final String name;
    private final String category;
    private final String setsReps;
    private boolean checked = false;
    // Add these fields inside your Exercise class:
    private String[] muscleTags = new String[0];
    private String tips = "";

    // Add these getters and setters inside your Exercise class:
    public String[] getMuscleTags() {
        return muscleTags;
    }

    public void setMuscleTags(String[] muscleTags) {
        if (muscleTags != null) {
            this.muscleTags = muscleTags;
        } else {
            this.muscleTags = new String[0];
        }
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        if (tips != null) {
            this.tips = tips;
        } else {
            this.tips = "";
        }
    }

    public Exercise(String name, String category, String setsReps) {
        if (name != null) {
            this.name = name;
        } else {
            this.name = "";
        }
        if (category != null) {
            this.category = category;
        } else {
            this.category = "";
        }
        if (setsReps != null) {
            this.setsReps = setsReps;
        } else {
            this.setsReps = "";
        }
        this.checked = false;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getSetsReps() { return setsReps; }
    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}

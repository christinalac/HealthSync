//******************************************
// Program Name: ExerciseCatalog.java
// Date Created: 11/9/2026
// Version: 2.0
// Purpose: List of exercises the workout search looks
// through (populated from Free Exercise DB API with local fallback),
// plus the filter logic (search text + muscle group).
//******************************************
package com.example.healthsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class ExerciseCatalog {

    // Filter values shown as chips in the search sheet
    public static final String FILTER_ALL = "All";
    public static final String FILTER_ARMS = "Arms";
    public static final String FILTER_LEGS = "Legs";
    public static final String FILTER_BACK = "Back";
    public static final String FILTER_CHEST = "Chest";
    public static final String FILTER_SHOULDERS = "Shoulders";
    public static final String FILTER_CORE = "Core";

    // One searchable exercise
    public static final class Entry {
        public final String name;
        // Arms, Legs, Chest, Back, Shoulders, Core
        public final String group;
        // e.g. "3x12 · 25 lbs"
        public final String setsReps;
        public final String[] muscleTags;
        public final String tips;

        public Entry(String name, String group, String setsReps, String[] muscleTags, String tips) {
            if (name != null) {
                this.name = name;
            } else {
                this.name = "";
            }
            if (group != null) {
                this.group = group;
            } else {
                this.group = "";
            }
            if (setsReps != null) {
                this.setsReps = setsReps;
            } else {
                this.setsReps = "";
            }
            if (muscleTags != null) {
                this.muscleTags = muscleTags;
            } else {
                this.muscleTags = new String[0];
            }
            if (tips != null) {
                this.tips = tips;
            } else {
                this.tips = "";
            }
        }

        Entry(String name, String group, String setsReps) {
            this(name, group, setsReps, new String[]{group, "General", "Fitness"}, group);
        }
    }

    // Starts with local fallback exercises, replaced once API loads
    private static List<Entry> exercises = new ArrayList<>(Arrays.asList(
            new Entry("Bicep Curls", "Arms", "3x12 · 25 lbs"),
            new Entry("Tricep Pushdowns", "Arms", "3x15"),
            new Entry("Hammer Curls", "Arms", "3x10"),
            new Entry("Skull Crushers", "Arms", "3x12"),
            new Entry("Preacher Curls", "Arms", "3x10"),
            new Entry("Tricep Dips", "Arms", "3x15"),
            new Entry("Squats", "Legs", "4x12 · 185 lbs"),
            new Entry("Leg Extensions", "Legs", "3x15"),
            new Entry("Lunges", "Legs", "3x10 steps"),
            new Entry("Leg Press", "Legs", "4x10"),
            new Entry("Romanian Deadlifts", "Legs", "4x8"),
            new Entry("Calf Raises", "Legs", "4x20"),
            new Entry("Bench Press", "Chest", "4x10 · 135 lbs"),
            new Entry("Incline Dumbbell Press", "Chest", "3x10"),
            new Entry("Chest Flyes", "Chest", "3x12"),
            new Entry("Push-Ups", "Chest", "3x20"),
            new Entry("Pull-Ups", "Back", "3x8"),
            new Entry("Lat Pulldowns", "Back", "3x12"),
            new Entry("Bent Over Rows", "Back", "4x10"),
            new Entry("Deadlifts", "Back", "1x5"),
            new Entry("Overhead Press", "Shoulders", "4x8"),
            new Entry("Lateral Raises", "Shoulders", "3x15"),
            new Entry("Plank Hold", "Core", "3x60 sec"),
            new Entry("Hanging Leg Raises", "Core", "3x12")
    ));

    private ExerciseCatalog() { }

    public static void setExercises(List<Entry> apiExercises) {
        if (apiExercises != null && !apiExercises.isEmpty()) {
            exercises = new ArrayList<>(apiExercises);
        }
    }

    // Returns exercises whose name or group contains the query, limited to the chosen filter.
    public static List<Entry> search(String query, String filter) {
        String searchText = "";
        List<Entry> results = new ArrayList<>();
        boolean groupOk = false;
        boolean textOk = false;
        if (query == null) {
            searchText = "";
        } else {
            searchText = query.trim().toLowerCase(Locale.ROOT);
        }
        for (Entry entry : exercises) {
            groupOk = FILTER_ALL.equals(filter) || entry.group.equalsIgnoreCase(filter);
            textOk = searchText.isEmpty()
                    || entry.name.toLowerCase(Locale.ROOT).contains(searchText)
                    || entry.group.toLowerCase(Locale.ROOT).contains(searchText);
            if (groupOk && textOk) {
                results.add(entry);
            }
        }
        return results;
    }
}

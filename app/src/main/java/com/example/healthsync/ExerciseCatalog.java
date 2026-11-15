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
        public final String group;      // Arms, Legs, Chest, Back, Shoulders, Core
        public final String setsReps;   // e.g. "3x12 · 25 lbs"
        public final String[] muscleTags;
        public final String tips;

        public Entry(String name, String group, String setsReps, String[] muscleTags, String tips) {
            this.name = name;
            this.group = group;
            this.setsReps = setsReps;
            this.muscleTags = muscleTags;
            this.tips = tips;
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
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Entry> results = new ArrayList<>();
        for (Entry e : exercises) {
            boolean groupOk = FILTER_ALL.equals(filter) || e.group.equalsIgnoreCase(filter);
            boolean textOk = q.isEmpty()
                    || e.name.toLowerCase(Locale.ROOT).contains(q)
                    || e.group.toLowerCase(Locale.ROOT).contains(q);
            if (groupOk && textOk) {
                results.add(e);
            }
        }
        return results;
    }
}

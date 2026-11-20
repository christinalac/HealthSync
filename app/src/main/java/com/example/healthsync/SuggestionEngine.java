//******************************************
// Program Name: SuggestionEngine.java
// Developer: Anthony Ramirez and Christina LaCombe
// Date Created: 11/15/2026
// Version: 1.1
// Purpose: Picks food and workout suggestions for the user from a
// built-in list (no outside API), based on their goal, daily calorie
// target, activity level, and today's workout from the weekly split.
// Works the same way the sleep suggestion picks hours based on age.
//******************************************
package com.example.healthsync;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class SuggestionEngine {

    private SuggestionEngine() { }

    // ─────────────────────────────────────────────────────────
    // WHAT A SUGGESTION LOOKS LIKE
    // ─────────────────────────────────────────────────────────

    /** One row in the suggestions list. Exactly one of food / workout is set. */
    public static class Suggestion {
        // e.g. "Peanut Butter Toast"
        public final String title;
        // e.g. "2 slices · 390 kcal · 14 g protein"
        public final String detail;
        // why it was picked, e.g. "Calorie-dense ..."
        public final String reason;
        // set for food suggestions
        public final FoodItem food;
        // set for workout suggestions
        public final ExerciseCatalog.Entry workout;

        Suggestion(String title, String detail, String reason,
                   FoodItem food, ExerciseCatalog.Entry workout) {
            if (title != null) {
                this.title = title;
            } else {
                this.title = "";
            }
            if (detail != null) {
                this.detail = detail;
            } else {
                this.detail = "";
            }
            if (reason != null) {
                this.reason = reason;
            } else {
                this.reason = "";
            }
            this.food = food;
            this.workout = workout;
        }
    }
    // end of class Suggestion

    // ─────────────────────────────────────────────────────────
    // BUILT-IN FOOD LIST (per serving)
    // ─────────────────────────────────────────────────────────

    // name, serving, kcal, protein g, carbs g, fat g
    private static final Object[][] FOODS = {
            {"Peanut Butter Toast", "2 slices", 390, 14, 36, 22},
            {"Trail Mix", "1/2 cup", 350, 10, 33, 22},
            {"Protein Shake with Milk", "1 shake", 300, 35, 20, 8},
            {"Salmon with Rice", "1 plate", 620, 40, 55, 24},
            {"Chicken Burrito Bowl", "1 bowl", 650, 42, 70, 20},
            {"Whole Milk", "1 cup", 150, 8, 12, 8},
            {"Avocado Toast with Egg", "1 slice", 330, 13, 28, 19},
            {"Beef and Pasta", "1 plate", 700, 38, 75, 24},
            {"Bagel with Cream Cheese", "1 bagel", 380, 12, 58, 11},
            {"Granola with Yogurt", "1 bowl", 420, 16, 60, 13},
            {"Grilled Chicken Breast", "4 oz", 185, 35, 0, 4},
            {"Greek Yogurt (plain, nonfat)", "1 cup", 130, 23, 9, 0},
            {"Hard-Boiled Eggs", "2 eggs", 155, 13, 1, 11},
            {"Garden Salad with Tuna", "1 bowl", 250, 28, 10, 10},
            {"Vegetable Stir-Fry with Tofu", "1 plate", 300, 18, 25, 14},
            {"Cottage Cheese with Berries", "1 cup", 200, 25, 18, 3},
            {"Turkey Lettuce Wraps", "3 wraps", 220, 26, 8, 9},
            {"Apple with Almonds", "1 apple + 10 almonds", 165, 3, 27, 7},
            {"Oatmeal with Banana", "1 bowl", 310, 9, 58, 5},
            {"Banana", "1 large", 120, 1, 31, 0},
            {"Whole Wheat Pasta", "1.5 cups", 300, 12, 60, 2},
            {"Sweet Potato", "1 medium", 115, 2, 27, 0},
            {"Brown Rice and Beans", "1 bowl", 350, 13, 68, 3},
            {"Whole Wheat Bagel with Honey", "1 bagel", 330, 11, 70, 2},
            {"Fruit Smoothie", "16 oz", 250, 6, 55, 2},
            {"Turkey Sandwich", "1 sandwich", 350, 25, 38, 10},
            {"Lentil Soup", "1.5 cups", 270, 18, 45, 2},
            {"Hummus with Veggies", "1/4 cup + veggies", 180, 6, 20, 9},
            {"Grilled Fish Tacos", "2 tacos", 400, 28, 40, 14},
            {"Quinoa Salad", "1 bowl", 330, 12, 45, 12},
    };

    /**
     * Food suggestions for the user, best first.
     *
     * The calorie target decides what "a good serving size" is: about a third of the
     * day per meal. Someone gaining muscle on 2,600 kcal gets calorie-dense foods,
     * someone losing weight gets filling foods that are high in protein per calorie.
     */
    public static List<Suggestion> foodSuggestions(UserProfile profile, int dailyKcal, int max) {
        String goal = "";
        int perMeal = Math.max(1, dailyKcal / 3);
        List<Object[]> foods = new ArrayList<>();
        Comparator<Object[]> order;
        List<Suggestion> suggestions = new ArrayList<>();
        String name = "";
        String serving = "";
        int percent = 0;
        String detail = "";
        String reason = "";
        FoodItem item = null;

        if (profile.getGoal() != null) {
            goal = profile.getGoal();
        }
        final String goalFinal = goal;
        Collections.addAll(foods, FOODS);

        // ── Rank the foods for this goal ──
        switch (goalFinal) {
            case PersonalizedCalculations.GOAL_GAIN_MUSCLE:
                // Most calories and protein per serving first
                order = (first, second) -> Double.compare(kcal(second) + 4 * protein(second), kcal(first) + 4 * protein(first));
                break;
            case PersonalizedCalculations.GOAL_LOSE_WEIGHT:
                // Most protein per calorie first (keeps you full for fewer calories)
                order = (first, second) -> Double.compare(protein(second) / kcal(second), protein(first) / kcal(first));
                break;
            case PersonalizedCalculations.GOAL_IMPROVE_ENDURANCE:
                // Most carbs first (carbs fuel long workouts)
                order = (first, second) -> Double.compare(carbs(second), carbs(first));
                break;
            default:
                // Maintain / overall health: closest to half a meal (a balanced snack or side)
                order = Comparator.comparingDouble(food -> Math.abs(kcal(food) - perMeal / 2.0));
                break;
        }
        // end of goal switch
        Collections.sort(foods, order);

        // ── Build the rows ──
        for (Object[] foodRow : foods) {
            if (suggestions.size() >= max) break;

            // Losing weight: skip anything that would eat more than half a meal's calories
            if (PersonalizedCalculations.GOAL_LOSE_WEIGHT.equals(goalFinal) && kcal(foodRow) > perMeal * 0.6) continue;

            name = (String) foodRow[0];
            serving = (String) foodRow[1];
            percent = (int) Math.round(kcal(foodRow) * 100.0 / Math.max(1, dailyKcal));

            detail = String.format(Locale.US, "%s · %d kcal · %d g protein · %d g carbs",
                    serving, (int) kcal(foodRow), (int) protein(foodRow), (int) carbs(foodRow));
            reason = foodReason(goalFinal, percent, dailyKcal);

            item = new FoodItem("suggest_" + name.toLowerCase(Locale.US).replace(' ', '_'),
                    name, "HealthSync suggestion", null, serving,
                    kcal(foodRow), carbs(foodRow), protein(foodRow), fat(foodRow));
            suggestions.add(new Suggestion(name, detail, reason, item, null));
        }
        // end of for loop over the foods
        return suggestions;
    }
    // end of foodSuggestions

    private static String foodReason(String goal, int percent, int dailyKcal) {
        String share = String.format(Locale.US, "%d%% of your %,d kcal goal", percent, dailyKcal);
        switch (goal) {
            case PersonalizedCalculations.GOAL_GAIN_MUSCLE:
                return "Calorie-dense with protein to help you reach " + String.format(Locale.US, "%,d", dailyKcal) + " kcal (" + share + ")";
            case PersonalizedCalculations.GOAL_LOSE_WEIGHT:
                return "High protein for few calories, keeps you full (" + share + ")";
            case PersonalizedCalculations.GOAL_IMPROVE_ENDURANCE:
                return "Carb-rich fuel for longer workouts (" + share + ")";
            default:
                return "Balanced choice for your goal (" + share + ")";
        }
        // end of goal switch
    }
    // end of foodReason

    // ─────────────────────────────────────────────────────────
    // BUILT-IN WORKOUT LIST
    // ─────────────────────────────────────────────────────────

    // name, group, sets/reps (normal), muscle tags (3), tip, which goals it suits
    // Goal letters: E = endurance, M = gain muscle, L = lose weight, H = overall health / maintain
    private static final Object[][] WORKOUTS = {
            // ── Cardio ──
            {"Running", "Cardio", "30 min easy pace", new String[]{"Legs", "Heart", "Lungs"}, "Keep a pace where you can still talk in short sentences.", "ELH"},
            {"Interval Sprints", "Cardio", "8 x 30 s fast / 90 s walk", new String[]{"Legs", "Heart", "Lungs"}, "Warm up for 5 minutes first; sprint hard, recover fully.", "EL"},
            {"Cycling", "Cardio", "40 min steady", new String[]{"Quads", "Calves", "Heart"}, "Keep your knees in line with your feet and a steady cadence.", "ELH"},
            {"Jump Rope", "Cardio", "10 x 1 min / 30 s rest", new String[]{"Calves", "Shoulders", "Heart"}, "Stay on the balls of your feet with small, quick jumps.", "EL"},
            {"Rowing Machine", "Cardio", "20 min steady", new String[]{"Back", "Legs", "Heart"}, "Push with the legs first, then pull with the arms.", "ELH"},
            {"Brisk Walk", "Cardio", "30 min", new String[]{"Legs", "Heart", "Core"}, "Swing your arms and walk fast enough to breathe harder.", "LH"},
            {"Swimming", "Cardio", "20 min easy laps", new String[]{"Shoulders", "Back", "Heart"}, "Breathe every 2-3 strokes and keep a long, smooth stroke.", "EH"},
            // ── Strength ──
            {"Back Squats", "Legs", "4x8", new String[]{"Quads", "Glutes", "Hamstrings"}, "Chest up, knees track over toes, go to at least parallel.", "M"},
            {"Deadlifts", "Back", "4x6", new String[]{"Hamstrings", "Glutes", "Lower Back"}, "Keep the bar close and your back flat; push the floor away.", "M"},
            {"Bench Press", "Chest", "4x8", new String[]{"Pectorals", "Triceps", "Front Deltoid"}, "Feet planted, shoulder blades pinched, lower to mid-chest.", "M"},
            {"Bent-Over Rows", "Back", "4x10", new String[]{"Lats", "Rhomboids", "Biceps"}, "Hinge at the hips and pull the bar to your belly button.", "M"},
            {"Overhead Press", "Shoulders", "4x8", new String[]{"Deltoids", "Triceps", "Core"}, "Squeeze your glutes and press straight up past your face.", "M"},
            {"Pull-Ups", "Back", "4x6", new String[]{"Lats", "Biceps", "Rear Deltoid"}, "Start from a dead hang and pull your chest to the bar.", "M"},
            {"Bicep Curls", "Arms", "3x12", new String[]{"Biceps", "Forearms", "Grip"}, "Keep elbows pinned to your sides; no swinging.", "M"},
            {"Walking Lunges", "Legs", "3x12 each leg", new String[]{"Quads", "Glutes", "Balance"}, "Long steps, back knee nearly touches the floor.", "MLH"},
            {"Push-Ups", "Chest", "3x15", new String[]{"Pectorals", "Triceps", "Core"}, "Body in a straight line; lower until your chest nearly touches.", "MLH"},
            // ── Full body / core ──
            {"Full-Body Circuit", "Core", "3 rounds: squats, push-ups, rows", new String[]{"Full Body", "Heart", "Core"}, "Move between exercises with little rest to keep your heart rate up.", "LH"},
            {"Burpees", "Core", "4x10", new String[]{"Full Body", "Heart", "Core"}, "Land softly and keep a steady rhythm.", "EL"},
            {"Plank", "Core", "3 x 45 s", new String[]{"Abs", "Core", "Shoulders"}, "Squeeze glutes and abs; don't let your hips sag.", "MLHE"},
            {"Yoga Flow", "Core", "20 min", new String[]{"Flexibility", "Core", "Balance"}, "Breathe slowly and move with your breath.", "H"},
    };

    /**
     * Workout suggestions for the user, best first.
     * Exercises that match today's day in the weekly split come first (e.g. Legs day
     * suggests leg exercises). Sedentary users get an easier version of the sets.
     */
    public static List<Suggestion> workoutSuggestions(Context context, UserProfile profile, int max) {
        String goalLetter = goalLetter(profile.getGoal());
        // e.g. "Legs", "Rest", or null
        String today = todaysSplit(context);
        boolean easier = PersonalizedCalculations.ACTIVITY_SEDENTARY.equals(profile.getActivityLevel())
                || PersonalizedCalculations.ACTIVITY_LIGHTLY_ACTIVE.equals(profile.getActivityLevel());
        List<Object[]> matches = new ArrayList<>();
        List<Suggestion> suggestions = new ArrayList<>();
        String name = "";
        String rowGroup = "";
        String sets = "";
        String reason = "";
        ExerciseCatalog.Entry entry = null;

        for (Object[] workoutRow : WORKOUTS) {
            if (((String) workoutRow[5]).contains(goalLetter)) matches.add(workoutRow);
        }
        // end of for loop that filters by goal

        // Today's split group first, then the rest in list order
        if (today != null) {
            final String group = today;
            Collections.sort(matches, (first, second) -> Boolean.compare(
                    group.equalsIgnoreCase((String) second[1]), group.equalsIgnoreCase((String) first[1])));
        }
        // end of today's split check

        for (Object[] workoutRow : matches) {
            if (suggestions.size() >= max) break;

            name = (String) workoutRow[0];
            rowGroup = (String) workoutRow[1];
            if (easier) {
                sets = "Easier: " + easierSets((String) workoutRow[2]);
            } else {
                sets = (String) workoutRow[2];
            }

            reason = workoutReason(profile.getGoal(), rowGroup, today, easier);
            entry = new ExerciseCatalog.Entry(
                    name, rowGroup, sets, (String[]) workoutRow[3], (String) workoutRow[4]);
            suggestions.add(new Suggestion(name, rowGroup + " · " + sets, reason, null, entry));
        }
        // end of for loop that builds the rows
        return suggestions;
    }
    // end of workoutSuggestions

    private static String workoutReason(String goal, String group, String today, boolean easier) {
        String why;
        if (PersonalizedCalculations.GOAL_IMPROVE_ENDURANCE.equals(goal)) {
            why = "Builds endurance";
        } else if (PersonalizedCalculations.GOAL_GAIN_MUSCLE.equals(goal)) {
            why = "Builds muscle with heavier sets";
        } else if (PersonalizedCalculations.GOAL_LOSE_WEIGHT.equals(goal)) {
            why = "Burns calories";
        } else {
            why = "Good for overall health";
        }
        // end of goal if/else

        if (today != null && today.equalsIgnoreCase(group)) why += " · matches today's " + today + " day";
        if (easier) why += " · scaled for your activity level";
        return why;
    }
    // end of workoutReason

    // A gentler version of the sets for less active users: fewer sets, shorter time
    private static String easierSets(String sets) {
        // 20 min is shortened first, so the 40 -> 20 change below isn't shortened a second time
        return sets.replace("4x", "3x").replace("20 min", "10 min").replace("40 min", "20 min")
                   .replace("30 min", "15 min").replace("10 x 1 min", "5 x 1 min")
                   .replace("8 x 30 s", "5 x 30 s").replace("3 rounds", "2 rounds");
    }
    // end of easierSets

    private static String goalLetter(String goal) {
        if (PersonalizedCalculations.GOAL_IMPROVE_ENDURANCE.equals(goal)) return "E";
        if (PersonalizedCalculations.GOAL_GAIN_MUSCLE.equals(goal)) return "M";
        if (PersonalizedCalculations.GOAL_LOSE_WEIGHT.equals(goal)) return "L";
        // maintain weight and improve overall health
        return "H";
    }
    // end of goalLetter

    // Today's entry from the Workout tab's weekly split (saved by WorkoutActivity),
    // or null if it isn't set up yet or today is a Rest day
    private static String todaysSplit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("healthsync_workouts", Context.MODE_PRIVATE);
        String json = prefs.getString("weekly_split", null);
        JSONArray days = null;
        int index = 0;
        String today = null;
        if (json == null) return null;
        try {
            days = new JSONArray(json);
            // Sunday = 0
            index = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
            today = days.optString(index, null);
            if (today == null || "Rest".equals(today)) return null;
            return today;
        } catch (Exception exception) {
            return null;
        }
        // end of try/catch
    }
    // end of todaysSplit

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    /** The user's daily calorie target, same formula as the rest of the app. */
    public static int dailyCalories(UserProfile profile) {
        return PersonalizedCalculations.calculateDailyCalories(
                profile.getGender(), profile.getWeightLbs(), profile.getHeightFt(), profile.getHeightIn(),
                profile.getAge(), profile.getActivityLevel(), profile.getGoal(), profile.isRestDay());
    }
    // end of dailyCalories

    private static double kcal(Object[] food)    { return ((Integer) food[2]).doubleValue(); }
    private static double protein(Object[] food) { return ((Integer) food[3]).doubleValue(); }
    private static double carbs(Object[] food)   { return ((Integer) food[4]).doubleValue(); }
    private static double fat(Object[] food)     { return ((Integer) food[5]).doubleValue(); }

}
// end of class SuggestionEngine

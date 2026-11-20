package com.example.healthsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutActivity extends AppCompatActivity
        implements ExerciseSearchBottomSheet.Listener {

    private final List<Exercise> exerciseList = new ArrayList<>();

    private ExerciseAdapter adapter = null;

    private TextView tvInfoCardTitle = null;
    private TextView tvMuscleTag1 = null;
    private TextView tvMuscleTag2 = null;
    private TextView tvMuscleTag3 = null;
    private TextView tvInfoCardTips = null;
    // everything under the Exercise Info title
    private android.view.View infoContent = null;
    // ▼ / ▲ next to the Exercise Info title
    private TextView infoArrow = null;

    // ── Weekly split (what the user trains on each day) ──────
    // Index 0 = Sunday ... 6 = Saturday, same order as the cells in the card
    private static final String[] SPLIT_DAY_NAMES = {
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };

    // What the user can pick for a day in the edit pop-up
    private static final String[] SPLIT_OPTIONS = {
            "Rest", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio", "Full Body"
    };

    // Starting split for a brand new user (Sunday and Thursday are rest days)
    private static final String[] DEFAULT_SPLIT = {
            "Rest", "Legs", "Chest", "Back", "Rest", "Shoulders", "Cardio"
    };

    // what is shown and saved
    private final String[] weeklySplit = new String[7];
    // one cell per day in the card
    private final TextView[] splitDayViews = new TextView[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_activity);
        // Set "Week of [Monday's Date]" and "[TODAY]'S PLAN" dynamically
        TextView tvWeekOf = findViewById(R.id.textWorkoutPlannerPlaceholder);
        TextView tvDayPlan = findViewById(R.id.textWeeklyPlanHeader);

        java.util.Calendar calendar = java.util.Calendar.getInstance();

        // Day of week in uppercase, e.g., "WEDNESDAY'S PLAN"
        String dayName = new java.text.SimpleDateFormat("EEEE", Locale.US)
                .format(calendar.getTime())
                .toUpperCase(Locale.US);
        tvDayPlan.setText(dayName + "'S PLAN");

        // Find the Monday of the current week, e.g., "Week of Nov 9"
        calendar.setFirstDayOfWeek(java.util.Calendar.MONDAY);
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        String weekStart = new java.text.SimpleDateFormat("MMM d", Locale.US)
                .format(calendar.getTime());
        tvWeekOf.setText("Week of " + weekStart);

        // ── Weekly split card ──────────────────────────────────
        // load what was saved, fill in the cells, and let the Edit button change it
        setupWeeklySplit();

        // Views in the bottom Exercise Info card
        tvInfoCardTitle = findViewById(R.id.textExerciseInfoTitle);
        tvMuscleTag1 = findViewById(R.id.tagPectorals);
        tvMuscleTag2 = findViewById(R.id.tagTriceps);
        tvMuscleTag3 = findViewById(R.id.tagDeltoid);
        tvInfoCardTips = findViewById(R.id.textTipsDescription);

        // Load saved workouts and set initial Exercise Info card state
        loadExercises();
        if (exerciseList.isEmpty()) {
            resetExerciseInfoCard();
        } else {
            showExerciseInfo(exerciseList.get(0));
        }

        // ── Exercise Info drop down ─────────────────────────────
        // Tapping the title row opens / closes the card. Open or closed is remembered
        infoContent = findViewById(R.id.layoutExerciseInfoContent);
        infoArrow = findViewById(R.id.textExerciseInfoArrow);
        android.view.View infoHeaderRow = findViewById(R.id.rowExerciseInfoHeader);
        SharedPreferences workoutPrefs = getSharedPreferences("healthsync_workouts", MODE_PRIVATE);

        showInfoOpen(workoutPrefs.getBoolean("info_open", false));
        infoHeaderRow.setOnClickListener(view -> {
            boolean open = infoContent.getVisibility() != android.view.View.VISIBLE;
            showInfoOpen(open);
            workoutPrefs.edit().putBoolean("info_open", open).apply();
        });

        // Exercise list
        RecyclerView rvExercises = findViewById(R.id.rvExercises);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        // Tapping an exercise (its name or the ⓘ) opens the info card and scrolls down to it
        adapter = new ExerciseAdapter(exerciseList, this::openExerciseInfo);
        rvExercises.setAdapter(adapter);

        // Top-left header button: workout suggestions based on the user's goal and today's split.
        // Picking one adds it through onExercisePicked, the same as the "+" search
        findViewById(R.id.buttonSuggestions).setOnClickListener(view ->
                SuggestionsBottomSheet.newInstance(SuggestionsBottomSheet.TYPE_WORKOUT)
                        .show(getSupportFragmentManager(), "workout_suggestions"));

        // "+" opens the exercise search pop-up (ExerciseSearchBottomSheet fetches the API)
        FloatingActionButton fabAddWorkout = findViewById(R.id.fabAddWorkout);
        fabAddWorkout.setOnClickListener(view ->
                new ExerciseSearchBottomSheet().show(getSupportFragmentManager(), "exercise_search"));

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_workout);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ProgressActivity.class));
                return true;
            } else if (id == R.id.nav_nutrition) {
                startActivity(new Intent(this, NutritionActivity.class));
                return true;
            } else if (id == R.id.nav_workout) {
                return true;
            } else if (id == R.id.nav_sleep) {
                startActivity(new Intent(this, SleepActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return false;
        });
    }

    // ─────────────────────────────────────────────────────────
    // Weekly split card
    // ─────────────────────────────────────────────────────────

    // Finds the 7 day cells, loads the saved split, and hooks up the Edit button
    private void setupWeeklySplit() {
        splitDayViews[0] = findViewById(R.id.textSplitSunday);
        splitDayViews[1] = findViewById(R.id.textSplitMonday);
        splitDayViews[2] = findViewById(R.id.textSplitTuesday);
        splitDayViews[3] = findViewById(R.id.textSplitWednesday);
        splitDayViews[4] = findViewById(R.id.textSplitThursday);
        splitDayViews[5] = findViewById(R.id.textSplitFriday);
        splitDayViews[6] = findViewById(R.id.textSplitSaturday);

        loadWeeklySplit();
        showWeeklySplit();

        findViewById(R.id.buttonEditSplit).setOnClickListener(view -> showEditSplitDialog());

        // ── Drop down: tapping the title opens / closes the list of days ──
        // The title and Edit button always stay visible. Open or closed is remembered
        // so the card looks the same when the user comes back to this tab
        TextView splitHeader = findViewById(R.id.textWeeklySplitHeader);
        android.view.View splitGrid = findViewById(R.id.gridWeeklySplit);
        SharedPreferences prefs = getSharedPreferences("healthsync_workouts", MODE_PRIVATE);

        showSplitOpen(splitHeader, splitGrid, prefs.getBoolean("split_open", false));
        splitHeader.setOnClickListener(view -> {
            boolean open = splitGrid.getVisibility() != android.view.View.VISIBLE;
            showSplitOpen(splitHeader, splitGrid, open);
            prefs.edit().putBoolean("split_open", open).apply();
        });
        // end of setupWeeklySplit
    }

    // Shows or hides the days, and flips the arrow on the title (▼ = closed, ▲ = open)
    private void showSplitOpen(TextView header, android.view.View grid, boolean open) {
        int gridVisibility = android.view.View.GONE;
        String headerText = "📅 WEEKLY SPLIT  ▼";
        String contentDescription = "Weekly split, collapsed. Tap to expand";

        if (open) {
            gridVisibility = android.view.View.VISIBLE;
            headerText = "📅 WEEKLY SPLIT  ▲";
            contentDescription = "Weekly split, expanded. Tap to collapse";
        }

        grid.setVisibility(gridVisibility);
        header.setText(headerText);
        // TalkBack reads this so screen reader users know what tapping will do
        header.setContentDescription(contentDescription);
        // end of showSplitOpen
    }

    // Writes "Sunday: Rest" etc. into each cell. Today's cell is bold and green so it stands out
    private void showWeeklySplit() {
        // Calendar says Sunday = 1, so subtract 1 to line up with our 0-6 index
        int today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1;

        for (int i = 0; i < 7; i++) {
            splitDayViews[i].setText(SPLIT_DAY_NAMES[i] + ": " + weeklySplit[i]);

            if (i == today) {
                splitDayViews[i].setTypeface(null, Typeface.BOLD);
                splitDayViews[i].setTextColor(ContextCompat.getColor(this, R.color.text_dark_green));
            } else {
                splitDayViews[i].setTypeface(null, Typeface.NORMAL);
                splitDayViews[i].setTextColor(ContextCompat.getColor(this, R.color.workout_text_main));
                // end of today highlight if/else
            }
            // end of for loop over the days
        }
        // end of showWeeklySplit
    }

    // Pop-up with one row per day: the day name and a spinner to pick its workout
    private void showEditSplitDialog() {
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout rows = new LinearLayout(this);
        // keeps track of the spinner for each day so Save can read them back
        final Spinner[] spinners = new Spinner[7];
        // ScrollView so all 7 rows are still reachable in landscape or on small screens
        ScrollView scroll = new ScrollView(this);

        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(padding, padding / 2, padding, 0);

        for (int i = 0; i < 7; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView dayLabel = new TextView(this);
            dayLabel.setText(SPLIT_DAY_NAMES[i]);
            dayLabel.setTextSize(15);
            dayLabel.setTextColor(ContextCompat.getColor(this, R.color.navy_text));
            row.addView(dayLabel, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            Spinner spinner = new Spinner(this);
            ArrayAdapter<String> optionAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, SPLIT_OPTIONS);
            optionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(optionAdapter);
            spinner.setSelection(indexOfOption(weeklySplit[i]));
            row.addView(spinner, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            spinners[i] = spinner;
            rows.addView(row);
            // end of for loop that builds the rows
        }

        scroll.addView(rows);

        // same green dialog theme as the sleep page so the Save button is easy to see
        new AlertDialog.Builder(this, R.style.ThemeOverlay_HealthSync_SleepDialog)
                .setTitle("Edit weekly split")
                .setView(scroll)
                .setPositiveButton("Save", (dialog, which) -> {
                    for (int i = 0; i < 7; i++) {
                        weeklySplit[i] = SPLIT_OPTIONS[spinners[i].getSelectedItemPosition()];
                        // end of for loop that reads the spinners
                    }
                    saveWeeklySplit();
                    showWeeklySplit();
                    Toast.makeText(this, "Weekly split saved", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
        // end of showEditSplitDialog
    }

    // Position of a workout name in SPLIT_OPTIONS (falls back to Rest if it isn't found)
    private int indexOfOption(String name) {
        for (int i = 0; i < SPLIT_OPTIONS.length; i++) {
            if (SPLIT_OPTIONS[i].equals(name)) return i;
            // end of for loop
        }
        return 0;
        // end of indexOfOption
    }

    // Saves the 7 days as a JSON list in the same prefs file the exercises use
    private void saveWeeklySplit() {
        try {
            JSONArray array = new JSONArray();
            for (String day : weeklySplit) {
                array.put(day);
            }
            SharedPreferences prefs = getSharedPreferences("healthsync_workouts", MODE_PRIVATE);
            prefs.edit().putString("weekly_split", array.toString()).apply();
        } catch (Exception exception) {
            Log.e("WorkoutActivity", "Could not save the weekly split", exception);
            // end of try/catch
        }
        // end of saveWeeklySplit
    }

    // Loads the saved split. Anything missing or broken falls back to the default split
    private void loadWeeklySplit() {
        System.arraycopy(DEFAULT_SPLIT, 0, weeklySplit, 0, 7);

        SharedPreferences prefs = getSharedPreferences("healthsync_workouts", MODE_PRIVATE);
        String json = prefs.getString("weekly_split", null);
        // nothing saved yet, so keep the defaults
        if (json == null) return;

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < 7 && i < array.length(); i++) {
                weeklySplit[i] = SPLIT_OPTIONS[indexOfOption(array.getString(i))];
                // end of for loop
            }
        } catch (Exception exception) {
            Log.e("WorkoutActivity", "Could not load the weekly split", exception);
            // end of try/catch
        }
        // end of loadWeeklySplit
    }

    @Override
    public void onExercisePicked(ExerciseCatalog.Entry entry) {
        for (Exercise existing : exerciseList) {
            if (existing.getName().equalsIgnoreCase(entry.name)) {
                Toast.makeText(this, R.string.workout_already_added, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Exercise newExercise = new Exercise(entry.name, entry.group, entry.setsReps);
        newExercise.setMuscleTags(entry.muscleTags);
        newExercise.setTips(entry.tips);

        exerciseList.add(newExercise);
        adapter.notifyItemInserted(exerciseList.size() - 1);
        saveExercises();
        showExerciseInfo(newExercise);
        Toast.makeText(this, "Added " + entry.name, Toast.LENGTH_SHORT).show();
    }

    // Shows or hides everything under the Exercise Info title, and flips the arrow
    private void showInfoOpen(boolean open) {
        int contentVisibility = android.view.View.GONE;
        String arrowText = "▼";
        String stateDescription = ", collapsed. Tap to expand";

        if (open) {
            contentVisibility = android.view.View.VISIBLE;
            arrowText = "▲";
            stateDescription = ", expanded. Tap to collapse";
        }

        infoContent.setVisibility(contentVisibility);
        infoArrow.setText(arrowText);
        // TalkBack reads this so screen reader users know what tapping will do
        findViewById(R.id.rowExerciseInfoHeader).setContentDescription(
                tvInfoCardTitle.getText() + stateDescription);
        // end of showInfoOpen
    }

    // Called when the user taps an exercise: fill in the card, open it, and scroll to it
    // so the user actually sees the info they asked for
    private void openExerciseInfo(Exercise exercise) {
        showExerciseInfo(exercise);
        showInfoOpen(true);
        getSharedPreferences("healthsync_workouts", MODE_PRIVATE)
                .edit().putBoolean("info_open", true).apply();

        androidx.core.widget.NestedScrollView scroll = findViewById(R.id.scrollWorkout);
        android.view.View card = findViewById(R.id.cardExerciseInfo);
        // post() waits until the card has been laid out at its new (open) size
        scroll.post(() -> scroll.smoothScrollTo(0, card.getTop()));
        // end of openExerciseInfo
    }

    private void showExerciseInfo(Exercise exercise) {
        String[] tags = exercise.getMuscleTags();

        tvInfoCardTitle.setText("EXERCISE INFO - " + exercise.getName().toUpperCase(Locale.ROOT));

        if (exercise.getTips() != null && !exercise.getTips().isEmpty()) {
            tvInfoCardTips.setText(exercise.getTips());
        } else {
            tvInfoCardTips.setText(exercise.getCategory());
        }

        if (tags == null || tags.length < 3) {
            tags = new String[]{exercise.getCategory(), "General", "Fitness"};
        }

        tvMuscleTag1.setText(tags[0]);
        tvMuscleTag2.setText(tags[1]);
        tvMuscleTag3.setText(tags[2]);
    }

    public void resetExerciseInfoCard() {
        tvInfoCardTitle.setText("EXERCISE INFO");
        tvMuscleTag1.setText("Muscle 1");
        tvMuscleTag2.setText("Muscle 2");
        tvMuscleTag3.setText("Category");
        tvInfoCardTips.setText("Tap '+' to add an exercise, then tap its name or the ⓘ to view primary muscles and instructions.");
    }

    public void saveExercises() {
        try {
            JSONArray array = new JSONArray();
            for (Exercise exercise : exerciseList) {
                JSONObject exerciseJson = new JSONObject();
                String tips = "";
                if (exercise.getTips() != null) {
                    tips = exercise.getTips();
                }
                exerciseJson.put("name", exercise.getName());
                exerciseJson.put("category", exercise.getCategory());
                exerciseJson.put("setsReps", exercise.getSetsReps());
                exerciseJson.put("checked", exercise.isChecked());
                exerciseJson.put("tips", tips);

                String[] tags = exercise.getMuscleTags();
                if (tags != null && tags.length >= 3) {
                    exerciseJson.put("tag1", tags[0]);
                    exerciseJson.put("tag2", tags[1]);
                    exerciseJson.put("tag3", tags[2]);
                }
                array.put(exerciseJson);
            }
            SharedPreferences prefs = getSharedPreferences("healthsync_workouts", MODE_PRIVATE);
            prefs.edit().putString("saved_exercises", array.toString()).apply();
        } catch (Exception exception) {
            Log.e("WorkoutActivity", "Could not save the workout list", exception);
        }
    }

    private void loadExercises() {
        exerciseList.clear();
        SharedPreferences prefs = getSharedPreferences("healthsync_workouts", MODE_PRIVATE);
        String json = prefs.getString("saved_exercises", null);
        if (json == null) return;

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject exerciseJson = array.getJSONObject(i);
                Exercise exercise = new Exercise(
                        exerciseJson.getString("name"),
                        exerciseJson.getString("category"),
                        exerciseJson.getString("setsReps")
                );
                exercise.setChecked(exerciseJson.optBoolean("checked", false));
                exercise.setTips(exerciseJson.optString("tips", ""));
                if (exerciseJson.has("tag1") && exerciseJson.has("tag2") && exerciseJson.has("tag3")) {
                    exercise.setMuscleTags(new String[]{
                            exerciseJson.getString("tag1"),
                            exerciseJson.getString("tag2"),
                            exerciseJson.getString("tag3")
                    });
                }
                exerciseList.add(exercise);
            }
        } catch (Exception exception) {
            Log.e("WorkoutActivity", "Could not load the saved workouts", exception);
        }
    }
}

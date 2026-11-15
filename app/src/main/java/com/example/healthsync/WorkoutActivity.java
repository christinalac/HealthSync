package com.example.healthsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

    private ExerciseAdapter adapter;

    private TextView tvInfoCardTitle;
    private TextView tvMuscleTag1;
    private TextView tvMuscleTag2;
    private TextView tvMuscleTag3;
    private TextView tvInfoCardTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_activity);
        // Set "Week of [Monday's Date]" and "[TODAY]'S PLAN" dynamically
        TextView tvWeekOf = findViewById(R.id.textWorkoutPlannerPlaceholder);
        TextView tvDayPlan = findViewById(R.id.textWeeklyPlanHeader);

        java.util.Calendar cal = java.util.Calendar.getInstance();

        // Day of week in uppercase, e.g., "WEDNESDAY'S PLAN"
        String dayName = new java.text.SimpleDateFormat("EEEE", Locale.US)
                .format(cal.getTime())
                .toUpperCase(Locale.US);
        tvDayPlan.setText(dayName + "'S PLAN");

        // Find the Monday of the current week, e.g., "Week of Nov 9"
        cal.setFirstDayOfWeek(java.util.Calendar.MONDAY);
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        String weekStart = new java.text.SimpleDateFormat("MMM d", Locale.US)
                .format(cal.getTime());
        tvWeekOf.setText("Week of " + weekStart);

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

        // Exercise list
        RecyclerView rvExercises = findViewById(R.id.rvExercises);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter(exerciseList, this::showExerciseInfo);
        rvExercises.setAdapter(adapter);

        // "+" opens the exercise search pop-up (ExerciseSearchBottomSheet fetches the API)
        FloatingActionButton fabAddWorkout = findViewById(R.id.fabAddWorkout);
        fabAddWorkout.setOnClickListener(v ->
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

    private void showExerciseInfo(Exercise exercise) {
        tvInfoCardTitle.setText("EXERCISE INFO - " + exercise.getName().toUpperCase(Locale.ROOT));

        if (exercise.getTips() != null && !exercise.getTips().isEmpty()) {
            tvInfoCardTips.setText(exercise.getTips());
        } else {
            tvInfoCardTips.setText(exercise.getCategory());
        }

        String[] tags = exercise.getMuscleTags();
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
        tvInfoCardTips.setText("Tap '+' to add an exercise, then tap its name to view primary muscles and instructions.");
    }

    public void saveExercises() {
        try {
            JSONArray array = new JSONArray();
            for (Exercise ex : exerciseList) {
                JSONObject obj = new JSONObject();
                obj.put("name", ex.getName());
                obj.put("category", ex.getCategory());
                obj.put("setsReps", ex.getSetsReps());
                obj.put("checked", ex.isChecked());
                obj.put("tips", ex.getTips() != null ? ex.getTips() : "");

                String[] tags = ex.getMuscleTags();
                if (tags != null && tags.length >= 3) {
                    obj.put("tag1", tags[0]);
                    obj.put("tag2", tags[1]);
                    obj.put("tag3", tags[2]);
                }
                array.put(obj);
            }
            SharedPreferences prefs = getSharedPreferences("healthsync_workouts", MODE_PRIVATE);
            prefs.edit().putString("saved_exercises", array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
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
                JSONObject obj = array.getJSONObject(i);
                Exercise ex = new Exercise(
                        obj.getString("name"),
                        obj.getString("category"),
                        obj.getString("setsReps")
                );
                ex.setChecked(obj.optBoolean("checked", false));
                ex.setTips(obj.optString("tips", ""));
                if (obj.has("tag1") && obj.has("tag2") && obj.has("tag3")) {
                    ex.setMuscleTags(new String[]{
                            obj.getString("tag1"),
                            obj.getString("tag2"),
                            obj.getString("tag3")
                    });
                }
                exerciseList.add(ex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

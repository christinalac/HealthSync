package com.example.healthsync;

// Intent lets us launch other Activities (screens)
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// WorkoutActivity is the Workout screen - Tab 3.
// Shows today's plan, exercise muscle info, and a "+" search to add exercises.
// The "+" opens ExerciseSearchBottomSheet (same style as the nutrition food search),
// which calls back into onExercisePicked() when the user taps Add.
public class WorkoutActivity extends AppCompatActivity
        implements ExerciseSearchBottomSheet.Listener {

    private final List<Exercise> exerciseList = new ArrayList<>(Arrays.asList(
            new Exercise("Bench Press", "Chest", "4x10 · 135 lbs"),
            new Exercise("Pull-Ups", "Back & Biceps", "3x8"),
            new Exercise("Squat", "Legs", "4x12 · 185 lbs"),
            new Exercise("Plank Hold", "Core", "3x60 sec")
    ));

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

        // Views in the bottom Exercise Info card
        tvInfoCardTitle = findViewById(R.id.textExerciseInfoTitle);
        tvMuscleTag1 = findViewById(R.id.tagPectorals);
        tvMuscleTag2 = findViewById(R.id.tagTriceps);
        tvMuscleTag3 = findViewById(R.id.tagDeltoid);
        tvInfoCardTips = findViewById(R.id.textTipsDescription);

        // Exercise list
        RecyclerView rvExercises = findViewById(R.id.rvExercises);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter(exerciseList, this::showExerciseInfo);
        rvExercises.setAdapter(adapter);

        // "+" opens the exercise search pop-up
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
                // Already on this screen
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

    // Called by ExerciseSearchBottomSheet when the user taps Add on a result
    @Override
    public void onExercisePicked(ExerciseCatalog.Entry entry) {
        for (Exercise existing : exerciseList) {
            if (existing.getName().equalsIgnoreCase(entry.name)) {
                Toast.makeText(this, R.string.workout_already_added, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        exerciseList.add(new Exercise(entry.name, entry.group, entry.setsReps));
        adapter.notifyItemInserted(exerciseList.size() - 1);
        Toast.makeText(this, "Added " + entry.name, Toast.LENGTH_SHORT).show();
    }

    // Updates the info card with the tapped exercise's muscles
    private void showExerciseInfo(Exercise exercise) {
        tvInfoCardTitle.setText("EXERCISE INFO - " + exercise.getName().toUpperCase(Locale.ROOT));
        tvInfoCardTips.setText(exercise.getCategory());

        String[] tags;
        switch (exercise.getName()) {
            case "Bench Press":
                tags = new String[]{"Pectorals", "Triceps", "Front Deltoid"}; break;
            case "Pull-Ups":
                tags = new String[]{"Lats", "Biceps", "Rear Delts"}; break;
            case "Squat":
            case "Squats":
                tags = new String[]{"Quads", "Glutes", "Hamstrings"}; break;
            case "Plank Hold":
                tags = new String[]{"Abs", "Obliques", "Core"}; break;
            case "Bicep Curls":
            case "Hammer Curls":
            case "Preacher Curls":
                tags = new String[]{"Biceps", "Brachialis", "Forearms"}; break;
            case "Tricep Pushdowns":
            case "Skull Crushers":
            case "Tricep Dips":
                tags = new String[]{"Triceps", "Long Head", "Anconeus"}; break;
            case "Leg Extensions":
            case "Leg Press":
                tags = new String[]{"Quads", "Rectus Fem", "Thighs"}; break;
            case "Lunges":
            case "Romanian Deadlifts":
                tags = new String[]{"Glutes", "Hamstrings", "Adductors"}; break;
            case "Calf Raises":
                tags = new String[]{"Gastrocnemius", "Soleus", "Calves"}; break;
            case "Lat Pulldowns":
            case "Bent Over Rows":
                tags = new String[]{"Lats", "Rhomboids", "Traps"}; break;
            case "Overhead Press":
            case "Lateral Raises":
                tags = new String[]{"Deltoids", "Lateral Head", "Traps"}; break;
            default:
                tags = new String[]{exercise.getCategory(), "General", "Fitness"}; break;
        }
        tvMuscleTag1.setText(tags[0]);
        tvMuscleTag2.setText(tags[1]);
        tvMuscleTag3.setText(tags[2]);
    }
}

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

// WorkoutActivity is the Workout screen — Tab 3.
// Shows today's plan, exercise muscle info, and a "+" search to add exercises.
public class WorkoutActivity extends AppCompatActivity {

    // A searchable exercise entry: name plus "category - volume - weight" description
    private static final class SearchableWorkout {
        final String name;
        final String description;

        SearchableWorkout(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    // Local exercise catalogue the "+" search looks through
    private static final List<SearchableWorkout> WORKOUT_DATABASE = Arrays.asList(
            // Arms
            new SearchableWorkout("Bicep Curls", "arm workout - 3x12 - 25 lbs"),
            new SearchableWorkout("Tricep Pushdowns", "arm workout - 3x15"),
            new SearchableWorkout("Hammer Curls", "arm workout - 3x10"),
            new SearchableWorkout("Skull Crushers", "arm workout - 3x12"),
            new SearchableWorkout("Preacher Curls", "arm workout - 3x10"),
            new SearchableWorkout("Tricep Dips", "arm workout - 3x15"),
            // Legs
            new SearchableWorkout("Squats", "leg workout - 4x12 - 185 lbs"),
            new SearchableWorkout("Leg Extensions", "leg workout - 3x15"),
            new SearchableWorkout("Lunges", "leg workout - 3x10 steps"),
            new SearchableWorkout("Leg Press", "leg workout - 4x10"),
            new SearchableWorkout("Romanian Deadlifts", "leg workout - 4x8"),
            new SearchableWorkout("Calf Raises", "leg workout - 4x20"),
            // Chest
            new SearchableWorkout("Bench Press", "chest workout - 4x10 - 135 lbs"),
            new SearchableWorkout("Incline Dumbbell Press", "chest workout - 3x10"),
            new SearchableWorkout("Chest Flyes", "chest workout - 3x12"),
            new SearchableWorkout("Push-Ups", "chest workout - 3x20"),
            // Back
            new SearchableWorkout("Pull-Ups", "back workout - 3x8"),
            new SearchableWorkout("Lat Pulldowns", "back workout - 3x12"),
            new SearchableWorkout("Bent Over Rows", "back workout - 4x10"),
            new SearchableWorkout("Deadlifts", "back workout - 1x5"),
            // Shoulders & core
            new SearchableWorkout("Overhead Press", "shoulder workout - 4x8"),
            new SearchableWorkout("Lateral Raises", "shoulder workout - 3x15"),
            new SearchableWorkout("Plank Hold", "core workout - 3x60 sec"),
            new SearchableWorkout("Hanging Leg Raises", "core workout - 3x12")
    );

    private final List<Exercise> exerciseList = new ArrayList<>(Arrays.asList(
            new Exercise("Bench Press", "Chest", "4x10 · 135 lbs"),
            new Exercise("Pull-Ups", "Back & Biceps", "3x8"),
            new Exercise("Squat", "Legs", "4x12 · 185 lbs"),
            new Exercise("Plank Hold", "Core", "3x60 sec")
    ));

    private final Random random = new Random();
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

        // "+" opens the exercise search dialog
        FloatingActionButton fabAddWorkout = findViewById(R.id.fabAddWorkout);
        fabAddWorkout.setOnClickListener(v -> showSearchDialog());

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

    // Material dialog with an outlined text field for the search query
    private void showSearchDialog() {
        TextInputLayout textInputLayout = new TextInputLayout(this);
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBoxCornerRadii(16f, 16f, 16f, 16f);
        textInputLayout.setHint("e.g., arm, leg, chest, back");
        textInputLayout.setPadding(60, 20, 60, 0);

        TextInputEditText input = new TextInputEditText(textInputLayout.getContext());
        textInputLayout.addView(input);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Find an Exercise")
                .setMessage("Enter a muscle group or movement style.")
                .setView(textInputLayout)
                .setPositiveButton("Search", (dialog, which) -> {
                    String query = input.getText() == null ? "" : input.getText().toString();
                    addRandomMatch(query.trim().toLowerCase(Locale.ROOT));
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    // Picks a random matching exercise from the catalogue and adds it if not already listed.
    // The search is local and in-memory, so it runs on the main thread.
    private void addRandomMatch(String query) {
        List<SearchableWorkout> matches = new ArrayList<>();
        for (SearchableWorkout w : WORKOUT_DATABASE) {
            if (w.name.toLowerCase(Locale.ROOT).contains(query)
                    || w.description.toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(w);
            }
        }

        if (matches.isEmpty()) {
            Toast.makeText(this, "No workouts found matching '" + query + "'", Toast.LENGTH_LONG).show();
            return;
        }

        SearchableWorkout match = matches.get(random.nextInt(matches.size()));

        for (Exercise existing : exerciseList) {
            if (existing.getName().equalsIgnoreCase(match.name)) {
                Toast.makeText(this, "Exercise already added!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Description looks like "arm workout - 3x12 - 25 lbs"; the second segment is the volume
        String volume = "3x10";
        String[] segments = match.description.split("-");
        if (segments.length >= 2) {
            volume = segments[1].trim();
        }

        exerciseList.add(new Exercise(match.name, "Custom", volume));
        adapter.notifyItemInserted(exerciseList.size() - 1);
        Toast.makeText(this, "Found and added: " + match.name, Toast.LENGTH_SHORT).show();
    }
}

package com.example.healthsync

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutActivity : AppCompatActivity() {

    // 💡 MOVE VARIABLES HERE: Making these class properties lets all functions below access them!
    private val exerciseList = mutableListOf<Exercise>(
        Exercise("Bench Press", "Chest", "4x10 · 135 lbs"),
        Exercise("Pull-Ups", "Back & Biceps", "3x8 "),
        Exercise("Squat", "Legs", "4x12 · 185 lbs"),
        Exercise("Plank Hold", "Core", "3x60 sec")
    )
    private lateinit var adapter: ExerciseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout_activity)

        // Find the views for your bottom Exercise Info Card
        // Note: Change these IDs if they do not match your exact layout XML file
        // 1. Find all three muscle tag views and the tips paragraph
        val tvInfoCardTitle = findViewById<TextView>(R.id.textExerciseInfoTitle)
        val tvMuscleTag1 = findViewById<TextView>(R.id.tagPectorals)     // First tag
        val tvMuscleTag2 = findViewById<TextView>(R.id.tagTriceps)       // Second tag
        val tvMuscleTag3 = findViewById<TextView>(R.id.tagDeltoid)       // Third tag (Front Deltoid)
        val tvInfoCardTips = findViewById<TextView>(R.id.textTipsDescription)

        // 2. Set up the custom adapter with the dynamic click handler
        val rvExercises = findViewById<RecyclerView>(R.id.rvExercises)
        rvExercises.layoutManager = LinearLayoutManager(this)

        adapter = ExerciseAdapter(exerciseList) { clickedExercise ->
            // Update the main header title and description tips
            tvInfoCardTitle.text = "EXERCISE INFO - ${clickedExercise.name.uppercase()}"
            tvInfoCardTips.text = clickedExercise.category

            // Dynamically assign muscle badges based on the exercise selected
            when (clickedExercise.name) {
                "Bench Press" -> {
                    tvMuscleTag1.text = "Pectorals"
                    tvMuscleTag2.text = "Triceps"
                    tvMuscleTag3.text = "Front Deltoid"
                }
                "Pull-Ups" -> {
                    tvMuscleTag1.text = "Lats"
                    tvMuscleTag2.text = "Biceps"
                    tvMuscleTag3.text = "Rear Delts"
                }
                "Squat" -> {
                    tvMuscleTag1.text = "Quads"
                    tvMuscleTag2.text = "Glutes"
                    tvMuscleTag3.text = "Hamstrings"
                }
                "Plank Hold" -> {
                    tvMuscleTag1.text = "Abs"
                    tvMuscleTag2.text = "Obliques"
                    tvMuscleTag3.text = "Core"
                }
                // --- ARM WORKOUT SEARCH RESULTS ---
                "Bicep Curls", "Hammer Curls", "Preacher Curls" -> {
                    tvMuscleTag1.text = "Biceps"
                    tvMuscleTag2.text = "Brachialis"
                    tvMuscleTag3.text = "Forearms"
                }
                "Tricep Pushdowns", "Skull Crushers", "Tricep Dips" -> {
                    tvMuscleTag1.text = "Triceps"
                    tvMuscleTag2.text = "Long Head"
                    tvMuscleTag3.text = "Anconeus"
                }
                // --- LEG WORKOUT SEARCH RESULTS ---
                "Leg Extensions", "Leg Press" -> {
                    tvMuscleTag1.text = "Quads"
                    tvMuscleTag2.text = "Rectus Fem"
                    tvMuscleTag3.text = "Thighs"
                }
                "Lunges", "Romanian Deadlifts" -> {
                    tvMuscleTag1.text = "Glutes"
                    tvMuscleTag2.text = "Hamstrings"
                    tvMuscleTag3.text = "Adductors"
                }
                "Calf Raises" -> {
                    tvMuscleTag1.text = "Gastrocnemius"
                    tvMuscleTag2.text = "Soleus"
                    tvMuscleTag3.text = "Calves"
                }
                // --- BACK & SHOULDER SEARCH RESULTS ---
                "Lat Pulldowns", "Bent Over Rows" -> {
                    tvMuscleTag1.text = "Lats"
                    tvMuscleTag2.text = "Rhomboids"
                    tvMuscleTag3.text = "Traps"
                }
                "Overhead Press", "Lateral Raises" -> {
                    tvMuscleTag1.text = "Deltoids"
                    tvMuscleTag2.text = "Lateral Head"
                    tvMuscleTag3.text = "Traps"
                }
                else -> {
                    tvMuscleTag1.text = clickedExercise.category
                    tvMuscleTag2.text = "General"
                    tvMuscleTag3.text = "Fitness"
                }
            }
        }

        rvExercises.adapter = adapter

        // Set up the "+" FAB Button
        val fabAddWorkout = findViewById<FloatingActionButton>(R.id.fabAddWorkout)
        fabAddWorkout.setOnClickListener {
            // 1. Create a modern Material Alert Dialog with rounded corners
            val builder = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            builder.setTitle("Find an Exercise")
            builder.setMessage("Enter a muscle group or movement style.")

            // 2. Wrap a Material TextInputEditText inside a TextInputLayout for a sleek floating border outline
            val context = this
            val textInputLayout =
                com.google.android.material.textfield.TextInputLayout(context).apply {
                    boxBackgroundMode =
                        com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE
                    setBoxCornerRadii(
                        16f,
                        16f,
                        16f,
                        16f
                    ) // Beautiful rounded edges matching your main list cards
                    hint = "e.g., arm, leg, chest, back"
                    isHintEnabled = true
                    // Pad it slightly so it doesn't touch the edges of the dialog box
                    setPadding(60, 20, 60, 0)
                }

            val input =
                com.google.android.material.textfield.TextInputEditText(textInputLayout.context)
            textInputLayout.addView(input)
            builder.setView(textInputLayout)

            // 3. Set up the sleek text action buttons
            builder.setPositiveButton("Search") { dialog, _ ->
                val searchQuery = input.text.toString().trim().lowercase()
                lifecycleScope.launch {
                    fetchSearchableWorkouts(searchQuery)
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }


        // 🧭 BOTTOM NAVIGATION (Moved back inside onCreate so it runs immediately)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_workout
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, ProgressActivity::class.java))
                    true
                }

                R.id.nav_nutrition -> {
                    startActivity(Intent(this, NutritionActivity::class.java))
                    true
                }

                R.id.nav_workout -> true // Already on this screen
                R.id.nav_sleep -> {
                    startActivity(Intent(this, SleepActivity::class.java))
                    true
                }

                R.id.nav_account -> {
                    startActivity(Intent(this, AccountActivity::class.java))
                    true
                }

                else -> false
            }
        }
    } // 👈 onCreate Ends Here Cleanly!

    // 🔍 SEARCH HUB (Sits completely on its own out here)
    private suspend fun fetchSearchableWorkouts(query: String) {
        try {
            // --- EXPANDED LOCAL SEARCHABLE DATA HUB ---
            val fullApiDatabase = listOf(
                // 💪 ARM WORKOUTS
                ApiWorkout("Bicep Curls", "arm workout - 3x12 - 25 lbs"),
                ApiWorkout("Tricep Pushdowns", "arm workout - 3x15"),
                ApiWorkout("Hammer Curls", "arm workout - 3x10"),
                ApiWorkout("Skull Crushers", "arm workout - 3x12"),
                ApiWorkout("Preacher Curls", "arm workout - 3x10"),
                ApiWorkout("Tricep Dips", "arm workout - 3x15"),

                // 🦵 LEG WORKOUTS
                ApiWorkout("Squats", "leg workout - 4x12 - 185 lbs"),
                ApiWorkout("Leg Extensions", "leg workout - 3x15"),
                ApiWorkout("Lunges", "leg workout - 3x10 steps"),
                ApiWorkout("Leg Press", "leg workout - 4x10"),
                ApiWorkout("Romanian Deadlifts", "leg workout - 4x8"),
                ApiWorkout("Calf Raises", "leg workout - 4x20"),

                // 🪵 CHEST WORKOUTS
                ApiWorkout("Bench Press", "chest workout - 4x10 - 135 lbs"),
                ApiWorkout("Incline Dumbbell Press", "chest workout - 3x10"),
                ApiWorkout("Chest Flyes", "chest workout - 3x12"),
                ApiWorkout("Push-Ups", "chest workout - 3x20"),

                // 🦅 BACK WORKOUTS
                ApiWorkout("Pull-Ups", "back workout - 3x8"),
                ApiWorkout("Lat Pulldowns", "back workout - 3x12"),
                ApiWorkout("Bent Over Rows", "back workout - 4x10"),
                ApiWorkout("Deadlifts", "back workout - 1x5"),

                // 🛡️ SHOULDER & CORE WORKOUTS
                ApiWorkout("Overhead Press", "shoulder workout - 4x8"),
                ApiWorkout("Lateral Raises", "shoulder workout - 3x15"),
                ApiWorkout("Plank Hold", "core workout - 3x60 sec"),
                ApiWorkout("Hanging Leg Raises", "core workout - 3x12")
            )

            val filteredResults = fullApiDatabase.filter { exercise ->
                exercise.name.lowercase().contains(query) ||
                        exercise.description.lowercase().contains(query)
            }

            if (filteredResults.isNotEmpty()) {
                val randomMatch = filteredResults.random()

                // 🛑 DUPLICATE CHECK: Make sure it's not already in your active routine
                val isAlreadyInList = exerciseList.any { existingExercise ->
                    existingExercise.name.equals(randomMatch.name, ignoreCase = true)
                }

                if (isAlreadyInList) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WorkoutActivity, "Exercise already added!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Extract the text layout segments (e.g., "arm workout - 3x12 - 25 lbs")
                    val rawDesc = randomMatch.description

                    // Smart split text by dashes to extract volume metadata cleanly
                    val parsedVolume = if (rawDesc.contains("-")) {
                        val segments = rawDesc.split("-")
                        if (segments.size >= 2) segments[1].trim() else "3x10"
                    } else {
                        "3x10" // Default fallback value if no dash exists
                    }

                    // Map ApiWorkout to regular Exercise using the clean parsed volume format!
                    // This populates your list rows with the exact format like "3x12"
                    val newExercise = Exercise(randomMatch.name, "Custom", parsedVolume)

                    exerciseList.add(newExercise)
                    withContext(Dispatchers.Main) {
                        adapter.notifyItemInserted(exerciseList.size - 1)

                Toast.makeText(
                            this@WorkoutActivity,
                            "Found and added: ${newExercise.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@WorkoutActivity,
                        "No workouts found matching '$query'",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@WorkoutActivity,
                    "Error filtering data: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
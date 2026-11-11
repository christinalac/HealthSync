//******************************************
// Program Name: NutritionActivity.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 3.0
// Purpose: Nutrition screen (Tab 2). Shows the calorie summary, hydration tracker, and Today's Meals, and opens the food search from the + button.
//******************************************

package com.example.healthsync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Nutrition screen — Tab 2.
// Calorie summary, hydration tracker, and Today's Meals.
// The green "+" opens a food search; picking a food adds it to Today's Meals.
public class NutritionActivity extends AppCompatActivity {

    private NutritionViewModel viewModel;

    private TextView tvHeaderKcal, tvGoal, tvEaten, tvLeft;
    private ProgressBar calorieProgress;
    private TextView tvCarbs, tvProtein, tvFat;
    private TextView tvHydrationTitle, tvGlassCount;
    private WaterProgressBar waterBar;
    private MaterialCardView hydrationCard;
    private TextView tvMealsEmpty;
    private LinearLayout mealsContainer;
    private MealsListController mealsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nutrition_activity);

        viewModel = new ViewModelProvider(this).get(NutritionViewModel.class);

        bindViews();
        setupHydrationTaps();
        setupMeals();
        setupBottomNav();

        // Redraw whenever the data changes
        viewModel.getState().observe(this, this::render);
    }

    // Recalculate goals when coming back from other tabs (the profile may have changed)
    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshGoals();
    }

    private void bindViews() {
        tvHeaderKcal = findViewById(R.id.tvHeaderKcal);
        tvGoal = findViewById(R.id.tvGoal);
        tvEaten = findViewById(R.id.tvEaten);
        tvLeft = findViewById(R.id.tvLeft);
        calorieProgress = findViewById(R.id.calorieProgress);
        tvCarbs = findViewById(R.id.tvCarbs);
        tvProtein = findViewById(R.id.tvProtein);
        tvFat = findViewById(R.id.tvFat);
        tvHydrationTitle = findViewById(R.id.tvHydrationTitle);
        waterBar = findViewById(R.id.waterBar);
        tvGlassCount = findViewById(R.id.tvGlassCount);
        hydrationCard = findViewById(R.id.hydrationCard);
        tvMealsEmpty = findViewById(R.id.tvMealsEmpty);
        mealsContainer = findViewById(R.id.mealsContainer);
    }

    // Tap anywhere on the hydration card = +1 cup. Long-press = undo a cup.
    private void setupHydrationTaps() {
        hydrationCard.setOnClickListener(v -> viewModel.addCup());
        hydrationCard.setOnLongClickListener(v -> {
            viewModel.removeCup();
            return true;
        });
    }

    private void setupMeals() {
        mealsController = new MealsListController(mealsContainer, new MealsListController.Listener() {
            @Override public void onIncrease(LoggedMeal meal) { viewModel.changeServings(meal.id, +0.5); }
            @Override public void onDecrease(LoggedMeal meal) { viewModel.changeServings(meal.id, -0.5); }
            @Override public void onDelete(LoggedMeal meal)   { viewModel.removeMeal(meal.id); }
        });

        // The green "+" opens the food search pop-up
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> new FoodSearchBottomSheet().show(getSupportFragmentManager(), "food_search"));
    }

    private void render(NutritionState s) {
        // Header pill + calorie card
        tvHeaderKcal.setText(getString(R.string.kcal_progress, s.getCaloriesEaten(), s.calorieGoal));
        tvGoal.setText(String.valueOf(s.calorieGoal));
        tvEaten.setText(String.valueOf(s.getCaloriesEaten()));
        tvLeft.setText(String.valueOf(s.getCaloriesLeft()));
        calorieProgress.setProgress(s.getCalorieProgressPercent());
        tvCarbs.setText(s.getCarbsG() + "g");
        tvProtein.setText(s.getProteinG() + "g");
        tvFat.setText(s.getFatG() + "g");

        // Hydration card (cups)
        tvHydrationTitle.setText("HYDRATION – " + s.hydrationCups + " / "
                + Format.fmt1(s.hydrationGoalCups) + " CUPS");
        // If your WaterProgressBar only has setProgress(float), drop the second argument
        waterBar.setProgress(s.getHydrationProgress(), true);
        waterBar.setContentDescription("Hydration " + s.getHydrationPercent() + " percent of daily goal");
        if (s.getCupsRemaining() <= 0.0) {
            tvGlassCount.setText("Goal reached 🎉");
        } else {
            tvGlassCount.setText(Format.fmt1(s.getCupsRemaining()) + " cups to go · "
                    + s.getHydrationPercent() + "%");
        }

        // Today's meals
        mealsController.submit(s.meals);
        tvMealsEmpty.setVisibility(s.meals.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // If you've moved this into your own BottomNavHelper, keep that and delete this method.
    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_nutrition);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, ProgressActivity.class));
                return true;
            } else if (id == R.id.nav_nutrition) {
                return true;
            } else if (id == R.id.nav_workout) {
                startActivity(new Intent(this, WorkoutActivity.class));
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
}

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

import java.util.Locale;

// Nutrition screen — Tab 2.
// Calorie summary, hydration tracker, and Today's Meals.
// The green "+" opens a food search; picking a food adds it to Today's Meals.
public class NutritionActivity extends AppCompatActivity {

    private NutritionViewModel viewModel;

    private TextView tvHeaderKcal;
    private TextView tvGoal;
    private TextView tvEaten;
    private TextView tvLeft;
    private ProgressBar calorieProgress;
    private TextView tvCarbs;
    private TextView tvProtein;
    private TextView tvFat;
    private TextView tvHydrationTitle;
    private WaterProgressBar waterBar;
    private TextView tvGlassCount;
    private MaterialCardView hydrationCard;

    // --- MEALS ---
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

    // Tap anywhere on the hydration card = +1 glass. Long-press = undo a glass.
    private void setupHydrationTaps() {
        hydrationCard.setOnClickListener(v -> viewModel.addGlass());
        hydrationCard.setOnLongClickListener(v -> {
            viewModel.removeGlass();
            return true;
        });
    }

    // --- MEALS ---
    private void setupMeals() {
        mealsController = new MealsListController(
                mealsContainer,
                meal -> viewModel.changeServings(meal.getId(), +0.5),
                meal -> viewModel.changeServings(meal.getId(), -0.5),
                meal -> viewModel.removeMeal(meal.getId())
        );

        // The green "+" opens the food search pop-up
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v ->
                new FoodSearchBottomSheet().show(getSupportFragmentManager(), "food_search"));
    }

    private void render(NutritionState s) {
        // Header pill + calorie card
        tvHeaderKcal.setText(getString(R.string.kcal_progress, s.getCaloriesEaten(), s.getCalorieGoal()));
        tvGoal.setText(String.valueOf(s.getCalorieGoal()));
        tvEaten.setText(String.valueOf(s.getCaloriesEaten()));
        tvLeft.setText(String.valueOf(s.getCaloriesLeft()));
        calorieProgress.setProgress(s.getCalorieProgressPercent());
        tvCarbs.setText(s.getCarbsG() + "g");
        tvProtein.setText(s.getProteinG() + "g");
        tvFat.setText(s.getFatG() + "g");

        // Hydration card
        tvHydrationTitle.setText(String.format(
                Locale.US, "HYDRATION – %.1fL / %.1fL", s.getHydrationLiters(), s.getHydrationGoalLiters()));
        waterBar.setProgress(s.getHydrationProgress());
        waterBar.setContentDescription("Hydration " + s.getHydrationPercent() + " percent of daily goal");
        tvGlassCount.setText(s.getGlassesDrunk() + " / " + s.getTotalGlasses()
                + " glasses · " + s.getHydrationPercent() + "%");

        // Today's meals
        mealsController.submit(s.getMeals());
        tvMealsEmpty.setVisibility(s.getMeals().isEmpty() ? View.VISIBLE : View.GONE);
    }

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

package com.example.healthsync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class ProgressActivity extends AppCompatActivity {

    private int dailyCalories;
    private double dailyWater;
    private double sleepHours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_activity);

        // Fetch user profile and compute personalized values
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        UserProfile profile = databaseHelper.getLatestUserProfile();

        dailyCalories = PersonalizedCalculations.calculateDailyCalories(
                profile.getGender(),
                profile.getWeightLbs(),
                profile.getHeightFt(),
                profile.getHeightIn(),
                profile.getAge(),
                profile.getActivityLevel(),
                profile.getGoal(),
                false
        );

        dailyWater = PersonalizedCalculations.calculateDailyWaterLitres(
                profile.getGender(),
                profile.getWeightLbs(),
                profile.getActivityLevel(),
                profile.getGoal(),
                profile.isPregnant(),
                profile.isBreastfeeding()
        );

        String sleepSuggestion = PersonalizedCalculations.getSleepSuggestion(profile.getAge());
        sleepHours = parseSleepSuggestionHours(sleepSuggestion);

        // Populate header and Today's goals summary
        TextView tvHeaderKcal = findViewById(R.id.tvHeaderKcal);
        if (tvHeaderKcal != null) {
            tvHeaderKcal.setText(String.format("Target: %,d kcal | %.1f L | %.1f hrs", dailyCalories, dailyWater, sleepHours));
        }

        TextView tvTodayCalories = findViewById(R.id.tvTodayCalories);
        if (tvTodayCalories != null) {
            tvTodayCalories.setText(String.format("%,d", dailyCalories));
        }

        TextView tvTodayWater = findViewById(R.id.tvTodayWater);
        if (tvTodayWater != null) {
            tvTodayWater.setText(String.format("%.1f", dailyWater));
        }

        TextView tvTodaySleep = findViewById(R.id.tvTodaySleep);
        if (tvTodaySleep != null) {
            tvTodaySleep.setText(String.format("%.1f", sleepHours));
        }

        // Subtitles initial values
        TextView tvCaloriesSubtitle = findViewById(R.id.tvCaloriesSubtitle);
        if (tvCaloriesSubtitle != null) {
            tvCaloriesSubtitle.setText(String.format("Avg: %,d kcal / day", dailyCalories));
        }

        TextView tvWaterSubtitle = findViewById(R.id.tvWaterSubtitle);
        if (tvWaterSubtitle != null) {
            tvWaterSubtitle.setText(String.format("Avg: %.1f L / day", dailyWater));
        }

        TextView tvSleepSubtitle = findViewById(R.id.tvSleepSubtitle);
        if (tvSleepSubtitle != null) {
            tvSleepSubtitle.setText(String.format("Avg: %.1f hrs / night", sleepHours));
        }

        // Find bottom nav view
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_nutrition) {
                startActivity(new Intent(this, NutritionActivity.class));
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

        setupToggleListeners();
    }

    private double parseSleepSuggestionHours(String suggestion) {
        if (suggestion == null) return 8.0;
        try {
            String[] parts = suggestion.replace(" hours", "").split(" to ");
            if (parts.length == 2) {
                double min = Double.parseDouble(parts[0].trim());
                double max = Double.parseDouble(parts[1].trim());
                return (min + max) / 2.0;
            }
        } catch (Exception e) {
            // fallback
        }
        return 8.0;
    }

    private void setupToggleListeners() {
        // Calorie Graph Toggle
        MaterialButtonToggleGroup toggleCaloriesGroup = findViewById(R.id.toggleCaloriesGroup);
        LinearLayout layoutCaloriesWeekly = findViewById(R.id.layoutCaloriesWeekly);
        LinearLayout layoutCaloriesMonthly = findViewById(R.id.layoutCaloriesMonthly);
        TextView tvCaloriesSubtitle = findViewById(R.id.tvCaloriesSubtitle);

        toggleCaloriesGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnCaloriesWeekly) {
                    layoutCaloriesWeekly.setVisibility(View.VISIBLE);
                    layoutCaloriesMonthly.setVisibility(View.GONE);
                    tvCaloriesSubtitle.setText(String.format("Avg: %,d kcal / day", dailyCalories));
                } else if (checkedId == R.id.btnCaloriesMonthly) {
                    layoutCaloriesWeekly.setVisibility(View.GONE);
                    layoutCaloriesMonthly.setVisibility(View.VISIBLE);
                    tvCaloriesSubtitle.setText(String.format("Avg: %,d kcal / week", dailyCalories * 7));
                }
            }
        });

        // Water Graph Toggle
        MaterialButtonToggleGroup toggleWaterGroup = findViewById(R.id.toggleWaterGroup);
        LinearLayout layoutWaterWeekly = findViewById(R.id.layoutWaterWeekly);
        LinearLayout layoutWaterMonthly = findViewById(R.id.layoutWaterMonthly);
        TextView tvWaterSubtitle = findViewById(R.id.tvWaterSubtitle);

        toggleWaterGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnWaterWeekly) {
                    layoutWaterWeekly.setVisibility(View.VISIBLE);
                    layoutWaterMonthly.setVisibility(View.GONE);
                    tvWaterSubtitle.setText(String.format("Avg: %.1f L / day", dailyWater));
                } else if (checkedId == R.id.btnWaterMonthly) {
                    layoutWaterWeekly.setVisibility(View.GONE);
                    layoutWaterMonthly.setVisibility(View.VISIBLE);
                    tvWaterSubtitle.setText(String.format("Avg: %.1f L / week", dailyWater * 7));
                }
            }
        });

        // Sleep Graph Toggle
        MaterialButtonToggleGroup toggleSleepGroup = findViewById(R.id.toggleSleepGroup);
        LinearLayout layoutSleepWeekly = findViewById(R.id.layoutSleepWeekly);
        LinearLayout layoutSleepMonthly = findViewById(R.id.layoutSleepMonthly);
        TextView tvSleepSubtitle = findViewById(R.id.tvSleepSubtitle);

        toggleSleepGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnSleepWeekly) {
                    layoutSleepWeekly.setVisibility(View.VISIBLE);
                    layoutSleepMonthly.setVisibility(View.GONE);
                    tvSleepSubtitle.setText(String.format("Avg: %.1f hrs / night", sleepHours));
                } else if (checkedId == R.id.btnSleepMonthly) {
                    layoutSleepWeekly.setVisibility(View.GONE);
                    layoutSleepMonthly.setVisibility(View.VISIBLE);
                    tvSleepSubtitle.setText(String.format("Avg: %.1f hrs / week", sleepHours * 7));
                }
            }
        });
    }
}

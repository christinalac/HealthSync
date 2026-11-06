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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_activity);

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
                    tvCaloriesSubtitle.setText("Avg: 2,135 kcal / day");
                } else if (checkedId == R.id.btnCaloriesMonthly) {
                    layoutCaloriesWeekly.setVisibility(View.GONE);
                    layoutCaloriesMonthly.setVisibility(View.VISIBLE);
                    tvCaloriesSubtitle.setText("Avg: 2,135 kcal / week");
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
                    tvWaterSubtitle.setText("Avg: 2.6 L / day");
                } else if (checkedId == R.id.btnWaterMonthly) {
                    layoutWaterWeekly.setVisibility(View.GONE);
                    layoutWaterMonthly.setVisibility(View.VISIBLE);
                    tvWaterSubtitle.setText("Avg: 2.6 L / week");
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
                    tvSleepSubtitle.setText("Avg: 7.6 hrs / night");
                } else if (checkedId == R.id.btnSleepMonthly) {
                    layoutSleepWeekly.setVisibility(View.GONE);
                    layoutSleepMonthly.setVisibility(View.VISIBLE);
                    tvSleepSubtitle.setText("Avg: 7.6 hrs / week");
                }
            }
        });
    }
}

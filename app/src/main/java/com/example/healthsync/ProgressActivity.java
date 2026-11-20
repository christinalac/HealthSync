//******************************************
// Program Name: ProgressActivity.java
// Developer: Jared Baldwin
// Date Created: 10/12/2026
// Version: 2.1
// Purpose: Progress Activity screen to show the recorded progress of the user
// with daily and weekly graphs.
//******************************************
package com.example.healthsync;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProgressActivity extends AppCompatActivity {

    private int dailyCalories = 0;
    private double dailyWater = 0;
    private double sleepHours = 0;

    // ── Graph settings ────────────────────────────────────────
    private static final String[] WEEK_LABELS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    // Full month names, shown in the Monthly subtitle, e.g. "(November)"
    private static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"
    };
    // same height as the old sample bars
    private static final int BAR_TRACK_HEIGHT_DP = 120;

    // Which kind of graph is being drawn; picks the labels, colors and number format
    private static final int CALORIES = 0;
    private static final int WATER = 1;
    private static final int SLEEP = 2;

    // Day-by-day totals saved by the Nutrition and Sleep pages
    private ProgressHistory history = null;

    // Remembers if each card is on Monthly, so the right subtitle is shown after a refresh
    private final boolean[] showingMonthly = new boolean[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_activity);

        history = new ProgressHistory(this);

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

        // Header shows the user's targets (these come from their profile, not their progress)
        TextView tvHeaderKcal = findViewById(R.id.tvHeaderKcal);
        if (tvHeaderKcal != null) {
            tvHeaderKcal.setText(String.format("Target: %,d kcal | %.1f L | %.1f hrs", dailyCalories, dailyWater, sleepHours));
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

    // Redraw every time the page comes back, so meals, water and sleep logged on the
    // other tabs show up right away
    @Override
    protected void onResume() {
        super.onResume();
        showTodaySummary();
        buildGraphs();
        showSuggestionsPreview();
    }

    // ─────────────────────────────────────────────────────────
    // SUGGESTED FOR YOU (preview)
    // ─────────────────────────────────────────────────────────

    // Shows the top 3 food and top 3 workout picks. "See all" opens the full pop-up.
    // Re-run on every resume, so a changed goal or weekly split shows up right away
    private void showSuggestionsPreview() {
        UserProfile profile = new DatabaseHelper(this).getLatestUserProfile();
        int kcal = SuggestionEngine.dailyCalories(profile);

        ((TextView) findViewById(R.id.textPreviewBasis)).setText(String.format(Locale.US,
                "Based on: %s · %,d kcal / day", profile.getGoal(), kcal));

        fillPreview(findViewById(R.id.layoutPreviewFoods),
                SuggestionEngine.foodSuggestions(profile, kcal, 3));
        fillPreview(findViewById(R.id.layoutPreviewWorkouts),
                SuggestionEngine.workoutSuggestions(this, profile, 3));

        findViewById(R.id.buttonSeeAllFoods).setOnClickListener(clickedView ->
                SuggestionsBottomSheet.newInstance(SuggestionsBottomSheet.TYPE_FOOD)
                        .show(getSupportFragmentManager(), "food_suggestions"));
        findViewById(R.id.buttonSeeAllWorkouts).setOnClickListener(clickedView ->
                SuggestionsBottomSheet.newInstance(SuggestionsBottomSheet.TYPE_WORKOUT)
                        .show(getSupportFragmentManager(), "workout_suggestions"));
    }

    // One short line per suggestion: "• Name — details"
    private void fillPreview(LinearLayout box, java.util.List<SuggestionEngine.Suggestion> items) {
        box.removeAllViews();
        for (SuggestionEngine.Suggestion suggestion : items) {
            TextView line = new TextView(this);
            line.setText("• " + suggestion.title + "  —  " + suggestion.detail);
            line.setTextColor(ContextCompat.getColor(this, R.color.navy_text));
            line.setTextSize(13);
            line.setPadding(0, dp(3), 0, dp(3));
            box.addView(line);
        }
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
        } catch (Exception parseException) {
            // fallback
        }
        return 8.0;
    }

    // ─────────────────────────────────────────────────────────
    // TODAY'S GOALS SUMMARY
    // ─────────────────────────────────────────────────────────

    // Shows what the user actually logged today. "--" until something is logged
    private void showTodaySummary() {
        Date today = new Date();
        Double kcal = history.getCalories(today);
        Double water = history.getWater(today);
        Double sleep = history.getSleep(today);
        String kcalText = "--";
        String waterText = "--";
        String sleepText = "--";

        if (kcal != null) {
            kcalText = String.format(Locale.US, "%,d", Math.round(kcal));
        }
        if (water != null) {
            waterText = String.format(Locale.US, "%.1f", water);
        }
        if (sleep != null) {
            sleepText = String.format(Locale.US, "%.1f", sleep);
        }

        ((TextView) findViewById(R.id.tvTodayCalories)).setText(kcalText);
        ((TextView) findViewById(R.id.tvTodayWater)).setText(waterText);
        ((TextView) findViewById(R.id.tvTodaySleep)).setText(sleepText);
    }

    // ─────────────────────────────────────────────────────────
    // GRAPHS (weekly = Mon - Sun of this week, monthly = the weeks of this month)
    // ─────────────────────────────────────────────────────────

    private void buildGraphs() {
        buildGraph(CALORIES, R.id.layoutCaloriesWeekly, R.id.layoutCaloriesMonthly, dailyCalories);
        buildGraph(WATER, R.id.layoutWaterWeekly, R.id.layoutWaterMonthly, dailyWater);
        buildGraph(SLEEP, R.id.layoutSleepWeekly, R.id.layoutSleepMonthly, sleepHours);
        updateSubtitles();
    }

    // Fills one card's weekly and monthly graphs from the saved history
    private void buildGraph(int type, int weeklyId, int monthlyId, double target) {
        Double[] weekValues = new Double[7];
        Calendar cal = mondayOfThisWeek();
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH);
        // 4 or 5 weeks
        int weekCount = (daysInMonth + 6) / 7;
        String[] weekLabels = new String[weekCount];
        Double[] monthValues = new Double[weekCount];

        // ── Weekly: one bar per day, Monday to Sunday of the current week ──
        for (int i = 0; i < 7; i++) {
            weekValues[i] = valueFor(type, cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // ── Monthly: one bar per week of the current month ──
        // Wk 1 = the 1st - 7th, Wk 2 = 8th - 14th, ... the last week holds whatever days are left.
        // Each bar is the average per day for that week (only days that have data count)
        for (int weekIndex = 0; weekIndex < weekCount; weekIndex++) {
            int firstDay = weekIndex * 7 + 1;
            int lastDay = Math.min(firstDay + 6, daysInMonth);
            weekLabels[weekIndex] = "Wk " + (weekIndex + 1);
            monthValues[weekIndex] = history.getAverage(year, month, firstDay, lastDay, typeKey(type));
        }

        fillBars(findViewById(weeklyId), WEEK_LABELS, weekValues, type, target, 16);
        fillBars(findViewById(monthlyId), weekLabels, monthValues, type, target, 26);
    }

    // Clears the graph and adds one column (value, bar, label) for each entry.
    // A null value means nothing was logged, so its bar stays empty and its label is blank
    private void fillBars(LinearLayout graph, String[] labels, Double[] values,
                          int type, double target, int barWidthDp) {
        double scale = target;
        // 7 days and 4-5 weeks both fit at the normal text size
        boolean small = false;
        int labelColor = 0xFF7A869A;
        int valueColor = ContextCompat.getColor(this, R.color.navy_text);
        String valueText = "";
        int valueTextSize = 10;
        int labelTextSize = 11;

        graph.removeAllViews();

        // The tallest bar is the target or the biggest value, whichever is larger,
        // so going over the goal still fits inside the track
        for (Double barValue : values) {
            if (barValue != null && barValue > scale) scale = barValue;
        }
        if (scale <= 0) scale = 1;

        if (small) {
            valueTextSize = 8;
            labelTextSize = 9;
        }

        for (int i = 0; i < labels.length; i++) {
            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.CENTER_HORIZONTAL);
            graph.addView(column, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            // Number above the bar (blank when there's no data)
            if (values[i] == null) {
                valueText = "";
            } else {
                valueText = formatValue(type, values[i], small);
            }
            TextView value = new TextView(this);
            value.setText(valueText);
            value.setTextColor(valueColor);
            value.setTextSize(valueTextSize);
            value.setTypeface(null, android.graphics.Typeface.BOLD);
            value.setMaxLines(1);
            column.addView(value);

            // Gray track with the colored fill inside it, growing from the bottom
            FrameLayout track = new FrameLayout(this);
            track.setBackgroundResource(R.drawable.bg_chart_bar_track);
            LinearLayout.LayoutParams trackParams =
                    new LinearLayout.LayoutParams(dp(barWidthDp), dp(BAR_TRACK_HEIGHT_DP));
            trackParams.topMargin = dp(4);
            column.addView(track, trackParams);

            if (values[i] != null && values[i] > 0) {
                View fill = new View(this);
                fill.setBackgroundResource(barDrawable(type));
                int height = (int) Math.round(dp(BAR_TRACK_HEIGHT_DP) * Math.min(1.0, values[i] / scale));
                FrameLayout.LayoutParams fillParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, Math.max(height, dp(2)));
                fillParams.gravity = Gravity.BOTTOM;
                track.addView(fill, fillParams);
            }

            // Day or month name under the bar
            TextView label = new TextView(this);
            label.setText(labels[i]);
            label.setTextColor(labelColor);
            label.setTextSize(labelTextSize);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = dp(6);
            column.addView(label, labelParams);
        }
    }

    // "Avg: ..." lines under each card title. "No data yet" until something is logged
    private void updateSubtitles() {
        setSubtitle(R.id.tvCaloriesSubtitle, CALORIES, showingMonthly[CALORIES]);
        setSubtitle(R.id.tvWaterSubtitle, WATER, showingMonthly[WATER]);
        setSubtitle(R.id.tvSleepSubtitle, SLEEP, showingMonthly[SLEEP]);
    }

    private void setSubtitle(int viewId, int type, boolean monthly) {
        double total = 0;
        int count = 0;
        String noDataText = "No data yet this week";
        String period = "";

        Calendar now = Calendar.getInstance();
        String monthName = MONTH_NAMES[now.get(Calendar.MONTH)];

        if (monthly) {
            noDataText = "No data yet in " + monthName;
            period = " (" + monthName + ")";
        }

        if (monthly) {
            // Average of every day this month that has data
            Double monthAverage = history.getAverage(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                    1, now.getActualMaximum(Calendar.DAY_OF_MONTH), typeKey(type));
            if (monthAverage != null) { total = monthAverage; count = 1; }
        } else {
            // Average of the days that have data this week
            Calendar cal = mondayOfThisWeek();
            for (int i = 0; i < 7; i++) {
                Double dayValue = valueFor(type, cal.getTime());
                if (dayValue != null) { total += dayValue; count++; }
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        TextView subtitle = findViewById(viewId);
        if (count == 0) {
            subtitle.setText(noDataText);
            return;
        }

        double avg = total / count;
        if (type == CALORIES) {
            subtitle.setText(String.format(Locale.US, "Avg: %,d kcal / day%s", Math.round(avg), period));
        } else if (type == WATER) {
            subtitle.setText(String.format(Locale.US, "Avg: %.1f L / day%s", avg, period));
        } else {
            subtitle.setText(String.format(Locale.US, "Avg: %.1f hrs / night%s", avg, period));
        }
    }

    // ── Small helpers ─────────────────────────────────────────

    private Double valueFor(int type, Date date) {
        if (type == CALORIES) return history.getCalories(date);
        if (type == WATER) return history.getWater(date);
        return history.getSleep(date);
    }

    private String typeKey(int type) {
        if (type == CALORIES) return ProgressHistory.TYPE_CALORIES;
        if (type == WATER) return ProgressHistory.TYPE_WATER;
        return ProgressHistory.TYPE_SLEEP;
    }

    private int barDrawable(int type) {
        if (type == CALORIES) return R.drawable.bg_chart_bar_calories;
        if (type == WATER) return R.drawable.bg_chart_bar_water;
        return R.drawable.bg_chart_bar_sleep;
    }

    // Short numbers for the monthly graph (12 bars), e.g. "2.1k" instead of "2,100"
    private String formatValue(int type, double amount, boolean small) {
        String caloriesText = "";
        if (type == CALORIES) {
            if (small) {
                caloriesText = String.format(Locale.US, "%.1fk", amount / 1000.0);
            } else {
                caloriesText = String.format(Locale.US, "%,d", Math.round(amount));
            }
            return caloriesText;
        }
        if (type == WATER) return String.format(Locale.US, "%.1fL", amount);
        return String.format(Locale.US, "%.1fh", amount);
    }

    // Monday at midnight of the current week (the weekly graph runs Mon - Sun)
    private Calendar mondayOfThisWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    // ─────────────────────────────────────────────────────────
    // WEEKLY / MONTHLY TOGGLES
    // ─────────────────────────────────────────────────────────

    private void setupToggleListeners() {
        setupToggle(CALORIES, R.id.toggleCaloriesGroup, R.id.btnCaloriesMonthly,
                R.id.layoutCaloriesWeekly, R.id.layoutCaloriesMonthly, R.id.tvCaloriesSubtitle);
        setupToggle(WATER, R.id.toggleWaterGroup, R.id.btnWaterMonthly,
                R.id.layoutWaterWeekly, R.id.layoutWaterMonthly, R.id.tvWaterSubtitle);
        setupToggle(SLEEP, R.id.toggleSleepGroup, R.id.btnSleepMonthly,
                R.id.layoutSleepWeekly, R.id.layoutSleepMonthly, R.id.tvSleepSubtitle);
    }

    // Same behavior for all three cards: swap the graph and update the "Avg" line
    private void setupToggle(int type, int groupId, int monthlyButtonId,
                             int weeklyLayoutId, int monthlyLayoutId, int subtitleId) {
        MaterialButtonToggleGroup group = findViewById(groupId);
        View weekly = findViewById(weeklyLayoutId);
        View monthly = findViewById(monthlyLayoutId);

        group.addOnButtonCheckedListener((toggleGroup, checkedId, isChecked) -> {
            if (!isChecked) return;
            boolean isMonthly = checkedId == monthlyButtonId;
            int weeklyVisibility = View.VISIBLE;
            int monthlyVisibility = View.GONE;
            if (isMonthly) {
                weeklyVisibility = View.GONE;
                monthlyVisibility = View.VISIBLE;
            }
            showingMonthly[type] = isMonthly;
            weekly.setVisibility(weeklyVisibility);
            monthly.setVisibility(monthlyVisibility);
            setSubtitle(subtitleId, type, isMonthly);
        });
    }
}

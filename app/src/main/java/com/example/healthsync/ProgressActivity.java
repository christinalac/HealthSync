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

    private int dailyCalories;
    private double dailyWater;
    private double sleepHours;

    // ── Graph settings ────────────────────────────────────────
    private static final String[] WEEK_LABELS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    // Full month names, shown in the Monthly subtitle, e.g. "(November)"
    private static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"
    };
    private static final int BAR_TRACK_HEIGHT_DP = 120; // same height as the old sample bars

    // Which kind of graph is being drawn; picks the labels, colors and number format
    private static final int CALORIES = 0;
    private static final int WATER = 1;
    private static final int SLEEP = 2;

    // Day-by-day totals saved by the Nutrition and Sleep pages
    private ProgressHistory history;

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

    // ─────────────────────────────────────────────────────────
    // TODAY'S GOALS SUMMARY
    // ─────────────────────────────────────────────────────────

    // Shows what the user actually logged today. "--" until something is logged
    private void showTodaySummary() {
        Date today = new Date();
        Double kcal = history.getCalories(today);
        Double water = history.getWater(today);
        Double sleep = history.getSleep(today);

        ((TextView) findViewById(R.id.tvTodayCalories))
                .setText(kcal == null ? "--" : String.format(Locale.US, "%,d", Math.round(kcal)));
        ((TextView) findViewById(R.id.tvTodayWater))
                .setText(water == null ? "--" : String.format(Locale.US, "%.1f", water));
        ((TextView) findViewById(R.id.tvTodaySleep))
                .setText(sleep == null ? "--" : String.format(Locale.US, "%.1f", sleep));
    } // end of showTodaySummary

    // ─────────────────────────────────────────────────────────
    // GRAPHS (weekly = Mon - Sun of this week, monthly = the weeks of this month)
    // ─────────────────────────────────────────────────────────

    private void buildGraphs() {
        buildGraph(CALORIES, R.id.layoutCaloriesWeekly, R.id.layoutCaloriesMonthly, dailyCalories);
        buildGraph(WATER, R.id.layoutWaterWeekly, R.id.layoutWaterMonthly, dailyWater);
        buildGraph(SLEEP, R.id.layoutSleepWeekly, R.id.layoutSleepMonthly, sleepHours);
        updateSubtitles();
    } // end of buildGraphs

    // Fills one card's weekly and monthly graphs from the saved history
    private void buildGraph(int type, int weeklyId, int monthlyId, double target) {
        // ── Weekly: one bar per day, Monday to Sunday of the current week ──
        Double[] weekValues = new Double[7];
        Calendar cal = mondayOfThisWeek();
        for (int i = 0; i < 7; i++) {
            weekValues[i] = valueFor(type, cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } // end of for loop over the week

        // ── Monthly: one bar per week of the current month ──
        // Wk 1 = the 1st - 7th, Wk 2 = 8th - 14th, ... the last week holds whatever days are left.
        // Each bar is the average per day for that week (only days that have data count)
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH);
        int weekCount = (daysInMonth + 6) / 7; // 4 or 5 weeks

        String[] weekLabels = new String[weekCount];
        Double[] monthValues = new Double[weekCount];
        for (int w = 0; w < weekCount; w++) {
            int firstDay = w * 7 + 1;
            int lastDay = Math.min(firstDay + 6, daysInMonth);
            weekLabels[w] = "Wk " + (w + 1);
            monthValues[w] = history.getAverage(year, month, firstDay, lastDay, typeKey(type));
        } // end of for loop over the weeks of the month

        fillBars(findViewById(weeklyId), WEEK_LABELS, weekValues, type, target, 16);
        fillBars(findViewById(monthlyId), weekLabels, monthValues, type, target, 26);
    } // end of buildGraph

    // Clears the graph and adds one column (value, bar, label) for each entry.
    // A null value means nothing was logged, so its bar stays empty and its label is blank
    private void fillBars(LinearLayout graph, String[] labels, Double[] values,
                          int type, double target, int barWidthDp) {
        graph.removeAllViews();

        // The tallest bar is the target or the biggest value, whichever is larger,
        // so going over the goal still fits inside the track
        double scale = target;
        for (Double v : values) {
            if (v != null && v > scale) scale = v;
        } // end of for loop that finds the scale
        if (scale <= 0) scale = 1;

        boolean small = false; // 7 days and 4-5 weeks both fit at the normal text size
        int labelColor = 0xFF7A869A;
        int valueColor = ContextCompat.getColor(this, R.color.navy_text);

        for (int i = 0; i < labels.length; i++) {
            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.CENTER_HORIZONTAL);
            graph.addView(column, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            // Number above the bar (blank when there's no data)
            TextView value = new TextView(this);
            value.setText(values[i] == null ? "" : formatValue(type, values[i], small));
            value.setTextColor(valueColor);
            value.setTextSize(small ? 8 : 10);
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
            } // end of has-data check

            // Day or month name under the bar
            TextView label = new TextView(this);
            label.setText(labels[i]);
            label.setTextColor(labelColor);
            label.setTextSize(small ? 9 : 11);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = dp(6);
            column.addView(label, labelParams);
        } // end of for loop that builds the columns
    } // end of fillBars

    // "Avg: ..." lines under each card title. "No data yet" until something is logged
    private void updateSubtitles() {
        setSubtitle(R.id.tvCaloriesSubtitle, CALORIES, showingMonthly[CALORIES]);
        setSubtitle(R.id.tvWaterSubtitle, WATER, showingMonthly[WATER]);
        setSubtitle(R.id.tvSleepSubtitle, SLEEP, showingMonthly[SLEEP]);
    } // end of updateSubtitles

    private void setSubtitle(int viewId, int type, boolean monthly) {
        double total = 0;
        int count = 0;

        Calendar now = Calendar.getInstance();
        String monthName = MONTH_NAMES[now.get(Calendar.MONTH)];

        if (monthly) {
            // Average of every day this month that has data
            Double v = history.getAverage(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                    1, now.getActualMaximum(Calendar.DAY_OF_MONTH), typeKey(type));
            if (v != null) { total = v; count = 1; }
        } else {
            // Average of the days that have data this week
            Calendar cal = mondayOfThisWeek();
            for (int i = 0; i < 7; i++) {
                Double v = valueFor(type, cal.getTime());
                if (v != null) { total += v; count++; }
                cal.add(Calendar.DAY_OF_MONTH, 1);
            } // end of for loop over the week
        } // end of weekly/monthly if/else

        TextView subtitle = findViewById(viewId);
        if (count == 0) {
            subtitle.setText(monthly ? "No data yet in " + monthName : "No data yet this week");
            return;
        }

        double avg = total / count;
        String period = monthly ? " (" + monthName + ")" : "";
        if (type == CALORIES) {
            subtitle.setText(String.format(Locale.US, "Avg: %,d kcal / day%s", Math.round(avg), period));
        } else if (type == WATER) {
            subtitle.setText(String.format(Locale.US, "Avg: %.1f L / day%s", avg, period));
        } else {
            subtitle.setText(String.format(Locale.US, "Avg: %.1f hrs / night%s", avg, period));
        } // end of type if/else
    } // end of setSubtitle

    // ── Small helpers ─────────────────────────────────────────

    private Double valueFor(int type, Date date) {
        if (type == CALORIES) return history.getCalories(date);
        if (type == WATER) return history.getWater(date);
        return history.getSleep(date);
    } // end of valueFor

    private String typeKey(int type) {
        if (type == CALORIES) return ProgressHistory.TYPE_CALORIES;
        if (type == WATER) return ProgressHistory.TYPE_WATER;
        return ProgressHistory.TYPE_SLEEP;
    } // end of typeKey

    private int barDrawable(int type) {
        if (type == CALORIES) return R.drawable.bg_chart_bar_calories;
        if (type == WATER) return R.drawable.bg_chart_bar_water;
        return R.drawable.bg_chart_bar_sleep;
    } // end of barDrawable

    // Short numbers for the monthly graph (12 bars), e.g. "2.1k" instead of "2,100"
    private String formatValue(int type, double v, boolean small) {
        if (type == CALORIES) {
            return small ? String.format(Locale.US, "%.1fk", v / 1000.0)
                         : String.format(Locale.US, "%,d", Math.round(v));
        }
        if (type == WATER) return String.format(Locale.US, "%.1fL", v);
        return String.format(Locale.US, "%.1fh", v);
    } // end of formatValue

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
    } // end of mondayOfThisWeek

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    } // end of dp

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
    } // end of setupToggleListeners

    // Same behavior for all three cards: swap the graph and update the "Avg" line
    private void setupToggle(int type, int groupId, int monthlyButtonId,
                             int weeklyLayoutId, int monthlyLayoutId, int subtitleId) {
        MaterialButtonToggleGroup group = findViewById(groupId);
        View weekly = findViewById(weeklyLayoutId);
        View monthly = findViewById(monthlyLayoutId);

        group.addOnButtonCheckedListener((g, checkedId, isChecked) -> {
            if (!isChecked) return;
            boolean isMonthly = checkedId == monthlyButtonId;
            showingMonthly[type] = isMonthly;
            weekly.setVisibility(isMonthly ? View.GONE : View.VISIBLE);
            monthly.setVisibility(isMonthly ? View.VISIBLE : View.GONE);
            setSubtitle(subtitleId, type, isMonthly);
        });
    } // end of setupToggle
}

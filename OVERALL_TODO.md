# HealthSync — Team Task Breakdown

## App Structure Overview

**Bottom Navigation Bar (5 tabs):**
| Tab | Screen |
|---|---|
| 1 | Home / Progress |
| 2 | Nutrition |
| 3 | Workout |
| 4 | Sleep |
| 5 | Account (last tab) |

**Key Notes:**
- [ ] Home and Progress are **combined into one screen**
- [ ] No REM score on the Sleep screen
- [ ] Food search powered by an **external Nutrition API**
- [ ] Workout library powered by an **external Workout API**
- [ ] API calls use **Apache HttpClient** via Maven:

```xml
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.13</version>
</dependency>
```

---

## Person 1 — Login / Splash Screen & Account Class
**File:** `Account.java` + Login/Splash UI (`activity_login.xml`, `activity_splash.xml`)

- [ ] Build the Splash screen (HealthSync logo, tagline, runner graphic)
- [ ] Build the Login screen with:
    - [ ] "Log In" button — navigates to Home/Progress on success
    - [ ] "Create an Account" button — navigates to a registration form
- [ ] Build the registration form (collect: first name, last name, email, username, password, height, weight, experience level)
- [ ] Wire all buttons using `OnClickListener` in Android
- [ ] Connect form inputs to the `Account` class (getters/setters already defined)
- [ ] Store account data using `SharedPreferences` or a local SQLite database

---

## Person 2 — Home / Progress Screen (Combined)
**File:** `Home.java` + `Progress.java` + UI (`activity_home.xml`)

- [ ] Build the combined Home + Progress screen
- [ ] **Today's Goals section** — 4 circular progress rings showing:
    - [ ] Daily Steps
    - [ ] Calories
    - [ ] Water intake
    - [ ] Sleep hours
- [ ] **Quick Stats section:**
    - [ ] Active Calories bar with current value
    - [ ] Hydration bar with current value
    - [ ] Weekly Streak count
- [ ] **Progress Tracker — Weekly View:**
    - [ ] Bar charts for: Daily Steps, Calories Burned, Sleep Duration
    - [ ] Show avg and best values below each chart
- [ ] **Progress Tracker — Monthly View:**
    - [ ] Monthly Avg Sleep chart
    - [ ] September Summary cards: Workouts Completed, Hydration Goal Met, Calorie Goal Met
- [ ] Wire the bottom navigation bar buttons to switch between all 5 screens
- [ ] Use `Progress.java` getters/setters to populate chart data

---

## Person 3 — Nutrition Screen
**File:** `Nutrition.java` + UI (`activity_nutrition.xml`)

- [ ] Build the Nutrition & Hydration screen
- [ ] **Calorie Summary section:**
    - [ ] Display Goal, Eaten, and Left calorie values
    - [ ] Macros bar (shows protein/carb/fat breakdown in grams)
- [ ] **Hydration section:**
    - [ ] Display current intake vs. goal (e.g., 1.4L / 2.5L)
    - [ ] Row of water drop icons — tapping one logs a glass (250ml)
- [ ] **Today's Meals section:**
    - [ ] List of logged meals with calorie count and a "Logged" tag
    - [ ] "+" floating button to add a new meal
- [ ] **Food API integration:**
    - [ ] Use Apache HttpClient to call a food/nutrition API (e.g., Open Food Facts or Nutritionix)
    - [ ] Search for food items by name and pull back calorie + macro data
    - [ ] Populate meal entries from API results
- [ ] Use `Nutrition.java` getters/setters to track and update calorie/water data

---

## Person 4 — Workout Screen
**File:** `Workout.java` + UI (`activity_workout.xml`)

- [ ] Build the Workout Planner screen
- [ ] **Weekly Plan section:**
    - [ ] Display a list of planned exercises for the current day
    - [ ] Each item shows: exercise name, muscle group/sets/reps, and a checkbox to mark complete
- [ ] **Exercise Info panel:**
    - [ ] Tapping an exercise opens a detail card showing:
        - [ ] Primary muscles targeted (shown as tags)
        - [ ] Tips / form cues
- [ ] **Workout API integration:**
    - [ ] Use Apache HttpClient to call a workout/exercise API (e.g., API Ninjas Exercises API or ExerciseDB)
    - [ ] Pull exercise data by muscle group filter
    - [ ] Populate the workout list from API results
- [ ] Wire the "+" button to add a workout to today's plan
- [ ] Use `Workout.java` methods: `filterWorkouts()`, `addWorkoutToPlan()`, `removeWorkoutFromPlan()`

---

## Person 5 — Sleep Screen
**File:** `Sleep.java` + UI (`activity_sleep.xml`)

- [ ] Build the Sleep Tracker screen
- [ ] **Sleep Score section:**
    - [ ] Large circular score display (0–100)
    - [ ] Short status label below (e.g., "Good — Aim for 8+ hours for peak recovery")
    - [ ] **No REM score**
- [ ] **Sleep Stages bar:**
    - [ ] Visual bar broken into Light / Deep segments with time labels
- [ ] **Last Night's Stats section:**
    - [ ] Bedtime — display stored time
    - [ ] Wake time — display stored time
    - [ ] Total sleep hours calculated from the two
- [ ] **Add Sleep Entry button:**
    - [ ] Opens a form to input bedtime and wake time
    - [ ] Calls `addSleepEntry()` from `Sleep.java`
- [ ] **Alarm section:**
    - [ ] Toggle to enable/disable alarm
    - [ ] Sound selector
    - [ ] Wire to `setAlarmEnabled()` and `setAlarmSound()` in `Sleep.java`
- [ ] Display sleep recommendation text via `getSleepRecommendation()`

---

## Person 6 — Account / Profile Screen
**File:** `Account.java` (shared) + UI (`activity_account.xml`)

- [ ] Build the My Profile / Account & Settings screen
- [ ] **Profile header:**
    - [ ] Display avatar icon, full name, email
    - [ ] Show stats row: Age, Height, Weight, Level (e.g., Active)
- [ ] **Account Settings section — each item is a tappable row:**
    - [ ] Change Username — opens editable field, calls `setUsername()`
    - [ ] Change Email — opens editable field, calls `setEmail()`
    - [ ] Change Password — opens editable field, calls `setPassword()`
    - [ ] Passkey / Google Sign-In — placeholder button (stretch goal)
- [ ] **Log Out button:**
    - [ ] Clears session/SharedPreferences and returns to Login screen
- [ ] Pull all displayed data from `Account.java` getters
- [ ] This screen is the **last tab** in the bottom navigation bar

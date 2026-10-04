# HealthSync — Weekend Goals
### Objective: Get a working walkthrough demo ready (screens + navigation buttons)

The goal this weekend is not full functionality — just being able to tap through every screen
using the bottom navigation bar and have each screen show its basic layout.

---

## Setup
- [ ] Create a new Android Studio project (Empty Activity)
- [ ] Add all 5 screens as Activities or Fragments
- [ ] Set up the bottom navigation bar with 5 tabs (Home/Progress, Nutrition, Workout, Sleep, Account)
- [ ] Add the Apache HttpClient dependency to `build.gradle`

---

## Splash / Login Screen
- [ ] Create `activity_splash.xml` — logo, tagline, buttons
- [ ] "Log In" button navigates to Home/Progress screen
- [ ] "Create an Account" button navigates to a basic registration screen

---

## Home / Progress Screen
- [ ] Create `activity_home.xml` — placeholder layout with section titles
- [ ] Show 4 placeholder circles for Today's Goals (Steps, Calories, Water, Sleep)
- [ ] Show placeholder text for Quick Stats and Weekly Streak
- [ ] Bottom nav bar visible and working

---

## Nutrition Screen
- [ ] Create `activity_nutrition.xml` — placeholder layout with section titles
- [ ] Show placeholder Calorie Summary section
- [ ] Show placeholder Hydration section
- [ ] Show placeholder Today's Meals list
- [ ] "+" button present (does not need to work yet)
- [ ] Bottom nav bar visible and working

---

## Workout Screen
- [ ] Create `activity_workout.xml` — placeholder layout with section titles
- [ ] Show placeholder weekly plan list with a few dummy exercises
- [ ] "+" button present (does not need to work yet)
- [ ] Bottom nav bar visible and working

---

## Sleep Screen
- [ ] Create `activity_sleep.xml` — placeholder layout with section titles
- [ ] Show placeholder circular Sleep Score
- [ ] Show placeholder Last Night's Stats (Bedtime, Wake Time)
- [ ] "Add Sleep Entry" button present (does not need to work yet)
- [ ] Bottom nav bar visible and working

---

## Account Screen
- [ ] Create `activity_account.xml` — placeholder layout
- [ ] Show placeholder profile header (name, email, stats row)
- [ ] Show placeholder Account Settings list (Username, Email, Password)
- [ ] "Log Out" button present and navigates back to Login screen
- [ ] Bottom nav bar visible and working

---

## Walkthrough Check
- [ ] Can tap from Login → Home/Progress
- [ ] Can tap all 5 bottom nav tabs and land on the correct screen
- [ ] Can tap Log Out and return to Login screen
- [ ] App runs on Android Virtual Machine (AVD) without crashing



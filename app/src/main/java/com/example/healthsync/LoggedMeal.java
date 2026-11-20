//******************************************
// Program Name: LoggedMeal.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 2.0
// Purpose: Model for one entry in Today's Meals: a food's per-serving nutrition plus the number of servings eaten.
//******************************************

package com.example.healthsync;

// One entry in "Today's Meals". Stores per-serving values + how many servings were eaten.
// Immutable: use withServings() to get a changed copy.
public class LoggedMeal {
    private static final double MAX_NUTRITION_VALUE = 1_000_000.0;
    private static final double MAX_SERVINGS = 1_000_000.0;

    public final String id;
    public final String code;
    public final String name;
    public final String brand;
    // may be null
    public final String imageUrl;
    public final String servingLabel;
    public final double kcalPerServing;
    public final double carbsPerServing;
    public final double proteinPerServing;
    public final double fatPerServing;
    public final double servings;

    public LoggedMeal(String id, String code, String name, String brand, String imageUrl,
                      String servingLabel, double kcalPerServing, double carbsPerServing,
                      double proteinPerServing, double fatPerServing, double servings) {
        // Strings: null becomes "" (imageUrl is allowed to stay null)
        if (id != null) {
            this.id = id;
        } else {
            this.id = "";
        }
        if (code != null) {
            this.code = code;
        } else {
            this.code = "";
        }
        if (name != null) {
            this.name = name;
        } else {
            this.name = "";
        }
        if (brand != null) {
            this.brand = brand;
        } else {
            this.brand = "";
        }
        this.imageUrl = imageUrl;
        if (servingLabel != null) {
            this.servingLabel = servingLabel;
        } else {
            this.servingLabel = "";
        }
        // Numbers: accept a generous range, otherwise store a sentinel (-1 for nutrition, 0 for servings)
        if (kcalPerServing >= 0 && kcalPerServing <= MAX_NUTRITION_VALUE) {
            this.kcalPerServing = kcalPerServing;
        } else {
            this.kcalPerServing = -1;
        }
        if (carbsPerServing >= 0 && carbsPerServing <= MAX_NUTRITION_VALUE) {
            this.carbsPerServing = carbsPerServing;
        } else {
            this.carbsPerServing = -1;
        }
        if (proteinPerServing >= 0 && proteinPerServing <= MAX_NUTRITION_VALUE) {
            this.proteinPerServing = proteinPerServing;
        } else {
            this.proteinPerServing = -1;
        }
        if (fatPerServing >= 0 && fatPerServing <= MAX_NUTRITION_VALUE) {
            this.fatPerServing = fatPerServing;
        } else {
            this.fatPerServing = -1;
        }
        if (servings >= 0 && servings <= MAX_SERVINGS) {
            this.servings = servings;
        } else {
            this.servings = 0;
        }
    }

    public LoggedMeal withServings(double newServings) {
        return new LoggedMeal(id, code, name, brand, imageUrl, servingLabel,
                kcalPerServing, carbsPerServing, proteinPerServing, fatPerServing, newServings);
    }

    public double totalKcal()    { return kcalPerServing * servings; }
    public double totalCarbs()   { return carbsPerServing * servings; }
    public double totalProtein() { return proteinPerServing * servings; }
    public double totalFat()     { return fatPerServing * servings; }
}

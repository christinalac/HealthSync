package com.example.healthsync;

// One entry in "Today's Meals". Stores per-serving values + how many servings were eaten.
// Immutable: use withServings() to get a changed copy.
public class LoggedMeal {
    public final String id;
    public final String code;
    public final String name;
    public final String brand;
    public final String imageUrl;      // may be null
    public final String servingLabel;
    public final double kcalPerServing;
    public final double carbsPerServing;
    public final double proteinPerServing;
    public final double fatPerServing;
    public final double servings;

    public LoggedMeal(String id, String code, String name, String brand, String imageUrl,
                      String servingLabel, double kcalPerServing, double carbsPerServing,
                      double proteinPerServing, double fatPerServing, double servings) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.brand = brand;
        this.imageUrl = imageUrl;
        this.servingLabel = servingLabel;
        this.kcalPerServing = kcalPerServing;
        this.carbsPerServing = carbsPerServing;
        this.proteinPerServing = proteinPerServing;
        this.fatPerServing = fatPerServing;
        this.servings = servings;
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

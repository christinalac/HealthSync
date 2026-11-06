package com.example.healthsync;

import androidx.annotation.Nullable;

// One entry in "Today's Meals". Stores per-serving values + how many servings were eaten.
public final class LoggedMeal {

    private final String id;
    private final String code;
    private final String name;
    private final String brand;
    @Nullable private final String imageUrl;
    private final String servingLabel;
    private final double kcalPerServing;
    private final double carbsPerServing;
    private final double proteinPerServing;
    private final double fatPerServing;
    private final double servings;

    public LoggedMeal(String id, String code, String name, String brand, @Nullable String imageUrl,
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

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    @Nullable public String getImageUrl() { return imageUrl; }
    public String getServingLabel() { return servingLabel; }
    public double getKcalPerServing() { return kcalPerServing; }
    public double getCarbsPerServing() { return carbsPerServing; }
    public double getProteinPerServing() { return proteinPerServing; }
    public double getFatPerServing() { return fatPerServing; }
    public double getServings() { return servings; }

    public double getTotalKcal() { return kcalPerServing * servings; }
    public double getTotalCarbs() { return carbsPerServing * servings; }
    public double getTotalProtein() { return proteinPerServing * servings; }
    public double getTotalFat() { return fatPerServing * servings; }

    // Replaces Kotlin's data-class copy() for the servings field (the only one changed anywhere).
    public LoggedMeal withServings(double newServings) {
        return new LoggedMeal(id, code, name, brand, imageUrl, servingLabel,
                kcalPerServing, carbsPerServing, proteinPerServing, fatPerServing, newServings);
    }
}

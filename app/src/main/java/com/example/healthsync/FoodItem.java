package com.example.healthsync;

import java.util.UUID;

// A food returned by the Open Food Facts search.
// All nutrition values are PER SERVING (see OpenFoodFactsApi for how a serving is worked out).
public class FoodItem {
    public final String code;          // barcode
    public final String name;
    public final String brand;
    public final String imageUrl;      // may be null
    public final String servingLabel;  // e.g. "30 g" (falls back to "100 g")
    public final double kcal;
    public final double carbsG;
    public final double proteinG;
    public final double fatG;

    public FoodItem(String code, String name, String brand, String imageUrl, String servingLabel,
                    double kcal, double carbsG, double proteinG, double fatG) {
        this.code = code;
        this.name = name;
        this.brand = brand;
        this.imageUrl = imageUrl;
        this.servingLabel = servingLabel;
        this.kcal = kcal;
        this.carbsG = carbsG;
        this.proteinG = proteinG;
        this.fatG = fatG;
    }

    // Turns a search result into a "Today's Meals" entry with 1 serving
    public LoggedMeal toMeal() {
        return new LoggedMeal(UUID.randomUUID().toString(), code, name, brand, imageUrl,
                servingLabel, kcal, carbsG, proteinG, fatG, 1.0);
    }
}

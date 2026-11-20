//******************************************
// Program Name: FoodItem.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 2.0
// Purpose: Model for a food search result from Open Food Facts, with nutrition values per serving.
//******************************************

package com.example.healthsync;

import java.util.UUID;

// A food returned by the Open Food Facts search.
// All nutrition values are PER SERVING (see OpenFoodFactsApi for how a serving is worked out).
public class FoodItem {
    // barcode
    public final String code;
    public final String name;
    public final String brand;
    // may be null
    public final String imageUrl;
    // e.g. "30 g" (falls back to "100 g")
    public final String servingLabel;
    public final double kcal;
    public final double carbsG;
    public final double proteinG;
    public final double fatG;

    public FoodItem(String code, String name, String brand, String imageUrl, String servingLabel,
                    double kcal, double carbsG, double proteinG, double fatG) {
        // Text must not be null (null becomes ""); imageUrl is allowed to be null
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
        // Nutrition values: accept 0 to 100000 per serving, otherwise use 0
        if (kcal >= 0 && kcal <= 100000) {
            this.kcal = kcal;
        } else {
            this.kcal = 0;
        }
        if (carbsG >= 0 && carbsG <= 100000) {
            this.carbsG = carbsG;
        } else {
            this.carbsG = 0;
        }
        if (proteinG >= 0 && proteinG <= 100000) {
            this.proteinG = proteinG;
        } else {
            this.proteinG = 0;
        }
        if (fatG >= 0 && fatG <= 100000) {
            this.fatG = fatG;
        } else {
            this.fatG = 0;
        }
    }

    // Turns a search result into a "Today's Meals" entry with 1 serving
    public LoggedMeal toMeal() {
        return new LoggedMeal(UUID.randomUUID().toString(), code, name, brand, imageUrl,
                servingLabel, kcal, carbsG, proteinG, fatG, 1.0);
    }
}

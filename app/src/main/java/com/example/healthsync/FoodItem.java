package com.example.healthsync;

import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.UUID;

// A food returned by the Open Food Facts search.
// All nutrition values are PER SERVING (see OpenFoodFactsApi for how a serving is worked out).
public final class FoodItem {

    private final String code;            // barcode
    private final String name;
    private final String brand;
    @Nullable private final String imageUrl;
    private final String servingLabel;    // e.g. "30 g" (falls back to "100 g")
    private final double kcal;
    private final double carbsG;
    private final double proteinG;
    private final double fatG;

    public FoodItem(String code, String name, String brand, @Nullable String imageUrl,
                    String servingLabel, double kcal, double carbsG, double proteinG, double fatG) {
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

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    @Nullable public String getImageUrl() { return imageUrl; }
    public String getServingLabel() { return servingLabel; }
    public double getKcal() { return kcal; }
    public double getCarbsG() { return carbsG; }
    public double getProteinG() { return proteinG; }
    public double getFatG() { return fatG; }

    public LoggedMeal toMeal() {
        return new LoggedMeal(
                UUID.randomUUID().toString(),
                code, name, brand, imageUrl,
                servingLabel,
                kcal, carbsG, proteinG, fatG,
                1.0
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoodItem)) return false;
        FoodItem that = (FoodItem) o;
        return Double.compare(that.kcal, kcal) == 0
                && Double.compare(that.carbsG, carbsG) == 0
                && Double.compare(that.proteinG, proteinG) == 0
                && Double.compare(that.fatG, fatG) == 0
                && Objects.equals(code, that.code)
                && Objects.equals(name, that.name)
                && Objects.equals(brand, that.brand)
                && Objects.equals(imageUrl, that.imageUrl)
                && Objects.equals(servingLabel, that.servingLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, brand, imageUrl, servingLabel, kcal, carbsG, proteinG, fatG);
    }
}

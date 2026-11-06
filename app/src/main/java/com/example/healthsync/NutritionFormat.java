package com.example.healthsync;

import java.util.Locale;

// Formatting helpers that were Kotlin top-level / extension functions.
public final class NutritionFormat {

    private NutritionFormat() { }

    // 1.0 -> "1", 1.5 -> "1.5"
    public static String fmt1(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return Integer.toString((int) value);
        }
        return String.format(Locale.US, "%.1f", value);
    }

    // "120 kcal · C 20g · P 3g · F 4g" (same order as the macros on the summary card)
    public static String nutrientLine(double kcal, double carbs, double protein, double fat) {
        return Math.round(kcal) + " kcal · C " + fmt1(carbs) + "g · P " + fmt1(protein) + "g · F " + fmt1(fat) + "g";
    }
}

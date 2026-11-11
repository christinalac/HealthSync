//******************************************
// Program Name: Format.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 2.0
// Purpose: Text formatting helpers for servings and nutrition lines, such as "120 kcal · C 20g · P 3g · F 4g".
//******************************************

package com.example.healthsync;

import java.util.Locale;

// Small text-formatting helpers shared by the Nutrition screen.
public final class Format {
    private Format() {}

    // 1.0 -> "1", 1.5 -> "1.5"
    public static String fmt1(double v) {
        if (v == Math.floor(v)) return String.valueOf((long) v);
        return String.format(Locale.US, "%.1f", v);
    }

    // "120 kcal · C 20g · P 3g · F 4g" (same order as the macros on the summary card)
    public static String nutrientLine(double kcal, double carbs, double protein, double fat) {
        return Math.round(kcal) + " kcal · C " + fmt1(carbs) + "g · P " + fmt1(protein)
                + "g · F " + fmt1(fat) + "g";
    }
}

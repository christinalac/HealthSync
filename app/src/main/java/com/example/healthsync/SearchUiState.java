//******************************************
// Program Name: SearchUiState.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 2.0
// Purpose: Describes what the food search pop-up should show: idle, loading, results, no results, or an error.
//******************************************

package com.example.healthsync;

import java.util.Collections;
import java.util.List;

// Everything the search sheet can be showing
public class SearchUiState {
    public enum Type { IDLE, LOADING, EMPTY, RESULTS, ERROR }

    public final Type type;
    public final List<FoodItem> foods;   // only for RESULTS
    public final String message;         // only for ERROR

    private SearchUiState(Type type, List<FoodItem> foods, String message) {
        this.type = type;
        this.foods = foods;
        this.message = message;
    }

    public static SearchUiState idle()    { return new SearchUiState(Type.IDLE, Collections.<FoodItem>emptyList(), null); }
    public static SearchUiState loading() { return new SearchUiState(Type.LOADING, Collections.<FoodItem>emptyList(), null); }
    public static SearchUiState empty()   { return new SearchUiState(Type.EMPTY, Collections.<FoodItem>emptyList(), null); }
    public static SearchUiState results(List<FoodItem> foods) { return new SearchUiState(Type.RESULTS, foods, null); }
    public static SearchUiState error(String message) { return new SearchUiState(Type.ERROR, Collections.<FoodItem>emptyList(), message); }
}

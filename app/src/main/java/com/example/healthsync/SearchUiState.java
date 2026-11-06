package com.example.healthsync;

import java.util.Collections;
import java.util.List;

// Everything the search sheet can be showing.
// Replaces the Kotlin sealed interface with a small Java class hierarchy.
public abstract class SearchUiState {

    private SearchUiState() { }

    public static final class Idle extends SearchUiState {
        public static final Idle INSTANCE = new Idle();
        private Idle() { }
    }

    public static final class Loading extends SearchUiState {
        public static final Loading INSTANCE = new Loading();
        private Loading() { }
    }

    public static final class Empty extends SearchUiState {
        public static final Empty INSTANCE = new Empty();
        private Empty() { }
    }

    public static final class Results extends SearchUiState {
        private final List<FoodItem> foods;

        public Results(List<FoodItem> foods) {
            this.foods = Collections.unmodifiableList(foods);
        }

        public List<FoodItem> getFoods() {
            return foods;
        }
    }

    public static final class Error extends SearchUiState {
        private final String message;

        public Error(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}

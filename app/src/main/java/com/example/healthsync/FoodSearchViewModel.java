//******************************************
// Program Name: FoodSearchViewModel.java
// Developer: Maximiliano Salinas
// Date Created: 10/12/2026
// Version: 2.0
// Purpose: Runs food searches on a background thread and exposes the search state to the search pop-up.
//******************************************

package com.example.healthsync;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

// Runs food searches on a background thread and exposes what the search sheet should show.
public class FoodSearchViewModel extends ViewModel {

    private final MutableLiveData<SearchUiState> state = new MutableLiveData<>(SearchUiState.idle());
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final AtomicInteger latestRequest = new AtomicInteger();

    public LiveData<SearchUiState> getState() {
        return state;
    }

    public void search(String query) {
        final String q = query == null ? "" : query.trim();
        if (q.length() < 2) return;

        final int requestId = latestRequest.incrementAndGet();
        state.setValue(SearchUiState.loading());

        executor.execute(() -> {
            SearchUiState result;
            try {
                List<FoodItem> foods = OpenFoodFactsApi.search(q);
                result = foods.isEmpty() ? SearchUiState.empty() : SearchUiState.results(foods);
            } catch (OpenFoodFactsApi.RateLimitedException e) {
                result = SearchUiState.error(
                        "Open Food Facts limits searches to about 10 a minute. Wait a moment and try again.");
            } catch (Exception e) {
                result = SearchUiState.error(
                        "Couldn't reach Open Food Facts. Check your connection and try again.");
            }
            // Ignore answers to older searches
            if (requestId == latestRequest.get()) state.postValue(result);
        });
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}

package com.example.healthsync;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class FoodSearchViewModel extends ViewModel {

    private final MutableLiveData<SearchUiState> state =
            new MutableLiveData<>(SearchUiState.Idle.INSTANCE);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    // Tracks the in-flight search so a newer one supersedes it (mirrors job?.cancel()).
    private Future<?> job;
    private AtomicBoolean cancelled;

    public LiveData<SearchUiState> getState() {
        return state;
    }

    public void search(String query) {
        final String q = query.trim();
        if (q.length() < 2) return;

        // Cancel whatever is running: mark its token cancelled and interrupt the task.
        if (cancelled != null) cancelled.set(true);
        if (job != null) job.cancel(true);

        final AtomicBoolean token = new AtomicBoolean(false);
        cancelled = token;

        state.setValue(SearchUiState.Loading.INSTANCE);

        job = executor.submit(() -> {
            try {
                List<FoodItem> foods = OpenFoodFactsApi.search(q);
                if (token.get()) return;
                SearchUiState next = foods.isEmpty()
                        ? SearchUiState.Empty.INSTANCE
                        : new SearchUiState.Results(foods);
                post(token, next);
            } catch (OpenFoodFactsApi.RateLimitedException e) {
                post(token, new SearchUiState.Error(
                        "Open Food Facts limits searches to about 10 a minute. Wait a moment and try again."));
            } catch (Exception e) {
                if (token.get() || Thread.currentThread().isInterrupted()) return;
                post(token, new SearchUiState.Error(
                        "Couldn't reach Open Food Facts. Check your connection and try again."));
            }
        });
    }

    private void post(AtomicBoolean token, SearchUiState next) {
        main.post(() -> {
            if (!token.get()) state.setValue(next);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (cancelled != null) cancelled.set(true);
        if (job != null) job.cancel(true);
        executor.shutdownNow();
    }
}

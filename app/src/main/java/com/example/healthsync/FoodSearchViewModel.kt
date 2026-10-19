package com.example.healthsync

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

// Everything the search sheet can be showing
sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    object Empty : SearchUiState
    data class Results(val foods: List<FoodItem>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class FoodSearchViewModel : ViewModel() {

    private val _state = MutableLiveData<SearchUiState>(SearchUiState.Idle)
    val state: LiveData<SearchUiState> = _state

    private var job: Job? = null

    fun search(query: String) {
        val q = query.trim()
        if (q.length < 2) return
        job?.cancel()
        job = viewModelScope.launch {
            _state.value = SearchUiState.Loading
            try {
                val foods = OpenFoodFactsApi.search(q)
                _state.value = if (foods.isEmpty()) SearchUiState.Empty else SearchUiState.Results(foods)
            } catch (e: CancellationException) {
                throw e
            } catch (e: OpenFoodFactsApi.RateLimitedException) {
                _state.value = SearchUiState.Error(
                    "Open Food Facts limits searches to about 10 a minute. Wait a moment and try again."
                )
            } catch (e: Exception) {
                _state.value = SearchUiState.Error(
                    "Couldn't reach Open Food Facts. Check your connection and try again."
                )
            }
        }
    }
}

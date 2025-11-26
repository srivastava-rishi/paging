package com.rsstudio.paginationpractice.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsstudio.paginationpractice.common.ApiResult
import com.rsstudio.paginationpractice.data.mapper.toMovieItem
import com.rsstudio.paginationpractice.domain.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScreenAViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val searchMoviesUseCase: SearchMoviesUseCase
) : ViewModel() {
    var uiState by mutableStateOf(ScreenAUiState())
        private set

    var uiSideEffect by mutableStateOf<ScreenASideEffect>(ScreenASideEffect.None)
        private set

    private var currentPage = 1
    
    // IMPROVEMENT: Replaced boolean with Job for better control
    // BEFORE: private var isLoadingInProgress = false
    // ISSUE: Boolean can't cancel ongoing requests, just prevents new ones
    // NOW: Job allows us to cancel previous request when user searches something new
    // BENEFIT: If user types new search while loading, we can cancel old request immediately
    private var paginationJob: Job? = null
    
    // FIX #4: Added flag to track when we've reached the end of results
    // ISSUE: Without this, pagination would continue indefinitely even when no more data exists
    private var hasMoreData = true

    init {
        // Start with empty state - user needs to search first
        // BEFORE: getMoviesData(1) - auto-loaded "spider" results
        // NOW: Wait for user to enter search query
    }

    private fun getMoviesData(page: Int, isLoadMore: Boolean = false) {
        // Cancel any ongoing pagination request before starting a new one
        // BEFORE: Just checked boolean, couldn't stop ongoing network calls
        // NOW: Actually cancel the coroutine job for better resource management
        paginationJob?.cancel()
        
        paginationJob = viewModelScope.launch {
            when (
                val result = searchMoviesUseCase.invoke(
                    apiKey = "8e3b2bb2",
                    query = uiState.searchQuery.ifBlank { "spider" }, // Use user's search query
                    pageNo = page.toString()
                )
            ) {
                is ApiResult.Failure -> {
                    Log.d("lion22", "getMoviesData:  ${result.error}")
                    
                    // FIX #7: Added proper error handling for pagination failures
                    // BEFORE: Errors were only logged, isLoadingMore stayed true forever, blocking future requests
                    // NOW: Reset loading state and show error to user
                    if (isLoadMore) {
                        uiState = uiState.copy(
                            isLoadingMore = false,
                            paginationError = "Failed to load more movies"
                        )
                    } else {
                        uiState = uiState.copy(
                            screenState = ScreenState.EMPTY,
                            isSearching = false
                        )
                    }
                    paginationJob = null // Clear the job reference
                }

                is ApiResult.Success -> {
                    Log.d("lion22", "getMoviesData:  ${result.data.movieList}")
                    
                    // FIX #6: Removed artificial 6-second delay
                    // BEFORE: if (isLoadMore) delay(6000) - This caused terrible UX
                    // NOW: Data loads immediately for better user experience
                    
                    // CRASH FIX: Safe handling of nullable movieList
                    // BEFORE: Crashed with NullPointerException when API returned null movieList
                    // ISSUE: API returns null for "Search" field when no results or invalid search
                    // NOW: Mapper handles null safely and returns empty list
                    val newMovies = result.data.movieList.toMovieItem()
                    
                    // FIX #4: Check if we received empty results (end of pagination)
                    // BEFORE: No check for end of data, would keep requesting forever
                    // NOW: Stop pagination when no more results are returned
                    if (newMovies.isEmpty()) {
                        hasMoreData = false
                    }
                    
                    uiState = uiState.copy(
                        data = if (isLoadMore) uiState.data + newMovies else newMovies,
                        screenState = if (newMovies.isEmpty() && !isLoadMore) ScreenState.EMPTY else ScreenState.DEFAULT,
                        isLoadingMore = false,
                        isSearching = false,
                        paginationError = null // Clear any previous errors
                    )
                    
                    // FIX #1: Actually increment the current page after successful load
                    // BEFORE: Called getMoviesData(currentPage + 1) but never updated currentPage
                    // ISSUE: This caused the same page to be loaded repeatedly (page 2, page 2, page 2...)
                    // NOW: Increment currentPage so next load more fetches the correct next page
                    if (isLoadMore) {
                        currentPage++
                    }
                    
                    // Clear job reference after successful completion
                    paginationJob = null
                }
            }

        }
    }

    fun resetSideEffect() {
        uiSideEffect = ScreenASideEffect.None
    }

    fun onEvent(
        event: ScreenAEvent
    ) {
        when (event) {
            is ScreenAEvent.OnSearchQueryChange -> {
                // Update search query as user types
                uiState = uiState.copy(searchQuery = event.query)
            }
            
            ScreenAEvent.OnSearch -> {
                // NEW: Handle search button click or search submission
                // Cancel any ongoing request and start fresh search
                if (uiState.searchQuery.isNotBlank()) {
                    paginationJob?.cancel() // Cancel previous search
                    currentPage = 1 // Reset to first page
                    hasMoreData = true // Reset pagination
                    uiState = uiState.copy(
                        isSearching = true,
                        data = emptyList(), // Clear old results
                        paginationError = null
                    )
                    getMoviesData(1, isLoadMore = false)
                }
            }
            
            ScreenAEvent.LoadMore -> {
                // IMPROVEMENT: Use Job instead of boolean for better control
                // BEFORE: if (!isLoadingInProgress && hasMoreData && !uiState.isLoadingMore)
                // ISSUE: Boolean can't cancel requests, just prevents new ones
                // NOW: Check if Job is active (null or completed means we can load more)
                // BENEFIT: More accurate state tracking and can cancel if needed
                
                if (paginationJob?.isActive != true && hasMoreData && !uiState.isLoadingMore) {
                    uiState = uiState.copy(isLoadingMore = true, paginationError = null)
                    getMoviesData(currentPage + 1, true)
                }
            }
        }
    }
}


data class ScreenAUiState(
    val screenState: ScreenState = ScreenState.DEFAULT,
    val isLoadingMore: Boolean = false,
    val isSearching: Boolean = false, // NEW: Track if initial search is in progress
    val searchQuery: String = "", // NEW: User's search query from text field
    val data: List<MovieItem> = emptyList(),
    val paginationError: String? = null // Added to show pagination errors to user
)

enum class ScreenState {
    LOADING,
    EMPTY,
    DEFAULT
}

data class MovieItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String
)

sealed interface ScreenAEvent {
    data class OnSearchQueryChange(val query: String) : ScreenAEvent // NEW: User typing in search box
    data object OnSearch : ScreenAEvent // NEW: User submits search
    data object LoadMore : ScreenAEvent
}

sealed interface ScreenASideEffect {
    data object None : ScreenASideEffect
    data object OnBack : ScreenASideEffect
}
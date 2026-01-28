package com.abundance.naivety.viewmodels

import com.abundance.naivety.models.SearchState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.repository.BrowseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

// app/src/main/java/com/abundance/naivety/viewmodels/SearchViewModel.kt

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: BrowseRepository
) : ViewModel() {
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState = _searchState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<OpenLibraryBook>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private var searchJob: Job? = null

    fun searchBooks(query: String) {
        // Cancel any previous search job to avoid race conditions
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchState.value = SearchState.Idle
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            _searchState.value = SearchState.Searching

            // Debounce to prevent excessive API calls while typing
            delay(300)

            try {
                val results = repository.searchBooks(query)

                _searchResults.value = results
                _searchState.value = if (results.isEmpty()) {
                    SearchState.NoResults
                } else {
                    SearchState.Success(query)
                }
            } catch (e: HttpException) {
                val isServerError = e.code() in 500..599
                val errorMessage = when (e.code()) {
                    500, 502, 503 -> "Open Library servers are experiencing issues"
                    429 -> "Too many requests. Please wait a moment"
                    else -> "Search failed (Error ${e.code()})"
                }
                _searchState.value = SearchState.Error(errorMessage, isServerError)
                _searchResults.value = emptyList()
            } catch (e: IOException) {
                _searchState.value = SearchState.Error("Network error. Check your connection.", false)
                _searchResults.value = emptyList()
            } catch (e: Exception) {
                _searchState.value = SearchState.Error("Something went wrong", false)
                _searchResults.value = emptyList()
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchState.value = SearchState.Idle
        _searchResults.value = emptyList()
    }
}
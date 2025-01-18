package com.example.naivety.viewmodels

import SearchState
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naivety.models.OpenLibraryBook
import com.example.naivety.repository.BrowseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// app/src/main/java/com/example/naivety/viewmodels/SearchViewModel.kt

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: BrowseRepository
) : ViewModel() {
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState = _searchState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<OpenLibraryBook>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    fun searchBooks(query: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Searching
            try {
                Log.d("SearchViewModel", "Searching for: $query")
                val results = repository.searchBooks(query)
                Log.d("SearchViewModel", "Found ${results.size} results")

                _searchResults.value = results
                _searchState.value = if (results.isEmpty()) {
                    SearchState.NoResults
                } else {
                    SearchState.Success(query)
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search error: ${e.message}")
                _searchState.value = SearchState.Error
                _searchResults.value = emptyList()
            }
        }
    }

    fun clearSearch() {
        _searchState.value = SearchState.Idle
        _searchResults.value = emptyList()
    }
}
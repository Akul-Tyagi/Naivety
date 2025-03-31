package com.abundance.naivety.viewmodels

import com.abundance.naivety.models.SearchState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.repository.BrowseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    fun searchBooks(query: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Searching
            try {
                val results = repository.searchBooks(query)

                _searchResults.value = results
                _searchState.value = if (results.isEmpty()) {
                    SearchState.NoResults
                } else {
                    SearchState.Success(query)
                }
            } catch (e: Exception) {
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
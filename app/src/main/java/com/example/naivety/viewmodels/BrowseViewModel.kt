// BrowseViewModel.kt
package com.example.naivety.viewmodels

import SearchState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.naivety.data.BookPagingSource
import com.example.naivety.models.OpenLibraryBook
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import com.example.naivety.repository.BrowseRepository
import kotlinx.coroutines.launch

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: BrowseRepository
) : ViewModel() {
    private val bookCache = mutableMapOf<String, OpenLibraryBook>()

    private val _selectedBook = MutableStateFlow<OpenLibraryBook?>(null)
    val selectedBook = _selectedBook.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState = _searchState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<OpenLibraryBook>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _currentQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            // Prefetch some books
            repository.getTrendingBooks().forEach { book ->
                bookCache[book.key] = book
            }
        }
    }

    fun getBookFromCache(key: String): OpenLibraryBook? = bookCache[key]

    val books = _currentQuery
        .flatMapLatest { query ->
            repository.getRecommendedBooks(query)
        }
        .cachedIn(viewModelScope)

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


    fun onBookLongPressed(book: OpenLibraryBook) {
        _selectedBook.value = book
    }

    fun clearSelectedBook() {
        _selectedBook.value = null
    }
}
// BrowseViewModel.kt
package com.example.naivety.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
    private val _selectedBook = MutableStateFlow<OpenLibraryBook?>(null)
    val selectedBook = _selectedBook.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val books = searchQuery
        .debounce(300L) // Add debounce to prevent too many API calls
        .flatMapLatest { query ->
            repository.getRecommendedBooks(query)
        }
        .cachedIn(viewModelScope)

    fun searchBooks(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
        }
    }

    fun onBookLongPressed(book: OpenLibraryBook) {
        _selectedBook.value = book
    }

    fun clearSelectedBook() {
        _selectedBook.value = null
    }
}
// BrowseViewModel.kt
package com.abundance.naivety.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.abundance.naivety.models.OpenLibraryBook
import com.abundance.naivety.models.SearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import com.abundance.naivety.repository.BrowseRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: BrowseRepository
) : ViewModel() {
    private val _booksState = MutableStateFlow<List<OpenLibraryBook>>(emptyList())
    val booksState = _booksState.asStateFlow()
    private val bookCache = mutableMapOf<String, OpenLibraryBook>()

    private val _selectedBook = MutableStateFlow<OpenLibraryBook?>(null)
    val selectedBook = _selectedBook.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState = _searchState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<OpenLibraryBook>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _currentQuery = MutableStateFlow("")

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    private var currentPage = 1
    private var hasMoreData = true

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading = _isInitialLoading.asStateFlow()

    init {
        loadInitialBooks()
    }

    private fun loadInitialBooks() {
        viewModelScope.launch {
            _isInitialLoading.value = true
            try {
                // Use getBestsellerBooks() instead of getTrendingBooks()
                val initialBooks = repository.getBestsellerBooks()
                _booksState.value = initialBooks

                initialBooks.forEach { book ->
                    bookCache[book.key] = book
                }
            } catch (e: Exception) {
                Log.e("BrowseViewModel", "Error loading initial books", e)
            } finally {
                _isInitialLoading.value = false
            }
        }
    }

    fun getBookFromCache(key: String): OpenLibraryBook? = bookCache[key]

    // In BrowseViewModel.kt, add this function:
    fun isBookInAnyList(bookKey: String): Flow<Boolean> {
        return repository.isBookInAnyList(bookKey)
    }

    // Keep the original books flow for compatibility
    @OptIn(ExperimentalCoroutinesApi::class)
    val books = _currentQuery
        .flatMapLatest { query ->
            repository.getRecommendedBooks(query)
        }
        .cachedIn(viewModelScope)

    fun loadMoreBooks() {
        if (_isLoadingMore.value || !hasMoreData) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val query = _currentQuery.value
                val newBooks = repository.getBooksByPage(query, currentPage + 1)

                if (newBooks.isEmpty()) {
                    hasMoreData = false
                } else {
                    // Update current book list with new books
                    val currentBooks = _booksState.value
                    _booksState.value = currentBooks + newBooks

                    // Save to cache for future reference
                    newBooks.forEach { book ->
                        bookCache[book.key] = book
                    }

                    currentPage++
                }
            } catch (e: Exception) {
                // Handle error
                Log.e("BrowseViewModel", "Error loading more books", e)
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    // Modify searchBooks function
    private var searchJob: Job? = null

    fun searchBooks(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchState.value = SearchState.Searching

            // Debounce typing
            delay(300)

            currentPage = 1
            try {
                val results = repository.getBooksByPage(query, 1)
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
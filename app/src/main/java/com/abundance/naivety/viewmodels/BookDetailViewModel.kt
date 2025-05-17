package com.abundance.naivety.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.asStateFlow
import com.abundance.naivety.network.models.OpenLibraryBookDetail
import com.abundance.naivety.repository.BrowseRepository

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val repository: BrowseRepository
) : ViewModel() {

    private val _bookDetails = MutableStateFlow<OpenLibraryBookDetail?>(null)
    val bookDetails = _bookDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _userRating = MutableStateFlow<Float>(0f)
    val userRating: StateFlow<Float> = _userRating.asStateFlow()

    private val _bookComments = MutableStateFlow<List<BookComment>>(emptyList())
    val bookComments: StateFlow<List<BookComment>> = _bookComments.asStateFlow()

    private val _averageRating = MutableStateFlow<Float>(0f)
    val averageRating: StateFlow<Float> = _averageRating.asStateFlow()

    private val _ratingsCount = MutableStateFlow<Int>(0)
    val ratingsCount: StateFlow<Int> = _ratingsCount.asStateFlow()

    private val _isBookInAnyList = MutableStateFlow(false)
    val isBookInAnyList: StateFlow<Boolean> = _isBookInAnyList

    private fun loadUserRating(bookKey: String) {
        viewModelScope.launch {
            try {
                // In a real app, this would load from local database or API
                // For now, we're just setting a default value
                _userRating.value = 0f
            } catch (e: Exception) {
                // Silently fail, we'll just show 0 stars
            }
        }
    }

    fun loadBookDetails(bookKey: String) {
        val cleanBookKey = bookKey.removePrefix("/works/")

        _bookDetails.value?.let { currentDetails ->
            if (currentDetails.key == cleanBookKey) return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val details = repository.getBookDetails(cleanBookKey)
                _bookDetails.value = details

                // Ensure ratings are properly set
                _averageRating.value = details.averageRating ?: 0f
                _ratingsCount.value = details.ratings_count ?: 0

                // Load user rating if available
                loadUserRating(cleanBookKey)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load book details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRating(rating: Float) {
        viewModelScope.launch {
            try {
                _userRating.value = rating
                // Here you would typically make an API call to save the rating
                // repository.saveRating(bookDetails.value?.key ?: "", rating)
            } catch (e: Exception) {
                _error.value = "Failed to update rating"
            }
        }
    }
    fun clearError() {
        _error.value = null
    }
}

// Data class for comments
data class BookComment(
    val id: String,
    val text: String,
    val timestamp: Long,
    val userId: String
)
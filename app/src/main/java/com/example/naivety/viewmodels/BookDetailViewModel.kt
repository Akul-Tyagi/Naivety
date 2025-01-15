package com.example.naivety.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.asStateFlow
import OpenLibraryBookDetail
import BrowseRepository

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

    fun loadBookDetails(bookKey: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Remove the "/works/" prefix if it exists in the bookKey
                val cleanBookKey = bookKey.removePrefix("/works/")
                val details = repository.getBookDetails(cleanBookKey) // Use the repository function instead
                _bookDetails.value = details

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

    fun addComment(comment: String) {
        viewModelScope.launch {
            try {
                val currentComments = _bookComments.value.toMutableList()
                currentComments.add(
                    BookComment(
                        id = System.currentTimeMillis().toString(),
                        text = comment,
                        timestamp = System.currentTimeMillis(),
                        userId = "current_user" // Replace with actual user ID
                    )
                )
                _bookComments.value = currentComments
                // Here you would typically make an API call to save the comment
                // repository.saveComment(bookDetails.value?.key ?: "", comment)
            } catch (e: Exception) {
                _error.value = "Failed to add comment"
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            try {
                val currentComments = _bookComments.value.toMutableList()
                currentComments.removeAll { it.id == commentId }
                _bookComments.value = currentComments
                // Here you would typically make an API call to delete the comment
                // repository.deleteComment(bookDetails.value?.key ?: "", commentId)
            } catch (e: Exception) {
                _error.value = "Failed to delete comment"
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
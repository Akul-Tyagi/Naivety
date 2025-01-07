package com.example.naivety.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.naivety.models.Book
import com.example.naivety.ui.screens.SortOrder
import com.example.naivety.utils.PdfThumbnailHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Here you would typically load books from your database
                // For now, we'll just load from SharedPreferences or similar storage

            } catch (e: Exception) {
                emitError("Failed to load books: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addBook(uri: Uri, title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val thumbnailPath = PdfThumbnailHelper.generateThumbnail(
                    getApplication(),
                    uri
                )

                val newBook = Book(
                    title = title,
                    filePath = uri.toString(),
                    thumbnailPath = thumbnailPath
                )

                _books.value = _books.value + newBook
                saveBooks()

            } catch (e: Exception) {
                emitError("Failed to add book: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBookProgress(bookId: String, page: Int, position: Float) {
        viewModelScope.launch {
            try {
                _books.value = _books.value.map { book ->
                    if (book.id == bookId) {
                        book.copy(
                            lastReadPage = page,
                            lastReadPosition = position
                        )
                    } else book
                }
                saveBooks()
            } catch (e: Exception) {
                emitError("Failed to update progress: ${e.message}")
            }
        }
    }

    fun sortBooks(sortOrder: SortOrder) {
        viewModelScope.launch {
            val sortedBooks = when (sortOrder) {
                SortOrder.RECENT -> _books.value.sortedByDescending { it.dateAdded }
                SortOrder.TITLE -> _books.value.sortedBy { it.title }
                SortOrder.AUTHOR -> _books.value.sortedBy { it.title } // Since we don't have author field, fallback to title
            }
            _books.value = sortedBooks
        }
    }

    private suspend fun emitError(message: String) {
        _errorMessage.emit(message)
    }

    private fun saveBooks() {
        // Here you would typically save to your database
        // For now, you could use SharedPreferences or similar storage
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
}
package com.example.naivety.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.naivety.data.AppDatabase
import com.example.naivety.models.Book
import com.example.naivety.ui.screens.SortOrder
import com.example.naivety.utils.PdfThumbnailHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val bookDao = AppDatabase.getDatabase(application).bookDao()
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
                // First emit empty list to clear any stale data
                _books.value = emptyList()

                // Collect books from database
                bookDao.getAllBooks()
                    .catch { e ->
                        emitError("Failed to load books: ${e.message}")
                        _isLoading.value = false
                    }
                    .collect { bookList ->
                        _books.value = bookList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                emitError("Failed to load books: ${e.message}")
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

                val totalPages = getTotalPages(uri)

                val newBook = Book(
                    title = title,
                    filePath = uri.toString(),
                    thumbnailPath = thumbnailPath,
                    totalPages = totalPages,
                    lastReadPage = 0,
                    dateAdded = System.currentTimeMillis()
                )

                bookDao.insertBook(newBook)
                // Force refresh the books list
                loadBooks()
            } catch (e: Exception) {
                emitError("Failed to add book: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getTotalPages(uri: Uri): Int = withContext(Dispatchers.IO) {
        try {
            getApplication<Application>().contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    return@withContext renderer.pageCount
                }
            } ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 0
        }
    }


    fun getPagesRemaining(book: Book): Int {
        return (book.totalPages - book.lastReadPage).coerceAtLeast(0)
    }

    fun getReadingProgress(book: Book): Float {
        if (book.totalPages == 0) return 0f
        return book.lastReadPage.toFloat() / book.totalPages.toFloat()
    }

    fun sortBooks(sortOrder: SortOrder) {
        viewModelScope.launch {
            val sortedBooks = when (sortOrder) {
                SortOrder.RECENT -> _books.value.sortedByDescending { it.dateAdded }
                SortOrder.TITLE -> _books.value.sortedBy { it.title }
                SortOrder.AUTHOR -> _books.value.sortedBy { it.author ?: it.title }
                SortOrder.PROGRESS -> _books.value.sortedByDescending { getReadingProgress(it) }
            }
            _books.value = sortedBooks
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            try {
                // Delete thumbnail file
                book.thumbnailPath.let { path ->
                    File(path).takeIf { it.exists() }?.delete()
                }

                // Delete from database
                bookDao.deleteBook(book)

                // Refresh books list
                loadBooks()
            } catch (e: Exception) {
                emitError("Failed to delete book: ${e.message}")
            }
        }
    }

    private suspend fun emitError(message: String) {
        _errorMessage.emit(message)
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
}
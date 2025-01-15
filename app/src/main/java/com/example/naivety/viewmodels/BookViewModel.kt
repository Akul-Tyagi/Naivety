package com.example.naivety.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.naivety.data.AppDatabase
import com.example.naivety.models.Book
import com.example.naivety.utils.PdfThumbnailHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.example.naivety.models.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val bookDao = AppDatabase.getDatabase(application).bookDao()
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                bookDao.getAllBooks()
                    .catch { e ->
                        _isLoading.value = false
                        e.printStackTrace()
                    }
                    .collect { bookList ->
                        _books.value = bookList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                e.printStackTrace()
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
                loadBooks() // Reload books after adding new one
            } catch (e: Exception) {
                e.printStackTrace()
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
            val currentBooks = _books.value.toMutableList()
            when (sortOrder) {
                SortOrder.RECENT -> currentBooks.sortByDescending { it.dateAdded }
                SortOrder.TITLE -> currentBooks.sortBy { it.title.lowercase() }
                SortOrder.PROGRESS -> currentBooks.sortByDescending { it.lastReadPage.toFloat() / it.totalPages }
                SortOrder.AUTHOR -> currentBooks.sortBy { it.author?.lowercase() }
            }
            _books.value = currentBooks
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
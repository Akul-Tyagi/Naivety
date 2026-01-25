// app/src/main/java/com/abundance/naivety/viewmodels/BookViewModel.kt
package com.abundance.naivety.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abundance.naivety.data.AppDatabase
import com.abundance.naivety.epub.ReadiumManager
import com.abundance.naivety.models.Book
import com.abundance.naivety.models.SortOrder
import com.abundance.naivety.utils.BookFileType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.toString

@HiltViewModel
class BookViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val readiumManager: ReadiumManager
) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentSortOrder = MutableStateFlow(SortOrder.RECENT)
    val currentSortOrder: StateFlow<SortOrder> = _currentSortOrder.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            database.bookDao().getAllBooks().collect { bookList ->
                _books.value = sortBookList(bookList, _currentSortOrder.value)
            }
        }
    }

    fun addBook(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("BookViewModel", "addBook called with uri: $uri")
            try {
                val fileType = getFileType(uri)
                Log.d("BookViewModel", "Detected file type: $fileType")
                when (fileType) {
                    BookFileType.PDF -> {
                        Log.d("BookViewModel", "Adding PDF book...")
                        addPdfBook(uri)
                    }
                    BookFileType.EPUB -> {
                        Log.d("BookViewModel", "Adding EPUB book...")
                        addEpubBook(uri)
                    }
                    BookFileType.UNKNOWN -> {
                        Log.e("BookViewModel", "Unknown file type for URI: $uri")
                    }
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error adding book", e)
            } finally {
                _isLoading.value = false
                Log.d("BookViewModel", "addBook completed, isLoading set to false")
            }
        }
    }

    private fun getFileType(uri: Uri): BookFileType {
        val mimeType = context.contentResolver.getType(uri)
        val fileName = getFileName(uri).lowercase()

        return when {
            mimeType == "application/epub+zip" -> BookFileType.EPUB
            mimeType == "application/pdf" -> BookFileType.PDF
            fileName.endsWith(".epub") -> BookFileType.EPUB
            fileName.endsWith(".pdf") -> BookFileType.PDF
            else -> BookFileType.PDF // Default to PDF
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "Unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private suspend fun addPdfBook(uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val fileName = getFileName(uri)
                val title = fileName.removeSuffix(".pdf").removeSuffix(".PDF")

                // Generate thumbnail
                val thumbnailPath = generatePdfThumbnail(uri)

                // Get page count
                val pageCount = getPdfPageCount(uri)

                val book = Book(
                    title = title,
                    filePath = uri.toString(),
                    thumbnailPath = thumbnailPath,
                    totalPages = pageCount,
                    fileType = BookFileType.PDF.name
                )

                database.bookDao().insertBook(book)
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error adding PDF book", e)
            }
        }
    }

    private suspend fun addEpubBook(uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val fileName = getFileName(uri)

                // Try to open with Readium to get metadata
                val result = readiumManager.openEpub(uri)
                val publication = result.getOrNull()

                val title = publication?.metadata?.title ?: fileName.removeSuffix(".epub")
                val author = publication?.metadata?.authors?.firstOrNull()?.name
                val pageCount = publication?.readingOrder?.size ?: 0

                // Generate thumbnail for EPUB
                val thumbnailPath = generateEpubThumbnail(uri, publication)

                val book = Book(
                    title = title,
                    author = author,
                    filePath = uri.toString(),
                    thumbnailPath = thumbnailPath,
                    totalPages = pageCount,
                    fileType = BookFileType.EPUB.name
                )

                database.bookDao().insertBook(book)
                readiumManager.closeCurrentPublication()
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error adding EPUB book", e)
            }
        }
    }

    private suspend fun generatePdfThumbnail(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                parcelFileDescriptor?.use { pfd ->
                    val renderer = PdfRenderer(pfd)
                    renderer.use { pdf ->
                        if (pdf.pageCount > 0) {
                            val page = pdf.openPage(0)
                            val bitmap = Bitmap.createBitmap(
                                page.width * 2,
                                page.height * 2,
                                Bitmap.Config.ARGB_8888
                            )
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            page.close()

                            val thumbnailFile = File(context.cacheDir, "thumb_${System.currentTimeMillis()}.png")
                            FileOutputStream(thumbnailFile).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                            }
                            bitmap.recycle()
                            return@withContext thumbnailFile.absolutePath
                        }
                    }
                }
                null
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error generating PDF thumbnail", e)
                null
            }
        }
    }

    private suspend fun generateEpubThumbnail(uri: Uri, publication: org.readium.r2.shared.publication.Publication?): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Try to get cover from publication
                publication?.let { pub ->
                    val coverLink = pub.linkWithRel("cover")
                        ?: pub.linkWithRel("cover-image")
                        ?: pub.resources.firstOrNull { it.mediaType?.toString()?.startsWith("image/") == true }


                    coverLink?.let { link ->
                        val resource = pub.get(link)
                        val bytes = resource?.read()?.getOrNull()
                        if (bytes != null && bytes.isNotEmpty()) {
                            val thumbnailFile = File(context.cacheDir, "thumb_${System.currentTimeMillis()}.png")
                            thumbnailFile.writeBytes(bytes)
                            return@withContext thumbnailFile.absolutePath
                        }
                    }
                }
                null
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error generating EPUB thumbnail", e)
                null
            }
        }
    }

    private fun getPdfPageCount(uri: Uri): Int {
        return try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            parcelFileDescriptor?.use { pfd ->
                val renderer = PdfRenderer(pfd)
                renderer.use { pdf ->
                    pdf.pageCount
                }
            } ?: 0
        } catch (e: Exception) {
            Log.e("BookViewModel", "Error getting PDF page count", e)
            0
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            try {
                // Delete thumbnail file if exists
                book.thumbnailPath?.let { path ->
                    try {
                        File(path).delete()
                    } catch (e: Exception) {
                        Log.e("BookViewModel", "Error deleting thumbnail", e)
                    }
                }
                database.bookDao().deleteBook(book)
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error deleting book", e)
            }
        }
    }

    fun sortBooks(sortOrder: SortOrder) {
        _currentSortOrder.value = sortOrder
        _books.value = sortBookList(_books.value, sortOrder)
    }

    private fun sortBookList(books: List<Book>, sortOrder: SortOrder): List<Book> {
        return when (sortOrder) {
            SortOrder.RECENT -> books.sortedByDescending { it.lastOpened ?: it.dateAdded }
            SortOrder.TITLE -> books.sortedBy { it.title.lowercase() }
            SortOrder.AUTHOR -> books.sortedBy { it.author?.lowercase() ?: "zzz" }
            SortOrder.PROGRESS -> books.sortedByDescending {
                if (it.totalPages > 0) it.lastReadPage.toFloat() / it.totalPages else 0f
            }}
    }

    fun updateReadingProgress(bookId: Long, page: Int, position: Float = 0f) {
        viewModelScope.launch {
            database.bookDao().updateReadingProgress(bookId.toString(), page, position)
        }
    }

    fun getReadingProgress(book: Book): Float {
        return if (book.totalPages > 0) {
            book.lastReadPage.toFloat() / book.totalPages.toFloat()
        } else {
            0f
        }
    }

    fun getPagesRemaining(book: Book): Int {
        return maxOf(0, book.totalPages - book.lastReadPage)
    }

    fun updateBookTitle(book: Book, newTitle: String) {
        viewModelScope.launch {
            val updatedBook = book.copy(title = newTitle)
            database.bookDao().insertBook(updatedBook)
        }
    }
}

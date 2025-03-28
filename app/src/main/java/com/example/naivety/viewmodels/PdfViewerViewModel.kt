package com.example.naivety.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naivety.data.AppDatabase
import com.example.naivety.data.Bookmark
import com.example.naivety.models.Book
import com.example.naivety.repository.BookRepository
import com.example.naivety.ui.pdf.*
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.naivety.repository.ReadingStatsRepository
import com.example.naivety.data.ReadingDay

@HiltViewModel
class PdfViewerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookRepository: BookRepository,
    private val readingStatsRepository: ReadingStatsRepository
) : ViewModel() {
    private val bookmarkDao = bookRepository.bookmarkDao
    private val _viewerState = MutableStateFlow(PdfViewerState())
    val viewerState = _viewerState.asStateFlow()
    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks = _bookmarks.asStateFlow()
    private val _isCurrentPageBookmarked = MutableStateFlow(false)
    val isCurrentPageBookmarked = _isCurrentPageBookmarked.asStateFlow()
    private var isDocumentLoaded = false
    private var currentBookId: String? = null

    init {
        loadSavedSettings(null)
    }

    // Add to PdfViewerViewModel
    private var sessionStartTime: Long = System.currentTimeMillis()
    private var initialPage: Int = 0

    fun startReadingSession(page: Int) {
        sessionStartTime = System.currentTimeMillis()
        initialPage = page
    }

    // In PdfViewerViewModel, update the endReadingSession method:
    fun endReadingSession(currentPage: Int, bookId: String) {
        val sessionTimeMinutes = ((System.currentTimeMillis() - sessionStartTime) / 1000 / 60).toInt()
        val pagesRead = (currentPage - initialPage).coerceAtLeast(0)

        // Only record if some time was spent or pages read
        if (sessionTimeMinutes > 0 || pagesRead > 0) {
            viewModelScope.launch {
                // Use logReadingSession instead of addReadingDay
                readingStatsRepository.logReadingSession(
                    bookId = bookId,
                    pagesRead = pagesRead,
                    timeSpentMinutes = sessionTimeMinutes
                )
            }
        }
    }

    fun initializePdfViewer() {
        viewModelScope.launch {
            isDocumentLoaded = false
            _viewerState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    currentPage = 0,
                    totalPages = 1,
                    isControlsVisible = false
                )
            }
        }
    }

    fun updateLoadingState(isLoading: Boolean) {
        viewModelScope.launch {
            _viewerState.update { currentState ->
                currentState.copy(
                    isLoading = isLoading,
                    isControlsVisible = if (isLoading) false else currentState.isControlsVisible
                )
            }
        }
    }


    fun updatePageCount(current: Int, total: Int, bookId: String?) {
        viewModelScope.launch {
            _viewerState.update { currentState ->
                currentState.copy(
                    currentPage = current,
                    totalPages = total.coerceAtLeast(1)
                )
            }
            bookId?.let { id ->
                updateCurrentPageBookmarkStatus(id, current)
            }
        }
    }

    fun toggleControls() {
        _viewerState.update { currentState ->
            currentState.copy(isControlsVisible = !currentState.isControlsVisible)
        }
    }

    fun updateSettings(settings: PdfSettings) {
        viewModelScope.launch {
            _viewerState.update { currentState ->
                currentState.copy(settings = settings)
            }
        }
    }

    fun updateReadingMode(mode: ReadingMode) {
        _viewerState.update { currentState ->
            currentState.copy(readingMode = mode)
        }
    }

    fun updateRotation(mode: RotationMode) {
        _viewerState.update { currentState ->
            currentState.copy(rotation = mode)
        }
    }

    fun updateBrightness(settings: BrightnessSettings) {
        _viewerState.update { currentState ->
            currentState.copy(brightness = settings)
        }
    }

    fun onPdfLoadComplete(pageCount: Int) {
        viewModelScope.launch {
            isDocumentLoaded = true
            _viewerState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    totalPages = pageCount.coerceAtLeast(1)
                )
            }
        }
    }

    fun onPdfLoadError() {
        viewModelScope.launch {
            isDocumentLoaded = false
            _viewerState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    isControlsVisible = true,
                    totalPages = 1
                )
            }
        }
    }

    fun updateCurrentPageBookmarkStatus(bookId: String, page: Int) {
        viewModelScope.launch {
            _isCurrentPageBookmarked.value = bookmarkDao.isPageBookmarked(bookId, page)
        }
    }

    fun addBookmark(bookId: String, page: Int) {
        viewModelScope.launch {
            try {
                val bookmark = Bookmark(
                    bookId = bookId,
                    page = page,
                    dateCreated = System.currentTimeMillis()
                )
                bookmarkDao.addBookmark(bookmark)

                // Refresh bookmarks list
                _bookmarks.value = bookmarkDao.getBookmarks(bookId)
                _isCurrentPageBookmarked.value = true
            } catch (e: Exception) {
            }
        }
    }

    fun removeBookmark(bookId: String, page: Int) {
        viewModelScope.launch {
            try {
                bookmarkDao.getBookmarkAtPage(bookId, page)?.let { bookmark ->
                    bookmarkDao.removeBookmark(bookmark)

                    // Refresh bookmarks list
                    _bookmarks.value = bookmarkDao.getBookmarks(bookId)
                    _isCurrentPageBookmarked.value = false
                }
            } catch (e: Exception) {
            }
        }
    }

    fun loadBookmarks(bookId: String) {
        currentBookId = bookId
        viewModelScope.launch {
            try {
                // Load bookmarks from database
                _bookmarks.value = bookmarkDao.getBookmarks(bookId)

                // Update current page bookmark status if we have a current page
                _viewerState.value.currentPage.let { currentPage ->
                    updateCurrentPageBookmarkStatus(bookId, currentPage)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun updatePage(bookId: String, page: Int, position: Float) {
        viewModelScope.launch {
            bookRepository.updateReadingProgress(bookId, page, position)
            _viewerState.update { it.copy(currentPage = page) }
        }
    }

    fun saveSettings(bookId: String?) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences(
                "pdf_viewer_settings_$bookId",
                Context.MODE_PRIVATE
            )
            with(prefs.edit()) {
                putBoolean("night_mode", viewerState.value.brightness.nightMode)
                putBoolean("system_brightness", viewerState.value.brightness.useSystemBrightness)
                putFloat("custom_brightness", viewerState.value.brightness.customBrightness)
                putBoolean("keep_screen_on", viewerState.value.settings.keepScreenOn)
                putBoolean("show_page_number", viewerState.value.settings.showPageNumber)
                putString("reading_mode", viewerState.value.readingMode.name)
                putString("rotation_mode", viewerState.value.rotation.name)
                apply()
            }
        }
    }

    private fun loadSavedSettings(bookId: String?) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences(
                "pdf_viewer_settings_$bookId",
                Context.MODE_PRIVATE
            )
            _viewerState.update { currentState ->
                currentState.copy(
                    brightness = currentState.brightness.copy(
                        nightMode = prefs.getBoolean("night_mode", false),
                        useSystemBrightness = prefs.getBoolean("system_brightness", true),
                        customBrightness = prefs.getFloat("custom_brightness", 0.5f)
                    ),
                    settings = currentState.settings.copy(
                        keepScreenOn = prefs.getBoolean("keep_screen_on", false),
                        showPageNumber = prefs.getBoolean("show_page_number", true)
                    ),
                    readingMode = prefs.getString("reading_mode", null)?.let {
                        ReadingMode.valueOf(it)
                    } ?: ReadingMode.VERTICAL_PAGED,
                    rotation = prefs.getString("rotation_mode", null)?.let {
                        RotationMode.valueOf(it)
                    } ?: RotationMode.PORTRAIT
                )
            }
        }
    }

    private fun handleFullscreenSettings(activity: Activity, isFullscreen: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, !isFullscreen)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            if (isFullscreen) {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    fun cleanup() {
        viewModelScope.launch {
            isDocumentLoaded = false
            _viewerState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    isControlsVisible = false
                )
            }
        }
    }
}
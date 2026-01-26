package com.abundance.naivety

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.abundance.naivety.data.AppDatabase
import com.abundance.naivety.data.Bookmark
import com.abundance.naivety.epub.*
import com.abundance.naivety.ui.components.epub.modals.*
import com.abundance.naivety.ui.components.pdf.*
import com.abundance.naivety.ui.components.pdf.modals.EditBookNameDialog
import com.abundance.naivety.ui.components.pdf.modals.RotationSheet
import com.abundance.naivety.ui.components.pdf.QuickNavScroll
import com.abundance.naivety.ui.components.pdf.TopBar
import com.abundance.naivety.ui.pdf.RotationMode
import com.abundance.naivety.ui.theme.NaivetyTheme
import com.abundance.naivety.ui.theme.TransparentSystemBars
import com.abundance.naivety.viewmodels.ReadingStatsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Url
import java.io.ByteArrayInputStream
import javax.inject.Inject

@AndroidEntryPoint
class EpubReaderActivity : ComponentActivity() {

    @Inject lateinit var readiumManager: ReadiumManager
    @Inject lateinit var database: AppDatabase

    private val readingStatsViewModel: ReadingStatsViewModel by viewModels()

    private var bookId: Long = 0L
    private var publication: Publication? = null
    private var startTime: Long = 0L
    private var startChapter: Int = 0
    private var startPageInChapter: Int = 0
    private var epubName: String = "EPUB"
    private var actualTotalPages: Int = 0

    // Session tracking for accurate reading stats
    private var sessionStartChapter: Int = 0 // Chapter when session started
    private var highestChapterReached: Int = 0 // Highest chapter reached (for forward progress)
    private var lastActiveTime: Long = 0L // For detecting idle time
    private var totalActiveReadingTime: Long = 0L // Accumulated active reading time in ms
    private val IDLE_THRESHOLD_MS = 120_000L // 2 minutes idle = not actively reading

    private val _viewerState = MutableStateFlow(EpubViewerState())
    private val viewerState: StateFlow<EpubViewerState> = _viewerState.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    private val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    private var webView: WebView? = null
    private lateinit var preferencesManager: EpubPreferencesManager

    // Chapter preloading cache for smooth transitions
    private val preloadedChapters = mutableMapOf<Int, String>() // chapterIndex -> styledHtmlContent
    private var preloadJob: kotlinx.coroutines.Job? = null

    // JavaScript interface for WebView communication
    inner class EpubJsInterface {
        @android.webkit.JavascriptInterface
        fun onPageCountCalculated(totalPages: Int) {
            Log.d(TAG, "JS: Page count calculated: $totalPages")
            runOnUiThread {
                val state = _viewerState.value
                _viewerState.value = state.copy(
                    totalPages = totalPages.coerceAtLeast(1),
                    currentPage = state.currentPage.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
                )
                updateGlobalPageCount()
            }
        }

        @android.webkit.JavascriptInterface
        fun onPageChanged(newPage: Int) {
            Log.d(TAG, "JS: Page changed to: $newPage")
            runOnUiThread {
                val oldPage = _viewerState.value.currentPage
                _viewerState.value = _viewerState.value.copy(currentPage = newPage)
                updateGlobalPageDisplay()

                // Track active reading time (user interaction indicates active reading)
                trackActiveReading()
            }
        }

        @android.webkit.JavascriptInterface
        fun requestNextChapter() {
            Log.d(TAG, "JS: Requesting next chapter")
            runOnUiThread { goToNextChapter() }
        }

        @android.webkit.JavascriptInterface
        fun requestPreviousChapter() {
            Log.d(TAG, "JS: Requesting previous chapter")
            runOnUiThread { goToPreviousChapter() }
        }

        @android.webkit.JavascriptInterface
        fun onContinuousScrollInit(totalChapters: Int) {
            Log.d(TAG, "JS: Continuous scroll initialized with $totalChapters chapters")
            runOnUiThread {
                _viewerState.value = _viewerState.value.copy(
                    totalChapters = totalChapters,
                    isContinuousMode = true
                )
            }
        }

        @android.webkit.JavascriptInterface
        fun onChapterChanged(chapterIndex: Int) {
            Log.d(TAG, "JS: Chapter changed to: $chapterIndex")
            runOnUiThread {
                val state = _viewerState.value
                if (state.currentChapter != chapterIndex) {
                    // Track highest chapter reached (forward progress only)
                    if (chapterIndex > highestChapterReached) {
                        highestChapterReached = chapterIndex
                    }

                    // Track active reading on chapter change
                    trackActiveReading()

                    _viewerState.value = state.copy(currentChapter = chapterIndex)
                    if (bookId > 0L) {
                        updateBookmarkStatus(bookId, chapterIndex)
                    }
                    // Save progress when chapter changes
                    saveProgress(chapterIndex, 0)
                }
            }
        }

        @android.webkit.JavascriptInterface
        fun onChapterAppended(chapterIndex: Int) {
            Log.d(TAG, "JS: Chapter $chapterIndex appended successfully")
        }

        @android.webkit.JavascriptInterface
        fun requestPreloadNextChapter() {
            Log.d(TAG, "JS: Requesting preload of next chapter")
            runOnUiThread { preloadNextChapter() }
        }

        @android.webkit.JavascriptInterface
        fun onScrollNearEnd(currentChapterIndex: Int, scrollPercent: Float) {
            Log.d(TAG, "JS: Near end of chapter $currentChapterIndex, scroll: ${scrollPercent}%")
            // Preload next chapter when user is 80% through current chapter
            if (scrollPercent >= 80f) {
                runOnUiThread { preloadNextChapter() }
            }
        }

        @android.webkit.JavascriptInterface
        fun onNearLastPage(currentPage: Int, totalPages: Int) {
            Log.d(TAG, "JS: Near last page: $currentPage/$totalPages")
            // Preload next chapter when user reaches last 2 pages
            if (totalPages > 0 && (totalPages - currentPage) <= 2) {
                runOnUiThread {
                    publication?.let { pub ->
                        preloadNextChapterForPaginated(pub, _viewerState.value.currentChapter + 1)
                    }
                }
            }
        }

        @android.webkit.JavascriptInterface
        fun log(message: String) {
            Log.d(TAG, "JS: $message")
        }
    }

    companion object {
        private const val TAG = "EpubReaderActivity"
        private const val EXTRA_BOOK_ID = "BOOK_ID"

        fun createIntent(context: Context, uri: Uri, bookId: Long): Intent {
            return Intent(context, EpubReaderActivity::class.java).apply {
                data = uri
                putExtra(EXTRA_BOOK_ID, bookId.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()

        preferencesManager = EpubPreferencesManager(this)
        // Parse bookId from String to Long
        val bookIdStr = intent.getStringExtra(EXTRA_BOOK_ID) ?: "0"
        bookId = bookIdStr.toLongOrNull() ?: 0L
        val uri = intent.data

        if (uri == null) {
            Log.e(TAG, "No URI provided")
            finish()
            return
        }

        startTime = System.currentTimeMillis()
        loadSavedSettings()

        lifecycleScope.launch {
            if (bookId > 0L) {
                val book = database.bookDao().getBookById(bookId)
                actualTotalPages = book?.totalPages ?: 0
                Log.d(TAG, "Loaded actual total pages from DB: $actualTotalPages")
            }
        }

        setContent {
            NaivetyTheme(darkTheme = true) {
                TransparentSystemBars(darkTheme = true)
                EpubReaderScreen(uri = uri, onBack = { finish() })
            }
        }
    }

    private fun setupWindow() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun EpubReaderScreen(uri: Uri, onBack: () -> Unit) {
        val context = LocalContext.current
        val state by viewerState.collectAsState()

        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        val showSettings = remember { mutableStateOf(false) }
        val showReadingMode = remember { mutableStateOf(false) }
        val showRotation = remember { mutableStateOf(false) }
        val showDisplaySettings = remember { mutableStateOf(false) }
        val showBookmarks = remember { mutableStateOf(false) }
        var showEditName by remember { mutableStateOf(false) }
        val bookIdStr = bookId.toString()
        var localEpubName by remember { mutableStateOf(preferencesManager.getSavedBookName(bookIdStr) ?: extractEpubName(uri, context)) }

        val backgroundColor = androidx.compose.ui.graphics.Color(state.displaySettings.pageTheme.backgroundColor)

        val currentBookmarks by bookmarks.collectAsState()

        // Load EPUB
        LaunchedEffect(uri) {
            loadEpub(uri, context) { pub, chapCount ->
                publication = pub
                val lastChapter = preferencesManager.loadLastChapter(bookIdStr)
                val lastPage = preferencesManager.loadLastPageInChapter(bookIdStr)
                val displayTotalPages = if (actualTotalPages > 0) actualTotalPages else chapCount

                _viewerState.value = _viewerState.value.copy(
                    totalChapters = chapCount,
                    currentChapter = lastChapter.coerceIn(0, chapCount - 1),
                    currentPage = lastPage,
                    globalTotalPages = displayTotalPages.coerceAtLeast(1),
                    globalPage = 1, // Will be recalculated when page count is determined
                    chapterPageCounts = List(chapCount) { 1 }
                )
                startChapter = lastChapter.coerceIn(0, chapCount - 1)
                startPageInChapter = lastPage

                // Initialize accurate session tracking
                sessionStartChapter = lastChapter.coerceIn(0, chapCount - 1)
                highestChapterReached = lastChapter.coerceIn(0, chapCount - 1)
                lastActiveTime = System.currentTimeMillis()
                totalActiveReadingTime = 0L

                epubName = pub.metadata.title ?: extractEpubName(uri, context)
                localEpubName = preferencesManager.getSavedBookName(bookIdStr) ?: epubName
                isLoading = false
                Log.d(TAG, "EPUB loaded: $chapCount chapters, starting at chapter $sessionStartChapter")
            }?.let { e ->
                error = e
                isLoading = false
            }
        }

        // Load bookmarks
        LaunchedEffect(bookId) {
            if (bookId > 0L) {
                loadBookmarks(bookId)
                updateBookmarkStatus(bookId, state.currentChapter)
            }
        }

        // Handle window settings
        LaunchedEffect(state.typographySettings.keepScreenOn) {
            if (state.typographySettings.keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
            when {
                isLoading -> PdfLoadingAnimation(modifier = Modifier.align(Alignment.Center))
                error != null -> ErrorDisplay(error!!, onBack, Modifier.align(Alignment.Center))
                publication != null -> {
                    EpubWebView(
                        publication = publication!!,
                        currentChapter = state.currentChapter,
                        displaySettings = state.displaySettings,
                        onTapLeft = { goToPreviousPage() },
                        onTapRight = { goToNextPage() },
                        onTapCenter = { toggleControls() },
                        onSwipeLeft = { goToNextPage() },
                        onSwipeRight = { goToPreviousPage() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }


            // Page indicator
            PageIndicator(
                state = state,
                isVisible = state.typographySettings.showPageNumber && !state.isControlsVisible,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            )

            // Top bar
            TopBar(
                pdfName = localEpubName,
                isVisible = state.isControlsVisible,
                onEditClick = { showEditName = true },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Bottom navigation
            BottomNavigationBar(
                state = state,
                onReadingModeClick = { showReadingMode.value = true },
                onDisplaySettingsClick = { showDisplaySettings.value = true },
                onBookmarkClick = { toggleBookmark() },
                onBookmarkLongClick = { showBookmarks.value = true },
                onRotationClick = { showRotation.value = true },
                onSettingsClick = { showSettings.value = true },
                onChapterSelect = { chapter -> goToChapter(chapter, 0) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Dialogs and sheets
            Dialogs(
                showEditName = showEditName,
                localEpubName = localEpubName,
                onEditNameDismiss = { showEditName = false },
                onNameChange = { newName ->
                    localEpubName = newName
                    preferencesManager.saveBookName(bookIdStr, newName)
                    showEditName = false
                },
                showSettings = showSettings,
                state = state,
                onSettingsChange = { settings ->
                    _viewerState.value = _viewerState.value.copy(typographySettings = settings)
                    clearPreloadCache() // Clear cache when typography settings change
                    publication?.let { loadCurrentChapter(it, state.currentChapter) }
                    saveSettings()
                },
                showReadingMode = showReadingMode,
                onReadingModeSelect = { mode ->
                    val currentChapter = _viewerState.value.currentChapter
                    _viewerState.value = _viewerState.value.copy(
                        readingMode = mode,
                        currentPage = 0,
                        isContinuousMode = mode == EpubReadingMode.CONTINUOUS_VERTICAL
                    )
                    clearPreloadCache() // Clear cache when reading mode changes
                    publication?.let { loadCurrentChapter(it, currentChapter) }
                    saveSettings()
                    showReadingMode.value = false
                    toggleControls()
                },
                showRotation = showRotation,
                onRotationSelect = { mode ->
                    _viewerState.value = _viewerState.value.copy(rotation = mode)
                    requestedOrientation = getOrientationForMode(mode)
                    toggleControls()
                    showRotation.value = false
                },
                showDisplaySettings = showDisplaySettings,
                onDisplaySettingsChange = { settings ->
                    _viewerState.value = _viewerState.value.copy(displaySettings = settings)
                    window.attributes = window.attributes.apply {
                        screenBrightness = if (settings.useSystemBrightness) -1.0f else settings.customBrightness
                    }
                    clearPreloadCache() // Clear cache when display settings change
                    publication?.let { loadCurrentChapter(it, state.currentChapter) }
                    saveSettings()
                },
                showBookmarks = showBookmarks,
                currentBookmarks = currentBookmarks,
                currentChapter = state.currentChapter,
                onBookmarkNavigate = { chapter, page ->
                    goToChapter(chapter, page)
                    showBookmarks.value = false
                    toggleControls()
                },
                onBookmarkRemove = { bookmark ->
                    removeBookmark(bookmark)
                }
            )
        }
    }

    // --- Helper Composables ---

    @Composable
    private fun ErrorDisplay(error: String, onBack: () -> Unit, modifier: Modifier) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Error loading EPUB", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) { Text("Go Back") }
        }
    }

    @Composable
    private fun PageIndicator(state: EpubViewerState, isVisible: Boolean, modifier: Modifier) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = modifier
        ) {
            Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = RoundedCornerShape(20.dp)) {
                val displayText = when (state.readingMode) {
                    EpubReadingMode.PAGE_HORIZONTAL, EpubReadingMode.CONTINUOUS_HORIZONTAL -> {
                        // Paginated modes - show page count within current chapter only
                        val currentPage = (state.currentPage + 1).coerceAtLeast(1)
                        val totalPages = state.totalPages.coerceAtLeast(1)
                        val pagesLeft = (totalPages - currentPage).coerceAtLeast(0)
                        val chapterInfo = "Ch ${state.currentChapter + 1}"
                        "$currentPage/$totalPages • $pagesLeft left • $chapterInfo"
                    }
                    EpubReadingMode.CONTINUOUS_VERTICAL -> {
                        // Continuous vertical - show chapter progress with "Scroll freely" hint
                        val currentChapter = state.currentChapter + 1
                        val totalChapters = state.totalChapters.coerceAtLeast(1)
                        "Ch $currentChapter of $totalChapters"
                    }
                    EpubReadingMode.CHAPTER_SCROLL -> {
                        // Chapter scroll mode - show chapter with navigation hint
                        val currentChapter = state.currentChapter + 1
                        val totalChapters = state.totalChapters.coerceAtLeast(1)
                        val chaptersLeft = (totalChapters - currentChapter).coerceAtLeast(0)
                        "Ch $currentChapter/$totalChapters • $chaptersLeft left"
                    }
                }
                Text(displayText, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }

    @Composable
    private fun BottomNavigationBar(
        state: EpubViewerState,
        onReadingModeClick: () -> Unit,
        onDisplaySettingsClick: () -> Unit,
        onBookmarkClick: () -> Unit,
        onBookmarkLongClick: () -> Unit,
        onRotationClick: () -> Unit,
        onSettingsClick: () -> Unit,
        onChapterSelect: (Int) -> Unit,
        modifier: Modifier
    ) {
        Box(modifier = modifier.fillMaxWidth()) {
            QuickNavScroll(
                currentPage = state.currentChapter,
                totalPages = state.totalChapters,
                isVisible = state.isControlsVisible,
                onPageSelect = onChapterSelect,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 83.dp)
            )

            AnimatedVisibility(
                visible = state.isControlsVisible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.91f).padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onReadingModeClick, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.Book, "Reading Mode", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDisplaySettingsClick, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.BrightnessHigh, "Display", tint = MaterialTheme.colorScheme.primary)
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = { onBookmarkLongClick() },
                                        onTap = { onBookmarkClick() }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (state.isCurrentPageBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                "Bookmark",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onRotationClick, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.ScreenRotation, "Rotation", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onSettingsClick, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Dialogs(
        showEditName: Boolean, localEpubName: String, onEditNameDismiss: () -> Unit, onNameChange: (String) -> Unit,
        showSettings: MutableState<Boolean>, state: EpubViewerState, onSettingsChange: (EpubTypographySettings) -> Unit,
        showReadingMode: MutableState<Boolean>, onReadingModeSelect: (EpubReadingMode) -> Unit,
        showRotation: MutableState<Boolean>, onRotationSelect: (RotationMode) -> Unit,
        showDisplaySettings: MutableState<Boolean>, onDisplaySettingsChange: (EpubDisplaySettings) -> Unit,
        showBookmarks: MutableState<Boolean>, currentBookmarks: List<Bookmark>, currentChapter: Int,
        onBookmarkNavigate: (Int, Int) -> Unit, onBookmarkRemove: (Bookmark) -> Unit
    ) {
        if (showEditName) {
            EditBookNameDialog(currentName = localEpubName, onNameChange = onNameChange, onDismiss = onEditNameDismiss)
        }
        if (showSettings.value) {
            EpubSettingsSheet(settings = state.typographySettings, onSettingsChange = onSettingsChange, onDismiss = { showSettings.value = false })
        }
        if (showReadingMode.value) {
            EpubReadingModeSheet(currentMode = state.readingMode, onModeSelect = onReadingModeSelect, onDismiss = { showReadingMode.value = false; toggleControls() })
        }
        if (showRotation.value) {
            RotationSheet(currentMode = state.rotation, onModeSelect = onRotationSelect, onDismiss = { showRotation.value = false; toggleControls() })
        }
        if (showDisplaySettings.value) {
            EpubDisplaySettingsSheet(settings = state.displaySettings, onSettingsChange = onDisplaySettingsChange, onDismiss = { showDisplaySettings.value = false; toggleControls() })
        }
        if (showBookmarks.value) {
            EpubBookmarksSheet(
                bookmarks = currentBookmarks,
                currentChapter = currentChapter,
                onBookmarkClick = onBookmarkNavigate,
                onBookmarkRemove = onBookmarkRemove,
                onDismiss = { showBookmarks.value = false; toggleControls() }
            )
        }
    }

    // --- WebView ---

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    @Composable
    private fun EpubWebView(
        publication: Publication,
        currentChapter: Int,
        displaySettings: EpubDisplaySettings,
        onTapLeft: () -> Unit,
        onTapRight: () -> Unit,
        onTapCenter: () -> Unit,
        onSwipeLeft: () -> Unit,
        onSwipeRight: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webView = this
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    setupWebViewSettings()
                    addJavascriptInterface(EpubJsInterface(), "AndroidBridge")

                    val bgColor = String.format("#%06X", 0xFFFFFF and displaySettings.pageTheme.backgroundColor.toInt())
                    setBackgroundColor(Color.parseColor(bgColor))

                    webViewClient = createWebViewClient(publication)
                    setupGestureDetector(ctx, onTapLeft, onTapRight, onTapCenter, onSwipeLeft, onSwipeRight)
                    loadCurrentChapter(publication, currentChapter)
                }
            },
            update = { view ->
                val bgColor = String.format("#%06X", 0xFFFFFF and displaySettings.pageTheme.backgroundColor.toInt())
                view.setBackgroundColor(Color.parseColor(bgColor))
            },
            modifier = modifier
        )
    }

    private fun WebView.setupWebViewSettings() {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    private fun createWebViewClient(publication: Publication): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                _viewerState.value = _viewerState.value.copy(isLoading = false)
                Log.d(TAG, "Page finished loading: $url")
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val urlString = request?.url?.toString() ?: return null
                if (urlString.startsWith("data:") || urlString.startsWith("https://fonts.") || urlString.startsWith("http")) {
                    Log.d(TAG, "Skipping external/data URL: $urlString")
                    return null
                }

                Log.d(TAG, "Intercepting request: $urlString")
                return try {
                    val possiblePaths = EpubResourceLoader.buildPossiblePaths(urlString)
                    var loadedBytes: ByteArray? = null
                    var successPath: String? = null

                    for (path in possiblePaths) {
                        val url = Url(path)
                        if (url != null) {
                            val resource = publication.get(url)
                            val bytes = kotlinx.coroutines.runBlocking {
                                resource?.read()?.getOrNull()
                            }
                            if (bytes != null && bytes.isNotEmpty()) {
                                loadedBytes = bytes
                                successPath = path
                                break
                            }
                        }
                    }

                    if (loadedBytes != null && successPath != null) {
                        val mimeType = EpubResourceLoader.guessMimeTypeFromExtension(urlString)
                            .takeIf { it != "application/octet-stream" }
                            ?: EpubResourceLoader.guessMimeTypeFromBytes(loadedBytes)
                        Log.d(TAG, "Loaded resource: $urlString from $successPath, type: $mimeType, size: ${loadedBytes.size}")
                        WebResourceResponse(mimeType, "UTF-8", ByteArrayInputStream(loadedBytes))
                    } else {
                        Log.w(TAG, "Resource not found: $urlString")
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading resource: $urlString", e)
                    null
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun WebView.setupGestureDetector(
        ctx: Context,
        onTapLeft: () -> Unit,
        onTapRight: () -> Unit,
        onTapCenter: () -> Unit,
        onSwipeLeft: () -> Unit,
        onSwipeRight: () -> Unit
    ) {
        val minSwipeDistance = 100
        val minSwipeDistanceVertical = 80

        val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                val currentReadingMode = _viewerState.value.readingMode
                val screenWidth = width

                when (currentReadingMode) {
                    EpubReadingMode.PAGE_HORIZONTAL -> {
                        // Page by Page vertical mode - use tap zones for navigation
                        when {
                            e.x < screenWidth / 4 -> { Log.d(TAG, "Tap left - previous page"); goToPreviousPage() }
                            e.x > screenWidth * 3 / 4 -> { Log.d(TAG, "Tap right - next page"); goToNextPage() }
                            else -> { Log.d(TAG, "Tap center"); onTapCenter() }
                        }
                    }
                    EpubReadingMode.CONTINUOUS_HORIZONTAL -> {
                        // Page by Page horizontal mode - use tap zones for navigation
                        when {
                            e.x < screenWidth / 4 -> { Log.d(TAG, "Tap left - previous page"); goToPreviousPage() }
                            e.x > screenWidth * 3 / 4 -> { Log.d(TAG, "Tap right - next page"); goToNextPage() }
                            else -> { Log.d(TAG, "Tap center"); onTapCenter() }
                        }
                    }
                    EpubReadingMode.CHAPTER_SCROLL -> {
                        // Chapter scroll mode - use tap zones for chapter navigation
                        when {
                            e.x < screenWidth / 4 -> { Log.d(TAG, "Tap left - previous chapter"); goToPreviousChapter() }
                            e.x > screenWidth * 3 / 4 -> { Log.d(TAG, "Tap right - next chapter"); goToNextChapter() }
                            else -> { Log.d(TAG, "Tap center"); onTapCenter() }
                        }
                    }
                    EpubReadingMode.CONTINUOUS_VERTICAL -> {
                        // Continuous vertical mode - only toggle controls on tap
                        Log.d(TAG, "Tap center - toggle controls")
                        onTapCenter()
                    }
                }
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val deltaX = e2.x - e1.x
                val deltaY = e2.y - e1.y
                val currentReadingMode = _viewerState.value.readingMode

                when (currentReadingMode) {
                    EpubReadingMode.PAGE_HORIZONTAL -> {
                        // Page by Page vertical mode - handle vertical swipes for page navigation
                        if (Math.abs(deltaY) > Math.abs(deltaX) && Math.abs(deltaY) > minSwipeDistanceVertical) {
                            if (deltaY > 0) {
                                Log.d(TAG, "Swipe down - go previous page")
                                goToPreviousPage()
                            } else {
                                Log.d(TAG, "Swipe up - go next page")
                                goToNextPage()
                            }
                            return true
                        }
                    }
                    EpubReadingMode.CONTINUOUS_HORIZONTAL -> {
                        // Page by Page horizontal mode - handle horizontal swipes for page navigation
                        if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > minSwipeDistance) {
                            if (deltaX > 0) {
                                Log.d(TAG, "Swipe right - go previous page")
                                goToPreviousPage()
                            } else {
                                Log.d(TAG, "Swipe left - go next page")
                                goToNextPage()
                            }
                            return true
                        }
                    }
                    EpubReadingMode.CHAPTER_SCROLL -> {
                        // Chapter scroll mode - ONLY handle horizontal swipes for chapter navigation
                        // Vertical scrolling is handled natively by WebView
                        if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > minSwipeDistance) {
                            if (deltaX > 0) {
                                Log.d(TAG, "Swipe right - go previous chapter")
                                goToPreviousChapter()
                            } else {
                                Log.d(TAG, "Swipe left - go next chapter")
                                goToNextChapter()
                            }
                            return true
                        }
                        // Return false for vertical scrolling - let WebView handle it
                        return false
                    }
                    EpubReadingMode.CONTINUOUS_VERTICAL -> {
                        // Continuous vertical mode - no gesture handling, use native scrolling
                        return false
                    }
                }
                return false
            }
        })

        setOnTouchListener { view, event ->
            val currentReadingMode = _viewerState.value.readingMode

            when (currentReadingMode) {
                EpubReadingMode.CONTINUOUS_VERTICAL -> {
                    // Continuous vertical - allow native scrolling, but detect taps for controls
                    gestureDetector.onTouchEvent(event)
                    false // Allow native scrolling
                }
                EpubReadingMode.CHAPTER_SCROLL -> {
                    // Chapter scroll - detect horizontal flings and taps, but allow native vertical scrolling
                    val handled = gestureDetector.onTouchEvent(event)
                    // Only consume if it was a horizontal fling, otherwise let WebView scroll
                    false // Always allow native scrolling - flings are handled in onFling
                }
                EpubReadingMode.PAGE_HORIZONTAL -> {
                    // Page by Page vertical - handle all gestures, no native scrolling
                    gestureDetector.onTouchEvent(event)
                    true // Consume touch to prevent native scrolling
                }
                EpubReadingMode.CONTINUOUS_HORIZONTAL -> {
                    // Page by Page horizontal - handle all gestures, no native scrolling
                    gestureDetector.onTouchEvent(event)
                    true // Consume touch to prevent native scrolling
                }
            }
        }
    }

    // --- Chapter Loading ---

    private fun loadCurrentChapter(publication: Publication, chapterIndex: Int, targetPage: Int = 0) {
        val state = _viewerState.value

        // For continuous vertical mode, use lazy loading approach
        if (state.readingMode == EpubReadingMode.CONTINUOUS_VERTICAL) {
            loadChapterForContinuousMode(publication, chapterIndex, isInitialLoad = true)
            return
        }

        val readingOrder = publication.readingOrder
        if (chapterIndex < 0 || chapterIndex >= readingOrder.size) {
            Log.e(TAG, "Invalid chapter index: $chapterIndex")
            return
        }

        val link = readingOrder[chapterIndex]
        Log.d(TAG, "Loading chapter $chapterIndex: ${link.href}, targetPage: $targetPage")

        // Check if chapter is preloaded for instant display
        // DON'T use preloaded content when going backwards (targetPage < 0) because
        // preloaded content has startPage=0 which would show page 1 first then jump to last page
        val preloadedContent = if (targetPage >= 0) {
            synchronized(preloadedChapters) { preloadedChapters[chapterIndex] }
        } else {
            null // Force fresh generation for backwards navigation
        }

        if (preloadedContent != null) {
            Log.d(TAG, "Using preloaded content for chapter $chapterIndex, targetPage: $targetPage")
            val linkPath = link.href.toString()
            val baseUrl = if (linkPath.contains("/")) "file:///${linkPath.substringBeforeLast("/")}/" else "file:///"
            webView?.loadDataWithBaseURL(baseUrl, preloadedContent, "text/html", "UTF-8", null)
            Log.d(TAG, "Chapter loaded from cache with base URL: $baseUrl, reading mode: ${state.readingMode}")

            // Preload adjacent chapters (next AND previous) after using cached content
            preloadAdjacentChapters(publication, chapterIndex)

            // Trim cache to only keep current, previous, and next chapters
            trimPreloadCache(chapterIndex)
            return
        }

        // Load chapter fresh (either not preloaded, or going backwards needing startPage=-1)
        lifecycleScope.launch {
            try {
                val resource = publication.get(link)
                var content = withContext(Dispatchers.IO) {
                    resource?.read()?.getOrNull()?.decodeToString()
                }

                if (content != null) {
                    content = EpubResourceLoader.preprocessEpubContent(content, publication, link.href.toString())
                    // Use targetPage so JavaScript knows whether to go to first or last page
                    val styledContent = EpubHtmlWrapper.wrapHtmlContent(
                        content, state.displaySettings, state.typographySettings, state.readingMode, targetPage
                    )

                    withContext(Dispatchers.Main) {
                        val linkPath = link.href.toString()
                        val baseUrl = if (linkPath.contains("/")) "file:///${linkPath.substringBeforeLast("/")}/" else "file:///"
                        webView?.loadDataWithBaseURL(baseUrl, styledContent, "text/html", "UTF-8", null)
                        Log.d(TAG, "Chapter loaded with base URL: $baseUrl, reading mode: ${state.readingMode}")

                        // Cache the current chapter too so we can navigate back (with startPage=0 for cache)
                        val cacheContent = EpubHtmlWrapper.wrapHtmlContent(
                            content, state.displaySettings, state.typographySettings, state.readingMode, 0
                        )
                        synchronized(preloadedChapters) {
                            preloadedChapters[chapterIndex] = cacheContent
                        }

                        // Preload adjacent chapters (next AND previous)
                        preloadAdjacentChapters(publication, chapterIndex)

                        // Trim cache to only keep current, previous, and next chapters
                        trimPreloadCache(chapterIndex)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading chapter", e)
            }
        }
    }

    /**
     * Preload adjacent chapters (both next and previous) for paginated modes.
     * This ensures seamless navigation in both directions.
     */
    private fun preloadAdjacentChapters(publication: Publication, currentChapterIndex: Int) {
        val state = _viewerState.value

        // Only preload for paginated modes
        if (state.readingMode != EpubReadingMode.PAGE_HORIZONTAL &&
            state.readingMode != EpubReadingMode.CONTINUOUS_HORIZONTAL) {
            return
        }

        // Preload next chapter
        preloadChapterIfNeeded(publication, currentChapterIndex + 1)

        // Preload previous chapter
        preloadChapterIfNeeded(publication, currentChapterIndex - 1)
    }

    /**
     * Preload a specific chapter for paginated modes (PAGE_HORIZONTAL, CONTINUOUS_HORIZONTAL)
     * This runs in background and caches the styled HTML for instant display
     */
    private fun preloadChapterIfNeeded(publication: Publication, chapterIndex: Int) {
        val state = _viewerState.value

        val readingOrder = publication.readingOrder
        if (chapterIndex < 0 || chapterIndex >= readingOrder.size) {
            return
        }

        // Don't preload if already cached
        if (preloadedChapters.containsKey(chapterIndex)) {
            Log.d(TAG, "Chapter $chapterIndex already preloaded")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val link = readingOrder[chapterIndex]
                Log.d(TAG, "Preloading chapter $chapterIndex: ${link.href}")

                val resource = publication.get(link)
                var content = resource?.read()?.getOrNull()?.decodeToString()

                if (content != null) {
                    content = EpubResourceLoader.preprocessEpubContent(content, publication, link.href.toString())

                    // For previous chapter, we want to start at last page (-1 signals this)
                    val startPage = 0
                    val styledContent = EpubHtmlWrapper.wrapHtmlContent(
                        content, state.displaySettings, state.typographySettings, state.readingMode, startPage
                    )

                    // Cache the preloaded content
                    synchronized(preloadedChapters) {
                        preloadedChapters[chapterIndex] = styledContent
                    }
                    Log.d(TAG, "Chapter $chapterIndex preloaded successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error preloading chapter $chapterIndex", e)
            }
        }
    }

    /**
     * Preload the next chapter for paginated modes (PAGE_HORIZONTAL, CONTINUOUS_HORIZONTAL)
     * This runs in background and caches the styled HTML for instant display
     */
    private fun preloadNextChapterForPaginated(publication: Publication, nextChapterIndex: Int) {
        preloadChapterIfNeeded(publication, nextChapterIndex)
    }

    /**
     * Clear preloaded chapters cache (call when settings change or on cleanup)
     */
    private fun clearPreloadCache() {
        preloadJob?.cancel()
        synchronized(preloadedChapters) {
            preloadedChapters.clear()
        }
    }

    /**
     * Manage cache size - keep only current, previous, and next chapters
     */
    private fun trimPreloadCache(currentChapter: Int) {
        synchronized(preloadedChapters) {
            val keysToRemove = preloadedChapters.keys.filter { key ->
                key < currentChapter - 1 || key > currentChapter + 1
            }
            keysToRemove.forEach { preloadedChapters.remove(it) }
            if (keysToRemove.isNotEmpty()) {
                Log.d(TAG, "Trimmed cache, removed chapters: $keysToRemove")
            }
        }
    }

    /**
     * Load a chapter for continuous vertical mode.
     * Uses lazy loading - chapters are appended as user scrolls near the end.
     */
    private fun loadChapterForContinuousMode(publication: Publication, chapterIndex: Int, isInitialLoad: Boolean) {
        val readingOrder = publication.readingOrder
        if (chapterIndex < 0 || chapterIndex >= readingOrder.size) {
            Log.e(TAG, "Invalid chapter index for continuous mode: $chapterIndex")
            return
        }

        val link = readingOrder[chapterIndex]
        Log.d(TAG, "Loading chapter $chapterIndex for continuous vertical mode: ${link.href}")

        lifecycleScope.launch {
            try {
                val state = _viewerState.value
                val resource = publication.get(link)
                var content = withContext(Dispatchers.IO) {
                    resource?.read()?.getOrNull()?.decodeToString()
                }

                if (content != null) {
                    content = EpubResourceLoader.preprocessEpubContent(content, publication, link.href.toString())

                    if (isInitialLoad) {
                        // For initial load, use the regular wrapper with continuous vertical CSS
                        val styledContent = EpubHtmlWrapper.wrapHtmlContent(
                            content, state.displaySettings, state.typographySettings,
                            EpubReadingMode.CONTINUOUS_VERTICAL, 0
                        )

                        withContext(Dispatchers.Main) {
                            val linkPath = link.href.toString()
                            val baseUrl = if (linkPath.contains("/")) "file:///${linkPath.substringBeforeLast("/")}/" else "file:///"
                            webView?.loadDataWithBaseURL(baseUrl, styledContent, "text/html", "UTF-8", null)

                            // Update state
                            _viewerState.value = state.copy(
                                currentChapter = chapterIndex,
                                isContinuousMode = true
                            )

                            Log.d(TAG, "Initial chapter $chapterIndex loaded for continuous vertical mode")
                        }
                    } else {
                        // For subsequent loads, append content via JavaScript
                        withContext(Dispatchers.Main) {
                            appendChapterViaJavaScript(content, chapterIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading chapter for continuous mode", e)
            }
        }
    }

    /**
     * Append a chapter to the existing page via JavaScript for seamless scrolling.
     */
    private fun appendChapterViaJavaScript(content: String, chapterIndex: Int) {
        // Extract just the body content from the chapter
        val bodyContent = extractBodyContent(content)

        // Escape the content for JavaScript string
        val escapedContent = bodyContent
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("</script>", "<\\/script>")

        val js = """
            (function() {
                var newChapter = document.createElement('div');
                newChapter.className = 'chapter-content';
                newChapter.setAttribute('data-chapter', '$chapterIndex');
                newChapter.innerHTML = '$escapedContent';
                document.body.appendChild(newChapter);
                
                // Update the loaded chapters tracking
                if (typeof window.loadedChapters === 'undefined') {
                    window.loadedChapters = [];
                }
                window.loadedChapters.push($chapterIndex);
                
                console.log('Appended chapter $chapterIndex');
                
                // Notify Android that chapter was appended
                if (typeof AndroidBridge !== 'undefined') {
                    AndroidBridge.onChapterAppended($chapterIndex);
                }
            })();
        """.trimIndent()

        webView?.evaluateJavascript(js, null)
        Log.d(TAG, "Appended chapter $chapterIndex via JavaScript")
    }

    /**
     * Extract body content from HTML string.
     */
    private fun extractBodyContent(html: String): String {
        val bodyStart = html.indexOf("<body", ignoreCase = true)
        val bodyEnd = html.indexOf("</body>", ignoreCase = true)

        return if (bodyStart >= 0 && bodyEnd > bodyStart) {
            val contentStart = html.indexOf(">", bodyStart) + 1
            html.substring(contentStart, bodyEnd)
        } else {
            html
        }
    }

    /**
     * Preload and append the next chapter for seamless continuous scrolling.
     */
    private fun preloadNextChapter() {
        val state = _viewerState.value
        val nextChapter = state.currentChapter + 1

        if (nextChapter < state.totalChapters) {
            publication?.let { pub ->
                loadChapterForContinuousMode(pub, nextChapter, isInitialLoad = false)
            }
        }
    }

    // --- Navigation ---

    private fun goToChapter(chapterIndex: Int, startAtPage: Int = 0) {
        val state = _viewerState.value
        if (chapterIndex in 0 until state.totalChapters) {
            // Check if we have preloaded content for seamless transition
            val hasPreloadedContent = synchronized(preloadedChapters) {
                preloadedChapters.containsKey(chapterIndex)
            }

            // Store the target page (keep -1 if going backwards to last page)
            val targetPage = startAtPage

            // Only show loading if content isn't preloaded (will cause flash anyway)
            if (!hasPreloadedContent) {
                _viewerState.value = state.copy(
                    currentChapter = chapterIndex,
                    currentPage = if (startAtPage >= 0) startAtPage else 0,
                    isLoading = true
                )
            } else {
                _viewerState.value = state.copy(
                    currentChapter = chapterIndex,
                    currentPage = if (startAtPage >= 0) startAtPage else 0
                )
            }

            // For continuous vertical mode with lazy loading, just reload from that chapter
            if (state.readingMode == EpubReadingMode.CONTINUOUS_VERTICAL) {
                publication?.let { loadChapterForContinuousMode(it, chapterIndex, isInitialLoad = true) }
            } else {
                // Pass the original targetPage (including -1 for last page)
                publication?.let { loadCurrentChapter(it, chapterIndex, targetPage) }
            }

            saveProgress(chapterIndex, if (startAtPage >= 0) startAtPage else 0)

            // Update bookmark status for the new chapter
            if (bookId > 0L) {
                updateBookmarkStatus(bookId, chapterIndex)
            }

            Log.d(TAG, "Navigated to chapter: $chapterIndex, page: $startAtPage")
        }
    }

    private fun goToNextChapter() {
        val state = _viewerState.value
        if (state.currentChapter < state.totalChapters - 1) {
            goToChapter(state.currentChapter + 1, 0)
        }
    }

    private fun goToPreviousChapter() {
        val state = _viewerState.value
        if (state.currentChapter > 0) {
            val isPaginated = state.readingMode == EpubReadingMode.PAGE_HORIZONTAL ||
                              state.readingMode == EpubReadingMode.CONTINUOUS_HORIZONTAL
            goToChapter(state.currentChapter - 1, if (isPaginated) -1 else 0)
        }
    }

    private fun goToNextPage() {
        val state = _viewerState.value
        when (state.readingMode) {
            EpubReadingMode.PAGE_HORIZONTAL,
            EpubReadingMode.CONTINUOUS_HORIZONTAL -> {
                // Paginated modes - call JS to go to next page
                webView?.evaluateJavascript("nextPage()", null)
            }
            EpubReadingMode.CHAPTER_SCROLL -> {
                // Chapter scroll - go to next chapter
                goToNextChapter()
            }
            EpubReadingMode.CONTINUOUS_VERTICAL -> {
                // Continuous vertical - no page navigation (free scroll)
                // User scrolls freely, chapter tracking is automatic
            }
        }
    }

    private fun goToPreviousPage() {
        val state = _viewerState.value
        when (state.readingMode) {
            EpubReadingMode.PAGE_HORIZONTAL,
            EpubReadingMode.CONTINUOUS_HORIZONTAL -> {
                // Paginated modes - call JS to go to previous page
                webView?.evaluateJavascript("previousPage()", null)
            }
            EpubReadingMode.CHAPTER_SCROLL -> {
                // Chapter scroll - go to previous chapter
                goToPreviousChapter()
            }
            EpubReadingMode.CONTINUOUS_VERTICAL -> {
                // Continuous vertical - no page navigation (free scroll)
                // User scrolls freely, chapter tracking is automatic
            }
        }
    }

    private fun toggleControls() {
        Log.d(TAG, "Controls visible: ${!_viewerState.value.isControlsVisible}")
        _viewerState.value = _viewerState.value.copy(isControlsVisible = !_viewerState.value.isControlsVisible)
    }

    /**
     * Apply style changes via JavaScript without reloading the page.
     * This is used for continuous vertical mode to avoid losing scroll position.
     */
    private fun applyStylesViaJavaScript(
        typographySettings: EpubTypographySettings,
        displaySettings: EpubDisplaySettings
    ) {
        val bgColor = String.format("#%06X", 0xFFFFFF and displaySettings.pageTheme.backgroundColor.toInt())
        val textColor = if (typographySettings.fontColor != null) {
            String.format("#%06X", 0xFFFFFF and typographySettings.fontColor.colorValue.toInt())
        } else {
            String.format("#%06X", 0xFFFFFF and displaySettings.pageTheme.textColor.toInt())
        }
        val fontSize = typographySettings.fontSize.toInt()
        val fontFamily = typographySettings.fontFamily.fontFamily
        val lineHeight = typographySettings.lineSpacing
        val fontWeight = typographySettings.fontWeight.toInt()
        val textAlign = typographySettings.textAlignment.cssValue
        val margin = displaySettings.pageMargin.toInt()
        val topPadding = margin + 48
        val bottomPadding = margin + 64
        val sidePadding = margin + 16

        val js = """
            (function() {
                document.body.style.backgroundColor = '$bgColor';
                document.body.style.color = '$textColor';
                document.body.style.fontFamily = '$fontFamily';
                document.body.style.fontSize = '${fontSize}px';
                document.body.style.fontWeight = '$fontWeight';
                document.body.style.lineHeight = '$lineHeight';
                document.body.style.textAlign = '$textAlign';
                document.body.style.padding = '${topPadding}px ${sidePadding}px ${bottomPadding}px ${sidePadding}px';
                
                document.documentElement.style.backgroundColor = '$bgColor';
                
                // Update all text elements
                var elements = document.querySelectorAll('p, span, div, h1, h2, h3, h4, h5, h6');
                for (var i = 0; i < elements.length; i++) {
                    elements[i].style.color = '$textColor';
                    elements[i].style.fontSize = '${fontSize}px';
                    elements[i].style.lineHeight = '$lineHeight';
                    elements[i].style.fontFamily = '$fontFamily';
                }
                
                // Update paragraphs specifically
                var paragraphs = document.querySelectorAll('p');
                for (var i = 0; i < paragraphs.length; i++) {
                    paragraphs[i].style.textAlign = '$textAlign';
                }
            })();
        """.trimIndent()

        webView?.evaluateJavascript(js, null)
        Log.d(TAG, "Applied style changes via JavaScript")
    }

    private fun toggleBookmark() {
        if (bookId > 0L) {
            lifecycleScope.launch {
                val currentChapter = _viewerState.value.currentChapter
                val isBookmarked = _viewerState.value.isCurrentPageBookmarked

                if (isBookmarked) {
                    // Remove bookmark
                    database.bookmarkDao().getBookmarkAtPage(bookId, currentChapter)?.let { bookmark ->
                        database.bookmarkDao().removeBookmark(bookmark)
                    }
                    _viewerState.value = _viewerState.value.copy(isCurrentPageBookmarked = false)
                } else {
                    // Add bookmark
                    val bookmark = Bookmark(
                        bookId = bookId,
                        page = currentChapter,
                        dateCreated = System.currentTimeMillis()
                    )
                    database.bookmarkDao().addBookmark(bookmark)
                    _viewerState.value = _viewerState.value.copy(isCurrentPageBookmarked = true)
                }

                // Refresh bookmarks list
                loadBookmarks(bookId)
            }
        }
    }

    private fun loadBookmarks(bookId: Long) {
        lifecycleScope.launch {
            try {
                _bookmarks.value = database.bookmarkDao().getBookmarks(bookId)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading bookmarks", e)
            }
        }
    }

    private fun updateBookmarkStatus(bookId: Long, chapter: Int) {
        lifecycleScope.launch {
            try {
                val isBookmarked = database.bookmarkDao().isPageBookmarked(bookId, chapter)
                _viewerState.value = _viewerState.value.copy(isCurrentPageBookmarked = isBookmarked)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking bookmark status", e)
            }
        }
    }

    private fun removeBookmark(bookmark: Bookmark) {
        lifecycleScope.launch {
            try {
                database.bookmarkDao().removeBookmark(bookmark)
                if (bookId > 0L) {
                    loadBookmarks(bookId)
                }

                // Update status if we removed the current chapter's bookmark
                if (bookmark.page == _viewerState.value.currentChapter) {
                    _viewerState.value = _viewerState.value.copy(isCurrentPageBookmarked = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing bookmark", e)
            }
        }
    }

    // --- State Updates ---

    private fun updateGlobalPageCount() {
        val state = _viewerState.value
        val chapterCounts = state.chapterPageCounts.toMutableList()
        // Ensure list has enough entries
        while (chapterCounts.size <= state.currentChapter) chapterCounts.add(1)
        // Update current chapter's page count
        chapterCounts[state.currentChapter] = state.totalPages.coerceAtLeast(1)
        val totalGlobalPages = chapterCounts.sum().coerceAtLeast(1)
        _viewerState.value = state.copy(
            chapterPageCounts = chapterCounts,
            globalTotalPages = totalGlobalPages
        )
        Log.d(TAG, "Global page count updated: total=$totalGlobalPages, chapter ${state.currentChapter} has ${state.totalPages} pages")
        // Also update the global page display
        updateGlobalPageDisplay()
    }

    private fun updateGlobalPageDisplay() {
        val state = _viewerState.value
        var globalPage = 0
        // Sum up pages from previous chapters
        for (i in 0 until state.currentChapter) {
            globalPage += state.chapterPageCounts.getOrElse(i) { 1 }
        }
        // Add current page in current chapter (1-based for display)
        globalPage += (state.currentPage + 1)
        _viewerState.value = state.copy(globalPage = globalPage.coerceAtLeast(1))
        Log.d(TAG, "Global page updated: $globalPage / ${state.globalTotalPages} (chapter: ${state.currentChapter}, page: ${state.currentPage})")
    }

    // --- Settings Persistence ---

    private fun loadSavedSettings() {
        if (bookId > 0L) {
            _viewerState.value = preferencesManager.loadSettings(bookId.toString())
        }
    }

    private fun saveSettings() {
        if (bookId > 0L) {
            preferencesManager.saveSettings(bookId.toString(), _viewerState.value)
        }
    }

    private fun saveProgress(chapter: Int, page: Int = 0) {
        if (bookId > 0L) {
            preferencesManager.saveProgress(bookId.toString(), chapter, page)
            lifecycleScope.launch {
                database.bookDao().updateReadingProgress(bookId, chapter + 1, 0f)
            }
        }
    }

    // --- Helpers ---

    private suspend fun loadEpub(uri: Uri, context: Context, onSuccess: (Publication, Int) -> Unit): String? {
        return try {
            val result = readiumManager.openEpub(uri)
            result.onSuccess { pub -> onSuccess(pub, pub.readingOrder.size) }
            result.onFailure { e -> return e.message ?: "Failed to open EPUB" }
            null
        } catch (e: Exception) {
            e.message ?: "Unknown error"
        }
    }

    private fun extractEpubName(uri: Uri, context: Context): String {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx != -1) cursor.getString(idx)?.removeSuffix(".epub") ?: "Unknown Book" else "Unknown Book"
                } else "Unknown Book"
            } ?: "Unknown Book"
        } catch (_: Exception) { "Unknown Book" }
    }

    private fun getOrientationForMode(mode: RotationMode): Int = when (mode) {
        RotationMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        RotationMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        RotationMode.LOCKED_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        RotationMode.LOCKED_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        RotationMode.REVERSE_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        RotationMode.FREE -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    override fun onResume() {
        super.onResume()
        // Reset session tracking for new/resumed reading session
        startTime = System.currentTimeMillis()
        lastActiveTime = System.currentTimeMillis()
        totalActiveReadingTime = 0L

        // Update session start point for accurate chapter tracking
        val state = _viewerState.value
        sessionStartChapter = state.currentChapter
        highestChapterReached = state.currentChapter

        Log.d(TAG, "Session resumed at chapter $sessionStartChapter")
    }

    override fun onPause() {
        super.onPause()
        val state = _viewerState.value
        saveProgress(state.currentChapter, state.currentPage)
        saveSettings()

        // Finalize active reading time before logging
        finalizeActiveReadingTime()
        logReadingSession()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearPreloadCache() // Clear preloaded chapters cache
        webView?.destroy()
        webView = null
        readiumManager.closeCurrentPublication()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Track active reading time. Called on user interactions (page changes, chapter changes).
     * This helps distinguish between active reading and idle time (phone left open).
     */
    private fun trackActiveReading() {
        val now = System.currentTimeMillis()
        val timeSinceLastActivity = now - lastActiveTime

        // If user has been active (not idle for more than threshold), add the time
        if (lastActiveTime > 0 && timeSinceLastActivity <= IDLE_THRESHOLD_MS) {
            totalActiveReadingTime += timeSinceLastActivity
        }

        lastActiveTime = now
    }

    /**
     * Finalize active reading time before session ends.
     * Adds any remaining active time since last interaction.
     */
    private fun finalizeActiveReadingTime() {
        val now = System.currentTimeMillis()
        val timeSinceLastActivity = now - lastActiveTime

        // Only add time if user was recently active (not idle)
        if (lastActiveTime > 0 && timeSinceLastActivity <= IDLE_THRESHOLD_MS) {
            totalActiveReadingTime += timeSinceLastActivity
        }
    }

    /**
     * Log the EPUB reading session with accurate time and chapter tracking.
     *
     * For EPUBs, we track:
     * - Active reading time (not just time app was open)
     * - Chapters progressed (forward progress only)
     * - Pages are estimated from reading time since EPUB pages are virtual
     */
    private fun logReadingSession() {
        val endTime = System.currentTimeMillis()
        val state = _viewerState.value

        // Calculate time spent - use either active reading time or total session time (whichever is less)
        // This prevents inflated times when phone is left open but not being read
        val totalSessionTime = endTime - startTime
        val activeTimeMinutes = (totalActiveReadingTime / 60_000).toInt()
        val sessionTimeMinutes = (totalSessionTime / 60_000).toInt()

        // Use the minimum of session time and active time + small buffer
        // The buffer accounts for reading without page interactions (long reads on same page)
        val readingTimeMinutes = if (totalActiveReadingTime > 0) {
            // Add 30% buffer for passive reading (reading without page turns)
            val bufferedActiveTime = (activeTimeMinutes * 1.3).toInt()
            minOf(bufferedActiveTime, sessionTimeMinutes)
        } else {
            // Fallback if no active tracking occurred (shouldn't happen normally)
            sessionTimeMinutes
        }

        // Calculate chapters progressed (only forward progress counts)
        val chaptersProgressed = (highestChapterReached - sessionStartChapter).coerceAtLeast(0)

        // Only log if meaningful activity occurred
        if (readingTimeMinutes > 0 || chaptersProgressed > 0) {
            if (bookId > 0L) {
                lifecycleScope.launch {
                    readingStatsViewModel.logEpubReadingSession(
                        bookId = bookId.toString(),
                        timeSpentMinutes = readingTimeMinutes,
                        startChapter = sessionStartChapter,
                        endChapter = highestChapterReached
                    )
                    Log.d(TAG, "Logged EPUB reading session: $readingTimeMinutes minutes, " +
                            "$chaptersProgressed chapters (from $sessionStartChapter to $highestChapterReached)")
                }
            }
        }
    }
}

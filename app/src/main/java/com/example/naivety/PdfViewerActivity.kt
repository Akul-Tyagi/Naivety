package com.example.naivety

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.naivety.ui.components.pdf.*
import com.example.naivety.ui.components.pdf.modals.*
import com.example.naivety.ui.pdf.*
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.naivety.ui.theme.NaivetyTheme
import com.example.naivety.viewmodels.PdfViewerViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import com.example.naivety.ui.theme.TransparentSystemBars
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PdfViewerActivity : ComponentActivity() {
    private val viewModel: PdfViewerViewModel by viewModels()
    private var bookId: String? = null
    private var pdfName: String = "PDF"
    private lateinit var pdfView: PDFView
    companion object {
        private const val PREFS_NAME = "pdf_viewer_prefs"
    }
    private val LocalViewModel = compositionLocalOf<PdfViewerViewModel> {
        error("No ViewModel provided")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()

        bookId = intent.getStringExtra("BOOK_ID")
        if (bookId == null) {
            // Generate a unique ID if none is provided
            bookId = java.util.UUID.randomUUID().toString()
        }

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastPage = prefs.getInt("${bookId}_last_page", 0)
        val savedReadingMode = prefs.getString("${bookId}_reading_mode", null)?.let {
            try {
                ReadingMode.valueOf(it)
            } catch (e: Exception) {
                ReadingMode.VERTICAL_PAGED
            }
        } ?: ReadingMode.VERTICAL_PAGED

        // Update ViewModel with saved reading mode
        viewModel.updateReadingMode(savedReadingMode)

        // Load bookmarks for this book
        viewModel.loadBookmarks(bookId!!)

        setContent {
            NaivetyTheme {
                PdfViewerScreen(
                    viewModel = viewModel,
                    bookId = bookId,
                    lastPage = lastPage,
                    intent = intent,
                    window = window,
                    onFinish = { finish() },
                    onSaveProgress = { page -> saveReadingProgress(page) }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PdfViewerScreen(
        viewModel: PdfViewerViewModel,
        bookId: String?,
        lastPage: Int,
        intent: Intent,
        window: Window,
        onFinish: () -> Unit,
        onSaveProgress: (Int) -> Unit
    ) {
        val context = LocalContext.current
        val userPreferencesRepository = remember {
            (context.applicationContext as NaivetyApplication).userPreferencesRepository
        }
        val isDarkTheme = userPreferencesRepository.isDarkTheme.collectAsState().value

        TransparentSystemBars(darkTheme = isDarkTheme)

        CompositionLocalProvider(LocalViewModel provides viewModel) {
            val showSettings = remember { mutableStateOf(false) }
            val showReadingMode = remember { mutableStateOf(false) }
            val showRotation = remember { mutableStateOf(false) }
            val showBrightness = remember { mutableStateOf(false) }
            val context = LocalContext.current
            val isCurrentPageBookmarked by viewModel.isCurrentPageBookmarked.collectAsState()
            val viewerState = viewModel.viewerState.collectAsState()
            val isBookmarked = remember { mutableStateOf(false) }
            val showBookmarksList = remember { mutableStateOf(false) }
            val currentBookmarks by viewModel.bookmarks.collectAsState()
            val systemUiController = rememberSystemUiController()
            val backgroundColor = Color(viewerState.value.settings.backgroundColor)
            var showEditName by remember { mutableStateOf(false) }
            var localPdfName by remember {
                mutableStateOf(
                    getSavedBookName(bookId) ?:
                    intent.data?.let { uri -> extractPdfName(uri, context) } ?: "PDF"
                )
            }

            LaunchedEffect(localPdfName) {
                pdfName = localPdfName
            }

            LaunchedEffect(bookId) {
                bookId?.let { id ->
                    viewModel.updateCurrentPageBookmarkStatus(id, viewerState.value.currentPage)
                }
            }

            // In PdfViewerActivity
            LaunchedEffect(viewerState.value.settings) {
                val settings = viewerState.value.settings
                window.apply {
                    if (settings.keepScreenOn) {
                        addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(viewerState.value.settings.backgroundColor))

            ) {


                // PDF View with improved settings
                val currentReadingMode = remember { mutableStateOf(viewerState.value.readingMode) }
                val currentNightMode = remember { mutableStateOf(viewerState.value.brightness.nightMode) }

                AndroidView(
                    factory = { context ->
                        PDFView(context, null).apply {
                            configurePdfView(
                                uri = intent.data!!,
                                lastPage = lastPage,
                                readingMode = viewerState.value.readingMode,
                                viewModel = viewModel,
                                bookId = bookId,
                                onPageChange = { page, pageCount ->
                                    viewModel.updatePageCount(page, pageCount, bookId)
                                    onSaveProgress(page)
                                }
                            )
                        }.also { pdfView = it }
                    },
                    update = { view ->
                        // Check if reading mode or night mode has changed
                        if (currentReadingMode.value != viewerState.value.readingMode ||
                            currentNightMode.value != viewerState.value.brightness.nightMode) {

                            // Update stored values
                            currentReadingMode.value = viewerState.value.readingMode
                            currentNightMode.value = viewerState.value.brightness.nightMode

                            val currentPage = view.currentPage

                            view.fromUri(intent.data!!)
                                .defaultPage(currentPage)
                                .enableDoubletap(false)
                                .enableAnnotationRendering(true)
                                .enableSwipe(true)
                                .swipeHorizontal(currentReadingMode.value.let {
                                    it == ReadingMode.LEFT_TO_RIGHT || it == ReadingMode.CONTINUOUS_HORIZONTAL
                                })
                                .spacing(when (currentReadingMode.value) {
                                    ReadingMode.CONTINUOUS_HORIZONTAL,
                                    ReadingMode.CONTINUOUS_VERTICAL -> 8
                                    else -> 0
                                })
                                .nightMode(currentNightMode.value)
                                .pageFitPolicy(when (viewModel.viewerState.value.settings.scaleType) {
                                    ScaleType.FIT_PAGE -> FitPolicy.BOTH
                                    ScaleType.FIT_WIDTH -> FitPolicy.WIDTH
                                    ScaleType.FIT_HEIGHT -> FitPolicy.HEIGHT
                                    else -> FitPolicy.WIDTH
                                })
                                .onLoad {
                                    viewModel.updateLoadingState(false)
                                }
                                .onPageChange { page, pageCount ->
                                    viewModel.updatePageCount(page, pageCount, bookId)
                                    onSaveProgress(page)
                                }
                                .onTap { _ ->
                                    viewModel.toggleControls()
                                    true
                                }
                                .pageSnap(currentReadingMode.value.let {
                                    it == ReadingMode.LEFT_TO_RIGHT || it == ReadingMode.VERTICAL_PAGED
                                })
                                .pageFling(currentReadingMode.value.let {
                                    it == ReadingMode.LEFT_TO_RIGHT || it == ReadingMode.VERTICAL_PAGED
                                })
                                .load()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .pointerInput(Unit) {
                            detectTapGestures { _ ->
                                viewModel.toggleControls()
                            }
                        }
                )

                // Loading Indicator
                if (viewerState.value.isLoading) {
                    PdfLoadingAnimation(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Page Number Indicator with improved visibility
                AnimatedVisibility(
                    visible = viewerState.value.settings.showPageNumber && !viewerState.value.isControlsVisible,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "${viewerState.value.currentPage + 1}/${viewerState.value.totalPages}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                TopBar(
                    pdfName = localPdfName,
                    isVisible = viewerState.value.isControlsVisible,
                    onEditClick = { showEditName = true },
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // Add QuickNavScroll
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    // Quick Navigation
                    QuickNavScroll(
                        currentPage = viewerState.value.currentPage,
                        totalPages = viewerState.value.totalPages,
                        isVisible = viewerState.value.isControlsVisible,
                        onPageSelect = { page ->
                            pdfView.jumpTo(page)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 83.dp) // Adjust this value to control space between nav bar
                    )


                    // Custom Navigation Bar
                    // Modify the AnimatedVisibility block for the navigation bar
                    AnimatedVisibility(
                        visible = viewerState.value.isControlsVisible,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp) // Add bottom padding for floating effect
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.91f) // Make the bar 85% of screen width
                                .padding(horizontal = 16.dp), // Add horizontal padding
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f), // Slightly transparent
                            shape = RoundedCornerShape(28.dp), // Round all corners
                            shadowElevation = 8.dp // Add elevation for floating effect
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(
                                        vertical = 12.dp,
                                        horizontal = 8.dp
                                    ), // Adjust internal padding
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Reading Mode Button
                                IconButton(
                                    onClick = { showReadingMode.value = true },
                                    modifier = Modifier.size(40.dp) // Consistent icon size
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = "Reading Mode",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Brightness Button
                                IconButton(
                                    onClick = { showBrightness.value = true },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BrightnessHigh,
                                        contentDescription = "Brightness",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }


                                // Bookmark Button
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    // Show bookmarks list on long press
                                                    showBookmarksList.value = true
                                                },
                                                onTap = {
                                                    bookId?.let { id ->
                                                        val currentPage = pdfView.currentPage
                                                        if (isCurrentPageBookmarked) {
                                                            viewModel.removeBookmark(
                                                                id,
                                                                currentPage
                                                            )
                                                        } else {
                                                            viewModel.addBookmark(id, currentPage)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                ) {
                                    Icon(
                                        modifier = Modifier.fillMaxSize(),
                                        imageVector = if (isCurrentPageBookmarked) {
                                            Icons.Default.Bookmark
                                        } else {
                                            Icons.Default.BookmarkBorder
                                        },
                                        contentDescription = "Bookmark",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Orientation Button
                                IconButton(
                                    onClick = { showRotation.value = true },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ScreenRotation,
                                        contentDescription = "Screen Rotation",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Settings Button
                                IconButton(
                                    onClick = { showSettings.value = true },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Edit Book Name Dialog
                // Replace the existing EditBookNameDialog usage
                if (showEditName) {
                    EditBookNameDialog(
                        currentName = localPdfName,
                        onNameChange = { newName ->
                            localPdfName = newName
                            saveBookName(newName)
                            showEditName = false
                        },
                        onDismiss = { showEditName = false }
                    )
                }
                // Modal Sheets
                if (showSettings.value) {
                    MainSettingsSheet(
                        settings = viewerState.value.settings,
                        onSettingsChange = { settings ->
                            viewModel.updateSettings(settings)
                            window.attributes = window.attributes.apply {
                                // Handle keep screen on
                                if (settings.keepScreenOn) {
                                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                } else {
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                }

                                // Update PDFView background
                                pdfView.setBackgroundColor(settings.backgroundColor.toInt())

                                // Get current state
                                val currentPage = pdfView.currentPage
                                val currentReadingMode = viewerState.value.readingMode

                                // Configure PDF view with new settings while maintaining reading mode
                                pdfView.fromUri(intent.data!!)
                                    .defaultPage(currentPage)
                                    .enableDoubletap(false)
                                    .enableAnnotationRendering(true)
                                    .enableSwipe(true)
                                    .swipeHorizontal(currentReadingMode.let {
                                        it == ReadingMode.LEFT_TO_RIGHT || it == ReadingMode.CONTINUOUS_HORIZONTAL
                                    })
                                    .spacing(when (currentReadingMode) {
                                        ReadingMode.CONTINUOUS_HORIZONTAL,
                                        ReadingMode.CONTINUOUS_VERTICAL -> 8
                                        else -> 0
                                    })
                                    .nightMode(viewerState.value.brightness.nightMode)
                                    .pageFitPolicy(when (settings.scaleType) {
                                        ScaleType.FIT_PAGE -> FitPolicy.BOTH
                                        ScaleType.FIT_WIDTH -> FitPolicy.WIDTH
                                        ScaleType.FIT_HEIGHT -> FitPolicy.HEIGHT
                                        else -> FitPolicy.WIDTH
                                    })
                                    .onLoad {
                                        viewModel.updateLoadingState(false)
                                    }
                                    .onPageChange { page, pageCount ->
                                        viewModel.updatePageCount(page, pageCount, bookId)
                                        onSaveProgress(page)
                                    }
                                    .onTap { _ ->
                                        viewModel.toggleControls()
                                        true
                                    }
                                    .pageSnap(currentReadingMode.let {
                                        it == ReadingMode.LEFT_TO_RIGHT || it == ReadingMode.VERTICAL_PAGED
                                    })
                                    .pageFling(currentReadingMode.let {
                                        it == ReadingMode.LEFT_TO_RIGHT || it == ReadingMode.VERTICAL_PAGED
                                    })
                                    .load()
                            }
                        },
                        onDismiss = { showSettings.value = false }
                    )
                }

                if (showReadingMode.value) {
                    ReadingModeSheet(
                        currentMode = viewerState.value.readingMode,
                        onModeSelect = { mode ->
                            viewModel.updateReadingMode(mode)
                            pdfView.configurePdfView(
                                uri = intent.data!!,
                                lastPage = viewerState.value.currentPage,
                                readingMode = mode,
                                viewModel = viewModel,
                                bookId = bookId,
                                onPageChange = { page, pageCount ->
                                    viewModel.updatePageCount(page, pageCount, bookId) // Update this line
                                    onSaveProgress(page)
                                }
                            )
                            showReadingMode.value = false
                            // Ensure controls are hidden after mode change
                            viewModel.toggleControls()
                        },
                        onDismiss = {
                            showReadingMode.value = false
                            viewModel.toggleControls()
                        }
                    )
                }

                if (showRotation.value) {
                    RotationSheet(
                        currentMode = viewerState.value.rotation,
                        onModeSelect = { mode ->
                            viewModel.updateRotation(mode)
                            requestedOrientation = when (mode) {
                                RotationMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                RotationMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                RotationMode.LOCKED_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                RotationMode.LOCKED_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                RotationMode.REVERSE_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                                RotationMode.FREE -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                            }
                            // Hide controls after changing orientation
                            viewModel.toggleControls()
                            showRotation.value = false
                        },
                        onDismiss = {
                            showRotation.value = false
                            viewModel.toggleControls()
                        }
                    )
                }

                if (showBrightness.value) {
                    BrightnessSheet(
                        settings = viewerState.value.brightness,
                        onSettingsChange = { brightnessSettings ->
                            viewModel.updateBrightness(brightnessSettings)

                            // Handle screen brightness
                            window.attributes = window.attributes.apply {
                                screenBrightness = when {
                                    brightnessSettings.useSystemBrightness -> -1.0f
                                    else -> brightnessSettings.customBrightness
                                }
                            }

                            // Store current state
                            val currentPage = pdfView.currentPage
                            val currentReadingMode = viewerState.value.readingMode

                            // Configure PDF view with new settings
                            pdfView.fromUri(intent.data!!)
                                .defaultPage(currentPage)
                                .spacing(when (currentReadingMode) {
                                    ReadingMode.CONTINUOUS_HORIZONTAL, ReadingMode.CONTINUOUS_VERTICAL -> 8
                                    else -> 0
                                })
                                .swipeHorizontal(currentReadingMode.isHorizontalMode())
                                .enableSwipe(true)
                                .enableDoubletap(false)
                                .enableAnnotationRendering(true)
                                .nightMode(brightnessSettings.nightMode) // Set night mode during configuration
                                .onLoad {
                                    // Complete loading without toggling controls
                                    viewModel.updateLoadingState(false)
                                }
                                .onPageChange { page, pageCount ->
                                    viewModel.updatePageCount(page, pageCount, bookId) // Update this line
                                    onSaveProgress(page)
                                }
                                .onTap { _ ->
                                    viewModel.toggleControls()
                                    true
                                }
                                .pageFitPolicy(FitPolicy.WIDTH)
                                .pageSnap(when (currentReadingMode) {
                                    ReadingMode.LEFT_TO_RIGHT, ReadingMode.VERTICAL_PAGED -> true
                                    else -> false
                                })
                                .pageFling(when (currentReadingMode) {
                                    ReadingMode.LEFT_TO_RIGHT, ReadingMode.VERTICAL_PAGED -> true
                                    else -> false
                                })
                                .load()
                        },
                        onDismiss = {
                            showBrightness.value = false
                            viewModel.toggleControls()
                        }
                    )
                }

                if (showBookmarksList.value) {
                    BookmarksList(
                        bookmarks = currentBookmarks,
                        onBookmarkClick = { page ->
                            pdfView.jumpTo(page)
                            showBookmarksList.value = false
                            viewModel.toggleControls()
                        },
                        onBookmarkRemove = { bookmark ->
                            viewModel.removeBookmark(bookmark.bookId, bookmark.page)
                        },
                        onDismiss = {
                            showBookmarksList.value = false
                            viewModel.toggleControls()
                        }
                    )
                }
            }
        }
    }

    // Add the function to extract the PDF name from URI
    private fun extractPdfName(uri: Uri, context: Context): String {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        cursor.getString(displayNameIndex)?.removeSuffix(".pdf") ?: "Unknown Book"
                    } else "Unknown Book"
                } else "Unknown Book"
            } ?: "Unknown Book"
        } catch (e: Exception) {
            "Unknown Book"
        }
    }

    private fun setupWindow() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Hide system bar icons
        WindowInsetsControllerCompat(window, window.decorView).apply {
            //hide icons
            hide(WindowInsetsCompat.Type.navigationBars())
        }

        // Keep screen on if needed
        if (viewModel.viewerState.value.settings.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun saveReadingProgress(page: Int) {
        bookId?.let { id ->
            lifecycleScope.launch {
                viewModel.updatePage(
                    bookId = id,
                    page = page,
                    position = pdfView.positionOffset
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveReadingProgress(pdfView.currentPage)
        saveViewerSettings()
        viewModel.saveSettings(bookId)

        // Add this code to log the reading session when pausing
        bookId?.let { id ->
            viewModel.endReadingSession(pdfView.currentPage, id)
        }
    }

    // Keep only one implementation of saveViewerSettings
    private fun saveViewerSettings() {
        bookId?.let { id ->
            applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt("${id}_last_page", pdfView.currentPage)
                .putString("${id}_reading_mode", viewModel.viewerState.value.readingMode.name)
                .putString("${id}_book_name", pdfName)
                .apply()
        }
    }

    private fun getSavedBookName(bookId: String?): String? {
        return bookId?.let { id ->
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString("${id}_book_name", null)
        }
    }

    private fun saveBookName(name: String) {
        bookId?.let { id ->
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putString("${id}_book_name", name)
                apply()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // End reading session before cleanup
        bookId?.let { id ->
            viewModel.endReadingSession(pdfView.currentPage, id)
        }

        viewModel.cleanup()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

private fun ReadingMode.isHorizontalMode(): Boolean {
    return this == ReadingMode.LEFT_TO_RIGHT || this == ReadingMode.CONTINUOUS_HORIZONTAL
}

private fun PDFView.configurePdfView(
    uri: Uri,
    lastPage: Int,
    readingMode: ReadingMode,
    viewModel: PdfViewerViewModel,
    bookId: String?,
    onPageChange: (Int, Int) -> Unit
) {
    viewModel.initializePdfViewer()
    setBackgroundColor(viewModel.viewerState.value.settings.backgroundColor.toInt())

    fromUri(uri)
        .defaultPage(lastPage)
        .enableDoubletap(false)
        .enableAnnotationRendering(true)
        .enableSwipe(true)
        .spacing(when (readingMode) {
            ReadingMode.CONTINUOUS_HORIZONTAL, ReadingMode.CONTINUOUS_VERTICAL -> 8
            else -> 0
        })
        .pageFitPolicy(when (viewModel.viewerState.value.settings.scaleType) {
            ScaleType.FIT_PAGE -> FitPolicy.BOTH
            ScaleType.FIT_WIDTH -> FitPolicy.WIDTH
            ScaleType.FIT_HEIGHT -> FitPolicy.HEIGHT
            else -> FitPolicy.WIDTH
        })
        .nightMode(viewModel.viewerState.value.brightness.nightMode)
        .onLoad {
            post {
                viewModel.onPdfLoadComplete(pageCount)
                viewModel.updatePageCount(currentPage, pageCount, bookId)
            }
        }
        .onPageChange { page, pageCount ->
            post {
                viewModel.updatePageCount(page, pageCount, bookId)
                onPageChange(page, pageCount)
            }
        }
        .onTap { _ ->  // Added this back
            viewModel.toggleControls()
            true
        }
        .onRender { page ->
            post {
                if (page == currentPage) {
                    viewModel.updateLoadingState(false)
                }
            }
        }
        .apply {
            when (readingMode) {
                ReadingMode.LEFT_TO_RIGHT -> {
                    swipeHorizontal(true)
                    pageSnap(true)
                    pageFling(true)
                    spacing(1)
                }
                ReadingMode.CONTINUOUS_HORIZONTAL -> {
                    swipeHorizontal(true)
                    pageSnap(true)
                    pageFling(false)
                    spacing(0)
                }
                ReadingMode.VERTICAL_PAGED -> {
                    swipeHorizontal(false)
                    pageSnap(true)
                    pageFling(true)
                    spacing(0)
                }
                ReadingMode.CONTINUOUS_VERTICAL -> {
                    swipeHorizontal(false)
                    pageSnap(true)
                    pageFling(false)
                    spacing(0)
                }
            }
        }
        .load()
}


// Extension function to safely post to main thread
private fun PDFView.post(action: () -> Unit) {
    if (isAttachedToWindow) {
        post(action)
    } else {
        action()
    }
}

private val PDFView.pageSnap: Boolean
    get() = true  // Default to true since we can't actually access this property

private val PDFView.isSwipeHorizontal: Boolean
    get() = true  // Default to true since we can't actually access this property
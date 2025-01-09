package com.example.naivety

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.draw.shadow
import com.example.naivety.ui.theme.NaivetyTheme
import com.example.naivety.viewmodels.PdfViewerViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import androidx.core.graphics.scaleMatrix
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy



class PdfViewerActivity : ComponentActivity() {
    private val viewModel: PdfViewerViewModel by viewModels()
    private var bookId: String? = null
    private lateinit var pdfView: PDFView
    private val LocalViewModel = compositionLocalOf<PdfViewerViewModel> {
        error("No ViewModel provided")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow()

        bookId = intent.getStringExtra("BOOK_ID")
        val lastPage = intent.getIntExtra("LAST_PAGE", 0)

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
        CompositionLocalProvider(LocalViewModel provides viewModel) {
            val showSettings = remember { mutableStateOf(false) }
            val showReadingMode = remember { mutableStateOf(false) }
            val showRotation = remember { mutableStateOf(false) }
            val showBrightness = remember { mutableStateOf(false) }
            val context = LocalContext.current

            val viewerState = viewModel.viewerState.collectAsState()
            val isBookmarked = remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(viewerState.value.settings.backgroundColor))
            ) {

                var currentReadingMode by remember {
                    mutableStateOf(ReadingMode.VERTICAL_PAGED) // Set default mode
                }

                // PDF View with improved settings
                AndroidView(
                    factory = { context ->
                        PDFView(context, null).apply {
                            configurePdfView(
                                uri = intent.data!!,
                                lastPage = lastPage,
                                readingMode = viewerState.value.readingMode,
                                viewModel = viewModel,
                                onPageChange = { page, pageCount ->
                                    viewModel.updatePageCount(page, pageCount)
                                    onSaveProgress(page)
                                }
                            )
                        }.also { pdfView = it }
                    },
                    update = { view ->
                        // Only update when reading mode changes its core behavior
                        val newReadingMode = viewerState.value.readingMode
                        val oldReadingMode = when {
                            view.isSwipeHorizontal -> {
                                if (view.pageSnap) ReadingMode.LEFT_TO_RIGHT
                                else ReadingMode.CONTINUOUS_HORIZONTAL
                            }

                            else -> {
                                if (view.pageSnap) ReadingMode.VERTICAL_PAGED
                                else ReadingMode.CONTINUOUS_VERTICAL
                            }
                        }

                        if (newReadingMode::class != oldReadingMode::class) {
                            val currentPage = view.currentPage
                            view.configurePdfView(
                                uri = intent.data!!,
                                lastPage = currentPage,
                                readingMode = newReadingMode,
                                viewModel = viewModel,
                                onPageChange = { page, pageCount ->
                                    viewModel.updatePageCount(page, pageCount)
                                    onSaveProgress(page)
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
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
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "${viewerState.value.currentPage + 1}/${viewerState.value.totalPages}",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Custom Navigation Bar
                // Modify the AnimatedVisibility block for the navigation bar
                AnimatedVisibility(
                    visible = viewerState.value.isControlsVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp) // Add bottom padding for floating effect
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.91f) // Make the bar 85% of screen width
                            .padding(horizontal = 16.dp), // Add horizontal padding
                        color = Color.Black.copy(alpha = 0.90f), // Slightly transparent
                        shape = RoundedCornerShape(28.dp), // Round all corners
                        shadowElevation = 8.dp // Add elevation for floating effect
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 12.dp, horizontal = 8.dp), // Adjust internal padding
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
                                    tint = Color(0xFF8E42FF)
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
                                    tint = Color(0xFF8E42FF)
                                )
                            }

                            // Bookmark Button
                            IconButton(
                                onClick = {
                                    isBookmarked.value = !isBookmarked.value
                                    if (isBookmarked.value) {
                                        viewModel.addBookmark(viewerState.value.currentPage)
                                    } else {
                                        viewModel.removeBookmark(viewerState.value.currentPage)
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked.value)
                                        Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = Color(0xFF8E42FF)
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
                                    tint = Color(0xFF8E42FF)
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
                                    tint = Color(0xFF8E42FF)
                                )
                            }
                        }
                    }
                }

                // Modal Sheets
                if (showSettings.value) {
                    MainSettingsSheet(
                        settings = viewerState.value.settings,
                        onSettingsChange = { settings ->
                            viewModel.updateSettings(settings)
                            window.attributes = window.attributes.apply {
                                if (settings.keepScreenOn) {
                                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                } else {
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                }
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
                                onPageChange = { page, pageCount ->
                                    viewModel.updatePageCount(page, pageCount)
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
                                    viewModel.updatePageCount(page, pageCount)
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
            }
        }
    }

    private fun setupWindow() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
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
        viewModel.saveSettings(bookId)
    }

    override fun onDestroy() {
        super.onDestroy()
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
    onPageChange: (Int, Int) -> Unit
) {
    setBackgroundColor(android.graphics.Color.BLACK)

    fromUri(uri)
        .defaultPage(lastPage)
        .onPageChange { page, pageCount ->
            onPageChange(page, pageCount)
        }
        .onLoad {
            viewModel.updateLoadingState(false)
        }
        .enableSwipe(true)
        .enableDoubletap(false)
        .enableAnnotationRendering(true)
        .nightMode(viewModel.viewerState.value.brightness.nightMode)  // Use current night mode setting
        .onTap { e ->
            viewModel.toggleControls()
            true
        }
        .apply {
            when (readingMode) {
                ReadingMode.LEFT_TO_RIGHT -> {
                    swipeHorizontal(true)
                    spacing(0)
                    autoSpacing(false)
                    pageFitPolicy(FitPolicy.WIDTH)
                    pageSnap(true)
                    pageFling(true)
                }
                ReadingMode.CONTINUOUS_HORIZONTAL -> {
                    swipeHorizontal(true)
                    spacing(8)
                    autoSpacing(true)
                    pageFitPolicy(FitPolicy.WIDTH)
                    pageSnap(false)
                    pageFling(false)
                }
                ReadingMode.VERTICAL_PAGED -> {
                    swipeHorizontal(false)
                    spacing(0)
                    autoSpacing(false)
                    pageFitPolicy(FitPolicy.WIDTH)
                    pageSnap(true)
                    pageFling(true)
                }
                ReadingMode.CONTINUOUS_VERTICAL -> {
                    swipeHorizontal(false)
                    spacing(8)
                    autoSpacing(true)
                    pageFitPolicy(FitPolicy.WIDTH)
                    pageSnap(false)
                    pageFling(false)
                }
            }
        }
        .load()

    // Additional settings after load
    post {
        setMinZoom(1.0f)
        setMidZoom(1.75f)
        setMaxZoom(3.0f)
        zoomTo(1.0f)
    }
}

// Add these helper methods to handle page counts and current page
private fun PDFView.getCurrentPage(): Int {
    return currentPage
}

private fun PDFView.getPageCount(): Int {
    return pageCount
}


private val PDFView.pageSnap: Boolean
    get() = true  // Default to true since we can't actually access this property

private val PDFView.isSwipeHorizontal: Boolean
    get() = true  // Default to true since we can't actually access this property
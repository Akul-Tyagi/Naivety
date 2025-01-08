package com.example.naivety

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
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
import com.example.naivety.ui.theme.NaivetyTheme
import com.example.naivety.viewmodels.PdfViewerViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy

class PdfViewerActivity : ComponentActivity() {
    private val viewModel: PdfViewerViewModel by viewModels()
    private var bookId: String? = null
    private lateinit var pdfView: PDFView

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
        val showSettings = remember { mutableStateOf(false) }
        val showReadingMode = remember { mutableStateOf(false) }
        val showRotation = remember { mutableStateOf(false) }
        val showBrightness = remember { mutableStateOf(false) }
        val showQuickNav = remember { mutableStateOf(false) }

        val viewerState = viewModel.viewerState.collectAsState()
        val isBookmarked = remember { mutableStateOf(false) }
        val context = LocalContext.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(viewerState.value.settings.backgroundColor))
        ) {
            // PDF View
            // PDF View
            AndroidView(
                factory = { context ->
                    PDFView(context, null).apply {
                        fromUri(intent.data!!)
                            .defaultPage(lastPage)
                            .onPageChange { page, pageCount ->
                                viewModel.updatePageCount(page, pageCount)
                                onSaveProgress(page)
                            }
                            .onLoad { viewModel.updateLoadingState(false) }
                            .enableSwipe(true)
                            .swipeHorizontal(viewerState.value.readingMode.isHorizontalMode())
                            .spacing(10)
                            .pageSnap(true)
                            .pageFling(true)
                            .nightMode(viewerState.value.settings.backgroundColor == 0xFF000000)
                            .autoSpacing(true)
                            .pageFitPolicy(FitPolicy.WIDTH)
                            .pageSnap(viewerState.value.settings.animatePageTransition)
                            .enableDoubletap(false)  // Disable double tap
                            .onTap { e ->           // Add custom tap handler
                                viewModel.toggleControls()
                                true
                            }
                            .onError { t ->
                                t.printStackTrace()
                                Toast.makeText(context, "Error loading PDF: ${t.message}", Toast.LENGTH_LONG).show()
                            }
                            .load()
                    }.also { pdfView = it }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Loading Indicator
            if (viewerState.value.isLoading) {
                PdfLoadingAnimation(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Page Number Indicator
            AnimatedVisibility(
                visible = viewerState.value.settings.showPageNumber && !viewerState.value.isControlsVisible,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "${viewerState.value.currentPage + 1}/${viewerState.value.totalPages}",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // In your Box composable, replace the TopAppBar section with this bottom navigation implementation:

// Controls
            AnimatedVisibility(
                visible = viewerState.value.isControlsVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(vertical = 8.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reading Mode
                        IconButton(onClick = { showReadingMode.value = true }) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = "Reading Mode",
                                    tint = Color.White
                                )
                                Text(
                                    "Reading",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Brightness
                        IconButton(onClick = { showBrightness.value = true }) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BrightnessHigh,
                                    contentDescription = "Brightness",
                                    tint = Color.White
                                )
                                Text(
                                    "Brightness",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Bookmark
                        IconButton(
                            onClick = {
                                val newValue = !isBookmarked.value
                                isBookmarked.value = newValue
                                if (newValue) {
                                    viewModel.addBookmark(viewerState.value.currentPage)
                                } else {
                                    viewModel.removeBookmark(viewerState.value.currentPage)
                                }
                            }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked.value) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = Color.White
                                )
                                Text(
                                    "Bookmark",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Quick Navigation
                        IconButton(onClick = { showQuickNav.value = true }) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ListAlt,
                                    contentDescription = "Navigation",
                                    tint = Color.White
                                )
                                Text(
                                    "Navigate",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Settings
                        IconButton(onClick = { showSettings.value = true }) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White
                                )
                                Text(
                                    "Settings",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
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
                        if (settings.keepScreenOn) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
                        pdfView.fromUri(intent.data!!)
                            .defaultPage(viewerState.value.currentPage)
                            .swipeHorizontal(mode.isHorizontalMode())
                            .load()
                        showReadingMode.value = false
                    },
                    onDismiss = { showReadingMode.value = false }
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
                            RotationMode.LOCKED_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
                            RotationMode.LOCKED_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            RotationMode.REVERSE_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                            RotationMode.FREE -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                        showRotation.value = false
                    },
                    onDismiss = { showRotation.value = false }
                )
            }

            if (showBrightness.value) {
                BrightnessSheet(
                    settings = viewerState.value.brightness,
                    onSettingsChange = { brightnessSettings ->
                        viewModel.updateBrightness(brightnessSettings)
                        window.attributes = window.attributes.apply {
                            screenBrightness = brightnessSettings.customBrightness
                        }
                        val isDark = brightnessSettings.colorFilter.run {
                            (red + green + blue) / 3 < 0.5f
                        }
                        pdfView.fromUri(intent.data!!)
                            .defaultPage(pdfView.currentPage)
                            .nightMode(isDark)
                            .load()
                    },
                    onDismiss = { showBrightness.value = false }
                )
            }

            if (showQuickNav.value) {
                QuickNavigation(
                    currentPage = viewerState.value.currentPage,
                    totalPages = viewerState.value.totalPages,
                    onPageSelect = { page ->
                        pdfView.jumpTo(page)
                        showQuickNav.value = false
                    },
                    onDismiss = { showQuickNav.value = false }
                )
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
    return this == ReadingMode.LEFT_TO_RIGHT || this == ReadingMode.RIGHT_TO_LEFT
}
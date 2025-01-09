// app/src/main/java/com/example/naivety/ui/pdf/PdfViewerState.kt
package com.example.naivety.ui.pdf

data class PdfViewerState(
    val isControlsVisible: Boolean = false,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val isLoading: Boolean = true,
    val settings: PdfSettings = PdfSettings(),
    val readingMode: ReadingMode = ReadingMode.VERTICAL_PAGED,
    val rotation: RotationMode = RotationMode.PORTRAIT,
    val brightness: BrightnessSettings = BrightnessSettings()
)

data class PdfSettings(
    val backgroundColor: Long = 0xFF000000,
    val showPageNumber: Boolean = true,
    val isFullscreen: Boolean = true,
    val keepScreenOn: Boolean = false,
    val animatePageTransition: Boolean = true,
    val scaleType: ScaleType = ScaleType.FIT_PAGE,
    val cropBorders: Boolean = false
)

data class BrightnessSettings(
    val customBrightness: Float = 0.5f,
    val useSystemBrightness: Boolean = true,
    val nightMode: Boolean = false,
    val isGreyscale: Boolean = false,
    val contrast: Float = 1.0f,
    val redLevel: Float = 1.0f,
    val greenLevel: Float = 1.0f,
    val blueLevel: Float = 1.0f
)

data class ColorFilter(
    val red: Float = 1f,
    val green: Float = 1f,
    val blue: Float = 1f,
    val alpha: Float = 1f
)

enum class ReadingMode {
    LEFT_TO_RIGHT,
    CONTINUOUS_HORIZONTAL,
    VERTICAL_PAGED,
    CONTINUOUS_VERTICAL
}

enum class RotationMode {
    FREE,
    PORTRAIT,
    LANDSCAPE,
    LOCKED_PORTRAIT,
    LOCKED_LANDSCAPE,
    REVERSE_PORTRAIT
}

enum class ScaleType {
    FIT_PAGE,
    FIT_WIDTH,
    FIT_HEIGHT,
    ZOOM
}
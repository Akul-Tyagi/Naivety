package com.abundance.naivety.epub

import com.abundance.naivety.ui.components.epub.modals.EpubDisplaySettings
import com.abundance.naivety.ui.components.epub.modals.EpubReadingMode
import com.abundance.naivety.ui.components.epub.modals.EpubTypographySettings
import com.abundance.naivety.ui.pdf.RotationMode

/**
 * State class for EPUB viewer - mirrors PdfViewerState structure
 */
data class EpubViewerState(
    val isControlsVisible: Boolean = false,
    val currentPage: Int = 0,           // Current page within chapter (for paginated modes)
    val totalPages: Int = 1,            // Total pages in current chapter (for paginated modes)
    val currentChapter: Int = 0,        // Current chapter index
    val totalChapters: Int = 1,         // Total chapters in book
    val globalPage: Int = 1,            // Global page number for display (1-based)
    val globalTotalPages: Int = 1,      // Total pages across all chapters
    val isLoading: Boolean = true,
    val readingMode: EpubReadingMode = EpubReadingMode.CHAPTER_SCROLL,
    val rotation: RotationMode = RotationMode.PORTRAIT,
    val displaySettings: EpubDisplaySettings = EpubDisplaySettings(),
    val typographySettings: EpubTypographySettings = EpubTypographySettings(),
    val isCurrentPageBookmarked: Boolean = false,
    val chapterPageCounts: List<Int> = emptyList(),  // Pages per chapter for global calculation
    val isContinuousMode: Boolean = false  // True when in continuous vertical mode
)

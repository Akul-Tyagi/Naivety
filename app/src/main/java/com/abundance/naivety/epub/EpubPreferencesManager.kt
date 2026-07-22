package com.abundance.naivety.epub

import android.content.Context
import android.content.SharedPreferences
import com.abundance.naivety.ui.components.epub.modals.*
import com.abundance.naivety.ui.pdf.RotationMode

/**
 * Manages EPUB reader preferences - saving and loading settings
 */
class EpubPreferencesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "epub_viewer_prefs"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save current reading progress (chapter and page within chapter)
     */
    fun saveProgress(bookId: String, chapter: Int, page: Int) {
        prefs.edit()
            .putInt("${bookId}_last_chapter", chapter)
            .putInt("${bookId}_last_page_in_chapter", page)
            .apply()
    }

    /**
     * Load last read chapter
     */
    fun loadLastChapter(bookId: String): Int {
        return prefs.getInt("${bookId}_last_chapter", 0)
    }

    /**
     * Load last read page within chapter
     */
    fun loadLastPageInChapter(bookId: String): Int {
        return prefs.getInt("${bookId}_last_page_in_chapter", 0)
    }

    /**
     * Save book display name
     */
    fun saveBookName(bookId: String, name: String) {
        prefs.edit().putString("${bookId}_book_name", name).apply()
    }

    /**
     * Get saved book display name
     */
    fun getSavedBookName(bookId: String): String? {
        return prefs.getString("${bookId}_book_name", null)
    }

    /**
     * Save all EPUB reader settings
     */
    fun saveSettings(bookId: String, state: EpubViewerState) {
        prefs.edit().apply {
            putString("${bookId}_epub_reading_mode", state.readingMode.name)
            putBoolean("${bookId}_use_system_brightness", state.displaySettings.useSystemBrightness)
            putFloat("${bookId}_custom_brightness", state.displaySettings.customBrightness)
            putString("${bookId}_page_theme", state.displaySettings.pageTheme.name)
            putFloat("${bookId}_page_margin", state.displaySettings.pageMargin)
            putFloat("${bookId}_font_size", state.typographySettings.fontSize)
            putString("${bookId}_font_family", state.typographySettings.fontFamily.name)
            // Save font color - use "AUTO" string for null (auto/theme color)
            putString("${bookId}_font_color", state.typographySettings.fontColor?.name ?: "AUTO")
            putFloat("${bookId}_line_spacing", state.typographySettings.lineSpacing)
            putFloat("${bookId}_font_weight", state.typographySettings.fontWeight)
            putString("${bookId}_text_alignment", state.typographySettings.textAlignment.name)
            putBoolean("${bookId}_keep_screen_on", state.typographySettings.keepScreenOn)
            putBoolean("${bookId}_show_page_number", state.typographySettings.showPageNumber)
            apply()
        }
    }

    /**
     * Load all EPUB reader settings
     */
    fun loadSettings(bookId: String): EpubViewerState {
        val savedReadingMode = try {
            EpubReadingMode.valueOf(prefs.getString("${bookId}_epub_reading_mode", EpubReadingMode.CHAPTER_SCROLL.name) ?: EpubReadingMode.CHAPTER_SCROLL.name)
        } catch (_: Exception) {
            EpubReadingMode.CHAPTER_SCROLL
        }

        val savedPageTheme = try {
            EpubPageTheme.valueOf(prefs.getString("${bookId}_page_theme", EpubPageTheme.WHITE.name) ?: EpubPageTheme.WHITE.name)
        } catch (_: Exception) {
            EpubPageTheme.WHITE
        }

        val savedFontFamily = try {
            EpubFontFamily.valueOf(prefs.getString("${bookId}_font_family", EpubFontFamily.BOOKERLY.name) ?: EpubFontFamily.BOOKERLY.name)
        } catch (_: Exception) {
            EpubFontFamily.BOOKERLY
        }

        val savedTextAlignment = try {
            EpubTextAlignment.valueOf(prefs.getString("${bookId}_text_alignment", EpubTextAlignment.JUSTIFY.name) ?: EpubTextAlignment.JUSTIFY.name)
        } catch (_: Exception) {
            EpubTextAlignment.JUSTIFY
        }

        // Load font color - "AUTO" or missing means null (auto/theme color)
        val savedFontColor = prefs.getString("${bookId}_font_color", "AUTO")?.let { colorName ->
            if (colorName == "AUTO") null
            else try { EpubFontColor.valueOf(colorName) } catch (_: Exception) { null }
        }

        return EpubViewerState(
            readingMode = savedReadingMode,
            displaySettings = EpubDisplaySettings(
                useSystemBrightness = prefs.getBoolean("${bookId}_use_system_brightness", true),
                customBrightness = prefs.getFloat("${bookId}_custom_brightness", 0.5f),
                pageTheme = savedPageTheme,
                pageMargin = prefs.getFloat("${bookId}_page_margin", 32f)
            ),
            typographySettings = EpubTypographySettings(
                fontSize = prefs.getFloat("${bookId}_font_size", 30f),
                fontFamily = savedFontFamily,
                fontColor = savedFontColor,
                lineSpacing = prefs.getFloat("${bookId}_line_spacing", 1.6f),
                fontWeight = prefs.getFloat("${bookId}_font_weight", 400f),
                textAlignment = savedTextAlignment,
                keepScreenOn = prefs.getBoolean("${bookId}_keep_screen_on", false),
                showPageNumber = prefs.getBoolean("${bookId}_show_page_number", true)
            )
        )
    }
}

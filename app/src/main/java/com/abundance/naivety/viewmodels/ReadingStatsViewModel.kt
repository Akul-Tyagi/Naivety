package com.abundance.naivety.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abundance.naivety.data.Achievement
import com.abundance.naivety.data.ReadingDayDao
import com.abundance.naivety.repository.ReadingStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ReadingStatsViewModel @Inject constructor(
    private val repository: ReadingStatsRepository,
    private val readingDayDao: ReadingDayDao
) : ViewModel() {

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    val readingDays = repository.readingDays
    val selectedYear = repository.selectedYear
    private val _availableYears = MutableStateFlow<List<Int>>(generateYearRange())
    val availableYears: StateFlow<List<Int>> = _availableYears.asStateFlow()

    // Expose total stats
    val totalPagesRead = repository.totalPagesRead
    val totalTimeSpent = repository.totalTimeSpent

    // Expose format-specific stats
    val pdfPagesRead = repository.pdfPagesRead
    val pdfTimeSpent = repository.pdfTimeSpent
    val epubEstimatedPages = repository.epubEstimatedPages
    val epubTimeSpent = repository.epubTimeSpent
    val epubChaptersRead = repository.epubChaptersRead

    init {
        loadAchievements()
        loadAvailableYears()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            repository.achievements.collect {
                _achievements.value = it
            }
        }
    }

    private fun loadAvailableYears() {
        viewModelScope.launch {
            // Combine repository years with generated range
            repository.availableYears.collect { years ->
                val currentYearRange = generateYearRange()
                val combinedYears = (years + currentYearRange).distinct().sorted()
                _availableYears.value = combinedYears
            }
        }
    }

    // Generate a range of years from 2023 to current year
    private fun generateYearRange(): List<Int> {
        val currentYear = LocalDate.now().year
        return (2024..currentYear).toList()
    }

    // Add to ReadingStatsViewModel class
    fun refreshReadingData() {
        viewModelScope.launch {
            // No need for repository.refreshReadingData() since we're using Flow collection
            loadAvailableYears()
        }
    }

    fun updateReadingProgress(bookId: String, pagesRead: Int, minutesSpent: Int) {
        viewModelScope.launch {
            repository.logReadingSession(
                bookId = bookId,
                pagesRead = pagesRead,
                timeSpentMinutes = minutesSpent
            )
        }
    }

    // Add this function
    fun selectYear(year: Int) {
        repository.selectYear(year)
    }

    fun shareAchievements() {
        // Implementation for sharing achievements
        // This can be expanded later with actual sharing functionality
    }

    suspend fun logReadingSession(bookId: String, pagesRead: Int, timeSpentMinutes: Int) {
        // Use repository method for proper date grouping and accumulation
        repository.logReadingSession(bookId, pagesRead, timeSpentMinutes)
    }

    /**
     * Log an EPUB reading session with chapter-based tracking.
     * Pages are automatically estimated from reading time.
     */
    suspend fun logEpubReadingSession(
        bookId: String,
        timeSpentMinutes: Int,
        startChapter: Int,
        endChapter: Int
    ) {
        repository.logEpubReadingSession(
            bookId = bookId,
            timeSpentMinutes = timeSpentMinutes,
            chaptersRead = (endChapter - startChapter).coerceAtLeast(0),
            startChapter = startChapter,
            endChapter = endChapter
        )
    }

}
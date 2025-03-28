package com.example.naivety.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naivety.data.Achievement
import com.example.naivety.data.ReadingDay
import com.example.naivety.repository.ReadingStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ReadingStatsViewModel @Inject constructor(
    private val repository: ReadingStatsRepository
) : ViewModel() {

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    val readingDays = repository.readingDays
    val selectedYear = repository.selectedYear
    private val _availableYears = MutableStateFlow<List<Int>>(generateYearRange())
    val availableYears: StateFlow<List<Int>> = _availableYears.asStateFlow()

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
}
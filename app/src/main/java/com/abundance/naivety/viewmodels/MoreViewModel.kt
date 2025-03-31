// MoreViewModel.kt
package com.abundance.naivety.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abundance.naivety.data.Achievement
import com.abundance.naivety.repository.ReadingStatsRepository
import com.abundance.naivety.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val readingStatsRepository: ReadingStatsRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val readingDays = readingStatsRepository.readingDays
    val selectedYear = readingStatsRepository.selectedYear
    val availableYears = readingStatsRepository.availableYears

    val streakGoal = readingStatsRepository.streakGoal

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.isDarkTheme.collectLatest {
                _isDarkTheme.value = it
            }
        }

        // Collect current streak
        viewModelScope.launch {
            readingStatsRepository.currentStreak.collectLatest {
                _currentStreak.value = it
            }
        }

        // Collect achievements
        viewModelScope.launch {
            readingStatsRepository.achievements.collectLatest {
                _achievements.value = it
            }
        }

        // Collect longest streak
        viewModelScope.launch {
            readingStatsRepository.longestStreak.collectLatest {
                _longestStreak.value = it
            }
        }
    }

    fun updateStreakGoal(goal: Int) {
        readingStatsRepository.updateStreakGoal(goal)
    }

    fun shareAchievements() {
        readingStatsRepository.shareAchievements()
    }

    fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this amazing book reading app: Naivety https://play.google.com/store/apps/details?id=com.abundance.naivety")
            type = "text/plain"
        }
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(Intent.createChooser(shareIntent, "Share via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun rateApp() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("market://details?id=com.abundance.naivety")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
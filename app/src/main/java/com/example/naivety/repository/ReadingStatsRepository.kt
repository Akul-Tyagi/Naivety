// app/src/main/java/com/example/naivety/repository/ReadingStatsRepository.kt
package com.example.naivety.repository

import android.content.Context
import com.example.naivety.data.Achievement
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingStatsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("reading_stats_prefs", Context.MODE_PRIVATE)

    private val _streakGoal = MutableStateFlow(preferences.getInt("streak_goal", 30))
    val streakGoal: Flow<Int> = _streakGoal.asStateFlow()

    // Add this method to update streak goal
    fun updateStreakGoal(goal: Int) {
        preferences.edit().putInt("streak_goal", goal).apply()
        _streakGoal.value = goal
    }
    private val allAchievements = listOf(
        // First book achievement (existing)
        Achievement(
            id = "first_book",
            title = "First Step",
            description = "Read your first book",
            iconName = "ic_achievement_book",
            unlocked = true,
            dateUnlocked = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000
        ),

        // Reading duration achievements
        Achievement(
            id = "marathon_reader",
            title = "Marathon Reader",
            description = "Read for 3 hours in a single day",
            iconName = "ic_achievement_clock",
            unlocked = false
        ),
        Achievement(
            id = "endurance_reader",
            title = "Endurance Reader",
            description = "Read for 6 hours in a single day",
            iconName = "ic_achievement_clock",
            unlocked = false
        ),
        Achievement(
            id = "reading_machine",
            title = "Reading Machine",
            description = "Read for 9 hours in a single day",
            iconName = "ic_achievement_clock",
            unlocked = false
        ),
        Achievement(
            id = "day_devotee",
            title = "Day Devotee",
            description = "Read for 12 hours in a single day",
            iconName = "ic_achievement_clock",
            unlocked = false
        ),

        // Streak achievements
        Achievement(
            id = "streak_7",
            title = "Weekly Habit",
            description = "Read 7 days in a row",
            iconName = "ic_achievement_fire",
            unlocked = true,
            dateUnlocked = System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000
        ),
        Achievement(
            id = "streak_14",
            title = "Consistent Reader",
            description = "Read 14 days in a row",
            iconName = "ic_achievement_fire",
            unlocked = false
        ),
        Achievement(
            id = "streak_21",
            title = "Reading Ritual",
            description = "Read 21 days in a row",
            iconName = "ic_achievement_fire",
            unlocked = false
        ),
        Achievement(
            id = "streak_30",
            title = "Monthly Dedication",
            description = "Read 30 days in a row",
            iconName = "ic_achievement_fire",
            unlocked = false
        ),
        Achievement(
            id = "streak_50",
            title = "Reading Champion",
            description = "Read 50 days in a row",
            iconName = "ic_achievement_fire",
            unlocked = false
        ),
        Achievement(
            id = "streak_100",
            title = "Reading Legend",
            description = "Read 100 days in a row",
            iconName = "ic_achievement_fire",
            unlocked = false
        ),

        // Pages read achievements
        Achievement(
            id = "pages_100",
            title = "Page Turner",
            description = "Read 100 pages in total",
            iconName = "ic_achievement_pages",
            unlocked = false
        ),
        Achievement(
            id = "pages_500",
            title = "Book Explorer",
            description = "Read 500 pages in total",
            iconName = "ic_achievement_pages",
            unlocked = false
        ),
        Achievement(
            id = "pages_1000",
            title = "Literature Lover",
            description = "Read 1,000 pages in total",
            iconName = "ic_achievement_pages",
            unlocked = false
        ),
        Achievement(
            id = "pages_5000",
            title = "Library Dweller",
            description = "Read 5,000 pages in total",
            iconName = "ic_achievement_pages",
            unlocked = false
        ),
        Achievement(
            id = "pages_10000",
            title = "Bibliophile",
            description = "Read 10,000 pages in total",
            iconName = "ic_achievement_pages",
            unlocked = false
        ),

        // Books completed achievements
        Achievement(
            id = "books_5",
            title = "Book Collector",
            description = "Complete 5 books",
            iconName = "ic_achievement_book",
            unlocked = false
        ),
        Achievement(
            id = "books_10",
            title = "Book Connoisseur",
            description = "Complete 10 books",
            iconName = "ic_achievement_book",
            unlocked = false
        ),

        // Special time/condition achievements
        Achievement(
            id = "night_owl",
            title = "Night Owl",
            description = "Read after midnight for 5 days",
            iconName = "ic_achievement_moon",
            unlocked = false
        ),
        Achievement(
            id = "early_bird",
            title = "Early Bird",
            description = "Read before 8am for 5 days",
            iconName = "ic_achievement_sun",
            unlocked = false
        ),
        Achievement(
            id = "weekend_warrior",
            title = "Weekend Warrior",
            description = "Read for 3+ hours on 5 weekends",
            iconName = "ic_achievement_calendar",
            unlocked = false
        )
    )

    private val _achievements = MutableStateFlow(allAchievements)
    val achievements: Flow<List<Achievement>> = _achievements.asStateFlow()

    // Reading Day class
    data class ReadingDay(
        val date: Long,
        val pagesRead: Int,
        val timeSpentMinutes: Int,
        val bookIds: List<String>
    )

    // Sample reading days
    private val sampleReadingDays = listOf(
        ReadingDay(
            date = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000, // 5 days ago
            pagesRead = 35,
            timeSpentMinutes = 60,
            bookIds = listOf("book1")
        ),
        ReadingDay(
            date = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000, // 4 days ago
            pagesRead = 42,
            timeSpentMinutes = 75,
            bookIds = listOf("book2")
        ),
        ReadingDay(
            date = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000, // 2 days ago
            pagesRead = 28,
            timeSpentMinutes = 45,
            bookIds = listOf("book1")
        ),
        ReadingDay(
            date = System.currentTimeMillis() - 24 * 60 * 60 * 1000, // yesterday
            pagesRead = 50,
            timeSpentMinutes = 90,
            bookIds = listOf("book3")
        ),
        ReadingDay(
            date = System.currentTimeMillis(), // today
            pagesRead = 20,
            timeSpentMinutes = 30,
            bookIds = listOf("book3")
        )
    )

    private val _readingDays = MutableStateFlow(sampleReadingDays)
    val readingDays: Flow<List<ReadingDay>> = _readingDays.asStateFlow()

    private val _selectedYear = MutableStateFlow(2025)
    val selectedYear: Flow<Int> = _selectedYear.asStateFlow()

    private val _availableYears = MutableStateFlow(listOf(2024, 2025))
    val availableYears: Flow<List<Int>> = _availableYears.asStateFlow()

    fun selectYear(year: Int) {
        _selectedYear.value = year
    }

    fun getCurrentStreak(): Flow<Int> {
        return readingDays.map { days ->
            // Sort days by date (newest first)
            val sortedDays = days.sortedByDescending { it.date }

            if (sortedDays.isEmpty()) {
                return@map 0
            }

            // Check if today has reading activity
            val today = java.time.LocalDate.now()
            val yesterday = today.minusDays(1)
            val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val yesterdayMillis =
                today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val hasTodayReading = sortedDays.any {
                it.date >= todayMillis && it.date < todayMillis + 24 * 60 * 60 * 1000
            }

            val hasYesterdayReading = sortedDays.any {
                it.date >= yesterdayMillis && it.date < todayMillis
            }

            // If no reading today or yesterday, no active streak
            if (!hasTodayReading && !hasYesterdayReading) {
                return@map 0
            }

            // Count streak by checking consecutive days
            var streak = if (hasTodayReading) 1 else 0
            var currentDate = if (hasTodayReading) yesterday else today.minusDays(2)

            while (true) {
                val currentDateMillis =
                    currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val nextDateMillis =
                    currentDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
                        .toEpochMilli()

                val hasReadingOnDate = sortedDays.any {
                    it.date >= currentDateMillis && it.date < nextDateMillis
                }

                if (!hasReadingOnDate) {
                    break
                }

                streak++
                currentDate = currentDate.minusDays(1)
            }

            streak
        }
    }
    // Add after getCurrentStreak()
    fun getLongestStreak(): Flow<Int> {
        return readingDays.map { days ->
            if (days.isEmpty()) {
                return@map 0
            }

            // Sort days by date (oldest first)
            val sortedDays = days.sortedBy { it.date }

            // Group reading days by their calendar date
            val readingDateSet = sortedDays.map {
                val instant = java.time.Instant.ofEpochMilli(it.date)
                val zdt = instant.atZone(ZoneId.systemDefault())
                // Create a normalized date value (days since epoch)
                zdt.toLocalDate().toEpochDay()
            }.toSet()

            var currentStreak = 0
            var maxStreak = 0
            var lastDay: Long? = null

            // Process all reading days chronologically
            readingDateSet.sorted().forEach { dayEpoch ->
                if (lastDay == null || dayEpoch == lastDay!! + 1) {
                    // Consecutive day
                    currentStreak++
                } else {
                    // Gap detected, reset streak
                    currentStreak = 1
                }

                maxStreak = maxOf(maxStreak, currentStreak)
                lastDay = dayEpoch
            }

            maxStreak
        }
    }
}
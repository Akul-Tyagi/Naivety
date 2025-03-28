package com.example.naivety.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.naivety.data.Achievement
import com.example.naivety.data.ReadingDay
import com.example.naivety.data.ReadingDayDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class ReadingStatsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val readingDayDao: ReadingDayDao
) {
    // State flows for UI
    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear

    private val _availableYears = MutableStateFlow<List<Int>>(emptyList())
    val availableYears: StateFlow<List<Int>> = _availableYears

    // Reading days flow from DAO
    private val _readingDays = MutableStateFlow<List<ReadingDay>>(emptyList())
    val readingDays: StateFlow<List<ReadingDay>> = _readingDays

    // Current streak
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak

    // Longest streak
    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak

    // Achievements tracker
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements

    // User streak goal
    private val _streakGoal = MutableStateFlow(30) // Default: 30 days
    val streakGoal: StateFlow<Int> = _streakGoal

    // Total reading stats
    private val _totalPagesRead = MutableStateFlow(0)
    val totalPagesRead: StateFlow<Int> = _totalPagesRead

    private val _totalTimeSpent = MutableStateFlow(0)
    val totalTimeSpent: StateFlow<Int> = _totalTimeSpent

    // Tracking for special achievements
    private val _nightReadingSessions = MutableStateFlow(0)
    private val _earlyMorningReadingSessions = MutableStateFlow(0)
    private val _weekendReadingSessions = MutableStateFlow(0)
    private val _completedBooks = MutableStateFlow(0)

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Initialize with predefined achievements
    private val allAchievements = listOf(
        // First book achievement
        Achievement(
            id = "first_book",
            title = "First Step",
            description = "Read your first book",
            iconName = "ic_achievement_book",
            unlocked = false
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
            unlocked = false
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

    init {
        // Load saved achievements
        loadAchievements()

        // Load streak goal from preferences
        _streakGoal.value = context.getSharedPreferences("reading_stats", Context.MODE_PRIVATE)
            .getInt("streak_goal", 30)

        // Set up collection from the database for automatic updates
        viewModelScope.launch {
            readingDayDao.getAllReadingDaysFlow().collect { days ->
                _readingDays.value = days
                updateAllStats(days)
            }
        }
    }

    private fun loadAchievements() {
        val prefs = context.getSharedPreferences("achievements", Context.MODE_PRIVATE)
        val savedAchievements = allAchievements.map { achievement ->
            val unlocked = prefs.getBoolean("${achievement.id}_unlocked", false)
            val dateUnlocked = prefs.getLong("${achievement.id}_date", 0L)
            val progress = prefs.getFloat("${achievement.id}_progress", 0f)

            achievement.copy(
                unlocked = unlocked,
                dateUnlocked = if (unlocked) dateUnlocked else 0L,
                progress = progress
            )
        }
        _achievements.value = savedAchievements
    }

    private fun saveAchievements() {
        val prefs = context.getSharedPreferences("achievements", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        _achievements.value.forEach { achievement ->
            editor.putBoolean("${achievement.id}_unlocked", achievement.unlocked)
            editor.putLong("${achievement.id}_date", achievement.dateUnlocked)
            editor.putFloat("${achievement.id}_progress", achievement.progress)
        }

        editor.apply()
    }

    private fun updateAllStats(readingDays: List<ReadingDay>) {
        viewModelScope.launch {
            // Calculate total pages and time
            val totalPages = readingDays.sumOf { it.pagesRead }
            val totalMinutes = readingDays.sumOf { it.timeSpentMinutes }

            _totalPagesRead.value = totalPages
            _totalTimeSpent.value = totalMinutes

            // Update available years for UI selection
            updateAvailableYearsNonSuspend(readingDays)

            // Calculate streaks
            calculateStreaks(readingDays)

            // Update achievements based on new data
            updateAchievements(readingDays)
        }
    }

    private fun updateAvailableYearsNonSuspend(readingDays: List<ReadingDay>) {
        val years = readingDays.map { day ->
            Instant.ofEpochMilli(day.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .year
        }.distinct().sorted()

        if (years.isEmpty()) {
            _availableYears.value = listOf(LocalDate.now().year)
        } else {
            _availableYears.value = years
        }
    }

    fun selectYear(year: Int) {
        _selectedYear.value = year
    }

    fun updateStreakGoal(goal: Int) {
        _streakGoal.value = goal

        // Save to preferences
        val prefs = context.getSharedPreferences("reading_stats", Context.MODE_PRIVATE)
        prefs.edit().putInt("streak_goal", goal).apply()
    }

    private fun calculateStreaks(readingDays: List<ReadingDay>) {
        if (readingDays.isEmpty()) {
            _currentStreak.value = 0
            _longestStreak.value = 0
            return
        }

        // Get today and yesterday for streak calculation
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        // Convert reading days to LocalDate objects for easier comparison
        val dates = readingDays.map { day ->
            Instant.ofEpochMilli(day.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.distinct().sorted()

        // Check if there's a reading entry for today
        val hasReadingToday = dates.any { it.isEqual(today) }

        // Calculate current streak
        var streak = 0
        var checkDate = if (hasReadingToday) today else yesterday

        // Find the most recent continuous streak
        while (dates.any { it.isEqual(checkDate) }) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        // Calculate longest streak
        var longestStreak = 0
        var currentRun = 0
        var previousDate: LocalDate? = null

        for (date in dates) {
            if (previousDate == null) {
                // First entry
                currentRun = 1
            } else {
                // Check if consecutive day
                val daysBetween = ChronoUnit.DAYS.between(previousDate, date)

                if (daysBetween == 1L) {
                    // Consecutive day
                    currentRun++
                } else if (daysBetween != 0L) {
                    // Streak broken (and not the same day)
                    longestStreak = maxOf(longestStreak, currentRun)
                    currentRun = 1
                }
            }
            previousDate = date
        }

        // Check final streak
        longestStreak = maxOf(longestStreak, currentRun)

        // Update state
        _currentStreak.value = streak
        _longestStreak.value = longestStreak
    }

    private fun updateAchievements(readingDays: List<ReadingDay>) {
        if (readingDays.isEmpty()) return

        val totalDaysRead = readingDays.size
        val totalPagesRead = readingDays.sumOf { it.pagesRead }
        val totalTimeSpentMinutes = readingDays.sumOf { it.timeSpentMinutes }

        // Create a mutable copy of achievements to update
        val updatedAchievements = _achievements.value.toMutableList()
        val now = System.currentTimeMillis()

        // Update each achievement based on its criteria
        updatedAchievements.forEachIndexed { index, achievement ->
            when (achievement.id) {
                // Streak achievements
                "streak_7" -> {
                    if (!achievement.unlocked && _currentStreak.value >= 7) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }
                "streak_14" -> {
                    if (!achievement.unlocked && _currentStreak.value >= 14) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }
                "streak_21" -> {
                    if (!achievement.unlocked && _currentStreak.value >= 21) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }
                "streak_30" -> {
                    if (!achievement.unlocked && _currentStreak.value >= 30) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }
                "streak_50" -> {
                    if (!achievement.unlocked && _currentStreak.value >= 50) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }
                "streak_100" -> {
                    if (!achievement.unlocked && _currentStreak.value >= 100) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }

                // Pages read achievements
                "pages_100" -> {
                    if (!achievement.unlocked && totalPagesRead >= 100) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }
                "pages_500" -> {
                    if (!achievement.unlocked && totalPagesRead >= 500) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }
                "pages_1000" -> {
                    if (!achievement.unlocked && totalPagesRead >= 1000) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }
                "pages_5000" -> {
                    if (!achievement.unlocked && totalPagesRead >= 5000) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }
                "pages_10000" -> {
                    if (!achievement.unlocked && totalPagesRead >= 10000) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }

                // First book achievement
                "first_book" -> {
                    if (!achievement.unlocked && totalDaysRead > 0) {
                        updatedAchievements[index] = achievement.copy(
                            unlocked = true,
                            dateUnlocked = now
                        )
                    }
                }

                // Duration achievements
                "marathon_reader", "endurance_reader", "reading_machine", "day_devotee" -> {
                    // Check for single day reading durations
                    val maxDailyMinutes = readingDays
                        .groupBy { it.date }
                        .maxOfOrNull { (_, daysForDate) -> daysForDate.sumOf { it.timeSpentMinutes } } ?: 0

                    val hoursRead = maxDailyMinutes / 60.0

                    when (achievement.id) {
                        "marathon_reader" -> {
                            if (!achievement.unlocked && hoursRead >= 3) {
                                updatedAchievements[index] = achievement.copy(
                                    unlocked = true,
                                    dateUnlocked = now
                                )
                            }
                        }
                        "endurance_reader" -> {
                            if (!achievement.unlocked && hoursRead >= 6) {
                                updatedAchievements[index] = achievement.copy(
                                    unlocked = true,
                                    dateUnlocked = now
                                )
                            }
                        }
                        "reading_machine" -> {
                            if (!achievement.unlocked && hoursRead >= 9) {
                                updatedAchievements[index] = achievement.copy(
                                    unlocked = true,
                                    dateUnlocked = now
                                )
                            }
                        }
                        "day_devotee" -> {
                            if (!achievement.unlocked && hoursRead >= 12) {
                                updatedAchievements[index] = achievement.copy(
                                    unlocked = true,
                                    dateUnlocked = now
                                )
                            }
                        }
                    }
                }
            }
        }

        // Update achievements if changed
        if (updatedAchievements != _achievements.value) {
            _achievements.value = updatedAchievements
            saveAchievements() // Save achievements to preferences
        }
    }

    suspend fun logReadingSession(bookId: String, pagesRead: Int, timeSpentMinutes: Int) {
        // Use today's date at midnight for consistent grouping
        val today = LocalDate.now()
        val todayEpochMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Check if we already have an entry for today
        val existingDay = readingDayDao.getReadingDayForDate(todayEpochMillis)

        if (existingDay != null) {
            // Update existing day
            val updatedDay = existingDay.copy(
                pagesRead = existingDay.pagesRead + pagesRead,
                timeSpentMinutes = existingDay.timeSpentMinutes + timeSpentMinutes
            )
            readingDayDao.insertReadingDay(updatedDay)
        } else {
            // Create new day
            val newDay = ReadingDay(
                date = todayEpochMillis,
                bookId = bookId,
                pagesRead = pagesRead,
                timeSpentMinutes = timeSpentMinutes
            )
            readingDayDao.insertReadingDay(newDay)
        }

        // No need for explicit refresh - Flow collection will update data
    }

    // Method to share achievements
    fun shareAchievements() {
        val unlockedCount = _achievements.value.count { it.unlocked }
        val totalCount = _achievements.value.size

        val shareText = "I've unlocked $unlockedCount/$totalCount reading achievements in Naivety! " +
                "My current reading streak is ${_currentStreak.value} days, and I've read " +
                "${_totalPagesRead.value} pages overall. Download the app and challenge me!"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(Intent.createChooser(sendIntent, "Share Achievements"))
    }

    fun getStats(): Triple<Int, Int, Int> {
        return Triple(
            _totalPagesRead.value,
            _totalTimeSpent.value,
            _readingDays.value.size
        )
    }

    suspend fun getReadingDataForYear(year: Int): List<ReadingDay> {
        val startDate = LocalDate.of(year, 1, 1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val endDate = LocalDate.of(year, 12, 31)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 86400000 // Add one day in millis

        return readingDayDao.getReadingDaysInRange(startDate, endDate)
    }
}
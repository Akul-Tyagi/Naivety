package com.abundance.naivety.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abundance.naivety.data.ReadingDay
import com.abundance.naivety.viewmodels.ReadingStatsViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import androidx.compose.foundation.border
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import java.time.Instant

@Composable
fun ReadingHeatmapScreen(
    customFont: FontFamily,
    onBackPressed: () -> Unit
) {
    val viewModel: ReadingStatsViewModel = hiltViewModel()
    val readingDays by viewModel.readingDays.collectAsState(initial = emptyList())
    val selectedYear by viewModel.selectedYear.collectAsState(initial = LocalDate.now().year)
    val availableYears by viewModel.availableYears.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        viewModel.refreshReadingData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 19.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Reading Activity",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = customFont,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Year selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableYears) { year ->
                    YearTab(
                        year = year,
                        isSelected = year == selectedYear,
                        onSelect = { viewModel.selectYear(year) },
                        customFont = customFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Make the heatmap scrollable
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
            ) {
                val scrollState = androidx.compose.foundation.rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        // Heatmap grid
                        ReadingHeatmap(
                            readingDays = readingDays,
                            selectedYear = selectedYear,
                            customFont = customFont
                        )

                        // Add extra space at bottom for better scrolling
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }

@Composable
fun YearTab(
    year: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    customFont: FontFamily
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .clickable(onClick = onSelect),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E1E1E)
    ) {
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = customFont),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ReadingHeatmap(
    readingDays: List<ReadingDay>,
    selectedYear: Int,
    customFont: FontFamily
) {
    // Group reading days by month
    val readingDaysByMonth = readingDays
        .filter {
            val date = timestampToLocalDate(it.date)
            date.year == selectedYear
        }
        .groupBy {
            val date = timestampToLocalDate(it.date)
            date.month
        }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Statistics card
        ReadingStatsCard(
            readingDays = readingDays,
            selectedYear = selectedYear,
            customFont = customFont
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Month grid headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Day of week headers
            DayOfWeek.values().take(7).forEach { day ->
                Box(
                    modifier = Modifier.width(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.getDisplayNameCompat(),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = customFont),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Month grids
        Month.values().forEach { month ->
            MonthGrid(
                month = month,
                readingDays = readingDaysByMonth[month] ?: emptyList(),
                year = selectedYear,
                customFont = customFont
            )
        }
    }
}

@Composable
fun ReadingStatsCard(
    readingDays: List<ReadingDay>,
    selectedYear: Int,
    customFont: FontFamily
) {
    // Filter reading days for the selected year
    val filteredReadingDays = readingDays.filter { day ->
        val date = timestampToLocalDate(day.date)
        date.year == selectedYear
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Year Overview: $selectedYear",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = customFont),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    value = filteredReadingDays.size.toString(),
                    label = "Days Read",
                    customFont = customFont
                )

                val totalPages = filteredReadingDays.sumOf { it.pagesRead }
                StatItem(
                    value = totalPages.toString(),
                    label = "Pages",
                    customFont = customFont
                )

                val totalMinutes = filteredReadingDays.sumOf { it.timeSpentMinutes }
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                val timeFormatted = String.format("%d:%02d", hours, minutes)

                StatItem(
                    value = timeFormatted,
                    label = "Time",
                    customFont = customFont
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    customFont: FontFamily
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = customFont),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = customFont),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun MonthGrid(
    month: Month,
    readingDays: List<ReadingDay>,
    year: Int,
    customFont: FontFamily
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = month.getDisplayNameCompat(),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = customFont),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Create a LocalDate for the first day of this month/year
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val daysInMonth = firstDayOfMonth.lengthOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday is 0

        // Create a map of reading days for faster lookup
        val readingDaysMap = readingDays.associateBy { day ->
            timestampToLocalDate(day.date)
        }

        // Day of week headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = customFont),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Weeks grid
        val weeks = (0 until (firstDayOfWeek + daysInMonth + 6) / 7)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (dayOfWeek in 0..6) {
                        val day = week * 7 + dayOfWeek + 1 - firstDayOfWeek
                        if (day in 1..daysInMonth) {
                            // Construct the date for this day
                            val currentDate = LocalDate.of(year, month, day)

                            // Check if this day has reading activity using the map
                            val readingDay = readingDaysMap[currentDate]
                            val hasReading = readingDay != null

                            val intensity = if (hasReading) {
                                val pagesRead = readingDay?.pagesRead ?: 0
                                when {
                                    pagesRead > 50 -> 1.0f
                                    pagesRead > 25 -> 0.7f
                                    pagesRead > 10 -> 0.4f
                                    else -> 0.2f
                                }
                            } else 0.0f

                            val isToday = currentDate.isEqual(LocalDate.now())

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when {
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            hasReading -> MaterialTheme.colorScheme.primary.copy(alpha = intensity)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .border(
                                        width = if (isToday) 1.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = customFont),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (hasReading || isToday) 1f else 0.4f)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
    }
}

// Add this function to the bottom of ReadingHeatmapScreen.kt
private fun timestampToLocalDate(timestamp: Long): LocalDate {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

// Helper function to get display name compatible with lower API levels
fun DayOfWeek.getDisplayNameCompat(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "M"
        DayOfWeek.TUESDAY -> "T"
        DayOfWeek.WEDNESDAY -> "W"
        DayOfWeek.THURSDAY -> "T"
        DayOfWeek.FRIDAY -> "F"
        DayOfWeek.SATURDAY -> "S"
        DayOfWeek.SUNDAY -> "S"
    }
}

// Helper function to get display name compatible with lower API levels
fun Month.getDisplayNameCompat(): String {
    return when (this) {
        Month.JANUARY -> "January"
        Month.FEBRUARY -> "February"
        Month.MARCH -> "March"
        Month.APRIL -> "April"
        Month.MAY -> "May"
        Month.JUNE -> "June"
        Month.JULY -> "July"
        Month.AUGUST -> "August"
        Month.SEPTEMBER -> "September"
        Month.OCTOBER -> "October"
        Month.NOVEMBER -> "November"
        Month.DECEMBER -> "December"
    }
}
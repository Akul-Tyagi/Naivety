// app/src/main/java/com/abundance/naivety/ui/screens/MoreScreen.kt
package com.abundance.naivety.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abundance.naivety.R
import com.abundance.naivety.viewmodel.MoreViewModel
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import androidx.compose.foundation.border
import com.abundance.naivety.data.ReadingDay
import com.abundance.naivety.utils.getDisplayNameCompat
import android.app.Activity
import com.abundance.naivety.ads.AdManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun MoreScreen(
    viewModel: MoreViewModel,
    navController: NavController,
    onNavigateToAchievements: () -> Unit,
    onNavigateToThemeSettings: () -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val longestStreak by viewModel.longestStreak.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val readingDays by viewModel.readingDays.collectAsState(initial = emptyList())
    val selectedYear by viewModel.selectedYear.collectAsState(initial = LocalDate.now().year)

    val sonderFont = FontFamily(Font(R.font.sonder))
    val alinsaFont = FontFamily(Font(R.font.alinsa))
    val fsFont = FontFamily(Font(R.font.montserratblack))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            ReadingStreakDisplay(
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                customFont = fsFont,
                viewModel = viewModel
            )

            ReadingHeatmapCard(
                readingDays = readingDays,
                selectedYear = selectedYear,
                customFont = alinsaFont,
                onViewDetailedActivity = { navController.navigate("reading_heatmap") }
            )

            // Reading Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
                        text = "Reading Stats",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = alinsaFont),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Streak",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = fsFont),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$currentStreak Days",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = fsFont),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val unlockedCount = achievements.count { it.unlocked }
                        val totalCount = achievements.size

                        Column {
                            Text(
                                text = "Achievements",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = fsFont),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$unlockedCount / $totalCount Done",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = fsFont),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                AdManager.showRewardedAd(
                                    activity = activity,
                                    onAdClosed = {
                                        onNavigateToAchievements()
                                    },
                                    onAdFailedToShow = {
                                        onNavigateToAchievements()
                                    }
                                )
                            } else {
                                onNavigateToAchievements()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View All Achievements", fontFamily = alinsaFont, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Settings Options
            SettingsSection(
                title = "App Settings",
                options = listOf(
                    SettingsOption(
                        title = "Theme Settings",
                        icon = Icons.Default.DarkMode,
                        onClick = onNavigateToThemeSettings
                    )
                ),
                customFont = alinsaFont
            )

            SettingsSection(
                title = "About",
                options = listOf(
                    SettingsOption(
                        title = "Share App",
                        icon = Icons.Default.Share,
                        onClick = { viewModel.shareApp() }
                    ),
                    SettingsOption(
                        title = "Rate App",
                        icon = Icons.Default.Star,
                        onClick = { viewModel.rateApp() }
                    )
                ),
                customFont = alinsaFont
            )

            // App Version
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Naivety v1.0.0",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = fsFont),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    options: List<SettingsOption>,
    customFont: FontFamily
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = customFont),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, option ->
                    SettingsItem(
                        title = option.title,
                        icon = option.icon,
                        onClick = option.onClick,
                        customFont = customFont,
                        isLast = index == options.size - 1
                    )
                }
            }
        }
    }
}

// Add this to MoreScreen.kt
@Composable
private fun ReadingHeatmapCard(
    readingDays: List<ReadingDay>,
    selectedYear: Int,
    customFont: FontFamily,
    onViewDetailedActivity: () -> Unit
) {
    // Use current date instead of hardcoded values
    val context = LocalContext.current
    val currentDate = LocalDate.now()
    val currentYear = currentDate.year
    val currentMonth = currentDate.monthValue

    val currentMonthReadingDays = readingDays.filter {
        val date = java.time.Instant.ofEpochMilli(it.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        date.year == currentYear && date.monthValue == currentMonth
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                text = "Reading Activity",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = customFont),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Month and year header - use current date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Month.of(currentMonth).getDisplayNameCompat(),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily(Font(R.font.montserratblack))),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Text(
                    text = currentYear.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily(Font(R.font.montserratblack))),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar grid - use current date
            val daysInMonth = currentDate.withDayOfMonth(1).plusMonths(1).minusDays(1).dayOfMonth
            val firstDayOfMonth = currentDate.withDayOfMonth(1).dayOfWeek.value % 7

            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = customFont),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weeks grid
            val weeks = (0 until (firstDayOfMonth + daysInMonth + 6) / 7)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (dayOfWeek in 0..6) {
                            val day = week * 7 + dayOfWeek + 1 - firstDayOfMonth
                            if (day in 1..daysInMonth) {
                                // Improved reading check logic
                                val hasReading = currentMonthReadingDays.any {
                                    val date = LocalDate.ofInstant(
                                        java.time.Instant.ofEpochMilli(it.date),
                                        ZoneId.systemDefault()
                                    )
                                    date.dayOfMonth == day
                                }

                                val intensity = if (hasReading) {
                                    val readingDay = currentMonthReadingDays.find {
                                        val date = LocalDate.ofInstant(
                                            java.time.Instant.ofEpochMilli(it.date),
                                            ZoneId.systemDefault()
                                        )
                                        date.dayOfMonth == day
                                    }

                                    val pagesRead = readingDay?.pagesRead ?: 0
                                    when {
                                        pagesRead > 50 -> 1.0f
                                        pagesRead > 25 -> 0.7f
                                        pagesRead > 10 -> 0.4f
                                        else -> 0.2f
                                    }
                                } else 0.0f

                                // Add today highlight
                                val isToday = day == currentDate.dayOfMonth
                                val backgroundColor = when {
                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    hasReading -> MaterialTheme.colorScheme.primary.copy(alpha = intensity)
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                }
                                val borderWidth = if (isToday) 1.dp else 0.dp

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(backgroundColor)
                                        .border(
                                            width = borderWidth,
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
                                Spacer(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val activity = (context as? Activity)
                    if (activity != null) {
                        AdManager.showRewardedAd(
                            activity = activity,
                            onAdClosed = {
                                onViewDetailedActivity()
                            },
                            onAdFailedToShow = {
                                onViewDetailedActivity()
                            }
                        )
                    } else {
                        onViewDetailedActivity()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("View Detailed Activity", fontFamily = customFont, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ReadingStreakDisplay(
    currentStreak: Int,
    longestStreak: Int,
    customFont: FontFamily,
    viewModel: MoreViewModel
) {
    // Get streak goal from ViewModel
    val streakGoal by viewModel.streakGoal.collectAsState(initial = 30)
    var isEditingGoal by remember { mutableStateOf(false) }
    // Temporary value for editing
    var tempGoal by remember(streakGoal) { mutableStateOf(streakGoal) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
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
                text = "Reading Streak",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = customFont),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Current streak
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(40.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentStreak.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = customFont,
                                fontSize = 32.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Current Streak",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = customFont),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "DAYS",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = customFont),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(80.dp)
                        .background(Color(0xFF333333))
                        .align(Alignment.CenterVertically)
                )

                // Longest streak
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(40.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = longestStreak.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = customFont,
                                fontSize = 32.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Longest Streak",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = customFont),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "DAYS",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = customFont),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Streak progress bar with gradient
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Goal text with edit functionality
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = customFont),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    if (isEditingGoal) {
                        // Edit mode controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (tempGoal > 1) tempGoal-- },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease goal",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                            }

                            Text(
                                text = "$tempGoal",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = customFont),
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = { tempGoal++ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase goal",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                            }

                            IconButton(
                                onClick = {
                                    isEditingGoal = false
                                    viewModel.updateStreakGoal(tempGoal)
                                },
                                modifier = Modifier.size(19.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save goal",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        // Display mode
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                isEditingGoal = true
                                tempGoal = streakGoal
                            }
                        ) {
                            Text(
                                text = "Goal: $streakGoal days",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = customFont),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit goal",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp).padding(start = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Progress bar with rainbow gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(minOf(currentStreak.toFloat() / streakGoal, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF8E42FF),  // Purple
                                        Color(0xFF5D6DFF),  // Blue
                                        Color(0xFF42DDFF),  // Cyan
                                        Color(0xFF3FFF8A),  // Green
                                        Color(0xFFFFE73F),  // Yellow
                                        Color(0xFFFF8C42)   // Orange
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Show streak progress as text
                Text(
                    text = "$currentStreak/$streakGoal days",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = customFont),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    customFont: FontFamily,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (isLast) 16.dp else 0.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = customFont),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }

    if (!isLast) {
        Divider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )
    }
}

private data class SettingsOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
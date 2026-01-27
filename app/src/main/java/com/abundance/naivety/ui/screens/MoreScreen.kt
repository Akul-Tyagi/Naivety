// app/src/main/java/com/abundance/naivety/ui/screens/MoreScreen.kt
package com.abundance.naivety.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abundance.naivety.R
import com.abundance.naivety.navigation.Destinations
import com.abundance.naivety.viewmodel.MoreViewModel
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import androidx.compose.foundation.border
import com.abundance.naivety.data.ReadingDay
import com.abundance.naivety.utils.getDisplayNameCompat
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import com.abundance.naivety.AuthActivity
import com.abundance.naivety.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import kotlin.or

@Composable
fun MoreScreen(
    viewModel: MoreViewModel,
    navController: NavController,
    onNavigateToAchievements: () -> Unit,
    onNavigateToThemeSettings: () -> Unit
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val longestStreak by viewModel.longestStreak.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val readingDays by viewModel.readingDays.collectAsState(initial = emptyList())
    val selectedYear by viewModel.selectedYear.collectAsState(initial = LocalDate.now().year)

    val sonderFont = FontFamily(Font(R.font.sonder))
    val alinsaFont = FontFamily(Font(R.font.nektar))
    val fsFont = FontFamily(Font(R.font.montserratblack))

    val context = LocalContext.current
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    val isGuestMode = PreferencesManager.isGuestMode(context)

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
                onViewDetailedActivity = { navController.navigate(Destinations.ReadingHeatmap.route) }
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
                                onNavigateToAchievements()
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

            // Support Us Section (replacing Book Download Resources)
            SupportUsSection(
                customFont = alinsaFont,
                onRateApp = { viewModel.rateApp() },
                onShareApp = { viewModel.shareApp() }
            )

            Spacer(modifier = Modifier.weight(2f))

            // Auth button (Sign Up or Logout)
            OutlinedButton(
                onClick = {
                    if (isGuestMode) {
                        // Guest user clicking "Sign Up" - redirect to AuthActivity
                        PreferencesManager.setGuestMode(context, false) // Clear guest mode
                        val intent = Intent(context, AuthActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        (context as? Activity)?.finish() // Ensure current activity is finished
                    } else {
                        // Logged in user clicking "Logout"
                        showLogoutConfirmation = true
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isGuestMode) MaterialTheme.colorScheme.primary else Color.Red
                ),
                border = BorderStroke(
                    1.dp,
                    if (isGuestMode) MaterialTheme.colorScheme.primary else Color.Red
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = if (isGuestMode) Icons.Default.PersonAdd else Icons.Default.Logout,
                    contentDescription = if (isGuestMode) "Sign Up" else "Logout",
                    tint = if (isGuestMode) MaterialTheme.colorScheme.primary else Color.Red
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isGuestMode) "Sign Up with Account" else "Logout",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Logout confirmation dialog
            if (showLogoutConfirmation) {
                AlertDialog(
                    onDismissRequest = { showLogoutConfirmation = false },
                    title = { Text(text ="Confirm Logout", fontFamily = alinsaFont) },
                    text = { Text("Are you sure you want to log out?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                PreferencesManager.setGuestMode(context, false)
                                val intent = Intent(context, AuthActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                (context as? Activity)?.finish()
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    dismissButton = {
                        TextButton(onClick = { showLogoutConfirmation = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // App Version
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Naivety v3.7.1",
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
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = customFont),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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
                        onViewDetailedActivity()
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

@Composable
private fun SupportUsSection(
    customFont: FontFamily,
    onRateApp: () -> Unit,
    onShareApp: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accentColor = MaterialTheme.colorScheme.primary
    val heartColor = Color(0xFFE91E63)
    val goldColor = Color(0xFFFFD700)

    // Animated heart beat
    val heartBeat = rememberInfiniteTransition(label = "heartbeat")
    val heartScale by heartBeat.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartScale"
    )

    // Floating animation for the icon
    val floatAnim = rememberInfiniteTransition(label = "float")
    val floatOffset by floatAnim.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    // Glow animation
    val glowAnim = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnim.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Star rotation animation
    val starRotation by floatAnim.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Subtle gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = if (isDarkTheme) 0.08f else 0.06f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated heart icon with glow
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.offset(y = (-floatOffset).dp)
                ) {
                    // Glow layer
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(heartColor.copy(alpha = glowAlpha * 0.3f))
                            .blur(20.dp)
                    )
                    // Background circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        heartColor.copy(alpha = 0.2f),
                                        heartColor.copy(alpha = 0.08f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = heartColor,
                            modifier = Modifier
                                .size(40.dp)
                                .scale(heartScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Main Title with gradient-like appearance
                Text(
                    text = "Keep Naivety Alive",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = customFont,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Badge-like subtitle
                Surface(
                    color = accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "100% Free • No Ads • Forever",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = customFont,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = accentColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Message card with elegant styling
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Naivety is free and ad-free, which means we don't have a marketing budget. We exist solely because users like you rate us, and your support means the world to us!✨",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = customFont,
                            lineHeight = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Rate Us Button - simplified and responsive
                Button(
                    onClick = onRateApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Leave a 5-Star Review",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = customFont,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = "It Only Takes A Second!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(7.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = goldColor,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(starRotation)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = goldColor,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(starRotation)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = goldColor,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(starRotation)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = goldColor,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(starRotation)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = goldColor,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(starRotation)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Share Button - simplified and responsive
                OutlinedButton(
                    onClick = onShareApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    ),
                    border = BorderStroke(2.dp, accentColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Share with Your Loved Ones",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = customFont,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "Give The Gift Of Free Reading",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Elegant footer with animated heart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Built with ",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = customFont),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = heartColor,
                        modifier = Modifier
                            .size(14.dp)
                            .scale(heartScale * 0.9f)
                    )
                    Text(
                        text = " for book lovers everywhere",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = customFont),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

private data class SettingsOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

package com.abundance.naivety.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abundance.naivety.R
import kotlinx.coroutines.delay

/**
 * Manager for handling app rating prompts.
 */
object RatingPromptManager {
    private const val PREFS_NAME = "rating_prompt_prefs"
    private const val KEY_APP_OPEN_COUNT = "app_open_count"
    private const val KEY_HAS_RATED = "has_rated"
    private const val KEY_REMIND_LATER_TIME = "remind_later_time"
    private const val KEY_NEVER_ASK = "never_ask"

    private const val MIN_OPENS_BEFORE_PROMPT = 2
    private const val REMIND_LATER_DELAY_MS = 2 * 24 * 60 * 60 * 1000L

    fun onAppOpened(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentCount = prefs.getInt(KEY_APP_OPEN_COUNT, 0)
        val newCount = currentCount + 1
        prefs.edit().putInt(KEY_APP_OPEN_COUNT, newCount).apply()
        android.util.Log.d("RatingPrompt", "onAppOpened called - count updated from $currentCount to $newCount")
    }

    fun shouldShowRatingPrompt(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val hasRated = prefs.getBoolean(KEY_HAS_RATED, false)
        if (hasRated) {
            android.util.Log.d("RatingPrompt", "Not showing - user has rated")
            return false
        }

        val neverAsk = prefs.getBoolean(KEY_NEVER_ASK, false)
        if (neverAsk) {
            android.util.Log.d("RatingPrompt", "Not showing - never ask enabled")
            return false
        }

        val openCount = prefs.getInt(KEY_APP_OPEN_COUNT, 0)
        if (openCount < MIN_OPENS_BEFORE_PROMPT) {
            android.util.Log.d("RatingPrompt", "Not showing - openCount ($openCount) < MIN_OPENS ($MIN_OPENS_BEFORE_PROMPT)")
            return false
        }

        val remindLaterTime = prefs.getLong(KEY_REMIND_LATER_TIME, 0)
        if (remindLaterTime > 0 && System.currentTimeMillis() < remindLaterTime) {
            android.util.Log.d("RatingPrompt", "Not showing - remind later time not reached")
            return false
        }

        android.util.Log.d("RatingPrompt", "Should show dialog! openCount=$openCount")
        return true
    }

    fun markAsRated(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HAS_RATED, true).apply()
    }

    fun remindLater(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_REMIND_LATER_TIME, System.currentTimeMillis() + REMIND_LATER_DELAY_MS).apply()
    }

    fun openPlayStoreForRating(context: Context) {
        val packageName = context.packageName
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    fun shareApp(context: Context) {
        val packageName = context.packageName
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out Naivety - Free Reading App")
            putExtra(Intent.EXTRA_TEXT, "I'm using Naivety - a 100% free, ad-free reading app. Check it out!\n\nhttps://play.google.com/store/apps/details?id=$packageName")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Naivety"))
    }

    /**
     * Reset all rating prompt preferences for testing purposes.
     * Call this to simulate a fresh install scenario.
     */
    fun resetForTesting(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        android.util.Log.d("RatingPrompt", "Reset all preferences for testing")
    }
}

@Composable
private fun AnimatedStar(
    index: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "star_$index")

    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000 + index * 100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation_$index"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800 + index * 50, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_$index"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200 + index * 100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_$index"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            initialScale = 0f,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f)
        ) + fadeIn()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
        ) {
            // Glow behind star
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700).copy(alpha = glowAlpha),
                modifier = Modifier
                    .size(40.dp)
                    .blur(6.dp)
                    .scale(scale * 1.2f)
            )
            // Main star
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
fun RatingPromptDialog(
    onDismiss: () -> Unit,
    onRateNow: () -> Unit,
    onRemindLater: () -> Unit
) {
    val context = LocalContext.current
    val sonderFont = FontFamily(Font(R.font.sonder))
    val nektarFont = FontFamily(Font(R.font.nektar))

    // Track star visibility for staggered animation
    var starsVisible by remember { mutableStateOf(List(5) { false }) }

    LaunchedEffect(Unit) {
        repeat(5) { index ->
            delay(100L + index * 80L)
            starsVisible = starsVisible.toMutableList().also { it[index] = true }
        }
    }

    // Bottom sheet slide-in animation
    var sheetVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        sheetVisible = true
    }

    // Heart beat animation
    val heartBeat = rememberInfiniteTransition(label = "heartbeat")
    val heartScale by heartBeat.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartScale"
    )

    // Button hover/press animation
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "buttonScale"
    )

    // Use Dialog for proper z-ordering and window management
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        // Full screen container with semi-transparent background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Bottom sheet modal
            AnimatedVisibility(
                visible = sheetVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                ) + fadeIn(animationSpec = tween(200)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(200)
                ) + fadeOut(animationSpec = tween(150))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Prevent click through */ },
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    // Drag handle indicator
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // App name with glow effect
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Naivety",
                            fontFamily = sonderFont,
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.blur(10.dp)
                        )
                        Text(
                            text = "Naivety",
                            fontFamily = sonderFont,
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated stars row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        starsVisible.forEachIndexed { index, isVisible ->
                            AnimatedStar(
                                index = index,
                                isVisible = isVisible
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title with heart
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Loving The Naivety Experience?",
                            fontFamily = nektarFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier
                                .size(18.dp)
                                .scale(heartScale)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Single message
                    Text(
                        text = "Naivety is free and ad-free, which means we don't have a marketing budget. We exist solely because users like you rate us, and your support means the world to us! ✨",
                        fontSize = 14.sp,
                        fontFamily = sonderFont,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Elegant Rate Button with smooth press animation
                    Button(
                        onClick = {
                            RatingPromptManager.markAsRated(context)
                            RatingPromptManager.openPlayStoreForRating(context)
                            onRateNow()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .scale(buttonScale),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp
                        ),
                        interactionSource = buttonInteractionSource
                    ) {
                        Text(
                            text = "✨ Rate us - It Only Takes 7 Seconds! ✨",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Maybe Later button
                    TextButton(
                        onClick = {
                            RatingPromptManager.remindLater(context)
                            onRemindLater()
                        },
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "Maybe Later",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        }
    }
}

@Composable
fun RatingPromptHandler(
    enabled: Boolean = true
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var hasChecked by remember { mutableStateOf(false) }

    // Check on first composition
    LaunchedEffect(enabled) {
        if (!hasChecked && enabled) {
            hasChecked = true
            // Small delay to let the screen settle
            delay(1000)
            val shouldShow = RatingPromptManager.shouldShowRatingPrompt(context)
            android.util.Log.d("RatingPrompt", "shouldShowRatingPrompt: $shouldShow")
            if (shouldShow) {
                showDialog = true
            }
        }
    }

    if (showDialog) {
        RatingPromptDialog(
            onDismiss = {
                RatingPromptManager.remindLater(context)
                showDialog = false
            },
            onRateNow = {
                showDialog = false
            },
            onRemindLater = {
                showDialog = false
            }
        )
    }
}

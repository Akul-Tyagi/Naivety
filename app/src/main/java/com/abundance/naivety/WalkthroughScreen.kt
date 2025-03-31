package com.abundance.naivety

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abundance.naivety.ui.theme.TransparentSystemBars
import androidx.compose.ui.platform.LocalContext
import com.abundance.naivety.ui.theme.NaivetyTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WalkthroughScreen(onFinish: () -> Unit) {
    val mainHeadings = listOf(
        "Welcome to Naivety. Books, but Better.",
        "Every book, perfectly placed.",
        "Built for bookworms, by bookworms."
    )

    val subHeadings = listOf(
        "Lets Make Reading Feel As Smooth As Turning A Page.",
        "Keep Your Favorite Novels, Romances, And Adventures At Your Fingertips, Always Waiting Right Where You Left Them.",
        "No Clutter, No Distractions—Just You And The Words That Matter. Ready To Dive In?"
    )

    val context = LocalContext.current
    val userPreferencesRepository = remember {
        (context.applicationContext as NaivetyApplication).userPreferencesRepository
    }
    val isDarkTheme = userPreferencesRepository.isDarkTheme.collectAsState().value

    TransparentSystemBars(darkTheme = isDarkTheme)

    var currentSlide by remember { mutableIntStateOf(0) }
    var shouldShowSubheading by remember { mutableStateOf(false) }

    // Animation states
    val mainHeadingProgress = remember { Animatable(initialValue = 0f) }
    val typewriterProgress = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(currentSlide) {
        // Reset states
        shouldShowSubheading = false
        mainHeadingProgress.snapTo(0f)
        typewriterProgress.snapTo(0f)

        // Animate main heading sliding in
        mainHeadingProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = EaseOutQuart)
        )

        // Start subheading animation after main heading
        shouldShowSubheading = true

        // Animate typewriter effect
        typewriterProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2025, delayMillis = 142)
        )
    }

    val mainFontFamily = FontFamily(Font(R.font.carmila, FontWeight.Bold))
    val secondaryFontFamily = FontFamily(Font(R.font.fsultralit, FontWeight.Normal))

    NaivetyTheme(darkTheme = isDarkTheme) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .padding(bottom = 34.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Heading with slide and fade animation
                Box(
                    modifier = Modifier
                        .offset(
                            x = (-(1f - mainHeadingProgress.value) * 200).dp,
                            y = 0.dp
                        )
                        .alpha(mainHeadingProgress.value)
                ) {
                    Text(
                        text = mainHeadings[currentSlide],
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 69.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = mainFontFamily,
                            lineHeight = 52.sp,
                            textAlign = TextAlign.Start
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subheading with typewriter effect
                if (shouldShowSubheading) {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = ((1f - typewriterProgress.value) * 200).dp,
                                y = 0.dp
                            )
                            .alpha(typewriterProgress.value)
                    ) {
                        val visibleText =
                            remember(subHeadings[currentSlide], typewriterProgress.value) {
                                subHeadings[currentSlide].take(
                                    (subHeadings[currentSlide].length * typewriterProgress.value).toInt()
                                )
                            }

                        Text(
                            text = visibleText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 43.sp,
                                lineHeight = 35.sp,
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontFamily = secondaryFontFamily,
                            ),
                        )
                    }
                }
            }

            // Next/Finish button with fade animation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(34.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = {
                        if (currentSlide < mainHeadings.size - 1) {
                            currentSlide++
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.extraLarge
                        )
                        .alpha(mainHeadingProgress.value),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF111111),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(text = if (currentSlide < mainHeadings.size - 1) "→" else "Finish")
                }
            }
        }
    }
}
// Custom easing curve for smooth animation
    private val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
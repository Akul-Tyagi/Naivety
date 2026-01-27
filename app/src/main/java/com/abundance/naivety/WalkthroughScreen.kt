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
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        "PDFs, EPUBs, Your Personal Lists—Everything Organized Beautifully. Browse Millions Of Books And Build Your Perfect Library.",
        "Completely Free, Forever Ad Free. Track Your Reading Journey With Stats, Streaks, And Achievements. Just Pure Reading Bliss."
    )

    val context = LocalContext.current
    val userPreferencesRepository = remember {
        (context.applicationContext as NaivetyApplication).userPreferencesRepository
    }
    val isDarkTheme = userPreferencesRepository.isDarkTheme.collectAsState().value

    TransparentSystemBars(darkTheme = isDarkTheme)

    var currentSlide by remember { mutableIntStateOf(0) }
    var shouldShowSubheading by remember { mutableStateOf(false) }
    var isSlideTransitioning by remember { mutableStateOf(false) }

    // Animation states
    val slideTransitionAlpha = remember { Animatable(initialValue = 1f) }
    val mainHeadingProgress = remember { Animatable(initialValue = 0f) }
    val mainHeadingScale = remember { Animatable(initialValue = 0.95f) }
    val typewriterProgress = remember { Animatable(initialValue = 0f) }
    val buttonScale = remember { Animatable(initialValue = 1f) }

    // Content visibility control
    val mainHeadingAlpha = remember { Animatable(initialValue = 0f) }
    val subHeadingAlpha = remember { Animatable(initialValue = 0f) }

    // Custom easing curves for smoother animations
    val easeOutQuint = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    val easeInOutQuint = CubicBezierEasing(0.83f, 0f, 0.17f, 1f)
    val easeOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    LaunchedEffect(currentSlide) {

        // Hide content immediately before any transitions
        mainHeadingAlpha.snapTo(0f)
        subHeadingAlpha.snapTo(0f)

        // Slide transition fade out - smoother with easing
        isSlideTransitioning = true
        slideTransitionAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(400, easing = easeOutQuint)
        )

        // Reset animation states
        shouldShowSubheading = false
        mainHeadingProgress.snapTo(0f)
        mainHeadingScale.snapTo(0.95f)
        typewriterProgress.snapTo(0f)

        // Slide transition fade in - slightly delayed for smoother feel
        slideTransitionAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(450, easing = easeOutExpo)
        )
        isSlideTransitioning = false

        delay(50)

        // Fade in main heading with smoother timing
        launch {
            mainHeadingAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = easeOutExpo)
            )
        }

        // Animate main heading sliding in with scale - smoother curve
        launch {
            mainHeadingProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(900, easing = easeOutExpo)
            )
        }

        launch {
            mainHeadingScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(1000, easing = easeOutExpo)
            )
        }

        // Add a short delay before showing subheading
        delay(150)
        shouldShowSubheading = true

        // Fade in subheading with smoother timing
        launch {
            subHeadingAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = easeOutExpo)
            )
        }

        // Animate typewriter effect with improved timing - slightly faster for better UX
        typewriterProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2500, delayMillis = 50, easing = easeInOutQuint)
        )
    }

    // Button smooth breathing animation
    LaunchedEffect(Unit) {
        while (true) {
            buttonScale.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(1200, easing = easeInOutQuint)
            )
            buttonScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(1200, easing = easeInOutQuint)
            )
            delay(800) // Pause between pulses
        }
    }

    val mainFontFamily = FontFamily(Font(R.font.carmila, FontWeight.Bold))
    val secondaryFontFamily = FontFamily(Font(R.font.fsultralit, FontWeight.Normal))

    NaivetyTheme(darkTheme = isDarkTheme) {
        Box(modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .alpha(slideTransitionAlpha.value)
        ) {
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
                            x = (-(1f - mainHeadingProgress.value) * 150).dp,
                            y = 0.dp
                        )
                        .alpha(mainHeadingAlpha.value * mainHeadingProgress.value)
                        .scale(mainHeadingScale.value)
                ) {
                    Text(
                        text = mainHeadings[currentSlide],
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 64.sp,
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
                                x = ((1f - typewriterProgress.value) * 100).dp,
                                y = 0.dp
                            )
                            .alpha(subHeadingAlpha.value * typewriterProgress.value.coerceIn(0f, 1f))
                    ) {
                        val visibleText = remember(subHeadings[currentSlide], typewriterProgress.value) {
                            subHeadings[currentSlide].take(
                                (subHeadings[currentSlide].length * typewriterProgress.value).toInt()
                            )
                        }

                        Text(
                            text = visibleText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 40.sp,
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
                        .scale(buttonScale.value)
                        .alpha(mainHeadingProgress.value),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF111111),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    enabled = !isSlideTransitioning,
                    contentPadding = if (currentSlide < mainHeadings.size - 1)
                        PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    else
                        PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentSlide < mainHeadings.size - 1) "→" else "Finish",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = mainFontFamily
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

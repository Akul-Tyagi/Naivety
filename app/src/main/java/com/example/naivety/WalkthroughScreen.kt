package com.example.naivety

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.tween
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WalkthroughScreen(onFinish: () -> Unit) {
    val mainHeadings = listOf(
        "Welcome to Naivety. Books, but Better.",
        "Every book, perfectly placed.",
        "Built for bookworms, by bookworms."
    )

    val subHeadings = listOf(
        "Let’s make reading feel as smooth as turning a page.",
        "Keep your favorite novels, romances, and adventures at your fingertips, always waiting right where you left them.",
        "No clutter, no distractions—just you and the words that matter. Ready to dive in?"
    )

    var currentSlide by remember { mutableIntStateOf(0) }

    val mainFontFamily = FontFamily(Font(R.font.carmila, FontWeight.Bold))
    val secondaryFontFamily = FontFamily(Font(R.font.sonder, FontWeight.Normal)) // Ensure this font file exists

    Box(
        modifier = Modifier
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .padding(bottom = 34.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        (slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(820)) + fadeIn(animationSpec = tween(340))) togetherWith
                                (slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(820)) + fadeOut(animationSpec = tween(340)))
                    }
                ) { targetSlide ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = mainHeadings[targetSlide],
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 69.sp,
                                color = Color(0xFF8E42FF),
                                fontFamily = mainFontFamily,
                                lineHeight = 52.sp,
                                textAlign = TextAlign.Start
                            ),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subHeadings[targetSlide],
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 43.sp,
                                lineHeight = 32.sp,
                                textAlign = TextAlign.End,
                                color = Color.White,
                                fontFamily = secondaryFontFamily,
                            ),
                        )
                    }
                }
            }
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
                    .border(1.dp, Color(0xFF8E42FF), MaterialTheme.shapes.extraLarge),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111), contentColor = Color.White),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(text = if (currentSlide < mainHeadings.size - 1) "→" else "Finish")
            }
        }
    }
}
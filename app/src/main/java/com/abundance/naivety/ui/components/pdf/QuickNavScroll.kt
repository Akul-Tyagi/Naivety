// QuickNavScroll.kt
package com.abundance.naivety.ui.components.pdf

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abundance.naivety.R
import com.abundance.naivety.ui.theme.NaivetyPurple
import kotlin.math.roundToInt

@Composable
fun QuickNavScroll(
    currentPage: Int,
    totalPages: Int,
    isVisible: Boolean,
    onPageSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = 100, // Slight delay after nav bar
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(300, delayMillis = 100)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200, easing = FastOutLinearInEasing)
        ) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .width(280.dp) // Make it shorter than nav bar
                .height(25.dp)
                .padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.79f),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            val xOffset = change.position.x
                            val percentage = (xOffset / size.width).coerceIn(0f, 1f)
                            val newPage = (percentage * (totalPages - 1)).roundToInt()
                            onPageSelect(newPage.coerceIn(0, totalPages - 1))
                        }
                    }
            ) {
                // Current page indicator
                Text(
                    text = "${currentPage + 1}",
                    color = NaivetyPurple,
                    fontFamily = FontFamily(Font(R.font.guyongazebor)),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
    }
}
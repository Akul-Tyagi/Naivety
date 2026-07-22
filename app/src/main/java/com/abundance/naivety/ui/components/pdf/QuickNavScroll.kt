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
import androidx.compose.ui.text.PlatformTextStyle
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
                .widthIn(min = 128.dp, max = 320.dp)
                .padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.79f),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 8.dp
        ) {
            Box(
                contentAlignment = Alignment.Center, // ADD THIS: Centers children perfectly
                modifier = Modifier
                    .fillMaxWidth() // CHANGED: Fill width for drag gestures, but NOT height
                    .heightIn(min = 25.dp, max = 30.dp)
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
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.titleSmall.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                    // REMOVED: modifier = Modifier.align(Alignment.Center) because Box handles it now
                )
            }
        }
    }
}
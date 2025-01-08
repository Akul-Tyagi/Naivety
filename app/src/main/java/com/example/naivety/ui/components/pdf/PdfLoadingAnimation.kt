// app/src/main/java/com/example/naivety/ui/components/pdf/PdfLoadingAnimation.kt
package com.example.naivety.ui.components.pdf

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.naivety.ui.theme.NaivetyPurple
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PdfLoadingAnimation(
    modifier: Modifier = Modifier
) {
    var rotation by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition()

    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    rotation = animatedRotation

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(
            modifier = Modifier.size(60.dp)
        ) {
            val radius = size.minDimension / 4
            val dots = 8
            val centerX = size.width / 2
            val centerY = size.height / 2

            for (i in 0 until dots) {
                val angle = (i * 2 * PI / dots) + (rotation * PI / 180)
                val x = centerX + cos(angle) * radius
                val y = centerY + sin(angle) * radius
                val alpha = ((i + 1) / dots.toFloat())

                drawCircle(
                    color = NaivetyPurple.copy(alpha = alpha),
                    radius = 8f,
                    center = androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat())
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading PDF...",
            color = Color.White
        )
    }
}
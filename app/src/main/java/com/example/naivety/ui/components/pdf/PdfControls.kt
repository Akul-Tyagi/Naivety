// app/src/main/java/com/example/naivety/ui/components/pdf/PdfControls.kt
package com.example.naivety.ui.components.pdf

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.naivety.ui.theme.NaivetyPurple

@Composable
fun PdfControls(
    isVisible: Boolean,
    onSettingsClick: () -> Unit,
    onReadingModeClick: () -> Unit,
    onRotationClick: () -> Unit,
    onBrightnessClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(16.dp),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = NaivetyPurple
                    )
                }
                IconButton(onClick = onReadingModeClick) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Reading Mode",
                        tint = NaivetyPurple
                    )
                }
                IconButton(onClick = onRotationClick) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Rotation",
                        tint = NaivetyPurple
                    )
                }
                IconButton(onClick = onBrightnessClick) {
                    Icon(
                        imageVector = Icons.Default.BrightnessHigh,
                        contentDescription = "Brightness",
                        tint = NaivetyPurple
                    )
                }
            }
        }
    }
}
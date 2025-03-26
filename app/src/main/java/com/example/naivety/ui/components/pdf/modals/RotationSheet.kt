// app/src/main/java/com/example/naivety/ui/components/pdf/modals/RotationSheet.kt
package com.example.naivety.ui.components.pdf.modals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.naivety.R
import com.example.naivety.ui.pdf.RotationMode
import com.example.naivety.ui.theme.NaivetyPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotationSheet(
    currentMode: RotationMode,
    onModeSelect: (RotationMode) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Rotation",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily(Font(R.font.alinsa)),
                color = NaivetyPurple
            )

            Spacer(modifier = Modifier.height(16.dp))

            RotationModeList(
                currentMode = currentMode,
                onModeSelect = onModeSelect
            )
        }
    }
}

@Composable
private fun RotationModeList(
    currentMode: RotationMode,
    onModeSelect: (RotationMode) -> Unit
) {
    val rotationModes = listOf(
        RotationMode.FREE to "Free Rotation",
        RotationMode.PORTRAIT to "Portrait",
        RotationMode.LANDSCAPE to "Landscape",
        RotationMode.LOCKED_PORTRAIT to "Locked Portrait",
        RotationMode.LOCKED_LANDSCAPE to "Locked Landscape",
        RotationMode.REVERSE_PORTRAIT to "Reverse Portrait"
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        rotationModes.forEach { (mode, title) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                onClick = { onModeSelect(mode) },
                color = if (mode == currentMode) Color(0xFF1A1A1A) else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = when(mode) {
                            RotationMode.FREE -> Icons.Default.ScreenRotation
                            RotationMode.PORTRAIT -> Icons.Default.StayPrimaryPortrait
                            RotationMode.LANDSCAPE -> Icons.Default.StayPrimaryLandscape
                            RotationMode.LOCKED_PORTRAIT -> Icons.Default.ScreenLockPortrait
                            RotationMode.LOCKED_LANDSCAPE -> Icons.Default.ScreenLockLandscape
                            RotationMode.REVERSE_PORTRAIT -> Icons.Default.ScreenRotation
                        },
                        contentDescription = title,
                        tint = NaivetyPurple
                    )
                    Text(text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily(Font(R.font.fsb)),
                    )
                }
            }
        }
    }
}
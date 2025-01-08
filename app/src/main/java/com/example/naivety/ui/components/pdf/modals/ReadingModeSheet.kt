// app/src/main/java/com/example/naivety/ui/components/pdf/modals/ReadingModeSheet.kt
package com.example.naivety.ui.components.pdf.modals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.naivety.ui.pdf.ReadingMode
import com.example.naivety.ui.theme.NaivetyPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingModeSheet(
    currentMode: ReadingMode,
    onModeSelect: (ReadingMode) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Reading Mode",
                style = MaterialTheme.typography.titleLarge,
                color = NaivetyPurple
            )
            Spacer(modifier = Modifier.height(16.dp))

            ReadingModeOption(
                mode = ReadingMode.LEFT_TO_RIGHT,
                currentMode = currentMode,
                icon = Icons.Default.KeyboardArrowRight,
                title = "Left to Right",
                onSelect = onModeSelect
            )

            ReadingModeOption(
                mode = ReadingMode.RIGHT_TO_LEFT,
                currentMode = currentMode,
                icon = Icons.Default.KeyboardArrowLeft,
                title = "Right to Left",
                onSelect = onModeSelect
            )

            ReadingModeOption(
                mode = ReadingMode.VERTICAL,
                currentMode = currentMode,
                icon = Icons.Default.KeyboardArrowDown,
                title = "Vertical",
                onSelect = onModeSelect
            )

            ReadingModeOption(
                mode = ReadingMode.CONTINUOUS_VERTICAL,
                currentMode = currentMode,
                icon = Icons.Default.UnfoldMore,
                title = "Continuous Vertical",
                onSelect = onModeSelect
            )
        }
    }
}

@Composable
private fun ReadingModeOption(
    mode: ReadingMode,
    currentMode: ReadingMode,
    icon: ImageVector,
    title: String,
    onSelect: (ReadingMode) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onSelect(mode) },
        color = if (mode == currentMode) Color(0xFF1A1A1A) else Color.Transparent,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = NaivetyPurple
            )
            Text(
                text = title,
                color = Color.White
            )
        }
    }
}
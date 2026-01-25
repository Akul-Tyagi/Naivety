package com.abundance.naivety.ui.components.epub.modals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.abundance.naivety.R

enum class EpubReadingMode(
    val displayName: String,
    val description: String,
    val icon: ImageVector
) {
    CHAPTER_SCROLL(
        "Chapter Scroll",
        "Scroll within chapter • Tap/swipe to change chapters",
        Icons.Default.SwapVert
    ),
    PAGE_HORIZONTAL(
        "Page by Page Vertical",
        "Tap sides or swipe up/down to turn pages",
        Icons.AutoMirrored.Filled.MenuBook
    ),
    CONTINUOUS_HORIZONTAL(
        "Page by Page Horizontal",
        "Tap sides or swipe left/right to turn pages",
        Icons.Default.ViewCarousel
    ),
    CONTINUOUS_VERTICAL(
        "Continuous Scroll",
        "Scroll freely through entire book without breaks",
        Icons.Default.ViewDay
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubReadingModeSheet(
    currentMode: EpubReadingMode,
    onModeSelect: (EpubReadingMode) -> Unit,
    onDismiss: () -> Unit
) {
    val purple = MaterialTheme.colorScheme.primary

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
                text = "Reading Mode",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = purple,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            EpubReadingMode.entries.forEach { mode ->
                ReadingModeOption(
                    title = mode.displayName,
                    description = mode.description,
                    icon = mode.icon,
                    isSelected = currentMode == mode,
                    onClick = { onModeSelect(mode) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReadingModeOption(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily(Font(R.font.fsb)),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

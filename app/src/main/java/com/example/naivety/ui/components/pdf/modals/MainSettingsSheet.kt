// app/src/main/java/com/example/naivety/ui/components/pdf/modals/MainSettingsSheet.kt
package com.example.naivety.ui.components.pdf.modals

import SettingItem
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.naivety.R
import com.example.naivety.ui.pdf.PdfSettings
import com.example.naivety.ui.pdf.ScaleType
import com.example.naivety.ui.theme.NaivetyPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsSheet(
    settings: PdfSettings,
    onSettingsChange: (PdfSettings) -> Unit,
    onDismiss: () -> Unit,

) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Black

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)

        ) {
            // Header
            Text(
                "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily(Font(R.font.alinsa)),
                color = NaivetyPurple,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Background Color Section
            SettingItem(
                title = "Background Color",
                icon = Icons.Default.Palette,
                fontFamily = FontFamily(Font(R.font.fsb))
            ) {
                BackgroundColorSelector(
                    selectedColor = settings.backgroundColor,
                    onColorSelected = { color ->
                        onSettingsChange(settings.copy(backgroundColor = color))
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Scale Type Section
            SettingItem(
                title = "Scale Type",
                icon = Icons.Default.ZoomIn,
                fontFamily = FontFamily(Font(R.font.fsb))
            ) {
                ScaleTypeSelector(
                    currentScale = settings.scaleType,
                    onScaleTypeSelect = { scaleType ->
                        onSettingsChange(settings.copy(scaleType = scaleType))
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Toggles Section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToggleSetting(
                    title = "Keep Screen On",
                    icon = Icons.Default.Visibility,
                    isChecked = settings.keepScreenOn,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(keepScreenOn = it))
                    }
                )

                ToggleSetting(
                    title = "Show Page Number",
                    icon = Icons.Default.Numbers,
                    isChecked = settings.showPageNumber,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(showPageNumber = it))
                    }
                )


                ToggleSetting(
                    title = "Animate Page Transition",
                    icon = Icons.Default.Animation,
                    isChecked = settings.animatePageTransition,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(animatePageTransition = it))
                    }
                )

            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BackgroundColorSelector(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ColorOption(
            color = 0xFF000000, // Black
            isSelected = selectedColor == 0xFF000000,
            onClick = { onColorSelected(0xFF000000) }
        )
        ColorOption(
            color = 0xFFFFFFFF, // White
            isSelected = selectedColor == 0xFFFFFFFF,
            onClick = { onColorSelected(0xFFFFFFFF) }
        )
        ColorOption(
            color = 0xFF424242, // Dark Gray
            isSelected = selectedColor == 0xFF424242,
            onClick = { onColorSelected(0xFF424242) }
        )
        ColorOption(
            color = 0xFFBE9D6A, // beige
            isSelected = selectedColor == 0xFFBE9D6A,
            onClick = { onColorSelected(0xFFBE9D6A) }
        )
        ColorOption(
            color = 0xFFF5F5DC, // beige
            isSelected = selectedColor == 0xFFF5F5DC,
            onClick = { onColorSelected(0xFFF5F5DC) }
        )
    }
}

@Composable
private fun ColorOption(
    color: Long,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(48.dp)
            .padding(4.dp),
        shape = MaterialTheme.shapes.small,
        border = if (isSelected) {
            BorderStroke(2.dp, NaivetyPurple)
        } else null,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(color))
        )
    }
}
@Composable
private fun ScaleTypeSelector(
    currentScale: ScaleType,
    onScaleTypeSelect: (ScaleType) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        items(listOf(ScaleType.FIT_PAGE, ScaleType.FIT_WIDTH, ScaleType.FIT_HEIGHT)) { scaleType ->
            FilterChip(
                selected = scaleType == currentScale,
                onClick = { onScaleTypeSelect(scaleType) },
                label = {
                    Text(
                        text = when (scaleType) {
                            ScaleType.FIT_PAGE -> "Fit Page"
                            ScaleType.FIT_WIDTH -> "Fit Width"
                            ScaleType.FIT_HEIGHT -> "Fit Height"
                            else -> ""
                        },
                        fontFamily = FontFamily.Default
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NaivetyPurple,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun ToggleSetting(
    title: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically  // Added this line
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically  // Added this line
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NaivetyPurple
                )
                Text(
                    text = title,
                    fontFamily = FontFamily(Font(R.font.fsb)),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NaivetyPurple,
                    checkedTrackColor = NaivetyPurple.copy(alpha = 0.5f)
                )
            )
        }
    }
}


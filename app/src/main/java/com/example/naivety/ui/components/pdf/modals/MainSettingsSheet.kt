// app/src/main/java/com/example/naivety/ui/components/pdf/modals/MainSettingsSheet.kt
package com.example.naivety.ui.components.pdf.modals

import SettingItem
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.naivety.ui.pdf.PdfSettings
import com.example.naivety.ui.pdf.ScaleType
import com.example.naivety.ui.theme.NaivetyPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsSheet(
    settings: PdfSettings,
    onSettingsChange: (PdfSettings) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Settings",
                style = MaterialTheme.typography.titleLarge,
                color = NaivetyPurple
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Background Color Picker
            SettingItem(
                title = "Background Color",
                icon = Icons.Default.Palette
            ) {
                // Color picker implementation
                ColorPicker(
                    currentColor = settings.backgroundColor,
                    onColorChange = { color ->
                        onSettingsChange(settings.copy(backgroundColor = color))
                    }
                )
            }

            // Toggle Settings
            ToggleSetting(
                title = "Show Page Number",
                icon = Icons.Default.Numbers,
                isChecked = settings.showPageNumber,
                onCheckedChange = {
                    onSettingsChange(settings.copy(showPageNumber = it))
                }
            )

            ToggleSetting(
                title = "Fullscreen",
                icon = Icons.Default.Fullscreen,
                isChecked = settings.isFullscreen,
                onCheckedChange = {
                    onSettingsChange(settings.copy(isFullscreen = it))
                }
            )

            ToggleSetting(
                title = "Keep Screen On",
                icon = Icons.Default.Visibility,
                isChecked = settings.keepScreenOn,
                onCheckedChange = {
                    onSettingsChange(settings.copy(keepScreenOn = it))
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

            ToggleSetting(
                title = "Crop Borders",
                icon = Icons.Default.Crop,
                isChecked = settings.cropBorders,
                onCheckedChange = {
                    onSettingsChange(settings.copy(cropBorders = it))
                }
            )

            // Scale Type Selector
            ScaleTypeSelector(
                currentScale = settings.scaleType,
                onScaleTypeSelect = { scaleType ->
                    onSettingsChange(settings.copy(scaleType = scaleType))
                }
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
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NaivetyPurple
                )
                Text(text = title, color = Color.White)
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

@Composable
private fun ScaleTypeSelector(
    currentScale: ScaleType,
    onScaleTypeSelect: (ScaleType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Scale Type",
            color = Color.White,
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScaleType.values().forEach { scaleType ->
                FilterChip(
                    selected = scaleType == currentScale,
                    onClick = { onScaleTypeSelect(scaleType) },
                    label = { Text(scaleType.name.replace("_", " ")) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NaivetyPurple,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun ColorPicker(
    currentColor: Long,
    onColorChange: (Long) -> Unit
) {
    // Implement a color picker UI here
    // You can use a custom implementation or a library
}
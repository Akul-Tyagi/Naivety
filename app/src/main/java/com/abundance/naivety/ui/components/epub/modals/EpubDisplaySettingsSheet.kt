package com.abundance.naivety.ui.components.epub.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.abundance.naivety.R

// Page theme colors for EPUB reader - 10 popular reading themes
enum class EpubPageTheme(
    val displayName: String,
    val backgroundColor: Long,
    val textColor: Long
) {
    WHITE("White", 0xFFFFFFFF, 0xFF1A1A1A),
    CREAM("Cream", 0xFFFFFBF0, 0xFF3D3D3D),
    SEPIA("Sepia", 0xFFF4ECD8, 0xFF5B4636),
    SEPIA_DARK("Sepia Dark", 0xFFE8DCC8, 0xFF4A3728),
    GRAY("Gray", 0xFFE8E8E8, 0xFF2D2D2D),
    DARK_GRAY("Dark Gray", 0xFF3A3A3A, 0xFFD0D0D0),
    BLACK("Black", 0xFF1A1A1A, 0xFFE0E0E0),
    AMOLED("AMOLED", 0xFF000000, 0xFFFFFFFF),
    MINT("Mint", 0xFFE8F5E9, 0xFF1B5E20),
    LAVENDER("Lavender", 0xFFF3E5F5, 0xFF4A148C)
}

data class EpubDisplaySettings(
    val useSystemBrightness: Boolean = true,
    val customBrightness: Float = 0.5f,
    val pageTheme: EpubPageTheme = EpubPageTheme.WHITE,
    val pageMargin: Float = 43f  // in dp, range 8-100
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubDisplaySettingsSheet(
    settings: EpubDisplaySettings,
    onSettingsChange: (EpubDisplaySettings) -> Unit,
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
                text = "Display Settings",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = purple,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // System Brightness Switch
            SwitchOption(
                title = "Use System Brightness",
                checked = settings.useSystemBrightness,
                onCheckedChange = {
                    onSettingsChange(settings.copy(useSystemBrightness = it))
                }
            )

            // Custom Brightness Slider
            if (!settings.useSystemBrightness) {
                SliderOption(
                    title = "Brightness",
                    value = settings.customBrightness,
                    onValueChange = {
                        onSettingsChange(settings.copy(customBrightness = it))
                    }
                )
            }


            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Page Margin Slider
            Text(
                text = "Page Margins",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Small",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = settings.pageMargin,
                    onValueChange = { onSettingsChange(settings.copy(pageMargin = it)) },
                    valueRange = 8f..100f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = purple,
                        activeTrackColor = purple
                    )
                )
                Text(
                    text = "Large",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Page Theme Colors
            Text(
                text = "Page Theme",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(EpubPageTheme.entries.toList()) { theme ->
                    PageThemeOption(
                        theme = theme,
                        isSelected = settings.pageTheme == theme,
                        onClick = { onSettingsChange(settings.copy(pageTheme = theme)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PageThemeOption(
    theme: EpubPageTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(theme.backgroundColor))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(theme.textColor),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Aa",
                    color = Color(theme.textColor),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SwitchOption(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = FontFamily(Font(R.font.fsb)),
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun SliderOption(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontFamily = FontFamily(Font(R.font.fsb)),
            color = MaterialTheme.colorScheme.onSurface
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

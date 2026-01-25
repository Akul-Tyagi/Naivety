package com.abundance.naivety.ui.components.epub.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abundance.naivety.R

// Font colors available for EPUB reader
enum class EpubFontColor(
    val displayName: String,
    val colorValue: Long
) {
    BLACK("Black", 0xFF1A1A1A),
    DARK_GRAY("Dark Gray", 0xFF3D3D3D),
    BROWN("Brown", 0xFF5B4636),
    NAVY("Navy", 0xFF1B365D),
    FOREST("Forest", 0xFF2D5A3D),
    GRAY("Gray", 0xFF6B6B6B),
    WHITE("White", 0xFFFFFFFF)
}

// Font families available for EPUB reader
enum class EpubFontFamily(
    val displayName: String,
    val fontFamily: String,
    val previewText: String,
    val googleFontName: String  // For Google Fonts import
) {
    BOOKERLY("Bookerly", "Georgia, 'Times New Roman', serif", "Aa", ""),  // Bookerly is Amazon's proprietary font, use Georgia as fallback
    LITERATA("Literata", "'Literata', serif", "Aa", "Literata:opsz,wght@7..72,300;7..72,400;7..72,500;7..72,700"),
    MERRIWEATHER("Merriweather", "'Merriweather', serif", "Aa", "Merriweather:wght@300;400;700"),
    ROBOTO("Roboto", "'Roboto', sans-serif", "Aa", "Roboto:wght@300;400;500;700"),
    OPEN_SANS("Open Sans", "'Open Sans', sans-serif", "Aa", "Open+Sans:wght@300;400;600;700"),
    LATO("Lato", "'Lato', sans-serif", "Aa", "Lato:wght@300;400;700"),
    SOURCE_SERIF("Source Serif", "'Source Serif 4', Georgia, serif", "Aa", "Source+Serif+4:opsz,wght@8..60,400;8..60,500;8..60,700"),
    CRIMSON("Crimson", "'Crimson Text', Georgia, serif", "Aa", "Crimson+Text:wght@400;600;700"),
    NOTO_SERIF("Noto Serif", "'Noto Serif', Georgia, serif", "Aa", "Noto+Serif:wght@400;500;700")
}

// Text alignment options
enum class EpubTextAlignment(
    val displayName: String,
    val cssValue: String
) {
    LEFT("Left", "left"),
    CENTER("Center", "center"),
    RIGHT("Right", "right"),
    JUSTIFY("Justify", "justify")
}

data class EpubTypographySettings(
    val fontSize: Float = 30f,
    val fontFamily: EpubFontFamily = EpubFontFamily.BOOKERLY,
    val fontColor: EpubFontColor? = null, // null means use theme color
    val lineSpacing: Float = 1.7f,
    val fontWeight: Float = 400f,
    val textAlignment: EpubTextAlignment = EpubTextAlignment.JUSTIFY,
    val keepScreenOn: Boolean = false,
    val showPageNumber: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubSettingsSheet(
    settings: EpubTypographySettings,
    onSettingsChange: (EpubTypographySettings) -> Unit,
    onDismiss: () -> Unit
) {
    val purple = MaterialTheme.colorScheme.primary
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 32.dp) // Extra padding at bottom for last item visibility
        ) {
            Text(
                text = "Reader Settings",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = purple,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Font Color Selection
            Text(
                text = "Font Color",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Add "Auto" option that uses theme color
                item {
                    FontColorOption(
                        displayName = "Auto",
                        color = null,
                        isSelected = settings.fontColor == null,
                        onClick = { onSettingsChange(settings.copy(fontColor = null)) }
                    )
                }
                items(EpubFontColor.entries.toList()) { fontColor ->
                    FontColorOption(
                        displayName = fontColor.displayName,
                        color = Color(fontColor.colorValue),
                        isSelected = settings.fontColor == fontColor,
                        onClick = { onSettingsChange(settings.copy(fontColor = fontColor)) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Font Size Slider
            SliderSettingWithValue(
                title = "Font Size",
                value = settings.fontSize,
                valueRange = 12f..70f,
                valueDisplay = "${settings.fontSize.toInt()}px",
                onValueChange = { onSettingsChange(settings.copy(fontSize = it)) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Font Family Selection
            Text(
                text = "Font Style",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                items(EpubFontFamily.entries.toList()) { font ->
                    FontFamilyOption(
                        font = font,
                        isSelected = settings.fontFamily == font,
                        onClick = { onSettingsChange(settings.copy(fontFamily = font)) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Line Spacing Slider
            SliderSettingWithValue(
                title = "Line Spacing",
                value = settings.lineSpacing,
                valueRange = 1.0f..3.0f,
                valueDisplay = String.format("%.1fx", settings.lineSpacing),
                onValueChange = { onSettingsChange(settings.copy(lineSpacing = it)) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Font Weight Slider
            SliderSettingWithValue(
                title = "Font Weight",
                value = settings.fontWeight,
                valueRange = 300f..700f,
                valueDisplay = when {
                    settings.fontWeight < 350 -> "Light"
                    settings.fontWeight < 450 -> "Regular"
                    settings.fontWeight < 550 -> "Medium"
                    settings.fontWeight < 650 -> "Semi-Bold"
                    else -> "Bold"
                },
                onValueChange = { onSettingsChange(settings.copy(fontWeight = it)) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Text Alignment
            Text(
                text = "Text Alignment",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EpubTextAlignment.entries.forEach { alignment ->
                    TextAlignmentOption(
                        alignment = alignment,
                        isSelected = settings.textAlignment == alignment,
                        onClick = { onSettingsChange(settings.copy(textAlignment = alignment)) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Toggle Options
            SwitchOption(
                title = "Keep Screen On",
                checked = settings.keepScreenOn,
                onCheckedChange = { onSettingsChange(settings.copy(keepScreenOn = it)) }
            )

            SwitchOption(
                title = "Show Page Number",
                checked = settings.showPageNumber,
                onCheckedChange = { onSettingsChange(settings.copy(showPageNumber = it)) }
            )


            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FontFamilyOption(
    font: EpubFontFamily,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(80.dp)
            .height(60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = font.previewText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = font.displayName,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TextAlignmentOption(
    alignment: EpubTextAlignment,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val lines = listOf(1f, 0.7f, 0.9f)
            lines.forEach { width ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .fillMaxWidth(width)
                        .height(3.dp)
                        .align(
                            when (alignment) {
                                EpubTextAlignment.LEFT -> Alignment.Start
                                EpubTextAlignment.CENTER -> Alignment.CenterHorizontally
                                EpubTextAlignment.RIGHT -> Alignment.End
                                EpubTextAlignment.JUSTIFY -> Alignment.CenterHorizontally
                            }
                        )
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            RoundedCornerShape(1.dp)
                        )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alignment.displayName,
                fontSize = 9.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SliderSettingWithValue(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueDisplay: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
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

@Composable
private fun SwitchOption(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
private fun FontColorOption(
    displayName: String,
    color: Color?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (color != null) color
                    else MaterialTheme.colorScheme.surfaceVariant
                )
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
                    tint = if (color != null) {
                        // Use contrasting color for check mark
                        if (color == Color(0xFF1A1A1A) || color == Color(0xFF3D3D3D) ||
                            color == Color(0xFF5B4636) || color == Color(0xFF1B365D) ||
                            color == Color(0xFF2D5A3D)) Color.White else Color.Black
                    } else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            } else if (color == null) {
                Text(
                    text = "A",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
    }
}


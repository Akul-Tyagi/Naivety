// app/src/main/java/com/abundance/naivety/ui/components/pdf/modals/MainSettingsSheet.kt
package com.abundance.naivety.ui.components.pdf.modals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abundance.naivety.R
import com.abundance.naivety.ui.pdf.PdfSettings
import com.abundance.naivety.ui.pdf.ScaleType
import com.abundance.naivety.ui.theme.NaivetyPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsSheet(
    settings: PdfSettings,
    onSettingsChange: (PdfSettings) -> Unit,
    onDismiss: () -> Unit,

) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface

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
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = NaivetyPurple,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Background Color Section
            Text(
                text = "Background Color",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            BackgroundColorSelector(
                selectedColor = settings.backgroundColor,
                onColorSelected = { color ->
                    onSettingsChange(settings.copy(backgroundColor = color))
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(20.dp))

            // Scale Type Section
            Text(
                text = "Page Fit",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ScaleTypeSelector(
                currentScale = settings.scaleType,
                onScaleTypeSelect = { scaleType ->
                    onSettingsChange(settings.copy(scaleType = scaleType))
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
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
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ColorOption(
                color = 0xFF000000, // Black
                label = "Black",
                isSelected = selectedColor == 0xFF000000,
                onClick = { onColorSelected(0xFF000000) }
            )
        }
        item {
            ColorOption(
                color = 0xFF424242, // Dark Gray
                label = "Dark",
                isSelected = selectedColor == 0xFF424242,
                onClick = { onColorSelected(0xFF424242) }
            )
        }
        item {
            ColorOption(
                color = 0xFFBE9D6A, // Sepia
                label = "Sepia",
                isSelected = selectedColor == 0xFFBE9D6A,
                onClick = { onColorSelected(0xFFBE9D6A) }
            )
        }
        item {
            ColorOption(
                color = 0xFFF5F5DC, // Cream
                label = "Cream",
                isSelected = selectedColor == 0xFFF5F5DC,
                onClick = { onColorSelected(0xFFF5F5DC) }
            )
        }
        item {
            ColorOption(
                color = 0xFFFFFFFF, // White
                label = "White",
                isSelected = selectedColor == 0xFFFFFFFF,
                onClick = { onColorSelected(0xFFFFFFFF) }
            )
        }
    }
}

@Composable
private fun ColorOption(
    color: Long,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier
                .size(48.dp),
            shape = CircleShape,
            border = if (isSelected) {
                BorderStroke(3.dp, NaivetyPurple)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            },
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(color))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) NaivetyPurple else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun ScaleTypeSelector(
    currentScale: ScaleType,
    onScaleTypeSelect: (ScaleType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Only show FIT_WIDTH and FIT_HEIGHT options
        ScaleTypeChip(
            label = "Fit Width",
            icon = Icons.Default.SwapHoriz,
            isSelected = currentScale == ScaleType.FIT_WIDTH || currentScale == ScaleType.FIT_PAGE,
            onClick = { onScaleTypeSelect(ScaleType.FIT_WIDTH) },
            modifier = Modifier.weight(1f)
        )

        ScaleTypeChip(
            label = "Fit Height",
            icon = Icons.Default.SwapVert,
            isSelected = currentScale == ScaleType.FIT_HEIGHT,
            onClick = { onScaleTypeSelect(ScaleType.FIT_HEIGHT) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScaleTypeChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) NaivetyPurple.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, NaivetyPurple) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) NaivetyPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontFamily = FontFamily(Font(R.font.fsb)),
                color = if (isSelected) NaivetyPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NaivetyPurple
                )
                Text(
                    text = title,
                    fontFamily = FontFamily(Font(R.font.fsb)),
                    color = MaterialTheme.colorScheme.onBackground,
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


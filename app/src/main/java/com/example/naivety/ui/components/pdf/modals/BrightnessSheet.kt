// app/src/main/java/com/example/naivety/ui/components/pdf/modals/BrightnessSheet.kt
package com.example.naivety.ui.components.pdf.modals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.naivety.ui.pdf.BrightnessSettings
import com.example.naivety.ui.pdf.ColorFilter
import com.example.naivety.ui.theme.NaivetyPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrightnessSheet(
    settings: BrightnessSettings,
    onSettingsChange: (BrightnessSettings) -> Unit,
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
                "Brightness & Color",
                style = MaterialTheme.typography.titleLarge,
                color = NaivetyPurple
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Brightness",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )

            Slider(
                value = settings.customBrightness,
                onValueChange = {
                    onSettingsChange(settings.copy(customBrightness = it))
                },
                colors = SliderDefaults.colors(
                    thumbColor = NaivetyPurple,
                    activeTrackColor = NaivetyPurple,
                    inactiveTrackColor = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ColorFilterControls(
                colorFilter = settings.colorFilter,
                onColorFilterChange = {
                    onSettingsChange(settings.copy(colorFilter = it))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Greyscale",
                    color = Color.White
                )
                Switch(
                    checked = settings.isGreyscale,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(isGreyscale = it))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NaivetyPurple,
                        checkedTrackColor = NaivetyPurple.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@Composable
private fun ColorFilterControls(
    colorFilter: ColorFilter,
    onColorFilterChange: (ColorFilter) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ColorSlider("Red", colorFilter.red) {
            onColorFilterChange(colorFilter.copy(red = it))
        }
        ColorSlider("Green", colorFilter.green) {
            onColorFilterChange(colorFilter.copy(green = it))
        }
        ColorSlider("Blue", colorFilter.blue) {
            onColorFilterChange(colorFilter.copy(blue = it))
        }
        ColorSlider("Alpha", colorFilter.alpha) {
            onColorFilterChange(colorFilter.copy(alpha = it))
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Text(
        text = label,
        color = Color.White,
        style = MaterialTheme.typography.titleSmall
    )
    Slider(
        value = value,
        onValueChange = onValueChange,
        colors = SliderDefaults.colors(
            thumbColor = NaivetyPurple,
            activeTrackColor = NaivetyPurple,
            inactiveTrackColor = Color.DarkGray
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
}
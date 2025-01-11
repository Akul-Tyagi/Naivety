// app/src/main/java/com/example/naivety/ui/components/pdf/modals/BrightnessSheet.kt
package com.example.naivety.ui.components.pdf.modals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.naivety.ui.pdf.BrightnessSettings
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.naivety.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrightnessSheet(
    settings: BrightnessSettings,
    onSettingsChange: (BrightnessSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val purple = Color(0xFF8E42FF)

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

            // Night Mode Switch
            SwitchOption(
                title = "Night Mode",
                checked = settings.nightMode,
                onCheckedChange = {
                    onSettingsChange(settings.copy(nightMode = it))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
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
            color = Color.White
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF8E42FF),
                checkedTrackColor = Color(0xFF8E42FF).copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun SliderOption(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontFamily= FontFamily(Font(R.font.fsb)),
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF8E42FF),
                activeTrackColor = Color(0xFF8E42FF).copy(alpha = 0.7f),
                inactiveTrackColor = Color(0xFF8E42FF).copy(alpha = 0.3f)
            )
        )
    }
}
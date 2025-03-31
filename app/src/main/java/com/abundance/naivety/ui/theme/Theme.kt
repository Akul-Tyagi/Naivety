package com.abundance.naivety.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = NaivetyPurple,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkBackground,
    surface = SurfaceDark,
    onBackground = Color.White,
    onSurface = Color.White
)

// Light color scheme
private val LightColorScheme = lightColorScheme(
    primary = NaivetyPurple,  // Keep purple consistent
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.White,
    surface = Color(0xFFF5F5F5),  // Light surface color
    onBackground = Color(0xFF121212),
    onSurface = Color(0xFF121212)
)

@Composable
fun NaivetyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // Use system default instead of forcing dark
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalView.current.context
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color to transparent instead of primary
            window.statusBarColor = Color.Transparent.toArgb()
            // Adjust status bar icon color based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
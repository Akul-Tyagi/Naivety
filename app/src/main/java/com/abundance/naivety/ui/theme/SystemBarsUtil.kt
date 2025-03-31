package com.abundance.naivety.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun TransparentSystemBars(darkTheme: Boolean) {
    val systemUiController = rememberSystemUiController()

    DisposableEffect(systemUiController, darkTheme) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !darkTheme,
            isNavigationBarContrastEnforced = false
        )

        onDispose {}
    }
}

@Composable
fun SplashSystemBars() {
    val systemUiController = rememberSystemUiController()
    val splashColor = Color(0xFF8E42FF) // Your splash screen purple color

    DisposableEffect(systemUiController) {
        systemUiController.setSystemBarsColor(
            color = splashColor,
            darkIcons = false
        )

        onDispose {}
    }
}
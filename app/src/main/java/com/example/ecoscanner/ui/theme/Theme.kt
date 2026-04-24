package com.example.ecoscanner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Tradewind,
    onPrimary = Color.White,
    primaryContainer = MossGreen,
    onPrimaryContainer = Como,
    secondary = Como,
    onSecondary = Color.White,
    secondaryContainer = GrayNurse,
    onSecondaryContainer = Como,
    tertiary = MossGreen,
    onTertiary = Como,
    background = SpringWood,
    onBackground = Como,
    surface = Color.White,
    onSurface = Como,
    surfaceVariant = GrayNurse,
    onSurfaceVariant = Como,
    outline = Tradewind
)

@Composable
fun EcoscannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Tradewind.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
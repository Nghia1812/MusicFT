package com.prj.musicft.presentation.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// We prioritize Dark Theme for the "Cyberpunk / Premium" look.
// Defining the Dark Color Scheme based on extracted colors.

private val DarkColorScheme =
        darkColorScheme(
                primary = CyberpunkTeal,
                onPrimary = Color.Black, // Text on Teal button
                primaryContainer = SurfaceSlate,
                onPrimaryContainer = CyberpunkTeal,
                secondary = CyberpunkMagenta,
                onSecondary = Color.White,
                secondaryContainer = SurfaceSlate,
                onSecondaryContainer = CyberpunkMagenta,
                tertiary = NeonGradientStart,
                background = DarkBackground,
                onBackground = LightText,
                surface = SurfaceSlate,
                onSurface = LightText,
                surfaceVariant = SurfaceSlate, // Slightly lighter if needed, or same
                onSurfaceVariant = GrayText,
                error = ErrorRed,
                onError = Color.White
        )

// Fallback Light Scheme (though app seems designed for Dark Mode)
private val LightColorScheme =
        lightColorScheme(
                primary = CyberpunkTeal,
                onPrimary = Color.White,
                secondary = CyberpunkMagenta,
                onSecondary = Color.White,
                background = Color(0xFFF5F5F5),
                surface = Color.White,
                onBackground = Color.Black,
                onSurface = Color.Black
        )

@Composable
fun MusicFTTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        // Dynamic color is available on Android 12+
        // For this specific design language, we might want to DISABLE dynamic color
        // to strictly adhere to the "Teal/Slate" aesthetic provided in the DNA.
        // However, I'll make it an option defaulting to FALSE to prioritize our custom DNA.
        dynamicColor: Boolean = false,
        content: @Composable () -> Unit
) {
    val colorScheme =
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context)
                    else dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

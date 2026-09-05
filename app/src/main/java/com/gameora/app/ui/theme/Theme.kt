package com.gameora.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val GameoraDarkColors = darkColorScheme(
    primary = GameoraPrimary,
    onPrimary = GameoraTextPrimary,

    primaryContainer = GameoraPrimaryDark,
    onPrimaryContainer = GameoraTextPrimary,

    secondary = GameoraSecondary,
    onSecondary = GameoraBackground,

    background = GameoraBackground,
    onBackground = GameoraTextPrimary,

    surface = GameoraSurface,
    onSurface = GameoraTextPrimary,

    surfaceVariant = GameoraSurfaceVariant,
    onSurfaceVariant = GameoraTextSecondary,

    outline = GameoraBorder,

    error = GameoraError,
    onError = GameoraTextPrimary
)

@Composable
fun GameoraTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicDarkColorScheme(LocalContext.current)
        }

        else -> GameoraDarkColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GameoraTypography,
        content = content
    )
}

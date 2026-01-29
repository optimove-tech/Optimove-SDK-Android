package com.optimove.android.optimovemobilesdk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// iOS-inspired palette: system grays, blue accent (#007AFF), clean backgrounds
private val iOSBlue = Color(0xFF007AFF)
private val iOSBlueDark = Color(0xFF0A84FF)
private val iOSGray6 = Color(0xFFF2F2F7)
private val iOSGray5 = Color(0xFFE5E5EA)
private val iOSGray4 = Color(0xFFD1D1D6)
private val iOSGray3 = Color(0xFFC7C7CC)
private val iOSGray2 = Color(0xFFAEAEB2)
private val iOSGray = Color(0xFF8E8E93)
private val iOSFill = Color(0xFF787880)
private val iOSRed = Color(0xFFC75858) // Softer red for error/destructive actions
private val iOSGreen = Color(0xFF34C759)

private val LightColorScheme = lightColorScheme(
    primary = iOSBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = iOSGray,
    onSecondary = Color.White,
    tertiary = iOSGray2,
    onTertiary = Color.White,
    surface = iOSGray6,
    onSurface = Color.Black,
    surfaceVariant = Color.White,
    onSurfaceVariant = Color(0xFF1C1B1F),
    outline = iOSGray4,
    error = iOSRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = iOSBlueDark,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004578),
    onPrimaryContainer = Color(0xFFD0E4FF),
    secondary = iOSGray2,
    onSecondary = Color.Black,
    surface = Color(0xFF1C1C1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFE5E5EA),
    outline = iOSGray3,
    error = iOSRed,
    onError = Color.Black
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

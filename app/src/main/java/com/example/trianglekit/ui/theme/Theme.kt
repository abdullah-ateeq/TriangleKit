package com.example.trianglekit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.trianglekit.repository.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = GeoBlue80,
    onPrimary = Color(0xFF00315F),
    primaryContainer = Color(0xFF004786),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = GeoTeal80,
    onSecondary = Color(0xFF003735),
    secondaryContainer = Color(0xFF004F4D),
    onSecondaryContainer = Color(0xFF9CF2EC),
    tertiary = AccentGold,
    background = GeoDarkBackground,
    surface = GeoDarkSurface,
    surfaceVariant = GeoDarkSurfaceVariant,
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6),
    onSurfaceVariant = Color(0xFFC4C6D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = GeoBlue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = GeoTeal40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF9CF2EC),
    onSecondaryContainer = Color(0xFF00201F),
    tertiary = Color(0xFF8A5100),
    background = GeoLightBackground,
    surface = GeoLightSurface,
    surfaceVariant = GeoLightSurfaceVariant,
    onBackground = Color(0xFF191C20),
    onSurface = Color(0xFF191C20),
    onSurfaceVariant = Color(0xFF44474F),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun TriangleKitTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

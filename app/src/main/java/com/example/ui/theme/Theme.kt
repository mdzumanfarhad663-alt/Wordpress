package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlueLight,
    onPrimary = FrostedBackgroundDark,
    primaryContainer = SkyBlueDark,
    onPrimaryContainer = DarkOnBackground,
    secondary = IndigoGlowLight,
    onSecondary = FrostedBackgroundDark,
    secondaryContainer = IndigoGlow.copy(alpha = 0.3f),
    onSecondaryContainer = DarkOnBackground,
    tertiary = EmeraldActive,
    onTertiary = FrostedBackgroundDark,
    background = FrostedBackgroundDark,
    onBackground = DarkOnBackground,
    surface = FrostedGlassDark,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = LightSurface,
    primaryContainer = SkyBlueUltraLight,
    onPrimaryContainer = SkyBlueDark,
    secondary = IndigoGlow,
    onSecondary = LightSurface,
    secondaryContainer = IndigoGlow.copy(alpha = 0.15f),
    onSecondaryContainer = SlateDark,
    tertiary = EmeraldActive,
    onTertiary = LightSurface,
    background = FrostedBackground,
    onBackground = SlateDark,
    surface = FrostedGlassWhite,
    onSurface = SlateDark,
    surfaceVariant = Color(0xFFE8EEF5),
    onSurfaceVariant = SlateMuted
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded audio palette crisp and consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

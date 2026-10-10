package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TextPrimaryDark,
    onPrimary = NeutralDarkBg,
    primaryContainer = SurfaceDarkVariant,
    onPrimaryContainer = TextPrimaryDark,
    secondary = UtilityAccentDark,
    onSecondary = NeutralDarkBg,
    background = NeutralDarkBg,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceBorderDark,
    outlineVariant = SurfaceBorderDark,
    error = UtilityRedDark,
    onError = NeutralDarkBg
)

private val LightColorScheme = lightColorScheme(
    primary = TextPrimaryLight,
    onPrimary = PureWhite,
    primaryContainer = SurfaceSubtle,
    onPrimaryContainer = TextPrimaryLight,
    secondary = UtilityAccent,
    onSecondary = PureWhite,
    background = NeutralWhite,
    onBackground = TextPrimaryLight,
    surface = PureWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = TextSecondaryLight,
    outline = SurfaceBorderLight,
    outlineVariant = SurfaceBorderLight,
    error = UtilityRed,
    onError = PureWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional brand colors
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

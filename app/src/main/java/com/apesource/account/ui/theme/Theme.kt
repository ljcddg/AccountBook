package com.apesource.account.ui.theme

import android.app.Activity
import androidx.compose.ui.graphics.Color
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
    primary = PrimaryLight,
    onPrimary = BackgroundDark,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = AccentLight,
    onSecondary = BackgroundDark,
    secondaryContainer = AccentDark,
    onSecondaryContainer = TextPrimaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    error = Error,
    onError = BackgroundDark,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error,
    outline = BorderDark,
    outlineVariant = BorderDark,
    scrim = Color(0xFF0F172A)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Surface,
    primaryContainer = CardBlue,
    onPrimaryContainer = PrimaryDark,
    secondary = Accent,
    onSecondary = Surface,
    secondaryContainer = CardCyan,
    onSecondaryContainer = AccentDark,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = Surface,
    errorContainer = CardRed,
    onErrorContainer = Error,
    outline = BorderLight,
    outlineVariant = Divider,
    scrim = Color(0x00000000)
)

@Composable
fun AccountTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

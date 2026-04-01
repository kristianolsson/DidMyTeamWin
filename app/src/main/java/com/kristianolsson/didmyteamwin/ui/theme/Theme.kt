package com.kristianolsson.didmyteamwin.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8BD0F0),
    onPrimary = Color(0xFF003547),
    primaryContainer = Color(0xFF004D65),
    onPrimaryContainer = Color(0xFFBEE8FF),
    secondary = Color(0xFF85D2A5),
    onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF005234),
    onSecondaryContainer = Color(0xFFA1F0BF),
    tertiary = Color(0xFFE0B8FF),
    onTertiary = Color(0xFF3F1F5C),
    tertiaryContainer = Color(0xFF573775),
    onTertiaryContainer = Color(0xFFF0DBFF),
    background = Color(0xFF0F1A20),
    onBackground = Color(0xFFDDE3E8),
    surface = Color(0xFF0F1A20),
    onSurface = Color(0xFFDDE3E8),
    surfaceVariant = Color(0xFF3F484E),
    onSurfaceVariant = Color(0xFFBFC8CE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006685),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBEE8FF),
    onPrimaryContainer = Color(0xFF001F2A),
    secondary = Color(0xFF246C47),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA1F0BF),
    onSecondaryContainer = Color(0xFF002111),
    background = Color(0xFFF6FAFE),
    onBackground = Color(0xFF171C1F),
    surface = Color(0xFFF6FAFE),
    onSurface = Color(0xFF171C1F),
)

@Composable
fun DidMyTeamWinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
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
        content = content,
    )
}

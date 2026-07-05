package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ImmersiveIndigo,
    onPrimary = DeepPitchBlack,
    primaryContainer = ImmersiveIndigoMuted,
    onPrimaryContainer = DeepPitchBlack,
    secondary = ImmersiveMutedText,
    onSecondary = DeepPitchBlack,
    background = DeepPitchBlack,
    onBackground = ImmersiveLightGray,
    surface = CharcoalSurface,
    onSurface = ImmersiveLightGray,
    surfaceVariant = ThinBorderColor,
    onSurfaceVariant = ImmersiveIndigo
)

private val LightColorScheme = lightColorScheme(
    primary = ImmersiveIndigoMuted,
    onPrimary = LightSlateSurface,
    primaryContainer = ImmersiveIndigo,
    onPrimaryContainer = LightSlateSurface,
    secondary = LightSlateText,
    onSecondary = LightSlateSurface,
    background = LightSlateBg,
    onBackground = LightSlateText,
    surface = LightSlateSurface,
    onSurface = LightSlateText,
    surfaceVariant = LightSlateDivider,
    onSurfaceVariant = ImmersiveIndigoMuted
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Explicitly false to preserve Immersive UI brand
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

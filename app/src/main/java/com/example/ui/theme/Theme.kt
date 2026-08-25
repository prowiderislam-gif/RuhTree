package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FuturisticDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ObsidianBg,
    primaryContainer = Color(0xFF0C4A6E),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = NeonPurple,
    onSecondary = ObsidianBg,
    secondaryContainer = Color(0xFF581C87),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = NeonEmerald,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = TextWhitePrimary,
    surface = DeepSpaceSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = GlassSurfaceDark,
    onSurfaceVariant = TextMutedSecondary,
    outline = GlassCardBorder,
    outlineVariant = Color(0x1AFFFFFF),
    error = DeceasedVignetteRed,
    onError = TextWhitePrimary
)

@Composable
fun RuhTreeTheme(
    darkTheme: Boolean = true, // Default to sleek futuristic dark glass theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FuturisticDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun FamilyTreeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = RuhTreeTheme(darkTheme = darkTheme, content = content)

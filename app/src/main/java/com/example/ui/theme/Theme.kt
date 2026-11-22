package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = VermilionAccent,
    onPrimary = Color.White,
    primaryContainer = VermilionSoft,
    onPrimaryContainer = Color(0xFF4A1207),
    secondary = CobaltAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE5FF),
    onSecondaryContainer = Color(0xFF0E2363),
    tertiary = OchreAccent,
    onTertiary = GalleryInk,
    background = GalleryPaper,
    onBackground = GalleryInk,
    surface = GallerySurface,
    onSurface = GalleryInk,
    surfaceVariant = GallerySurfaceSubtle,
    onSurfaceVariant = GalleryMutedInk,
    outline = GalleryDivider,
    outlineVariant = GalleryDivider
)

private val DarkColorScheme = darkColorScheme(
    primary = VermilionAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4F1A10),
    onPrimaryContainer = VermilionSoft,
    secondary = Color(0xFF8AA8FF),
    onSecondary = Color(0xFF0A1D54),
    secondaryContainer = Color(0xFF1A2D63),
    onSecondaryContainer = Color(0xFFDCE5FF),
    tertiary = OchreAccent,
    onTertiary = ObsidianCanvas,
    background = ObsidianCanvas,
    onBackground = ObsidianInk,
    surface = ObsidianSurface,
    onSurface = ObsidianInk,
    surfaceVariant = ObsidianSurfaceSubtle,
    onSurfaceVariant = ObsidianMutedInk,
    outline = ObsidianDivider,
    outlineVariant = ObsidianDivider
)

val FormgramShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FormgramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FormgramTypography,
        shapes = FormgramShapes,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.model.FormPalette

// Swiss Gallery Light Tokens
val GalleryPaper = Color(0xFFFAF9F6)
val GallerySurface = Color(0xFFFFFFFF)
val GallerySurfaceSubtle = Color(0xFFF2EFE9)
val GalleryInk = Color(0xFF141418)
val GalleryMutedInk = Color(0xFF6B6862)
val GalleryDivider = Color(0xFFE7E3DA)

// Swiss Gallery Dark Tokens
val ObsidianCanvas = Color(0xFF0E0E11)
val ObsidianSurface = Color(0xFF16161B)
val ObsidianSurfaceSubtle = Color(0xFF212128)
val ObsidianInk = Color(0xFFF5F3ED)
val ObsidianMutedInk = Color(0xFFA09C94)
val ObsidianDivider = Color(0xFF2A2A33)

// Accents
val VermilionAccent = Color(0xFFE04F33)
val VermilionSoft = Color(0xFFFFE5DF)
val CobaltAccent = Color(0xFF2557D6)
val OchreAccent = Color(0xFFE3A018)
val HeartRed = Color(0xFFFF3040)

// Instagram-inspired Bauhaus Story Ring Gradient Stops
val StoryRingStart = Color(0xFFE3A018)
val StoryRingMid = Color(0xFFE04F33)
val StoryRingEnd = Color(0xFFC13584)

// Curated Museum / Bauhaus Gallery Art Palettes for square posts
val GalleryPalettes: List<FormPalette> = listOf(
    FormPalette(
        id = "bauhaus_cream",
        name = "Warm Bone",
        background = Color(0xFFF6F1E7),
        backgroundAccent = Color(0xFFECE2D0),
        foreground = Color(0xFF18181D),
        gridTint = Color(0x1818181D),
        isDark = false
    ),
    FormPalette(
        id = "vermilion_poster",
        name = "Vermilion",
        background = Color(0xFFDC4930),
        backgroundAccent = Color(0xFFB8331E),
        foreground = Color(0xFFFFF8EE),
        gridTint = Color(0x22FFF8EE),
        isDark = true
    ),
    FormPalette(
        id = "klein_cobalt",
        name = "Ultramarine",
        background = Color(0xFF1B45B8),
        backgroundAccent = Color(0xFF102D82),
        foreground = Color(0xFFF7F4EC),
        gridTint = Color(0x24F7F4EC),
        isDark = true
    ),
    FormPalette(
        id = "obsidian_chalk",
        name = "Obsidian",
        background = Color(0xFF16161B),
        backgroundAccent = Color(0xFF262630),
        foreground = Color(0xFFF5F1E8),
        gridTint = Color(0x1FF5F1E8),
        isDark = true
    ),
    FormPalette(
        id = "ochre_sun",
        name = "Ochre",
        background = Color(0xFFEDB634),
        backgroundAccent = Color(0xFFD99A18),
        foreground = Color(0xFF19181A),
        gridTint = Color(0x1C19181A),
        isDark = false
    ),
    FormPalette(
        id = "sage_botanical",
        name = "Sage Studio",
        background = Color(0xFFD8E4D7),
        backgroundAccent = Color(0xFFC0D4BF),
        foreground = Color(0xFF1D3B2A),
        gridTint = Color(0x1A1D3B2A),
        isDark = false
    ),
    FormPalette(
        id = "forest_deep",
        name = "Pine Press",
        background = Color(0xFF173B2D),
        backgroundAccent = Color(0xFF0E291E),
        foreground = Color(0xFFEAF0DC),
        gridTint = Color(0x22EAF0DC),
        isDark = true
    ),
    FormPalette(
        id = "lilac_fog",
        name = "Lilac Print",
        background = Color(0xFFE9E2F5),
        backgroundAccent = Color(0xFFD7CBEB),
        foreground = Color(0xFF2E1E59),
        gridTint = Color(0x1C2E1E59),
        isDark = false
    ),
    FormPalette(
        id = "terracotta_clay",
        name = "Terracotta",
        background = Color(0xFFC86446),
        backgroundAccent = Color(0xFFA74B30),
        foreground = Color(0xFFFDF5EB),
        gridTint = Color(0x20FDF5EB),
        isDark = true
    ),
    FormPalette(
        id = "ice_blueprint",
        name = "Blueprint",
        background = Color(0xFFDCEBF7),
        backgroundAccent = Color(0xFFC4DDF2),
        foreground = Color(0xFF103667),
        gridTint = Color(0x1E103667),
        isDark = false
    ),
    FormPalette(
        id = "plum_velvet",
        name = "Plum Ink",
        background = Color(0xFF3E1C47),
        backgroundAccent = Color(0xFF281030),
        foreground = Color(0xFFFBE7CF),
        gridTint = Color(0x20FBE7CF),
        isDark = true
    ),
    FormPalette(
        id = "blush_ceramic",
        name = "Ceramic Blush",
        background = Color(0xFFF8E0DC),
        backgroundAccent = Color(0xFFEECAC4),
        foreground = Color(0xFF5C1E26),
        gridTint = Color(0x1A5C1E26),
        isDark = false
    ),
    FormPalette(
        id = "slate_petrol",
        name = "Petrol",
        background = Color(0xFF1B3A44),
        backgroundAccent = Color(0xFF112830),
        foreground = Color(0xFFF4D38B),
        gridTint = Color(0x20F4D38B),
        isDark = true
    ),
    FormPalette(
        id = "matcha_ink",
        name = "Chartreuse",
        background = Color(0xFFD9E678),
        backgroundAccent = Color(0xFFC3D455),
        foreground = Color(0xFF1A2212),
        gridTint = Color(0x1B1A2212),
        isDark = false
    )
)

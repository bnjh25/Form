package com.example.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class FormCategory {
    ALL,
    SHAPES,
    LETTERS,
    DIGITS
}

enum class BasicShapeType(val displayName: String, val shortLabel: String) {
    CIRCLE("Circle", "circle"),
    SQUARE("Square", "square"),
    TRIANGLE("Triangle", "triangle"),
    DIAMOND("Diamond", "diamond"),
    PENTAGON("Pentagon", "pentagon"),
    HEXAGON("Hexagon", "hexagon"),
    OCTAGON("Octagon", "octagon"),
    STAR("Five-Point Star", "star"),
    RING("Ring", "ring"),
    ARCH("Arch", "arch"),
    CROSS("Swiss Cross", "cross"),
    SEMICIRCLE("Semicircle", "half"),
    PILL("Capsule", "capsule"),
    CRESCENT("Crescent", "crescent"),
    PLUS_DOT("Dot Matrix", "dot")
}

enum class SurfaceFinish(val displayName: String) {
    CLEAN("Clean Paper"),
    SWISS_GRID("Swiss Grid"),
    RADIAL_GLOW("Soft Spotlight"),
    FRAMED_MAT("Gallery Mat"),
    HALFTONE_DOTS("Micro Dots"),
    DIAGONAL_BAND("Bauhaus Split")
}

enum class GlyphFontStyle(val displayName: String) {
    GEOMETRIC_SANS("Geometric"),
    EDITORIAL_SERIF("Editorial"),
    ARCHITECT_MONO("Monospace")
}

@Immutable
sealed interface FormSubject {
    val category: FormCategory
    val badgeLabel: String
    val accessibleTitle: String

    @Immutable
    data class Shape(
        val shapeType: BasicShapeType,
        val filled: Boolean = true,
        val rotationDeg: Float = 0f
    ) : FormSubject {
        override val category: FormCategory = FormCategory.SHAPES
        override val badgeLabel: String = "SHAPE"
        override val accessibleTitle: String =
            if (filled) "Solid ${shapeType.displayName}" else "Outlined ${shapeType.displayName}"
    }

    @Immutable
    data class Letter(
        val char: Char,
        val lowercase: Boolean = false,
        val fontStyle: GlyphFontStyle = GlyphFontStyle.GEOMETRIC_SANS
    ) : FormSubject {
        init {
            require(char.isLetter()) { "Letter subject must be a single alphabetic character" }
        }

        val renderedChar: String
            get() = (if (lowercase) char.lowercaseChar() else char.uppercaseChar()).toString()

        override val category: FormCategory = FormCategory.LETTERS
        override val badgeLabel: String = "LETTER"
        override val accessibleTitle: String =
            if (lowercase) "Lowercase letter ${char.lowercaseChar()}" else "Uppercase letter ${char.uppercaseChar()}"
    }

    @Immutable
    data class Digit(
        val digit: Int,
        val fontStyle: GlyphFontStyle = GlyphFontStyle.GEOMETRIC_SANS
    ) : FormSubject {
        init {
            require(digit in 0..9) { "Digit subject must strictly be a single digit between 0 and 9" }
        }

        val renderedDigit: String
            get() = digit.toString()

        override val category: FormCategory = FormCategory.DIGITS
        override val badgeLabel: String = "DIGIT"
        override val accessibleTitle: String = "Single digit $digit"
    }
}

@Immutable
data class FormPalette(
    val id: String,
    val name: String,
    val background: Color,
    val backgroundAccent: Color,
    val foreground: Color,
    val gridTint: Color,
    val isDark: Boolean
)

@Immutable
data class AppreciationNote(
    val id: String,
    val authorHandle: String,
    val text: String,
    val timeAgo: String
)

@Immutable
data class FormPost(
    val id: String,
    val sequenceIndex: Int,
    val handle: String,
    val locationTag: String,
    val isVerified: Boolean,
    val subject: FormSubject,
    val palette: FormPalette,
    val finish: SurfaceFinish,
    val caption: String,
    val timeAgo: String,
    val baseLikes: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isUserCreated: Boolean = false,
    val notes: List<AppreciationNote> = emptyList()
) {
    val displayLikes: Int
        get() = baseLikes + if (isLiked) 1 else 0
}

@Immutable
data class FormStory(
    val id: String,
    val label: String,
    val handle: String,
    val subject: FormSubject,
    val palette: FormPalette,
    val finish: SurfaceFinish,
    val blurb: String,
    val isSeen: Boolean = false
)

enum class HapticFeel(val title: String, val subtitle: String) {
    CRISP_DETENT("Crisp Detent", "Precision mechanical click per form"),
    SOFT_TICK("Soft Tick", "Whisper-light tactile pulse"),
    HEAVY_CLICK("Heavy Click", "Bold physical notch on scroll"),
    OFF("Silent (Off)", "Visual scrolling without vibration")
}

@Immutable
data class HapticPreferences(
    val feel: HapticFeel = HapticFeel.CRISP_DETENT,
    val snapToPost: Boolean = false,
    val microScrollTicks: Boolean = true
)

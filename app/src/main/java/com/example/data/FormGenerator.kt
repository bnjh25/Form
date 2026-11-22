package com.example.data

import com.example.model.AppreciationNote
import com.example.model.BasicShapeType
import com.example.model.FormPalette
import com.example.model.FormPost
import com.example.model.FormStory
import com.example.model.FormSubject
import com.example.model.GlyphFontStyle
import com.example.model.SurfaceFinish
import com.example.ui.theme.GalleryPalettes
import kotlin.random.Random

/**
 * Infinite procedural curator for Formgram.
 * Generates an endless, high-variety stream of single basic shapes,
 * single letters (A-Z / a-z), and strictly single-digit numbers (0-9).
 */
object FormGenerator {

    private val digitWords = listOf(
        "zero", "one", "two", "three", "four",
        "five", "six", "seven", "eight", "nine"
    )

    private val shapeLocations = listOf(
        "Euclidean Plane",
        "Primary Geometry Lab",
        "Bauhaus Drafting Desk",
        "Pure Symmetry Hall",
        "Vector Room B",
        "Quiet Angles Studio",
        "Flatland Gallery"
    )

    private val letterLocations = listOf(
        "Baseline Grid",
        "Glyph Foundry",
        "Single Character Wing",
        "Cap Height Terrace",
        "Specimen Room",
        "Ink & Kerning Dept."
    )

    private val digitLocations = listOf(
        "Single Digit Studio",
        "Base Ten Pavilion",
        "Below Ten Archive",
        "Unit Place Gallery",
        "Integer Alcove",
        "One Column Press"
    )

    private val shapeCaptions = listOf(
        "just pausing to admire how unapologetically {name} this is.",
        "no texture, no drama. simply a well-behaved {name}.",
        "honestly? best {name} i have seen all week.",
        "the poise on this {name} deserves a full three seconds of silence.",
        "woke up early to appreciate a crisp {name}. zero regrets.",
        "effortless geometry. a {name} that knows exactly what it is.",
        "some days all your brain really needs is one clean {name}.",
        "look at those edges. peak {name} energy right here.",
        "kept it simple today. just a singular {name} doing its job.",
        "museum-grade {name}. no notes."
    )

    private val letterCaptions = listOf(
        "quietly admiring the silhouette of this {glyph}.",
        "no full words needed. one {glyph} says plenty.",
        "the balance on this {glyph} is genuinely soothing.",
        "taking a deep breath and appreciating the curves of {glyph}.",
        "underrated character honestly. big fan of {glyph} today.",
        "just a lone {glyph} standing comfortably on the baseline.",
        "nothing to read, nothing to reply to. only {glyph}.",
        "look at the negative space inside this {glyph}.",
        "pure typographic calm. presenting: {glyph}.",
        "sometimes one {glyph} is more than enough."
    )

    private val digitCaptions = listOf(
        "strictly one digit. this {digit} needs no neighbors.",
        "nothing beats a sturdy {digit} on a quiet afternoon.",
        "single-digit perfection. look how grounded this {digit} feels.",
        "stopped scrolling just to appreciate the sheer presence of {digit}.",
        "ten is overrated. staying comfortably here with {digit}.",
        "a remarkably composed {digit}. zero clutter.",
        "if serenity were a single numeral, it might just be {digit}.",
        "no math required. just admire the shape of {digit}.",
        "solid {digit}. genuinely immaculate proportions.",
        "pure single-digit elegance: {digit}."
    )

    private val noteAuthors = listOf(
        "quiet.grid",
        "mono.observer",
        "baseline.club",
        "dot.and.line",
        "single.unit",
        "calm.vector",
        "serif.lover",
        "angle.check",
        "pure.plane"
    )

    private val shapeNotes = listOf(
        "that proportion is immaculate.",
        "genuinely calming to look at.",
        "needed this exact shape today.",
        "100% pure geometry, zero noise.",
        "the centering here is top tier.",
        "saved immediately."
    )

    private val letterNotes = listOf(
        "the negative space really carries it.",
        "underrated letter for sure.",
        "cleanest glyph on my feed today.",
        "no word could improve this.",
        "effortless silhouette."
    )

    private val digitNotes = listOf(
        "elite single digit.",
        "so glad there's no second digit ruining the vibe.",
        "honestly a 9 out of 9.",
        "peak numeral form.",
        "surprisingly comforting."
    )

    private val timeLabels = listOf(
        "Just now", "2m", "5m", "9m", "14m", "23m", "41m", "1h", "2h", "3h", "5h", "8h", "1d"
    )

    /**
     * Generates a curated batch of posts starting at [startIndex] using [streamSeed].
     * Guarantees no immediate repetition of exact subjects or palettes.
     */
    fun generateBatch(
        startIndex: Int,
        count: Int,
        streamSeed: Long = 104729L,
        idPrefix: String = "post"
    ): List<FormPost> {
        val result = ArrayList<FormPost>(count)
        var lastPaletteId: String? = null
        var lastSubjectKey: String? = null

        for (offset in 0 until count) {
            val idx = startIndex + offset
            val rng = Random(streamSeed + idx * 7919L + 31L)

            // Rotate rhythm cleanly across Shapes, Digits (0-9), and Letters (A-Z)
            val kindSlot = (idx + rng.nextInt(3)) % 3
            var subject = createSubject(kindSlot, rng)
            var subjectKey = subjectKey(subject)
            var retries = 0
            while (subjectKey == lastSubjectKey && retries < 5) {
                subject = createSubject((kindSlot + retries + 1) % 3, rng)
                subjectKey = subjectKey(subject)
                retries++
            }
            lastSubjectKey = subjectKey

            var palette = GalleryPalettes[rng.nextInt(GalleryPalettes.size)]
            if (palette.id == lastPaletteId) {
                palette = GalleryPalettes[(GalleryPalettes.indexOf(palette) + 1 + rng.nextInt(4)) % GalleryPalettes.size]
            }
            lastPaletteId = palette.id

            val finish = SurfaceFinish.entries[rng.nextInt(SurfaceFinish.entries.size)]

            val post = buildPost(
                id = "${idPrefix}_$idx",
                sequenceIndex = idx,
                subject = subject,
                palette = palette,
                finish = finish,
                rng = rng
            )
            result.add(post)
        }
        return result
    }

    private fun createSubject(kindSlot: Int, rng: Random): FormSubject {
        return when (kindSlot) {
            0 -> {
                val shapeType = BasicShapeType.entries[rng.nextInt(BasicShapeType.entries.size)]
                val filled = rng.nextFloat() < 0.72f
                val rotation = when (shapeType) {
                    BasicShapeType.TRIANGLE -> listOf(0f, 90f, 180f, 270f)[rng.nextInt(4)]
                    BasicShapeType.SEMICIRCLE -> listOf(0f, 90f, 180f, 270f)[rng.nextInt(4)]
                    BasicShapeType.PILL -> listOf(0f, 45f, 90f)[rng.nextInt(3)]
                    BasicShapeType.ARCH -> listOf(0f, 180f)[rng.nextInt(2)]
                    BasicShapeType.CRESCENT -> listOf(-20f, 0f, 25f)[rng.nextInt(3)]
                    else -> 0f
                }
                FormSubject.Shape(shapeType = shapeType, filled = filled, rotationDeg = rotation)
            }
            1 -> {
                // Strictly single digit 0..9!
                val digit = rng.nextInt(0, 10)
                val fontStyle = GlyphFontStyle.entries[rng.nextInt(GlyphFontStyle.entries.size)]
                FormSubject.Digit(digit = digit, fontStyle = fontStyle)
            }
            else -> {
                val char = ('A'.code + rng.nextInt(26)).toChar()
                val lowercase = rng.nextFloat() < 0.34f
                val fontStyle = GlyphFontStyle.entries[rng.nextInt(GlyphFontStyle.entries.size)]
                FormSubject.Letter(char = char, lowercase = lowercase, fontStyle = fontStyle)
            }
        }
    }

    private fun subjectKey(subject: FormSubject): String = when (subject) {
        is FormSubject.Shape -> "S_${subject.shapeType.name}_${subject.filled}"
        is FormSubject.Letter -> "L_${subject.renderedChar}"
        is FormSubject.Digit -> "D_${subject.digit}"
    }

    private fun buildPost(
        id: String,
        sequenceIndex: Int,
        subject: FormSubject,
        palette: FormPalette,
        finish: SurfaceFinish,
        rng: Random
    ): FormPost {
        val handle: String
        val location: String
        val caption: String
        val isVerified: Boolean
        val notePool: List<String>

        when (subject) {
            is FormSubject.Shape -> {
                val prefix = listOf("pure", "daily", "only", "calm", "basic", "studio")[rng.nextInt(6)]
                handle = "$prefix.${subject.shapeType.shortLabel}"
                location = shapeLocations[rng.nextInt(shapeLocations.size)]
                caption = shapeCaptions[rng.nextInt(shapeCaptions.size)]
                    .replace("{name}", subject.shapeType.displayName.lowercase())
                isVerified = subject.shapeType in setOf(
                    BasicShapeType.CIRCLE,
                    BasicShapeType.SQUARE,
                    BasicShapeType.TRIANGLE,
                    BasicShapeType.STAR
                )
                notePool = shapeNotes
            }
            is FormSubject.Letter -> {
                val prefix = listOf("letter", "glyph", "type", "single", "mono")[rng.nextInt(5)]
                handle = "$prefix.${subject.char.lowercaseChar()}"
                location = letterLocations[rng.nextInt(letterLocations.size)]
                caption = letterCaptions[rng.nextInt(letterCaptions.size)]
                    .replace("{glyph}", subject.renderedChar)
                isVerified = subject.char.uppercaseChar() in setOf('A', 'E', 'O', 'S', 'X', 'Z')
                notePool = letterNotes
            }
            is FormSubject.Digit -> {
                val prefix = listOf("digit", "numeral", "just", "sole", "unit")[rng.nextInt(5)]
                handle = "$prefix.${digitWords[subject.digit]}"
                location = digitLocations[rng.nextInt(digitLocations.size)]
                caption = digitCaptions[rng.nextInt(digitCaptions.size)]
                    .replace("{digit}", subject.digit.toString())
                isVerified = subject.digit in setOf(0, 1, 3, 7, 8)
                notePool = digitNotes
            }
        }

        val noteCount = rng.nextInt(1, 4)
        val notes = List(noteCount) { n ->
            AppreciationNote(
                id = "${id}_note_$n",
                authorHandle = noteAuthors[(sequenceIndex + n * 3 + rng.nextInt(noteAuthors.size)) % noteAuthors.size],
                text = notePool[(sequenceIndex + n + rng.nextInt(notePool.size)) % notePool.size],
                timeAgo = listOf("1m", "6m", "18m", "1h", "3h")[rng.nextInt(5)]
            )
        }

        val timeAgo = timeLabels[(sequenceIndex / 2).coerceAtMost(timeLabels.lastIndex)]
        // Keep base appreciation count humble & tasteful (single digit 1..9 for iconic charm!)
        val baseLikes = rng.nextInt(1, 10)

        return FormPost(
            id = id,
            sequenceIndex = sequenceIndex,
            handle = handle,
            locationTag = location,
            isVerified = isVerified,
            subject = subject,
            palette = palette,
            finish = finish,
            caption = caption,
            timeAgo = timeAgo,
            baseLikes = baseLikes,
            notes = notes
        )
    }

    /**
     * Generates the top Instagram Stories tray featuring curated basic shapes,
     * single letters, and strictly single-digit numbers (0-9).
     */
    fun generateInitialStories(): List<FormStory> = listOf(
        FormStory(
            id = "story_0",
            label = "circle",
            handle = "pure.circle",
            subject = FormSubject.Shape(BasicShapeType.CIRCLE, filled = true),
            palette = GalleryPalettes[1],
            finish = SurfaceFinish.RADIAL_GLOW,
            blurb = "Round. Complete. Zero corners to worry about."
        ),
        FormStory(
            id = "story_1",
            label = "7",
            handle = "digit.seven",
            subject = FormSubject.Digit(7, GlyphFontStyle.EDITORIAL_SERIF),
            palette = GalleryPalettes[0],
            finish = SurfaceFinish.SWISS_GRID,
            blurb = "A poised single seven. Nothing more, nothing less."
        ),
        FormStory(
            id = "story_2",
            label = "A",
            handle = "letter.a",
            subject = FormSubject.Letter('A', lowercase = false, fontStyle = GlyphFontStyle.GEOMETRIC_SANS),
            palette = GalleryPalettes[2],
            finish = SurfaceFinish.FRAMED_MAT,
            blurb = "Where the alphabet calmly begins."
        ),
        FormStory(
            id = "story_3",
            label = "tri",
            handle = "pure.triangle",
            subject = FormSubject.Shape(BasicShapeType.TRIANGLE, filled = true),
            palette = GalleryPalettes[4],
            finish = SurfaceFinish.CLEAN,
            blurb = "Three sides, steady base, endless charm."
        ),
        FormStory(
            id = "story_4",
            label = "0",
            handle = "digit.zero",
            subject = FormSubject.Digit(0, GlyphFontStyle.ARCHITECT_MONO),
            palette = GalleryPalettes[3],
            finish = SurfaceFinish.HALFTONE_DOTS,
            blurb = "Zero. Quietly holding everything together."
        ),
        FormStory(
            id = "story_5",
            label = "hex",
            handle = "studio.hexagon",
            subject = FormSubject.Shape(BasicShapeType.HEXAGON, filled = false),
            palette = GalleryPalettes[5],
            finish = SurfaceFinish.SWISS_GRID,
            blurb = "Six crisp edges in peaceful equilibrium."
        ),
        FormStory(
            id = "story_6",
            label = "g",
            handle = "glyph.g",
            subject = FormSubject.Letter('g', lowercase = true, fontStyle = GlyphFontStyle.EDITORIAL_SERIF),
            palette = GalleryPalettes[10],
            finish = SurfaceFinish.RADIAL_GLOW,
            blurb = "Admire the sculpted loop on a lowercase g."
        ),
        FormStory(
            id = "story_7",
            label = "4",
            handle = "unit.four",
            subject = FormSubject.Digit(4, GlyphFontStyle.GEOMETRIC_SANS),
            palette = GalleryPalettes[8],
            finish = SurfaceFinish.DIAGONAL_BAND,
            blurb = "Architectural stability in a single numeral."
        ),
        FormStory(
            id = "story_8",
            label = "star",
            handle = "pure.star",
            subject = FormSubject.Shape(BasicShapeType.STAR, filled = true),
            palette = GalleryPalettes[12],
            finish = SurfaceFinish.FRAMED_MAT,
            blurb = "Five sharp points. Classic."
        ),
        FormStory(
            id = "story_9",
            label = "9",
            handle = "sole.nine",
            subject = FormSubject.Digit(9, GlyphFontStyle.EDITORIAL_SERIF),
            palette = GalleryPalettes[11],
            finish = SurfaceFinish.CLEAN,
            blurb = "The highest we ever count around here."
        ),
        FormStory(
            id = "story_10",
            label = "Q",
            handle = "letter.q",
            subject = FormSubject.Letter('Q', lowercase = false, fontStyle = GlyphFontStyle.GEOMETRIC_SANS),
            palette = GalleryPalettes[6],
            finish = SurfaceFinish.SWISS_GRID,
            blurb = "A circle that brought along a tiny tail."
        ),
        FormStory(
            id = "story_11",
            label = "ring",
            handle = "calm.ring",
            subject = FormSubject.Shape(BasicShapeType.RING, filled = false),
            palette = GalleryPalettes[9],
            finish = SurfaceFinish.RADIAL_GLOW,
            blurb = "An open loop for quiet breathing."
        )
    )
}

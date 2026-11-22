package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BasicShapeType
import com.example.model.FormPalette
import com.example.model.FormSubject
import com.example.model.GlyphFontStyle
import com.example.model.SurfaceFinish
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.SpaceGroteskFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Renders any basic shape, single letter, or single digit (0-9) on a curated
 * museum-grade surface. Adapts cleanly from tiny circular avatars (32dp) up
 * to full-width 1:1 exhibition posts.
 */
@Composable
fun FormCanvasArtwork(
    subject: FormSubject,
    palette: FormPalette,
    finish: SurfaceFinish,
    modifier: Modifier = Modifier,
    compactMode: Boolean = false,
    showBadge: Boolean = false
) {
    BoxWithConstraints(
        modifier = modifier.semantics {
            contentDescription = "${subject.accessibleTitle} on ${palette.name}"
        },
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val minSidePx = with(density) { min(maxWidth.toPx(), maxHeight.toPx()) }

        // Background + Surface Finish + Geometric Shape Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGallerySurface(palette = palette, finish = finish, compactMode = compactMode)

            if (subject is FormSubject.Shape) {
                drawBasicShape(
                    shapeType = subject.shapeType,
                    filled = subject.filled,
                    rotationDeg = subject.rotationDeg,
                    color = palette.foreground,
                    backgroundColor = palette.background,
                    compactMode = compactMode
                )
            }
        }

        // Single Letter or Strictly Single Digit (0-9) Typography Layer
        when (subject) {
            is FormSubject.Letter -> {
                val fontFamily = when (subject.fontStyle) {
                    GlyphFontStyle.GEOMETRIC_SANS -> SpaceGroteskFamily
                    GlyphFontStyle.EDITORIAL_SERIF -> PlayfairDisplayFamily
                    GlyphFontStyle.ARCHITECT_MONO -> JetBrainsMonoFamily
                }
                val glyphSp = with(density) { (minSidePx * (if (compactMode) 0.56f else 0.54f)).toSp() }
                Text(
                    text = subject.renderedChar,
                    color = palette.foreground,
                    fontFamily = fontFamily,
                    fontWeight = if (subject.fontStyle == GlyphFontStyle.EDITORIAL_SERIF) FontWeight.Bold else FontWeight.Bold,
                    fontSize = glyphSp,
                    lineHeight = glyphSp,
                    textAlign = TextAlign.Center,
                    softWrap = false
                )
            }
            is FormSubject.Digit -> {
                val fontFamily = when (subject.fontStyle) {
                    GlyphFontStyle.GEOMETRIC_SANS -> SpaceGroteskFamily
                    GlyphFontStyle.EDITORIAL_SERIF -> PlayfairDisplayFamily
                    GlyphFontStyle.ARCHITECT_MONO -> JetBrainsMonoFamily
                }
                val digitSp = with(density) { (minSidePx * (if (compactMode) 0.58f else 0.58f)).toSp() }
                Text(
                    text = subject.renderedDigit,
                    color = palette.foreground,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = digitSp,
                    lineHeight = digitSp,
                    textAlign = TextAlign.Center,
                    softWrap = false
                )
            }
            is FormSubject.Shape -> Unit
        }

        // Subtle gallery specimen tag on full posts
        if (showBadge && !compactMode) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp),
                shape = RoundedCornerShape(50),
                color = palette.foreground.copy(alpha = 0.11f)
            ) {
                Text(
                    text = subject.badgeLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = palette.foreground.copy(alpha = 0.72f),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.1.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun DrawScope.drawGallerySurface(
    palette: FormPalette,
    finish: SurfaceFinish,
    compactMode: Boolean
) {
    // Base fill
    drawRect(color = palette.background)

    if (compactMode) {
        // Keep mini avatars & tiny grid previews super legible
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(palette.background, palette.backgroundAccent),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
        )
        return
    }

    when (finish) {
        SurfaceFinish.CLEAN -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.background, palette.backgroundAccent.copy(alpha = 0.45f))
                )
            )
        }
        SurfaceFinish.SWISS_GRID -> {
            val step = size.minDimension / 8f
            val stroke = 1.dp.toPx()
            var x = step
            while (x < size.width) {
                drawLine(palette.gridTint, Offset(x, 0f), Offset(x, size.height), strokeWidth = stroke)
                x += step
            }
            var y = step
            while (y < size.height) {
                drawLine(palette.gridTint, Offset(0f, y), Offset(size.width, y), strokeWidth = stroke)
                y += step
            }
        }
        SurfaceFinish.RADIAL_GLOW -> {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(palette.background, palette.backgroundAccent),
                    center = Offset(size.width * 0.5f, size.height * 0.48f),
                    radius = size.minDimension * 0.78f
                )
            )
        }
        SurfaceFinish.FRAMED_MAT -> {
            drawRect(color = palette.backgroundAccent.copy(alpha = 0.7f))
            val inset = size.minDimension * 0.08f
            drawRect(
                color = palette.background,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2f, size.height - inset * 2f)
            )
            drawRect(
                color = palette.gridTint,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2f, size.height - inset * 2f),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }
        SurfaceFinish.HALFTONE_DOTS -> {
            val step = size.minDimension / 12f
            val radius = (size.minDimension * 0.0045f).coerceAtLeast(1.5f)
            var y = step * 0.75f
            while (y < size.height) {
                var x = step * 0.75f
                while (x < size.width) {
                    drawCircle(color = palette.gridTint, radius = radius, center = Offset(x, y))
                    x += step
                }
                y += step
            }
        }
        SurfaceFinish.DIAGONAL_BAND -> {
            val path = Path().apply {
                moveTo(0f, size.height * 0.62f)
                lineTo(size.width, size.height * 0.34f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = path, color = palette.backgroundAccent.copy(alpha = 0.6f))
        }
    }
}

private fun DrawScope.drawBasicShape(
    shapeType: BasicShapeType,
    filled: Boolean,
    rotationDeg: Float,
    color: Color,
    backgroundColor: Color,
    compactMode: Boolean
) {
    val dim = size.minDimension
    val radius = dim * (if (compactMode) 0.28f else 0.27f)
    val strokeW = dim * (if (compactMode) 0.075f else 0.055f)
    val style = if (filled) Fill else Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val cx = size.width / 2f
    val cy = size.height / 2f

    rotate(degrees = rotationDeg, pivot = Offset(cx, cy)) {
        when (shapeType) {
            BasicShapeType.CIRCLE -> {
                drawCircle(color = color, radius = radius, center = Offset(cx, cy), style = style)
            }
            BasicShapeType.SQUARE -> {
                val side = radius * 1.72f
                drawRect(
                    color = color,
                    topLeft = Offset(cx - side / 2f, cy - side / 2f),
                    size = Size(side, side),
                    style = style
                )
            }
            BasicShapeType.TRIANGLE -> {
                val path = regularPolygonPath(cx, cy + radius * 0.12f, radius * 1.15f, 3, -90.0)
                drawPath(path = path, color = color, style = style)
            }
            BasicShapeType.DIAMOND -> {
                val path = Path().apply {
                    moveTo(cx, cy - radius * 1.15f)
                    lineTo(cx + radius * 0.85f, cy)
                    lineTo(cx, cy + radius * 1.15f)
                    lineTo(cx - radius * 0.85f, cy)
                    close()
                }
                drawPath(path = path, color = color, style = style)
            }
            BasicShapeType.PENTAGON -> {
                val path = regularPolygonPath(cx, cy + radius * 0.05f, radius * 1.06f, 5, -90.0)
                drawPath(path = path, color = color, style = style)
            }
            BasicShapeType.HEXAGON -> {
                val path = regularPolygonPath(cx, cy, radius * 1.06f, 6, -30.0)
                drawPath(path = path, color = color, style = style)
            }
            BasicShapeType.OCTAGON -> {
                val path = regularPolygonPath(cx, cy, radius * 1.06f, 8, -22.5)
                drawPath(path = path, color = color, style = style)
            }
            BasicShapeType.STAR -> {
                val path = starPath(cx, cy + radius * 0.05f, outerRadius = radius * 1.12f, innerRadius = radius * 0.48f, points = 5)
                drawPath(path = path, color = color, style = style)
            }
            BasicShapeType.RING -> {
                drawCircle(
                    color = color,
                    radius = radius * 0.92f,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeW * 1.75f)
                )
            }
            BasicShapeType.ARCH -> {
                val w = radius * 1.65f
                val h = radius * 1.85f
                val left = cx - w / 2f
                val top = cy - h / 2f
                val path = Path().apply {
                    moveTo(left, top + h)
                    lineTo(left, top + w / 2f)
                    arcTo(
                        rect = Rect(left, top, left + w, top + w),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(left + w, top + h)
                    close()
                }
                drawPath(path = path, color = color, style = style)
            }
            BasicShapeType.CROSS -> {
                val arm = radius * 0.56f
                val len = radius * 1.85f
                if (filled) {
                    drawRect(color, Offset(cx - arm / 2f, cy - len / 2f), Size(arm, len))
                    drawRect(color, Offset(cx - len / 2f, cy - arm / 2f), Size(len, arm))
                } else {
                    val halfArm = arm / 2f
                    val halfLen = len / 2f
                    val path = Path().apply {
                        moveTo(cx - halfArm, cy - halfLen)
                        lineTo(cx + halfArm, cy - halfLen)
                        lineTo(cx + halfArm, cy - halfArm)
                        lineTo(cx + halfLen, cy - halfArm)
                        lineTo(cx + halfLen, cy + halfArm)
                        lineTo(cx + halfArm, cy + halfArm)
                        lineTo(cx + halfArm, cy + halfLen)
                        lineTo(cx - halfArm, cy + halfLen)
                        lineTo(cx - halfArm, cy + halfArm)
                        lineTo(cx - halfLen, cy + halfArm)
                        lineTo(cx - halfLen, cy - halfArm)
                        lineTo(cx - halfArm, cy - halfArm)
                        close()
                    }
                    drawPath(path = path, color = color, style = style)
                }
            }
            BasicShapeType.SEMICIRCLE -> {
                val r = radius * 1.1f
                val rect = Rect(cx - r, cy - r * 0.55f, cx + r, cy + r * 1.45f)
                val path = Path().apply {
                    arcTo(rect, 180f, 180f, true)
                    close()
                }
                drawPath(path = path, color = color, style = style)
            }
            BasicShapeType.PILL -> {
                val w = radius * 2.0f
                val h = radius * 1.05f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(cx - w / 2f, cy - h / 2f),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(h / 2f, h / 2f),
                    style = style
                )
            }
            BasicShapeType.CRESCENT -> {
                drawCircle(color = color, radius = radius, center = Offset(cx, cy))
                drawCircle(
                    color = backgroundColor,
                    radius = radius * 0.82f,
                    center = Offset(cx + radius * 0.42f, cy - radius * 0.14f)
                )
            }
            BasicShapeType.PLUS_DOT -> {
                // Single bold minimalist focal dot framed by a thin hairline ring
                drawCircle(color = color, radius = radius * 0.54f, center = Offset(cx, cy))
                drawCircle(
                    color = color.copy(alpha = 0.35f),
                    radius = radius * 1.02f,
                    center = Offset(cx, cy),
                    style = Stroke(width = (strokeW * 0.32f).coerceAtLeast(1.5f))
                )
            }
        }
    }
}

private fun regularPolygonPath(
    cx: Float,
    cy: Float,
    radius: Float,
    sides: Int,
    startAngleDeg: Double
): Path {
    val path = Path()
    for (i in 0 until sides) {
        val angleRad = (startAngleDeg + i * (360.0 / sides)) * PI / 180.0
        val x = cx + (radius * cos(angleRad)).toFloat()
        val y = cy + (radius * sin(angleRad)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun starPath(
    cx: Float,
    cy: Float,
    outerRadius: Float,
    innerRadius: Float,
    points: Int
): Path {
    val path = Path()
    val totalVertices = points * 2
    for (i in 0 until totalVertices) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angleRad = (-90.0 + i * (360.0 / totalVertices)) * PI / 180.0
        val x = cx + (r * cos(angleRad)).toFloat()
        val y = cy + (r * sin(angleRad)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

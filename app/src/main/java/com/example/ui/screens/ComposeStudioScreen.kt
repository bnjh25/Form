package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.haptics.ScrollHapticsEngine
import com.example.model.BasicShapeType
import com.example.model.FormPalette
import com.example.model.FormSubject
import com.example.model.GlyphFontStyle
import com.example.model.HapticFeel
import com.example.model.SurfaceFinish
import com.example.ui.components.FormCanvasArtwork
import com.example.ui.theme.GalleryPalettes

private enum class StudioKind { SHAPE, LETTER, DIGIT }

@Composable
fun ComposeStudioScreen(
    hapticFeel: HapticFeel,
    hapticsEngine: ScrollHapticsEngine,
    onPublish: (FormSubject, FormPalette, SurfaceFinish, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    var kind by remember { mutableStateOf(StudioKind.SHAPE) }
    var selectedShape by remember { mutableStateOf(BasicShapeType.CIRCLE) }
    var shapeFilled by remember { mutableStateOf(true) }
    var selectedLetter by remember { mutableStateOf('A') }
    var lowercaseLetter by remember { mutableStateOf(false) }
    // Strictly single digit 0..9
    var selectedDigit by remember { mutableIntStateOf(7) }
    var fontStyle by remember { mutableStateOf(GlyphFontStyle.GEOMETRIC_SANS) }
    var selectedPalette by remember { mutableStateOf(GalleryPalettes[0]) }
    var selectedFinish by remember { mutableStateOf(SurfaceFinish.SWISS_GRID) }
    var caption by remember { mutableStateOf("") }

    val activeSubject: FormSubject = when (kind) {
        StudioKind.SHAPE -> FormSubject.Shape(shapeType = selectedShape, filled = shapeFilled)
        StudioKind.LETTER -> FormSubject.Letter(char = selectedLetter, lowercase = lowercaseLetter, fontStyle = fontStyle)
        StudioKind.DIGIT -> FormSubject.Digit(digit = selectedDigit.coerceIn(0, 9), fontStyle = fontStyle)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp)
                .testTag("compose_studio_screen")
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = stringResource(R.string.studio_title),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.studio_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Live 1:1 Gallery Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                FormCanvasArtwork(
                    subject = activeSubject,
                    palette = selectedPalette,
                    finish = selectedFinish,
                    compactMode = false,
                    showBadge = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Kind Selector (Shape / Letter / Single Digit 0-9)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    StudioKind.SHAPE to stringResource(R.string.studio_kind_shape),
                    StudioKind.LETTER to stringResource(R.string.studio_kind_letter),
                    StudioKind.DIGIT to stringResource(R.string.studio_kind_digit)
                ).forEach { (itemKind, label) ->
                    val selected = kind == itemKind
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable {
                                hapticsEngine.triggerActionClick(hapticFeel, view)
                                kind = itemKind
                            }
                            .testTag("studio_kind_${itemKind.name.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.background
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form Picker specific to active kind
            when (kind) {
                StudioKind.SHAPE -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(BasicShapeType.entries, key = { it.name }) { shape ->
                            val isSelected = selectedShape == shape
                            Surface(
                                modifier = Modifier
                                    .height(42.dp)
                                    .clickable {
                                        hapticsEngine.triggerMicroScrollTick(hapticFeel, view)
                                        selectedShape = shape
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                },
                                border = if (isSelected) {
                                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = shape.displayName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Solid Fill",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Switch(
                            checked = shapeFilled,
                            onCheckedChange = {
                                hapticsEngine.triggerActionClick(hapticFeel, view)
                                shapeFilled = it
                            }
                        )
                    }
                }

                StudioKind.LETTER -> {
                    val alphabet = remember { ('A'..'Z').toList() }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(alphabet, key = { it.code }) { ch ->
                            val isSelected = selectedLetter == ch
                            val display = if (lowercaseLetter) ch.lowercaseChar().toString() else ch.toString()
                            Surface(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clickable {
                                        hapticsEngine.triggerMicroScrollTick(hapticFeel, view)
                                        selectedLetter = ch
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                },
                                border = if (isSelected) {
                                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = display,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Lowercase", style = MaterialTheme.typography.titleSmall)
                        Switch(
                            checked = lowercaseLetter,
                            onCheckedChange = {
                                hapticsEngine.triggerActionClick(hapticFeel, view)
                                lowercaseLetter = it
                            }
                        )
                    }

                    GlyphFontStyleRow(
                        selected = fontStyle,
                        onSelect = {
                            hapticsEngine.triggerActionClick(hapticFeel, view)
                            fontStyle = it
                        }
                    )
                }

                StudioKind.DIGIT -> {
                    // Strictly single digits 0..9
                    val digits = remember { (0..9).toList() }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(digits, key = { it }) { d ->
                            val isSelected = selectedDigit == d
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable {
                                        hapticsEngine.triggerMicroScrollTick(hapticFeel, view)
                                        selectedDigit = d
                                    }
                                    .testTag("studio_digit_$d"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                },
                                border = if (isSelected) {
                                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = d.toString(),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlyphFontStyleRow(
                        selected = fontStyle,
                        onSelect = {
                            hapticsEngine.triggerActionClick(hapticFeel, view)
                            fontStyle = it
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Palette Swatches
            Text(
                text = stringResource(R.string.studio_palette_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(GalleryPalettes, key = { it.id }) { palette ->
                    val isSelected = selectedPalette.id == palette.id
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                hapticsEngine.triggerMicroScrollTick(hapticFeel, view)
                                selectedPalette = palette
                            },
                        shape = CircleShape,
                        color = palette.background,
                        border = BorderStroke(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(palette.foreground)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Surface Finish Picker
            Text(
                text = stringResource(R.string.studio_finish_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SurfaceFinish.entries, key = { it.name }) { finish ->
                    val isSelected = selectedFinish == finish
                    Surface(
                        modifier = Modifier
                            .height(38.dp)
                            .clickable {
                                hapticsEngine.triggerMicroScrollTick(hapticFeel, view)
                                selectedFinish = finish
                            },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = finish.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.background
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Caption Input
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text(stringResource(R.string.studio_caption_label)) },
                placeholder = { Text(stringResource(R.string.studio_caption_hint)) },
                maxLines = 2,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("studio_caption_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    hapticsEngine.triggerHeartBeat(hapticFeel, view)
                    onPublish(activeSubject, selectedPalette, selectedFinish, caption)
                    caption = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp)
                    .testTag("studio_publish_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.studio_publish_button),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun GlyphFontStyleRow(
    selected: GlyphFontStyle,
    onSelect: (GlyphFontStyle) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GlyphFontStyle.entries.forEach { style ->
            val isSelected = selected == style
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clickable { onSelect(style) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = style.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

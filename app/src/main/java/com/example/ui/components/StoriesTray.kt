package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.FormStory
import com.example.ui.theme.HeartRed
import com.example.ui.theme.StoryRingEnd
import com.example.ui.theme.StoryRingMid
import com.example.ui.theme.StoryRingStart
import kotlinx.coroutines.delay

@Composable
fun StoriesTray(
    stories: List<FormStory>,
    onStoryClick: (Int) -> Unit,
    onScrollTick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(firstVisibleIndex) {
        if (initialized) {
            onScrollTick()
        } else {
            initialized = true
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .testTag("stories_tray"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(stories, key = { _, item -> item.id }) { index, story ->
            StoryBubbleItem(
                story = story,
                onClick = { onStoryClick(index) }
            )
        }
    }
}

@Composable
private fun StoryBubbleItem(
    story: FormStory,
    onClick: () -> Unit
) {
    val ringBrush = if (story.isSeen) {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.outline,
                MaterialTheme.colorScheme.outline
            )
        )
    } else {
        Brush.linearGradient(
            listOf(StoryRingStart, StoryRingMid, StoryRingEnd)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
            .testTag("story_item_${story.id}")
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(ringBrush)
                .padding(2.5.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .padding(2.5.dp),
            contentAlignment = Alignment.Center
        ) {
            FormCanvasArtwork(
                subject = story.subject,
                palette = story.palette,
                finish = story.finish,
                compactMode = true,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = story.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (story.isSeen) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StoryViewerOverlay(
    stories: List<FormStory>,
    currentIndex: Int,
    onAdvance: (Int) -> Unit,
    onClose: () -> Unit,
    onAppreciatePulse: () -> Unit
) {
    BackHandler(onBack = onClose)

    val story = stories.getOrNull(currentIndex) ?: return
    var progress by remember(currentIndex) { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var appreciated by remember(currentIndex) { mutableStateOf(false) }

    LaunchedEffect(currentIndex, isPaused) {
        val stepMs = 40L
        val totalMs = 4500L
        while (progress < 1f) {
            if (!isPaused) {
                delay(stepMs)
                progress = (progress + stepMs.toFloat() / totalMs).coerceAtMost(1f)
            } else {
                delay(60L)
            }
        }
        onAdvance(1)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D11))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("story_viewer_overlay")
    ) {
        // Full-bleed Story Artwork
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                FormCanvasArtwork(
                    subject = story.subject,
                    palette = story.palette,
                    finish = story.finish,
                    compactMode = false,
                    showBadge = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = story.blurb,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.padding(horizontal = 28.dp)
            )
        }

        // Tap zones (left = prev, right = next, press = pause)
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .pointerInput(currentIndex) {
                        detectTapGestures(
                            onTap = { onAdvance(-1) },
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            }
                        )
                    }
            )
            Box(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .pointerInput(currentIndex) {
                        detectTapGestures(
                            onTap = { onAdvance(1) },
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            }
                        )
                    }
            )
        }

        // Top Progress Segments + Story Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                stories.forEachIndexed { idx, _ ->
                    val segProgress = when {
                        idx < currentIndex -> 1f
                        idx == currentIndex -> progress
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { segProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.28f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormCanvasArtwork(
                    subject = story.subject,
                    palette = story.palette,
                    finish = story.finish,
                    compactMode = true,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = story.handle,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Text(
                        text = story.subject.accessibleTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("story_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Quiet Appreciation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Text(
                    text = if (appreciated) {
                        stringResource(R.string.story_appreciated)
                    } else {
                        stringResource(R.string.story_reply_hint)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }

            IconButton(
                onClick = {
                    appreciated = !appreciated
                    onAppreciatePulse()
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("story_heart_button")
            ) {
                Icon(
                    imageVector = if (appreciated) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(R.string.cd_like_post),
                    tint = if (appreciated) HeartRed else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

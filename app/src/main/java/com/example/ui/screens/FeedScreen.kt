package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.haptics.ScrollHapticsEngine
import com.example.model.FormCategory
import com.example.model.FormPost
import com.example.model.FormStory
import com.example.model.HapticFeel
import com.example.model.HapticPreferences
import com.example.ui.components.InstagramPostCard
import com.example.ui.components.StoriesTray
import com.example.ui.theme.PlayfairDisplayFamily
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

@Composable
fun FeedScreen(
    posts: List<FormPost>,
    stories: List<FormStory>,
    activeCategory: FormCategory,
    hapticPrefs: HapticPreferences,
    hapticsEngine: ScrollHapticsEngine,
    listState: LazyListState,
    onSelectCategory: (FormCategory) -> Unit,
    onStoryClick: (Int) -> Unit,
    onDoubleTapLike: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onOpenNotes: (String) -> Unit,
    onShare: (FormPost) -> Unit,
    onToggleSave: (String) -> Unit,
    onInspectPost: (String) -> Unit,
    onOpenHapticsSheet: () -> Unit,
    onOpenArchiveShortcut: () -> Unit,
    onLoadMore: (Int) -> Unit,
    onRecordObserved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val latestPrefs by rememberUpdatedState(hapticPrefs)
    val latestPosts by rememberUpdatedState(posts)
    val tickStepPx = remember(density) { with(density) { 68.dp.toPx() } }
    var scrollAccumulator by remember { mutableFloatStateOf(0f) }

    // Continuous subtle crown-tick haptic connection while scrolling vertically
    val microTickNestedScroll = remember(tickStepPx) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val dy = abs(consumed.y)
                if (dy > 0f && latestPrefs.microScrollTicks && latestPrefs.feel != HapticFeel.OFF) {
                    scrollAccumulator += dy
                    if (scrollAccumulator >= tickStepPx) {
                        scrollAccumulator %= tickStepPx
                        hapticsEngine.triggerMicroScrollTick(latestPrefs.feel, view)
                    }
                }
                return Offset.Zero
            }
        }
    }

    // Focal-post detent haptic + infinite pagination + observed tracking
    LaunchedEffect(listState) {
        var lastFocusedKey: Any? = null
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportMid = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val centeredItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - viewportMid)
            }
            val lastVisibleIdx = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            Triple(centeredItem?.key, centeredItem?.index ?: 0, lastVisibleIdx)
        }
            .distinctUntilChanged()
            .collect { (focusedKey, _, lastVisibleIdx) ->
                if (focusedKey != null && focusedKey != lastFocusedKey) {
                    if (lastFocusedKey != null && focusedKey.toString().startsWith("post_")) {
                        hapticsEngine.triggerScrollDetent(latestPrefs.feel, view)
                    }
                    lastFocusedKey = focusedKey

                    if (focusedKey.toString().startsWith("post_")) {
                        val rawId = focusedKey.toString().removePrefix("post_")
                        onRecordObserved(rawId)
                    }
                }

                // Subtract header items (stories + category strip = 2 items)
                val postIndex = (lastVisibleIdx - 2).coerceAtLeast(0)
                if (latestPosts.isNotEmpty()) {
                    onLoadMore(postIndex)
                }
            }
    }

    val snapFling = rememberSnapFlingBehavior(lazyListState = listState)
    val defaultFling = ScrollableDefaults.flingBehavior()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
        ) {
            // Classic Instagram Top Header
            FeedTopHeader(
                hapticFeel = hapticPrefs.feel,
                onOpenHapticsSheet = onOpenHapticsSheet,
                onOpenArchiveShortcut = onOpenArchiveShortcut
            )

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            LazyColumn(
                state = listState,
                flingBehavior = if (hapticPrefs.snapToPost) snapFling else defaultFling,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(microTickNestedScroll)
                    .testTag("infinite_feed_list"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Stories Tray
                item(key = "header_stories") {
                    StoriesTray(
                        stories = stories,
                        onStoryClick = onStoryClick,
                        onScrollTick = {
                            hapticsEngine.triggerMicroScrollTick(latestPrefs.feel, view)
                        }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    )
                }

                // Category Filter Bar (All / Shapes / Letters / 0-9)
                item(key = "header_filters") {
                    CategoryFilterRow(
                        selected = activeCategory,
                        onSelect = { cat ->
                            hapticsEngine.triggerActionClick(latestPrefs.feel, view)
                            onSelectCategory(cat)
                        }
                    )
                }

                // Infinite Posts
                items(
                    items = posts,
                    key = { post -> "post_${post.id}" }
                ) { post ->
                    InstagramPostCard(
                        post = post,
                        onDoubleTapLike = {
                            hapticsEngine.triggerHeartBeat(latestPrefs.feel, view)
                            onDoubleTapLike(post.id)
                        },
                        onToggleLike = {
                            if (!post.isLiked) {
                                hapticsEngine.triggerHeartBeat(latestPrefs.feel, view)
                            } else {
                                hapticsEngine.triggerActionClick(latestPrefs.feel, view)
                            }
                            onToggleLike(post.id)
                        },
                        onOpenNotes = {
                            hapticsEngine.triggerActionClick(latestPrefs.feel, view)
                            onOpenNotes(post.id)
                        },
                        onShare = {
                            hapticsEngine.triggerActionClick(latestPrefs.feel, view)
                            onShare(post)
                        },
                        onToggleSave = {
                            hapticsEngine.triggerActionClick(latestPrefs.feel, view)
                            onToggleSave(post.id)
                        },
                        onInspect = {
                            hapticsEngine.triggerActionClick(latestPrefs.feel, view)
                            onInspectPost(post.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedTopHeader(
    hapticFeel: HapticFeel,
    onOpenHapticsSheet: () -> Unit,
    onOpenArchiveShortcut: () -> Unit
) {
    val feelLabel = when (hapticFeel) {
        HapticFeel.CRISP_DETENT -> "Crisp"
        HapticFeel.SOFT_TICK -> "Soft"
        HapticFeel.HEAVY_CLICK -> "Heavy"
        HapticFeel.OFF -> "Off"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.app_name),
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            letterSpacing = (-0.6).sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("feed_wordmark")
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier
                    .height(36.dp)
                    .clickable(onClick = onOpenHapticsSheet)
                    .testTag("haptics_settings_button"),
                shape = RoundedCornerShape(50),
                color = if (hapticFeel != HapticFeel.OFF) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                border = BorderStroke(
                    1.dp,
                    if (hapticFeel != HapticFeel.OFF) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Vibration,
                        contentDescription = stringResource(R.string.cd_haptic_settings),
                        tint = if (hapticFeel != HapticFeel.OFF) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = feelLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (hapticFeel != HapticFeel.OFF) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            IconButton(
                onClick = onOpenArchiveShortcut,
                modifier = Modifier.testTag("top_saved_shortcut")
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(R.string.cd_saved_shortcuts),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryFilterRow(
    selected: FormCategory,
    onSelect: (FormCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val chips = listOf(
        FormCategory.ALL to stringResource(R.string.filter_all),
        FormCategory.SHAPES to stringResource(R.string.filter_shapes),
        FormCategory.LETTERS to stringResource(R.string.filter_letters),
        FormCategory.DIGITS to stringResource(R.string.filter_digits)
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chips, key = { it.first.name }) { (category, label) ->
            val isSelected = selected == category
            Surface(
                modifier = Modifier
                    .height(34.dp)
                    .clickable { onSelect(category) }
                    .testTag("filter_chip_${category.name.lowercase()}"),
                shape = RoundedCornerShape(50),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
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
}

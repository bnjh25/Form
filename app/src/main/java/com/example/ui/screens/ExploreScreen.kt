package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.haptics.ScrollHapticsEngine
import com.example.model.FormCategory
import com.example.model.FormPost
import com.example.model.HapticPreferences
import com.example.ui.components.FormCanvasArtwork
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ExploreScreen(
    posts: List<FormPost>,
    activeCategory: FormCategory,
    hapticPrefs: HapticPreferences,
    hapticsEngine: ScrollHapticsEngine,
    onSelectCategory: (FormCategory) -> Unit,
    onInspectPost: (String) -> Unit,
    onLoadMore: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val gridState = rememberLazyGridState()
    val latestPrefs by rememberUpdatedState(hapticPrefs)
    val latestPosts by rememberUpdatedState(posts)

    // Row-step haptic ticks + infinite grid pagination
    LaunchedEffect(gridState) {
        var lastRow = -1
        snapshotFlow {
            val firstIdx = gridState.firstVisibleItemIndex
            val lastIdx = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            (firstIdx / 3) to lastIdx
        }
            .distinctUntilChanged()
            .collect { (row, lastIdx) ->
                if (lastRow != -1 && row != lastRow) {
                    hapticsEngine.triggerMicroScrollTick(latestPrefs.feel, view)
                }
                lastRow = row
                if (latestPosts.isNotEmpty()) {
                    onLoadMore(lastIdx)
                }
            }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.explore_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.explore_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CategoryFilterRow(
                selected = activeCategory,
                onSelect = { cat ->
                    hapticsEngine.triggerActionClick(latestPrefs.feel, view)
                    onSelectCategory(cat)
                }
            )

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("explore_grid"),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    FormCanvasArtwork(
                        subject = post.subject,
                        palette = post.palette,
                        finish = post.finish,
                        compactMode = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable {
                                hapticsEngine.triggerActionClick(latestPrefs.feel, view)
                                onInspectPost(post.id)
                            }
                            .testTag("explore_tile_${post.id}")
                    )
                }
            }
        }
    }
}

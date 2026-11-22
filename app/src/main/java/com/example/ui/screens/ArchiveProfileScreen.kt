package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.haptics.ScrollHapticsEngine
import com.example.model.BasicShapeType
import com.example.model.FormPost
import com.example.model.FormSubject
import com.example.model.HapticFeel
import com.example.model.SurfaceFinish
import com.example.ui.components.FormCanvasArtwork
import com.example.ui.theme.GalleryPalettes

@Composable
fun ArchiveProfileScreen(
    observedCount: Int,
    savedPosts: List<FormPost>,
    likedPosts: List<FormPost>,
    createdPosts: List<FormPost>,
    hapticFeel: HapticFeel,
    hapticsEngine: ScrollHapticsEngine,
    onInspectPost: (String) -> Unit,
    onOpenHapticsSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var selectedSubTab by rememberSaveable { mutableIntStateOf(0) }

    val displayedPosts = when (selectedSubTab) {
        0 -> savedPosts
        1 -> likedPosts
        else -> createdPosts
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .testTag("archive_profile_screen")
        ) {
            // Instagram-style Profile Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "@${stringResource(R.string.profile_handle)}",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(
                    onClick = {
                        hapticsEngine.triggerActionClick(hapticFeel, view)
                        onOpenHapticsSheet()
                    },
                    modifier = Modifier.testTag("profile_haptics_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Vibration,
                        contentDescription = stringResource(R.string.cd_haptic_settings),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Avatar + 3-Stat Row (Observed, Appreciated, Saved)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(76.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    FormCanvasArtwork(
                        subject = FormSubject.Shape(BasicShapeType.CIRCLE, filled = true),
                        palette = GalleryPalettes[0],
                        finish = SurfaceFinish.SWISS_GRID,
                        compactMode = true,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn(value = observedCount.toString(), label = stringResource(R.string.profile_stat_scrolled))
                    StatColumn(value = likedPosts.size.toString(), label = stringResource(R.string.profile_stat_liked))
                    StatColumn(value = savedPosts.size.toString(), label = stringResource(R.string.profile_stat_saved))
                }
            }

            // Bio Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_name),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.profile_bio),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Instagram Profile Icon Tabs (Saved / Liked / Exhibits)
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = {
                        hapticsEngine.triggerActionClick(hapticFeel, view)
                        selectedSubTab = 0
                    },
                    icon = {
                        Icon(
                            imageVector = if (selectedSubTab == 0) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(R.string.profile_tab_saved)
                        )
                    },
                    text = { Text(stringResource(R.string.profile_tab_saved)) },
                    modifier = Modifier.testTag("archive_tab_saved")
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = {
                        hapticsEngine.triggerActionClick(hapticFeel, view)
                        selectedSubTab = 1
                    },
                    icon = {
                        Icon(
                            imageVector = if (selectedSubTab == 1) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(R.string.profile_tab_liked)
                        )
                    },
                    text = { Text(stringResource(R.string.profile_tab_liked)) },
                    modifier = Modifier.testTag("archive_tab_liked")
                )
                Tab(
                    selected = selectedSubTab == 2,
                    onClick = {
                        hapticsEngine.triggerActionClick(hapticFeel, view)
                        selectedSubTab = 2
                    },
                    icon = {
                        Icon(
                            imageVector = if (selectedSubTab == 2) Icons.Filled.GridOn else Icons.Outlined.GridOn,
                            contentDescription = stringResource(R.string.profile_tab_created)
                        )
                    },
                    text = { Text(stringResource(R.string.profile_tab_created)) },
                    modifier = Modifier.testTag("archive_tab_created")
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            if (displayedPosts.isEmpty()) {
                val emptyMessage = when (selectedSubTab) {
                    0 -> stringResource(R.string.profile_empty_saved)
                    1 -> stringResource(R.string.profile_empty_liked)
                    else -> stringResource(R.string.profile_empty_created)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (selectedSubTab) {
                                0 -> Icons.Outlined.BookmarkBorder
                                1 -> Icons.Outlined.FavoriteBorder
                                else -> Icons.Outlined.GridOn
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(displayedPosts, key = { it.id }) { post ->
                        FormCanvasArtwork(
                            subject = post.subject,
                            palette = post.palette,
                            finish = post.finish,
                            compactMode = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable {
                                    hapticsEngine.triggerActionClick(hapticFeel, view)
                                    onInspectPost(post.id)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

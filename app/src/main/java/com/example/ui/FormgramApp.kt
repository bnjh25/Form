package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.haptics.ScrollHapticsEngine
import com.example.model.FormPost
import com.example.ui.components.AppreciationNotesSheet
import com.example.ui.components.HapticsSettingsSheet
import com.example.ui.components.PostInspectionOverlay
import com.example.ui.components.StoryViewerOverlay
import com.example.ui.screens.ArchiveProfileScreen
import com.example.ui.screens.ComposeStudioScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FeedScreen
import kotlinx.coroutines.launch

private data class NavDestination(
    val tab: FormgramTab,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

private val NavDestinations = listOf(
    NavDestination(
        tab = FormgramTab.FEED,
        labelRes = R.string.nav_feed,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        tag = "nav_tab_feed"
    ),
    NavDestination(
        tab = FormgramTab.EXPLORE,
        labelRes = R.string.nav_explore,
        selectedIcon = Icons.Filled.GridView,
        unselectedIcon = Icons.Outlined.GridView,
        tag = "nav_tab_explore"
    ),
    NavDestination(
        tab = FormgramTab.CREATE,
        labelRes = R.string.nav_create,
        selectedIcon = Icons.Filled.AddBox,
        unselectedIcon = Icons.Outlined.AddBox,
        tag = "nav_tab_create"
    ),
    NavDestination(
        tab = FormgramTab.ARCHIVE,
        labelRes = R.string.nav_gallery,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        tag = "nav_tab_archive"
    )
)

@Composable
fun FormgramApp(
    viewModel: FormgramViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val hapticsEngine = remember(context) { ScrollHapticsEngine(context) }
    val feedListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val publishedToast = stringResource(R.string.studio_published_toast)

    // Return to Feed on system Back when on any secondary tab
    BackHandler(
        enabled = uiState.selectedTab != FormgramTab.FEED &&
            uiState.activeStoryIndex == null &&
            uiState.inspectedPostId == null
    ) {
        viewModel.selectTab(FormgramTab.FEED)
    }

    val onSelectTab: (FormgramTab) -> Unit = { targetTab ->
        hapticsEngine.triggerActionClick(uiState.hapticPrefs.feel, view)
        if (targetTab == FormgramTab.FEED && uiState.selectedTab == FormgramTab.FEED) {
            scope.launch { feedListState.animateScrollToItem(0) }
        } else {
            viewModel.selectTab(targetTab)
        }
    }

    val onSharePost: (FormPost) -> Unit = { post ->
        shareFormPost(context, post)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            NavDestinations.forEach { dest ->
                                val selected = uiState.selectedTab == dest.tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { onSelectTab(dest.tab) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(dest.labelRes),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onBackground,
                                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(dest.tag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            modifier = Modifier
                                .fillMaxHeight()
                                .statusBarsPadding(),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            NavDestinations.forEach { dest ->
                                val selected = uiState.selectedTab == dest.tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { onSelectTab(dest.tab) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(dest.labelRes)) },
                                    modifier = Modifier.testTag(dest.tag)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        when (uiState.selectedTab) {
                            FormgramTab.FEED -> {
                                FeedScreen(
                                    posts = uiState.visibleFeedPosts,
                                    stories = uiState.stories,
                                    activeCategory = uiState.feedCategory,
                                    hapticPrefs = uiState.hapticPrefs,
                                    hapticsEngine = hapticsEngine,
                                    listState = feedListState,
                                    onSelectCategory = viewModel::selectFeedCategory,
                                    onStoryClick = { idx ->
                                        hapticsEngine.triggerActionClick(uiState.hapticPrefs.feel, view)
                                        viewModel.openStory(idx)
                                    },
                                    onDoubleTapLike = viewModel::likeFromDoubleTap,
                                    onToggleLike = viewModel::toggleLike,
                                    onOpenNotes = viewModel::openNotes,
                                    onShare = onSharePost,
                                    onToggleSave = viewModel::toggleSave,
                                    onInspectPost = viewModel::openInspection,
                                    onOpenHapticsSheet = {
                                        hapticsEngine.triggerActionClick(uiState.hapticPrefs.feel, view)
                                        viewModel.setShowHapticsSheet(true)
                                    },
                                    onOpenArchiveShortcut = {
                                        onSelectTab(FormgramTab.ARCHIVE)
                                    },
                                    onLoadMore = viewModel::loadMoreFeedIfNeeded,
                                    onRecordObserved = viewModel::recordObserved
                                )
                            }

                            FormgramTab.EXPLORE -> {
                                ExploreScreen(
                                    posts = uiState.visibleExplorePosts,
                                    activeCategory = uiState.exploreCategory,
                                    hapticPrefs = uiState.hapticPrefs,
                                    hapticsEngine = hapticsEngine,
                                    onSelectCategory = viewModel::selectExploreCategory,
                                    onInspectPost = viewModel::openInspection,
                                    onLoadMore = viewModel::loadMoreExploreIfNeeded
                                )
                            }

                            FormgramTab.CREATE -> {
                                ComposeStudioScreen(
                                    hapticFeel = uiState.hapticPrefs.feel,
                                    hapticsEngine = hapticsEngine,
                                    onPublish = { subject, palette, finish, caption ->
                                        viewModel.publishExhibit(subject, palette, finish, caption)
                                        scope.launch {
                                            feedListState.scrollToItem(0)
                                            snackbarHostState.showSnackbar(publishedToast)
                                        }
                                    }
                                )
                            }

                            FormgramTab.ARCHIVE -> {
                                ArchiveProfileScreen(
                                    observedCount = uiState.observedPostIds.size,
                                    savedPosts = uiState.savedPosts,
                                    likedPosts = uiState.likedPosts,
                                    createdPosts = uiState.createdPosts,
                                    hapticFeel = uiState.hapticPrefs.feel,
                                    hapticsEngine = hapticsEngine,
                                    onInspectPost = viewModel::openInspection,
                                    onOpenHapticsSheet = { viewModel.setShowHapticsSheet(true) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full-Screen Post Inspection Overlay (from Explore / Archive / More)
        val inspected = uiState.inspectedPost
        AnimatedVisibility(
            visible = inspected != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (inspected != null) {
                PostInspectionOverlay(
                    post = inspected,
                    onBack = viewModel::closeInspection,
                    onDoubleTapLike = {
                        hapticsEngine.triggerHeartBeat(uiState.hapticPrefs.feel, view)
                        viewModel.likeFromDoubleTap(inspected.id)
                    },
                    onToggleLike = {
                        if (!inspected.isLiked) {
                            hapticsEngine.triggerHeartBeat(uiState.hapticPrefs.feel, view)
                        } else {
                            hapticsEngine.triggerActionClick(uiState.hapticPrefs.feel, view)
                        }
                        viewModel.toggleLike(inspected.id)
                    },
                    onOpenNotes = {
                        hapticsEngine.triggerActionClick(uiState.hapticPrefs.feel, view)
                        viewModel.openNotes(inspected.id)
                    },
                    onShare = {
                        hapticsEngine.triggerActionClick(uiState.hapticPrefs.feel, view)
                        onSharePost(inspected)
                    },
                    onToggleSave = {
                        hapticsEngine.triggerActionClick(uiState.hapticPrefs.feel, view)
                        viewModel.toggleSave(inspected.id)
                    }
                )
            }
        }

        // Full-Screen Instagram Story Viewer Overlay
        val activeStoryIdx = uiState.activeStoryIndex
        AnimatedVisibility(
            visible = activeStoryIdx != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (activeStoryIdx != null) {
                StoryViewerOverlay(
                    stories = uiState.stories,
                    currentIndex = activeStoryIdx,
                    onAdvance = { delta ->
                        hapticsEngine.triggerMicroScrollTick(uiState.hapticPrefs.feel, view)
                        viewModel.advanceStory(delta)
                    },
                    onClose = viewModel::closeStory,
                    onAppreciatePulse = {
                        hapticsEngine.triggerHeartBeat(uiState.hapticPrefs.feel, view)
                    }
                )
            }
        }

        // Haptics & Scroll Preferences Bottom Sheet
        if (uiState.showHapticsSheet) {
            HapticsSettingsSheet(
                prefs = uiState.hapticPrefs,
                onSelectFeel = { feel ->
                    viewModel.updateHapticFeel(feel)
                    hapticsEngine.triggerScrollDetent(feel, view)
                },
                onToggleSnap = { enabled ->
                    viewModel.setSnapToPost(enabled)
                    hapticsEngine.triggerActionClick(uiState.hapticPrefs.feel, view)
                },
                onToggleMicroTicks = { enabled ->
                    viewModel.setMicroScrollTicks(enabled)
                    hapticsEngine.triggerActionClick(uiState.hapticPrefs.feel, view)
                },
                onTestPulse = {
                    hapticsEngine.triggerScrollDetent(uiState.hapticPrefs.feel, view)
                },
                onDismiss = { viewModel.setShowHapticsSheet(false) }
            )
        }

        // Appreciation Notes (Comments) Bottom Sheet
        val notesPost = uiState.notesPost
        if (notesPost != null) {
            AppreciationNotesSheet(
                post = notesPost,
                onAddNote = { text ->
                    hapticsEngine.triggerActionClick(uiState.hapticPrefs.feel, view)
                    viewModel.addNote(notesPost.id, text)
                },
                onDismiss = viewModel::closeNotes
            )
        }
    }
}

private fun shareFormPost(context: Context, post: FormPost) {
    val shareText = "Appreciating a ${post.subject.accessibleTitle} on Formgram (${post.palette.name}) — \"${post.caption}\""
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Form"))
}

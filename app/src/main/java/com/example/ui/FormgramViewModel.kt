package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FormGenerator
import com.example.data.HapticPreferencesRepository
import com.example.model.AppreciationNote
import com.example.model.FormCategory
import com.example.model.FormPalette
import com.example.model.FormPost
import com.example.model.FormStory
import com.example.model.FormSubject
import com.example.model.HapticFeel
import com.example.model.HapticPreferences
import com.example.model.SurfaceFinish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FormgramTab {
    FEED,
    EXPLORE,
    CREATE,
    ARCHIVE
}

data class FormgramUiState(
    val selectedTab: FormgramTab = FormgramTab.FEED,
    val feedPosts: List<FormPost> = emptyList(),
    val explorePosts: List<FormPost> = emptyList(),
    val stories: List<FormStory> = emptyList(),
    val feedCategory: FormCategory = FormCategory.ALL,
    val exploreCategory: FormCategory = FormCategory.ALL,
    val hapticPrefs: HapticPreferences = HapticPreferences(),
    val observedPostIds: Set<String> = emptySet(),
    val activeStoryIndex: Int? = null,
    val notesPostId: String? = null,
    val inspectedPostId: String? = null,
    val showHapticsSheet: Boolean = false,
    val userCreatedCount: Int = 0
) {
    val visibleFeedPosts: List<FormPost>
        get() = if (feedCategory == FormCategory.ALL) {
            feedPosts
        } else {
            feedPosts.filter { it.subject.category == feedCategory }
        }

    val visibleExplorePosts: List<FormPost>
        get() = if (exploreCategory == FormCategory.ALL) {
            explorePosts
        } else {
            explorePosts.filter { it.subject.category == exploreCategory }
        }

    val allKnownPosts: List<FormPost>
        get() = (feedPosts + explorePosts).distinctBy { it.id }

    val likedPosts: List<FormPost>
        get() = allKnownPosts.filter { it.isLiked }

    val savedPosts: List<FormPost>
        get() = allKnownPosts.filter { it.isSaved }

    val createdPosts: List<FormPost>
        get() = feedPosts.filter { it.isUserCreated }

    val notesPost: FormPost?
        get() = notesPostId?.let { id -> allKnownPosts.firstOrNull { it.id == id } }

    val inspectedPost: FormPost?
        get() = inspectedPostId?.let { id -> allKnownPosts.firstOrNull { it.id == id } }
}

class FormgramViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = HapticPreferencesRepository(application.applicationContext)

    private var nextFeedIndex = 0
    private var nextExploreIndex = 0
    private val feedSeed = 202607L
    private val exploreSeed = 884219L

    private val _uiState = MutableStateFlow(
        FormgramUiState(
            feedPosts = FormGenerator.generateBatch(0, INITIAL_FEED_BATCH, feedSeed, "feed"),
            explorePosts = FormGenerator.generateBatch(0, INITIAL_EXPLORE_BATCH, exploreSeed, "explore"),
            stories = FormGenerator.generateInitialStories()
        )
    )
    val uiState: StateFlow<FormgramUiState> = _uiState.asStateFlow()

    init {
        nextFeedIndex = INITIAL_FEED_BATCH
        nextExploreIndex = INITIAL_EXPLORE_BATCH

        viewModelScope.launch {
            prefsRepo.preferencesFlow.collect { prefs ->
                _uiState.update { it.copy(hapticPrefs = prefs) }
            }
        }
    }

    fun selectTab(tab: FormgramTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectFeedCategory(category: FormCategory) {
        _uiState.update { state ->
            val updated = state.copy(feedCategory = category)
            if (updated.visibleFeedPosts.size < 12) {
                val extra = FormGenerator.generateBatch(nextFeedIndex, 36, feedSeed, "feed")
                nextFeedIndex += 36
                updated.copy(feedPosts = updated.feedPosts + extra)
            } else {
                updated
            }
        }
    }

    fun selectExploreCategory(category: FormCategory) {
        _uiState.update { state ->
            val updated = state.copy(exploreCategory = category)
            if (updated.visibleExplorePosts.size < 24) {
                val extra = FormGenerator.generateBatch(nextExploreIndex, 48, exploreSeed, "explore")
                nextExploreIndex += 48
                updated.copy(explorePosts = updated.explorePosts + extra)
            } else {
                updated
            }
        }
    }

    fun loadMoreFeedIfNeeded(lastVisibleFilteredIndex: Int) {
        val visibleCount = _uiState.value.visibleFeedPosts.size
        if (visibleCount - lastVisibleFilteredIndex <= PREFETCH_THRESHOLD) {
            val batchSize = if (_uiState.value.feedCategory == FormCategory.ALL) 20 else 36
            val more = FormGenerator.generateBatch(nextFeedIndex, batchSize, feedSeed, "feed")
            nextFeedIndex += batchSize
            _uiState.update { it.copy(feedPosts = it.feedPosts + more) }
        }
    }

    fun loadMoreExploreIfNeeded(lastVisibleFilteredIndex: Int) {
        val visibleCount = _uiState.value.visibleExplorePosts.size
        if (visibleCount - lastVisibleFilteredIndex <= EXPLORE_PREFETCH_THRESHOLD) {
            val batchSize = 36
            val more = FormGenerator.generateBatch(nextExploreIndex, batchSize, exploreSeed, "explore")
            nextExploreIndex += batchSize
            _uiState.update { it.copy(explorePosts = it.explorePosts + more) }
        }
    }

    fun recordObserved(postId: String) {
        if (postId in _uiState.value.observedPostIds) return
        _uiState.update { it.copy(observedPostIds = it.observedPostIds + postId) }
    }

    fun toggleLike(postId: String) {
        updatePost(postId) { it.copy(isLiked = !it.isLiked) }
    }

    fun likeFromDoubleTap(postId: String) {
        updatePost(postId) { it.copy(isLiked = true) }
    }

    fun toggleSave(postId: String) {
        updatePost(postId) { it.copy(isSaved = !it.isSaved) }
    }

    fun addNote(postId: String, rawText: String) {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return
        val newNote = AppreciationNote(
            id = "user_note_${System.currentTimeMillis()}",
            authorHandle = "appreciator",
            text = trimmed,
            timeAgo = "Just now"
        )
        updatePost(postId) { post ->
            post.copy(notes = post.notes + newNote)
        }
    }

    fun openNotes(postId: String) {
        _uiState.update { it.copy(notesPostId = postId) }
    }

    fun closeNotes() {
        _uiState.update { it.copy(notesPostId = null) }
    }

    fun openInspection(postId: String) {
        _uiState.update { it.copy(inspectedPostId = postId) }
    }

    fun closeInspection() {
        _uiState.update { it.copy(inspectedPostId = null) }
    }

    fun openStory(index: Int) {
        val stories = _uiState.value.stories
        if (index !in stories.indices) return
        val storyId = stories[index].id
        _uiState.update { state ->
            state.copy(
                activeStoryIndex = index,
                stories = state.stories.map { if (it.id == storyId) it.copy(isSeen = true) else it }
            )
        }
    }

    fun advanceStory(delta: Int) {
        val current = _uiState.value.activeStoryIndex ?: return
        val next = current + delta
        val stories = _uiState.value.stories
        if (next !in stories.indices) {
            _uiState.update { it.copy(activeStoryIndex = null) }
        } else {
            val storyId = stories[next].id
            _uiState.update { state ->
                state.copy(
                    activeStoryIndex = next,
                    stories = state.stories.map { if (it.id == storyId) it.copy(isSeen = true) else it }
                )
            }
        }
    }

    fun closeStory() {
        _uiState.update { it.copy(activeStoryIndex = null) }
    }

    fun setShowHapticsSheet(show: Boolean) {
        _uiState.update { it.copy(showHapticsSheet = show) }
    }

    fun updateHapticFeel(feel: HapticFeel) {
        _uiState.update { it.copy(hapticPrefs = it.hapticPrefs.copy(feel = feel)) }
        viewModelScope.launch { prefsRepo.setHapticFeel(feel) }
    }

    fun setSnapToPost(enabled: Boolean) {
        _uiState.update { it.copy(hapticPrefs = it.hapticPrefs.copy(snapToPost = enabled)) }
        viewModelScope.launch { prefsRepo.setSnapToPost(enabled) }
    }

    fun setMicroScrollTicks(enabled: Boolean) {
        _uiState.update { it.copy(hapticPrefs = it.hapticPrefs.copy(microScrollTicks = enabled)) }
        viewModelScope.launch { prefsRepo.setMicroScrollTicks(enabled) }
    }

    fun publishExhibit(
        subject: FormSubject,
        palette: FormPalette,
        finish: SurfaceFinish,
        captionInput: String
    ) {
        val count = _uiState.value.userCreatedCount + 1
        val finalCaption = captionInput.trim().ifEmpty {
            when (subject) {
                is FormSubject.Shape -> "quietly exhibiting a pure ${subject.shapeType.displayName.lowercase()}."
                is FormSubject.Letter -> "a singular ${subject.renderedChar}, freshly framed."
                is FormSubject.Digit -> "strictly single digit ${subject.digit}. appreciated in peace."
            }
        }
        val post = FormPost(
            id = "user_post_${System.currentTimeMillis()}_$count",
            sequenceIndex = -count,
            handle = "appreciator",
            locationTag = "Personal Studio",
            isVerified = true,
            subject = subject,
            palette = palette,
            finish = finish,
            caption = finalCaption,
            timeAgo = "Just now",
            baseLikes = 1,
            isLiked = true,
            isSaved = true,
            isUserCreated = true,
            notes = emptyList()
        )
        _uiState.update { state ->
            state.copy(
                feedPosts = listOf(post) + state.feedPosts,
                feedCategory = FormCategory.ALL,
                selectedTab = FormgramTab.FEED,
                userCreatedCount = count
            )
        }
    }

    private fun updatePost(postId: String, transform: (FormPost) -> FormPost) {
        _uiState.update { state ->
            state.copy(
                feedPosts = state.feedPosts.map { if (it.id == postId) transform(it) else it },
                explorePosts = state.explorePosts.map { if (it.id == postId) transform(it) else it }
            )
        }
    }

    companion object {
        private const val INITIAL_FEED_BATCH = 30
        private const val INITIAL_EXPLORE_BATCH = 48
        private const val PREFETCH_THRESHOLD = 8
        private const val EXPLORE_PREFETCH_THRESHOLD = 15
    }
}

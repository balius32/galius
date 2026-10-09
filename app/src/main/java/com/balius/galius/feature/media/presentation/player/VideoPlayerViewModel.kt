package com.balius.galius.feature.media.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balius.galius.core.mvi.Reducer
import com.balius.galius.feature.media.domain.model.MediaBrowseSource
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.feature.media.domain.usecase.ObserveBrowseMediaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VideoPlayerState(
    val items: List<MediaItem> = emptyList(),
    val currentIndex: Int = 0,
    val startMediaId: String,
    val chromeVisible: Boolean = true,
    val seekOverlaySeconds: Int? = null,
    val seekOverlayForward: Boolean = true,
    val holdSeekActive: Boolean = false,
    val resizeToFill: Boolean = false,
) {
    val currentItem: MediaItem?
        get() = items.getOrNull(currentIndex)
}

sealed interface VideoPlayerIntent {
    data class ItemsUpdated(val items: List<MediaItem>) : VideoPlayerIntent
    data class PageChanged(val index: Int) : VideoPlayerIntent
    data object ToggleChrome : VideoPlayerIntent
    data class ShowDoubleTapSeek(val forward: Boolean) : VideoPlayerIntent
    data object ClearDoubleTapSeek : VideoPlayerIntent
    data class HoldSeekChanged(val active: Boolean, val forward: Boolean, val seconds: Int) :
        VideoPlayerIntent
    data object ToggleResize : VideoPlayerIntent
}

class VideoPlayerReducer : Reducer<VideoPlayerState, VideoPlayerIntent> {
    override fun reduce(state: VideoPlayerState, intent: VideoPlayerIntent): VideoPlayerState =
        when (intent) {
            is VideoPlayerIntent.ItemsUpdated -> {
                if (intent.items.isEmpty()) {
                    state.copy(items = emptyList(), currentIndex = 0)
                } else {
                    val preferredId = state.currentItem?.id ?: state.startMediaId
                    val index = intent.items.indexOfFirst { it.id == preferredId }
                        .takeIf { it >= 0 }
                        ?: intent.items.indexOfFirst { it.id == state.startMediaId }
                            .takeIf { it >= 0 }
                        ?: 0
                    state.copy(items = intent.items, currentIndex = index)
                }
            }
            is VideoPlayerIntent.PageChanged -> state.copy(
                currentIndex = intent.index.coerceIn(0, (state.items.size - 1).coerceAtLeast(0)),
            )
            VideoPlayerIntent.ToggleChrome -> state.copy(chromeVisible = !state.chromeVisible)
            is VideoPlayerIntent.ShowDoubleTapSeek -> state.copy(
                seekOverlaySeconds = 10,
                seekOverlayForward = intent.forward,
                holdSeekActive = false,
            )
            VideoPlayerIntent.ClearDoubleTapSeek -> state.copy(
                seekOverlaySeconds = if (state.holdSeekActive) state.seekOverlaySeconds else null,
            )
            is VideoPlayerIntent.HoldSeekChanged -> {
                if (intent.active) {
                    state.copy(
                        holdSeekActive = true,
                        seekOverlaySeconds = intent.seconds,
                        seekOverlayForward = intent.forward,
                    )
                } else {
                    state.copy(
                        holdSeekActive = false,
                        seekOverlaySeconds = null,
                    )
                }
            }
            VideoPlayerIntent.ToggleResize -> state.copy(resizeToFill = !state.resizeToFill)
        }
}

class VideoPlayerViewModel(
    startMediaId: String,
    source: MediaBrowseSource,
    private val reducer: VideoPlayerReducer,
    private val observeBrowseMediaUseCase: ObserveBrowseMediaUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(VideoPlayerState(startMediaId = startMediaId))
    val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    private var resumeMediaId: String? = null
    private var resumePositionMs: Long = 0L
    private var resumePlayWhenReady: Boolean = true

    fun resumePositionFor(mediaId: String): Long =
        if (resumeMediaId == mediaId) resumePositionMs else 0L

    fun resumePlayWhenReadyFor(mediaId: String): Boolean =
        if (resumeMediaId == mediaId) resumePlayWhenReady else true

    fun onPlaybackSample(mediaId: String, positionMs: Long, playWhenReady: Boolean, ready: Boolean) {
        if (!ready) return
        resumeMediaId = mediaId
        resumePositionMs = positionMs
        resumePlayWhenReady = playWhenReady
    }

    init {
        viewModelScope.launch {
            observeBrowseMediaUseCase(source)
                .distinctUntilChanged { old, new ->
                    old.size == new.size && old.zip(new).all { (a, b) -> a.id == b.id }
                }
                .collect { items ->
                    val videos = items.filter { it.type == MediaType.Video }
                    onIntent(VideoPlayerIntent.ItemsUpdated(videos))
                }
        }
    }

    fun onIntent(intent: VideoPlayerIntent) {
        _state.update { reducer.reduce(it, intent) }
    }
}

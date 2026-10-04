package com.balius.galius.feature.media.presentation.viewer

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balius.galius.R
import com.balius.galius.core.mvi.Reducer
import com.balius.galius.feature.media.domain.model.MediaBrowseSource
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaSharePackage
import com.balius.galius.feature.media.domain.usecase.ObserveBrowseMediaUseCase
import com.balius.galius.feature.media.domain.usecase.PrepareMediaShareUseCase
import com.balius.galius.feature.tags.domain.model.CategoryWithTags
import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.usecase.ObserveCategoriesUseCase
import com.balius.galius.feature.tags.domain.usecase.ObserveMediaTagsUseCase
import com.balius.galius.feature.tags.domain.usecase.SetMediaTagUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MediaViewerState(
    val items: List<MediaItem> = emptyList(),
    val currentIndex: Int = 0,
    val startMediaId: String,
    val chromeVisible: Boolean = true,
    val showDetails: Boolean = false,
    val showTagPicker: Boolean = false,
    val detailsAssignedTags: List<Tag> = emptyList(),
    val allCategories: List<CategoryWithTags> = emptyList(),
) {
    val currentItem: MediaItem?
        get() = items.getOrNull(currentIndex)
}

sealed interface MediaViewerIntent {
    data class ItemsUpdated(val items: List<MediaItem>) : MediaViewerIntent
    data class PageChanged(val index: Int) : MediaViewerIntent
    data object ToggleChrome : MediaViewerIntent
    data object OpenDetails : MediaViewerIntent
    data object CloseDetails : MediaViewerIntent
    data object OpenTagPicker : MediaViewerIntent
    data object CloseTagPicker : MediaViewerIntent
    data class DetailsTagsUpdated(val tags: List<Tag>) : MediaViewerIntent
    data class CategoriesUpdated(val categories: List<CategoryWithTags>) : MediaViewerIntent
    data class AddTag(val tagId: String) : MediaViewerIntent
    data class RemoveTag(val tagId: String) : MediaViewerIntent
    data object Share : MediaViewerIntent
}

sealed interface MediaViewerEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : MediaViewerEffect
    data class LaunchShare(val sharePackage: MediaSharePackage) : MediaViewerEffect
}

class MediaViewerReducer : Reducer<MediaViewerState, MediaViewerIntent> {
    override fun reduce(state: MediaViewerState, intent: MediaViewerIntent): MediaViewerState =
        when (intent) {
            is MediaViewerIntent.ItemsUpdated -> {
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
            is MediaViewerIntent.PageChanged -> state.copy(
                currentIndex = intent.index.coerceIn(0, (state.items.size - 1).coerceAtLeast(0)),
            )
            MediaViewerIntent.ToggleChrome -> state.copy(chromeVisible = !state.chromeVisible)
            MediaViewerIntent.OpenDetails -> state.copy(showDetails = true)
            MediaViewerIntent.CloseDetails -> state.copy(
                showDetails = false,
                showTagPicker = false,
            )
            MediaViewerIntent.OpenTagPicker -> state.copy(showTagPicker = true)
            MediaViewerIntent.CloseTagPicker -> state.copy(showTagPicker = false)
            is MediaViewerIntent.DetailsTagsUpdated -> state.copy(detailsAssignedTags = intent.tags)
            is MediaViewerIntent.CategoriesUpdated -> state.copy(allCategories = intent.categories)
            is MediaViewerIntent.AddTag -> state
            is MediaViewerIntent.RemoveTag -> state
            MediaViewerIntent.Share -> state
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewerViewModel(
    startMediaId: String,
    source: MediaBrowseSource,
    private val reducer: MediaViewerReducer,
    private val observeBrowseMediaUseCase: ObserveBrowseMediaUseCase,
    private val observeMediaTagsUseCase: ObserveMediaTagsUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val setMediaTagUseCase: SetMediaTagUseCase,
    private val prepareMediaShareUseCase: PrepareMediaShareUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MediaViewerState(startMediaId = startMediaId))
    val state: StateFlow<MediaViewerState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MediaViewerEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<MediaViewerEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            observeBrowseMediaUseCase(source)
                .distinctUntilChanged { old, new ->
                    old.size == new.size && old.zip(new).all { (a, b) -> a.id == b.id }
                }
                .collect { items ->
                    onIntent(MediaViewerIntent.ItemsUpdated(items))
                }
        }
        viewModelScope.launch {
            observeCategoriesUseCase().collect { categories ->
                onIntent(MediaViewerIntent.CategoriesUpdated(categories))
            }
        }
        viewModelScope.launch {
            _state
                .map { it.currentItem?.id }
                .distinctUntilChanged()
                .flatMapLatest { mediaId ->
                    if (mediaId == null) flowOf(emptyList()) else observeMediaTagsUseCase(mediaId)
                }
                .collect { tags ->
                    onIntent(MediaViewerIntent.DetailsTagsUpdated(tags))
                }
        }
    }

    fun onIntent(intent: MediaViewerIntent) {
        when (intent) {
            MediaViewerIntent.Share -> {
                val item = _state.value.currentItem ?: return
                viewModelScope.launch {
                    val sharePackage = prepareMediaShareUseCase(listOf(item))
                    if (sharePackage == null) {
                        _effects.emit(MediaViewerEffect.ShowMessage(R.string.media_share_failed))
                    } else {
                        _effects.emit(MediaViewerEffect.LaunchShare(sharePackage))
                    }
                }
            }
            is MediaViewerIntent.AddTag -> {
                val mediaId = _state.value.currentItem?.id ?: return
                viewModelScope.launch {
                    runCatching { setMediaTagUseCase.add(mediaId, intent.tagId) }
                }
            }
            is MediaViewerIntent.RemoveTag -> {
                val mediaId = _state.value.currentItem?.id ?: return
                viewModelScope.launch {
                    runCatching { setMediaTagUseCase.remove(mediaId, intent.tagId) }
                }
            }
            else -> _state.update { reducer.reduce(it, intent) }
        }
    }
}

package com.balius.galius.feature.home.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balius.galius.R
import com.balius.galius.core.mvi.Reducer
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.feature.media.domain.model.MediaSharePackage
import com.balius.galius.feature.media.domain.usecase.ObserveLibraryUseCase
import com.balius.galius.feature.media.domain.usecase.PrepareMediaShareUseCase
import com.balius.galius.feature.media.domain.usecase.RestoreAndRemoveMediaUseCase
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val isLoading: Boolean = true,
    val items: List<MediaItem> = emptyList(),
    val videosOnly: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    val isRemoving: Boolean = false,
    val detailsItemId: String? = null,
    val detailsAssignedTags: List<Tag> = emptyList(),
    val allCategories: List<CategoryWithTags> = emptyList(),
    val showTagPicker: Boolean = false,
    val gridLayout: HomeGridLayout = HomeGridLayout.Comfortable,
) {
    val visibleItems: List<MediaItem>
        get() = if (videosOnly) items.filter { it.type == MediaType.Video } else items

    val isEmpty: Boolean
        get() = visibleItems.isEmpty()

    val selectedCount: Int
        get() = selectedIds.size

    val detailsItem: MediaItem?
        get() = detailsItemId?.let { id -> items.find { it.id == id } }
}

sealed interface HomeIntent {
    data object Refresh : HomeIntent
    data object ToggleVideosOnly : HomeIntent
    data class SetGridLayout(val layout: HomeGridLayout) : HomeIntent
    data class LibraryUpdated(val items: List<MediaItem>) : HomeIntent
    data class LoadError(val message: String? = null) : HomeIntent
    data class LongPressItem(val id: String) : HomeIntent
    data class ToggleItemSelection(val id: String) : HomeIntent
    data class OpenDetails(val id: String) : HomeIntent
    data object CloseDetails : HomeIntent
    data object ClearSelection : HomeIntent
    data object ShareSelected : HomeIntent
    data object RemoveSelected : HomeIntent
    data object RemoveFinished : HomeIntent
    data class DetailsTagsUpdated(val tags: List<Tag>) : HomeIntent
    data class CategoriesUpdated(val categories: List<CategoryWithTags>) : HomeIntent
    data object OpenTagPicker : HomeIntent
    data object CloseTagPicker : HomeIntent
    data class AddTagToDetails(val tagId: String) : HomeIntent
    data class RemoveTagFromDetails(val tagId: String) : HomeIntent
}

sealed interface HomeEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : HomeEffect
    data class LaunchShare(val sharePackage: MediaSharePackage) : HomeEffect
}

class HomeReducer : Reducer<HomeState, HomeIntent> {
    override fun reduce(state: HomeState, intent: HomeIntent): HomeState = when (intent) {
        HomeIntent.Refresh -> state.copy(isLoading = true)
        HomeIntent.ToggleVideosOnly -> state.copy(videosOnly = !state.videosOnly)
        is HomeIntent.SetGridLayout -> state.copy(gridLayout = intent.layout)
        is HomeIntent.LibraryUpdated -> {
            val visibleIds = intent.items.map { it.id }.toSet()
            val selected = state.selectedIds.intersect(visibleIds)
            val detailsId = state.detailsItemId?.takeIf { it in visibleIds }
            state.copy(
                isLoading = false,
                items = intent.items,
                selectedIds = selected,
                selectionMode = state.selectionMode && selected.isNotEmpty(),
                detailsItemId = detailsId,
                showTagPicker = if (detailsId == null) false else state.showTagPicker,
                detailsAssignedTags = if (detailsId == null) emptyList() else state.detailsAssignedTags,
            )
        }
        is HomeIntent.LoadError -> state.copy(isLoading = false)
        is HomeIntent.LongPressItem -> state.copy(
            selectionMode = true,
            selectedIds = state.selectedIds + intent.id,
            detailsItemId = null,
            showTagPicker = false,
            detailsAssignedTags = emptyList(),
        )
        is HomeIntent.ToggleItemSelection -> {
            if (!state.selectionMode) return state
            val next = if (intent.id in state.selectedIds) {
                state.selectedIds - intent.id
            } else {
                state.selectedIds + intent.id
            }
            state.copy(
                selectedIds = next,
                selectionMode = next.isNotEmpty(),
            )
        }
        is HomeIntent.OpenDetails -> state.copy(
            detailsItemId = intent.id,
            showTagPicker = false,
            detailsAssignedTags = emptyList(),
        )
        HomeIntent.CloseDetails -> state.copy(
            detailsItemId = null,
            showTagPicker = false,
            detailsAssignedTags = emptyList(),
        )
        HomeIntent.ClearSelection -> state.copy(
            selectionMode = false,
            selectedIds = emptySet(),
        )
        HomeIntent.ShareSelected -> state
        HomeIntent.RemoveSelected -> state.copy(isRemoving = true)
        HomeIntent.RemoveFinished -> state.copy(
            isRemoving = false,
            selectionMode = false,
            selectedIds = emptySet(),
        )
        is HomeIntent.DetailsTagsUpdated -> state.copy(detailsAssignedTags = intent.tags)
        is HomeIntent.CategoriesUpdated -> state.copy(allCategories = intent.categories)
        HomeIntent.OpenTagPicker -> state.copy(showTagPicker = true)
        HomeIntent.CloseTagPicker -> state.copy(showTagPicker = false)
        is HomeIntent.AddTagToDetails, is HomeIntent.RemoveTagFromDetails -> state
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val reducer: HomeReducer,
    private val observeLibraryUseCase: ObserveLibraryUseCase,
    private val restoreAndRemoveMediaUseCase: RestoreAndRemoveMediaUseCase,
    private val prepareMediaShareUseCase: PrepareMediaShareUseCase,
    private val observeMediaTagsUseCase: ObserveMediaTagsUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val setMediaTagUseCase: SetMediaTagUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HomeEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            runCatching {
                observeLibraryUseCase().collect { items ->
                    onIntent(HomeIntent.LibraryUpdated(items))
                }
            }.onFailure {
                onIntent(HomeIntent.LoadError(it.message))
            }
        }
        viewModelScope.launch {
            observeCategoriesUseCase().collect { categories ->
                onIntent(HomeIntent.CategoriesUpdated(categories))
            }
        }
        viewModelScope.launch {
            _state
                .map { it.detailsItemId }
                .distinctUntilChanged()
                .flatMapLatest { mediaId -> observeMediaTagsUseCase(mediaId) }
                .collect { tags ->
                    onIntent(HomeIntent.DetailsTagsUpdated(tags))
                }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.ShareSelected -> {
                val current = _state.value
                val selected = current.visibleItems.filter { it.id in current.selectedIds }
                viewModelScope.launch {
                    val sharePackage = prepareMediaShareUseCase(selected)
                    if (sharePackage == null) {
                        _effects.emit(HomeEffect.ShowMessage(R.string.media_share_failed))
                    } else {
                        _effects.emit(HomeEffect.LaunchShare(sharePackage))
                    }
                }
            }
            HomeIntent.RemoveSelected -> {
                val ids = _state.value.selectedIds.toList()
                if (ids.isEmpty()) return
                _state.update { reducer.reduce(it, intent) }
                viewModelScope.launch {
                    val result = runCatching { restoreAndRemoveMediaUseCase(ids) }
                        .getOrElse { null }
                    onIntent(HomeIntent.RemoveFinished)
                    val messageRes = when {
                        result == null -> R.string.error_generic
                        result.restoredCount > 0 && result.failedCount == 0 ->
                            R.string.media_restored_to_gallery
                        result.restoredCount > 0 -> R.string.media_restore_partial
                        else -> R.string.media_restore_failed
                    }
                    _effects.emit(HomeEffect.ShowMessage(messageRes))
                }
            }
            is HomeIntent.AddTagToDetails -> {
                val mediaId = _state.value.detailsItemId ?: return
                viewModelScope.launch {
                    runCatching { setMediaTagUseCase.add(mediaId, intent.tagId) }
                        .onFailure {
                            _effects.emit(HomeEffect.ShowMessage(R.string.error_generic))
                        }
                }
            }
            is HomeIntent.RemoveTagFromDetails -> {
                val mediaId = _state.value.detailsItemId ?: return
                viewModelScope.launch {
                    runCatching { setMediaTagUseCase.remove(mediaId, intent.tagId) }
                        .onFailure {
                            _effects.emit(HomeEffect.ShowMessage(R.string.error_generic))
                        }
                }
            }
            else -> _state.update { reducer.reduce(it, intent) }
        }
    }
}

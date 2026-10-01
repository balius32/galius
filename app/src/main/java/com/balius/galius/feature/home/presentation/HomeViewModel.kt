package com.balius.galius.feature.home.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balius.galius.R
import com.balius.galius.core.mvi.Reducer
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.model.MediaType
import com.balius.galius.feature.media.domain.usecase.ObserveLibraryUseCase
import com.balius.galius.feature.media.domain.usecase.RestoreAndRemoveMediaUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val isLoading: Boolean = true,
    val items: List<MediaItem> = emptyList(),
    val videosOnly: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    val isRemoving: Boolean = false,
) {
    val visibleItems: List<MediaItem>
        get() = if (videosOnly) items.filter { it.type == MediaType.Video } else items

    val isEmpty: Boolean
        get() = visibleItems.isEmpty()

    val selectedCount: Int
        get() = selectedIds.size
}

sealed interface HomeIntent {
    data object Refresh : HomeIntent
    data object ToggleVideosOnly : HomeIntent
    data class LibraryUpdated(val items: List<MediaItem>) : HomeIntent
    data class LoadError(val message: String? = null) : HomeIntent
    data class LongPressItem(val id: String) : HomeIntent
    data class ToggleItemSelection(val id: String) : HomeIntent
    data object ClearSelection : HomeIntent
    data object RemoveSelected : HomeIntent
    data object RemoveFinished : HomeIntent
}

sealed interface HomeEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : HomeEffect
}

class HomeReducer : Reducer<HomeState, HomeIntent> {
    override fun reduce(state: HomeState, intent: HomeIntent): HomeState = when (intent) {
        HomeIntent.Refresh -> state.copy(isLoading = true)
        HomeIntent.ToggleVideosOnly -> state.copy(videosOnly = !state.videosOnly)
        is HomeIntent.LibraryUpdated -> {
            val visibleIds = intent.items.map { it.id }.toSet()
            val selected = state.selectedIds.intersect(visibleIds)
            state.copy(
                isLoading = false,
                items = intent.items,
                selectedIds = selected,
                selectionMode = state.selectionMode && selected.isNotEmpty(),
            )
        }
        is HomeIntent.LoadError -> state.copy(isLoading = false)
        is HomeIntent.LongPressItem -> state.copy(
            selectionMode = true,
            selectedIds = state.selectedIds + intent.id,
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
        HomeIntent.ClearSelection -> state.copy(
            selectionMode = false,
            selectedIds = emptySet(),
        )
        HomeIntent.RemoveSelected -> state.copy(isRemoving = true)
        HomeIntent.RemoveFinished -> state.copy(
            isRemoving = false,
            selectionMode = false,
            selectedIds = emptySet(),
        )
    }
}

class HomeViewModel(
    private val reducer: HomeReducer,
    private val observeLibraryUseCase: ObserveLibraryUseCase,
    private val restoreAndRemoveMediaUseCase: RestoreAndRemoveMediaUseCase,
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
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
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
            else -> _state.update { reducer.reduce(it, intent) }
        }
    }
}

package com.balius.galius.feature.search.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balius.galius.R
import com.balius.galius.core.mvi.Reducer
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.usecase.ObserveLibraryUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveMediaByCategoryUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveMediaByTagUseCase
import com.balius.galius.feature.tags.domain.model.CategoryWithTags
import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.usecase.AddMediaToCategoryUseCase
import com.balius.galius.feature.tags.domain.usecase.ObserveCategoriesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchState(
    val categories: List<CategoryWithTags> = emptyList(),
    val selectedCategoryId: String? = null,
    val selectedTagId: String? = null,
    val results: List<MediaItem> = emptyList(),
    val addMediaPickerVisible: Boolean = false,
    val libraryItems: List<MediaItem> = emptyList(),
    val pickerSelectedIds: Set<String> = emptySet(),
    val isAddingToCategory: Boolean = false,
) {
    val selectedCategory: CategoryWithTags?
        get() = categories.find { it.category.id == selectedCategoryId }

    val selectedTags: List<Tag>
        get() = selectedCategory?.tags.orEmpty()

    val pickerSelectedCount: Int
        get() = pickerSelectedIds.size
}

sealed interface SearchIntent {
    data class CategoriesUpdated(val categories: List<CategoryWithTags>) : SearchIntent
    data class CategorySelected(val categoryId: String) : SearchIntent
    data class TagSelected(val tagId: String) : SearchIntent
    data class ResultsUpdated(val results: List<MediaItem>) : SearchIntent
    data object OpenAddMediaPicker : SearchIntent
    data object DismissAddMediaPicker : SearchIntent
    data class LibraryUpdated(val items: List<MediaItem>) : SearchIntent
    data class TogglePickerSelection(val mediaId: String) : SearchIntent
    data object ConfirmAddToCategory : SearchIntent
    data class AddingToCategoryChanged(val isAdding: Boolean) : SearchIntent
}

sealed interface SearchEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : SearchEffect
}

class SearchReducer : Reducer<SearchState, SearchIntent> {
    override fun reduce(state: SearchState, intent: SearchIntent): SearchState = when (intent) {
        is SearchIntent.CategoriesUpdated -> {
            val selectedStillExists = state.selectedCategoryId?.let { id ->
                intent.categories.any { it.category.id == id }
            } == true
            state.copy(
                categories = intent.categories,
                selectedCategoryId = if (selectedStillExists) state.selectedCategoryId else null,
                selectedTagId = if (selectedStillExists) state.selectedTagId else null,
            )
        }
        is SearchIntent.CategorySelected -> state.copy(
            selectedCategoryId = intent.categoryId,
            selectedTagId = null,
            addMediaPickerVisible = false,
            pickerSelectedIds = emptySet(),
        )
        is SearchIntent.TagSelected -> state.copy(
            selectedTagId = if (state.selectedTagId == intent.tagId) null else intent.tagId,
        )
        is SearchIntent.ResultsUpdated -> state.copy(results = intent.results)
        SearchIntent.OpenAddMediaPicker -> state.copy(
            addMediaPickerVisible = true,
            pickerSelectedIds = emptySet(),
        )
        SearchIntent.DismissAddMediaPicker -> state.copy(
            addMediaPickerVisible = false,
            pickerSelectedIds = emptySet(),
        )
        is SearchIntent.LibraryUpdated -> state.copy(libraryItems = intent.items)
        is SearchIntent.TogglePickerSelection -> {
            val next = state.pickerSelectedIds.toMutableSet()
            if (intent.mediaId in next) {
                next.remove(intent.mediaId)
            } else {
                next.add(intent.mediaId)
            }
            state.copy(pickerSelectedIds = next)
        }
        SearchIntent.ConfirmAddToCategory -> state
        is SearchIntent.AddingToCategoryChanged -> state.copy(isAddingToCategory = intent.isAdding)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val reducer: SearchReducer,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val observeMediaByCategoryUseCase: ObserveMediaByCategoryUseCase,
    private val observeMediaByTagUseCase: ObserveMediaByTagUseCase,
    private val observeLibraryUseCase: ObserveLibraryUseCase,
    private val addMediaToCategoryUseCase: AddMediaToCategoryUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SearchEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<SearchEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCategoriesUseCase().collect { categories ->
                onIntent(SearchIntent.CategoriesUpdated(categories))
            }
        }
        viewModelScope.launch {
            combine(
                _state.map { it.selectedCategoryId }.distinctUntilChanged(),
                _state.map { it.selectedTagId }.distinctUntilChanged(),
            ) { categoryId, tagId -> categoryId to tagId }
                .flatMapLatest { (categoryId, tagId) ->
                    when {
                        tagId != null -> observeMediaByTagUseCase(tagId)
                        categoryId != null -> observeMediaByCategoryUseCase(categoryId)
                        else -> flowOf(emptyList())
                    }
                }
                .collect { results ->
                    onIntent(SearchIntent.ResultsUpdated(results))
                }
        }
        viewModelScope.launch {
            _state.map { it.addMediaPickerVisible }
                .distinctUntilChanged()
                .flatMapLatest { visible ->
                    if (visible) {
                        observeLibraryUseCase()
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { items ->
                    onIntent(SearchIntent.LibraryUpdated(items))
                }
        }
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            SearchIntent.ConfirmAddToCategory -> confirmAddToCategory()
            else -> _state.update { reducer.reduce(it, intent) }
        }
    }

    private fun confirmAddToCategory() {
        val categoryId = _state.value.selectedCategoryId ?: return
        val mediaIds = _state.value.pickerSelectedIds
        if (mediaIds.isEmpty()) return
        viewModelScope.launch {
            onIntent(SearchIntent.AddingToCategoryChanged(true))
            val result = runCatching {
                addMediaToCategoryUseCase(categoryId, mediaIds)
            }
            onIntent(SearchIntent.AddingToCategoryChanged(false))
            if (result.isSuccess) {
                onIntent(SearchIntent.DismissAddMediaPicker)
            } else {
                _effects.emit(SearchEffect.ShowMessage(R.string.error_generic))
            }
        }
    }
}

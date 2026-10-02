package com.balius.galius.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balius.galius.core.mvi.Reducer
import com.balius.galius.feature.media.domain.model.MediaItem
import com.balius.galius.feature.media.domain.usecase.ObserveMediaByCategoryUseCase
import com.balius.galius.feature.media.domain.usecase.ObserveMediaByTagUseCase
import com.balius.galius.feature.tags.domain.model.CategoryWithTags
import com.balius.galius.feature.tags.domain.model.Tag
import com.balius.galius.feature.tags.domain.usecase.ObserveCategoriesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
) {
    val selectedCategory: CategoryWithTags?
        get() = categories.find { it.category.id == selectedCategoryId }

    val selectedTags: List<Tag>
        get() = selectedCategory?.tags.orEmpty()
}

sealed interface SearchIntent {
    data class CategoriesUpdated(val categories: List<CategoryWithTags>) : SearchIntent
    data class CategorySelected(val categoryId: String) : SearchIntent
    data class TagSelected(val tagId: String) : SearchIntent
    data class ResultsUpdated(val results: List<MediaItem>) : SearchIntent
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
        )
        is SearchIntent.TagSelected -> state.copy(
            selectedTagId = if (state.selectedTagId == intent.tagId) null else intent.tagId,
        )
        is SearchIntent.ResultsUpdated -> state.copy(results = intent.results)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val reducer: SearchReducer,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val observeMediaByCategoryUseCase: ObserveMediaByCategoryUseCase,
    private val observeMediaByTagUseCase: ObserveMediaByTagUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

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
    }

    fun onIntent(intent: SearchIntent) {
        _state.update { reducer.reduce(it, intent) }
    }
}

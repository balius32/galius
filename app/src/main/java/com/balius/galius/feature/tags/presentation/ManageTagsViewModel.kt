package com.balius.galius.feature.tags.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balius.galius.R
import com.balius.galius.core.mvi.Reducer
import com.balius.galius.feature.tags.domain.model.CategoryWithTags
import com.balius.galius.feature.tags.domain.model.TagColorKey
import com.balius.galius.feature.tags.domain.usecase.CreateCategoryUseCase
import com.balius.galius.feature.tags.domain.usecase.CreateTagUseCase
import com.balius.galius.feature.tags.domain.usecase.DeleteTagUseCase
import com.balius.galius.feature.tags.domain.usecase.ObserveCategoriesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageTagsState(
    val categories: List<CategoryWithTags> = emptyList(),
    val createExpanded: Boolean = true,
    val categoryName: String = "",
    val categoryColor: TagColorKey = TagColorKey.Indigo,
    val addTagCategoryId: String? = null,
    val tagDraftName: String = "",
    val isSaving: Boolean = false,
)

sealed interface ManageTagsIntent {
    data class CategoriesUpdated(val categories: List<CategoryWithTags>) : ManageTagsIntent
    data object ToggleCreateExpanded : ManageTagsIntent
    data class CategoryNameChanged(val name: String) : ManageTagsIntent
    data class CategoryColorChanged(val colorKey: TagColorKey) : ManageTagsIntent
    data object SaveCategory : ManageTagsIntent
    data class OpenAddTag(val categoryId: String) : ManageTagsIntent
    data object DismissAddTag : ManageTagsIntent
    data class TagDraftChanged(val name: String) : ManageTagsIntent
    data object SaveTag : ManageTagsIntent
    data class DeleteTag(val tagId: String) : ManageTagsIntent
    data object SaveStarted : ManageTagsIntent
    data object SaveFinished : ManageTagsIntent
}

sealed interface ManageTagsEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : ManageTagsEffect
}

class ManageTagsReducer : Reducer<ManageTagsState, ManageTagsIntent> {
    override fun reduce(state: ManageTagsState, intent: ManageTagsIntent): ManageTagsState =
        when (intent) {
            is ManageTagsIntent.CategoriesUpdated -> state.copy(categories = intent.categories)
            ManageTagsIntent.ToggleCreateExpanded -> state.copy(
                createExpanded = !state.createExpanded,
            )
            is ManageTagsIntent.CategoryNameChanged -> state.copy(categoryName = intent.name)
            is ManageTagsIntent.CategoryColorChanged -> state.copy(categoryColor = intent.colorKey)
            ManageTagsIntent.SaveCategory -> state
            is ManageTagsIntent.OpenAddTag -> state.copy(
                addTagCategoryId = intent.categoryId,
                tagDraftName = "",
            )
            ManageTagsIntent.DismissAddTag -> state.copy(
                addTagCategoryId = null,
                tagDraftName = "",
            )
            is ManageTagsIntent.TagDraftChanged -> state.copy(tagDraftName = intent.name)
            ManageTagsIntent.SaveTag -> state
            is ManageTagsIntent.DeleteTag -> state
            ManageTagsIntent.SaveStarted -> state.copy(isSaving = true)
            ManageTagsIntent.SaveFinished -> state.copy(isSaving = false)
        }
}

class ManageTagsViewModel(
    private val reducer: ManageTagsReducer,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(ManageTagsState())
    val state: StateFlow<ManageTagsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ManageTagsEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<ManageTagsEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCategoriesUseCase().collect { categories ->
                onIntent(ManageTagsIntent.CategoriesUpdated(categories))
            }
        }
    }

    fun onIntent(intent: ManageTagsIntent) {
        when (intent) {
            ManageTagsIntent.SaveCategory -> saveCategory()
            ManageTagsIntent.SaveTag -> saveTag()
            is ManageTagsIntent.DeleteTag -> deleteTag(intent.tagId)
            else -> _state.update { reducer.reduce(it, intent) }
        }
    }

    private fun saveCategory() {
        val name = _state.value.categoryName.trim()
        if (name.isEmpty()) {
            viewModelScope.launch {
                _effects.emit(ManageTagsEffect.ShowMessage(R.string.error_category_name_required))
            }
            return
        }
        val color = _state.value.categoryColor
        _state.update { reducer.reduce(it, ManageTagsIntent.SaveStarted) }
        viewModelScope.launch {
            runCatching { createCategoryUseCase(name, color) }
                .onSuccess {
                    _state.update {
                        reducer.reduce(it, ManageTagsIntent.SaveFinished).copy(
                            categoryName = "",
                            createExpanded = false,
                        )
                    }
                }
                .onFailure {
                    _state.update { reducer.reduce(it, ManageTagsIntent.SaveFinished) }
                    _effects.emit(ManageTagsEffect.ShowMessage(R.string.error_generic))
                }
        }
    }

    private fun saveTag() {
        val current = _state.value
        val categoryId = current.addTagCategoryId ?: return
        val name = current.tagDraftName.trim()
        if (name.isEmpty()) {
            viewModelScope.launch {
                _effects.emit(ManageTagsEffect.ShowMessage(R.string.error_tag_name_required))
            }
            return
        }
        val categoryColor = current.categories
            .find { it.category.id == categoryId }
            ?.category
            ?.colorKey
            ?: TagColorKey.Indigo
        _state.update { reducer.reduce(it, ManageTagsIntent.SaveStarted) }
        viewModelScope.launch {
            runCatching { createTagUseCase(categoryId, name, categoryColor) }
                .onSuccess {
                    _state.update {
                        reducer.reduce(it, ManageTagsIntent.SaveFinished).copy(
                            addTagCategoryId = null,
                            tagDraftName = "",
                        )
                    }
                }
                .onFailure {
                    _state.update { reducer.reduce(it, ManageTagsIntent.SaveFinished) }
                    _effects.emit(ManageTagsEffect.ShowMessage(R.string.error_generic))
                }
        }
    }

    private fun deleteTag(tagId: String) {
        viewModelScope.launch {
            runCatching { deleteTagUseCase(tagId) }
                .onFailure {
                    _effects.emit(ManageTagsEffect.ShowMessage(R.string.error_generic))
                }
        }
    }
}

package com.balius.galius.feature.search.presentation

import androidx.lifecycle.ViewModel
import com.balius.galius.core.mvi.Reducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SearchState(
    val query: String = "",
)

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object ClearQuery : SearchIntent
}

class SearchReducer : Reducer<SearchState, SearchIntent> {
    override fun reduce(state: SearchState, intent: SearchIntent): SearchState = when (intent) {
        is SearchIntent.QueryChanged -> state.copy(query = intent.query)
        SearchIntent.ClearQuery -> state.copy(query = "")
    }
}

class SearchViewModel(
    private val reducer: SearchReducer,
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    fun onIntent(intent: SearchIntent) {
        _state.update { reducer.reduce(it, intent) }
    }
}

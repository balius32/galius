package com.balius.galius.feature.home.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeState(
    val isEmpty: Boolean = true,
)

sealed interface HomeIntent {
    data object Refresh : HomeIntent
}

class HomeReducer : com.balius.galius.core.mvi.Reducer<HomeState, HomeIntent> {
    override fun reduce(state: HomeState, intent: HomeIntent): HomeState = when (intent) {
        HomeIntent.Refresh -> state
    }
}

class HomeViewModel(
    private val reducer: HomeReducer,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun onIntent(intent: HomeIntent) {
        _state.update { reducer.reduce(it, intent) }
    }
}

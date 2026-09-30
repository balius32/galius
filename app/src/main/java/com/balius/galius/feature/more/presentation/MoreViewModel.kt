package com.balius.galius.feature.more.presentation

import androidx.lifecycle.ViewModel
import com.balius.galius.core.mvi.Reducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MoreState(
    val appLockEnabled: Boolean = false,
)

sealed interface MoreIntent {
    data class ToggleAppLock(val enabled: Boolean) : MoreIntent
}

class MoreReducer : Reducer<MoreState, MoreIntent> {
    override fun reduce(state: MoreState, intent: MoreIntent): MoreState = when (intent) {
        is MoreIntent.ToggleAppLock -> state.copy(appLockEnabled = intent.enabled)
    }
}

class MoreViewModel(
    private val reducer: MoreReducer,
) : ViewModel() {
    private val _state = MutableStateFlow(MoreState())
    val state: StateFlow<MoreState> = _state.asStateFlow()

    fun onIntent(intent: MoreIntent) {
        _state.update { reducer.reduce(it, intent) }
    }
}

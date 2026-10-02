package com.balius.galius.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balius.galius.common.model.AccentOption
import com.balius.galius.common.model.ThemeMode
import com.balius.galius.core.mvi.Reducer
import com.balius.galius.feature.settings.domain.model.LibraryStorageStats
import com.balius.galius.feature.settings.domain.model.SettingsPreferences
import com.balius.galius.feature.settings.domain.usecase.ObserveLibraryStorageUseCase
import com.balius.galius.feature.settings.domain.usecase.ObserveSettingsPreferencesUseCase
import com.balius.galius.feature.settings.domain.usecase.SetAccentUseCase
import com.balius.galius.feature.settings.domain.usecase.SetAppLockEnabledUseCase
import com.balius.galius.feature.settings.domain.usecase.SetThemeModeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.Dark,
    val accent: AccentOption = AccentOption.ElectricIndigo,
    val appLockEnabled: Boolean = false,
    val storage: LibraryStorageStats = LibraryStorageStats(),
)

sealed interface SettingsIntent {
    data class PreferencesUpdated(val preferences: SettingsPreferences) : SettingsIntent
    data class StorageUpdated(val storage: LibraryStorageStats) : SettingsIntent
    data class ThemeModeSelected(val mode: ThemeMode) : SettingsIntent
    data class AccentSelected(val accent: AccentOption) : SettingsIntent
    data class ToggleAppLock(val enabled: Boolean) : SettingsIntent
}

class SettingsReducer : Reducer<SettingsState, SettingsIntent> {
    override fun reduce(state: SettingsState, intent: SettingsIntent): SettingsState = when (intent) {
        is SettingsIntent.PreferencesUpdated -> state.copy(
            themeMode = intent.preferences.themeMode,
            accent = intent.preferences.accent,
            appLockEnabled = intent.preferences.appLockEnabled,
        )
        is SettingsIntent.StorageUpdated -> state.copy(storage = intent.storage)
        is SettingsIntent.ThemeModeSelected -> state.copy(themeMode = intent.mode)
        is SettingsIntent.AccentSelected -> state.copy(accent = intent.accent)
        is SettingsIntent.ToggleAppLock -> state.copy(appLockEnabled = intent.enabled)
    }
}

class SettingsViewModel(
    private val reducer: SettingsReducer,
    observeSettingsPreferencesUseCase: ObserveSettingsPreferencesUseCase,
    observeLibraryStorageUseCase: ObserveLibraryStorageUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setAccentUseCase: SetAccentUseCase,
    private val setAppLockEnabledUseCase: SetAppLockEnabledUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeSettingsPreferencesUseCase().collect { prefs ->
                onIntent(SettingsIntent.PreferencesUpdated(prefs))
            }
        }
        viewModelScope.launch {
            observeLibraryStorageUseCase().collect { stats ->
                onIntent(SettingsIntent.StorageUpdated(stats))
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeModeSelected -> viewModelScope.launch {
                setThemeModeUseCase(intent.mode)
            }
            is SettingsIntent.AccentSelected -> viewModelScope.launch {
                setAccentUseCase(intent.accent)
            }
            is SettingsIntent.ToggleAppLock -> viewModelScope.launch {
                setAppLockEnabledUseCase(intent.enabled)
            }
            else -> Unit
        }
        _state.update { reducer.reduce(it, intent) }
    }
}

package com.balius.galius.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balius.galius.common.model.AccentOption
import com.balius.galius.common.model.ThemeMode
import com.balius.galius.core.mvi.Reducer
import com.balius.galius.feature.settings.domain.AppPinRules
import com.balius.galius.feature.settings.domain.model.LibraryStorageStats
import com.balius.galius.feature.settings.domain.model.SettingsPreferences
import com.balius.galius.feature.settings.domain.usecase.ClearAppLockUseCase
import com.balius.galius.feature.settings.domain.usecase.ObserveLibraryStorageUseCase
import com.balius.galius.feature.settings.domain.usecase.ObserveSettingsPreferencesUseCase
import com.balius.galius.feature.settings.domain.usecase.SetAccentUseCase
import com.balius.galius.feature.settings.domain.usecase.SetAppPinUseCase
import com.balius.galius.feature.settings.domain.usecase.SetBiometricUnlockUseCase
import com.balius.galius.feature.settings.domain.usecase.SetThemeModeUseCase
import com.balius.galius.feature.settings.domain.usecase.VerifyAppPinUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppPinMode {
    Enable,
    Disable,
    ChangeCurrent,
    ChangeNew,
}

enum class AppPinError {
    TooShort,
    Mismatch,
    Wrong,
}

data class AppPinPrompt(
    val mode: AppPinMode,
    val draft: String = "",
    val firstPin: String? = null,
    val error: AppPinError? = null,
)

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.Dark,
    val accent: AccentOption = AccentOption.ElectricIndigo,
    val appLockEnabled: Boolean = false,
    val biometricUnlockEnabled: Boolean = false,
    val storage: LibraryStorageStats = LibraryStorageStats(),
    val pinPrompt: AppPinPrompt? = null,
    val promptBiometricAfterPin: Boolean = false,
)

sealed interface SettingsIntent {
    data class PreferencesUpdated(val preferences: SettingsPreferences) : SettingsIntent
    data class StorageUpdated(val storage: LibraryStorageStats) : SettingsIntent
    data class ThemeModeSelected(val mode: ThemeMode) : SettingsIntent
    data class AccentSelected(val accent: AccentOption) : SettingsIntent
    data object BeginEnablePin : SettingsIntent
    data object BeginDisablePin : SettingsIntent
    data object BeginChangePin : SettingsIntent
    data class PinDigit(val digit: Int) : SettingsIntent
    data object PinDelete : SettingsIntent
    data object PinSubmit : SettingsIntent
    data object PinDismiss : SettingsIntent
    data class SetBiometricUnlock(val enabled: Boolean) : SettingsIntent
    data object BiometricPromptHandled : SettingsIntent
}

class SettingsReducer : Reducer<SettingsState, SettingsIntent> {
    override fun reduce(state: SettingsState, intent: SettingsIntent): SettingsState = when (intent) {
        is SettingsIntent.PreferencesUpdated -> state.copy(
            themeMode = intent.preferences.themeMode,
            accent = intent.preferences.accent,
            appLockEnabled = intent.preferences.appLockEnabled,
            biometricUnlockEnabled = intent.preferences.biometricUnlockEnabled,
        )
        is SettingsIntent.StorageUpdated -> state.copy(storage = intent.storage)
        is SettingsIntent.ThemeModeSelected -> state.copy(themeMode = intent.mode)
        is SettingsIntent.AccentSelected -> state.copy(accent = intent.accent)
        SettingsIntent.BeginEnablePin -> state.copy(
            pinPrompt = AppPinPrompt(mode = AppPinMode.Enable),
        )
        SettingsIntent.BeginDisablePin -> state.copy(
            pinPrompt = AppPinPrompt(mode = AppPinMode.Disable),
        )
        SettingsIntent.BeginChangePin -> state.copy(
            pinPrompt = AppPinPrompt(mode = AppPinMode.ChangeCurrent),
        )
        is SettingsIntent.PinDigit -> {
            val prompt = state.pinPrompt ?: return state
            if (prompt.draft.length >= AppPinRules.MAX_LENGTH) state
            else state.copy(
                pinPrompt = prompt.copy(
                    draft = prompt.draft + intent.digit,
                    error = null,
                ),
            )
        }
        SettingsIntent.PinDelete -> {
            val prompt = state.pinPrompt ?: return state
            state.copy(
                pinPrompt = prompt.copy(
                    draft = prompt.draft.dropLast(1),
                    error = null,
                ),
            )
        }
        SettingsIntent.PinDismiss -> state.copy(pinPrompt = null)
        SettingsIntent.BiometricPromptHandled -> state.copy(promptBiometricAfterPin = false)
        SettingsIntent.PinSubmit,
        is SettingsIntent.SetBiometricUnlock,
        -> state
    }
}

class SettingsViewModel(
    private val reducer: SettingsReducer,
    observeSettingsPreferencesUseCase: ObserveSettingsPreferencesUseCase,
    observeLibraryStorageUseCase: ObserveLibraryStorageUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setAccentUseCase: SetAccentUseCase,
    private val setAppPinUseCase: SetAppPinUseCase,
    private val verifyAppPinUseCase: VerifyAppPinUseCase,
    private val clearAppLockUseCase: ClearAppLockUseCase,
    private val setBiometricUnlockUseCase: SetBiometricUnlockUseCase,
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
            SettingsIntent.PinSubmit -> submitPin()
            is SettingsIntent.SetBiometricUnlock -> viewModelScope.launch {
                setBiometricUnlockUseCase(intent.enabled)
            }
            else -> Unit
        }
        if (intent !is SettingsIntent.PinSubmit) {
            _state.update { reducer.reduce(it, intent) }
        }
    }

    private fun submitPin() {
        val prompt = _state.value.pinPrompt ?: return
        val draft = prompt.draft
        when (prompt.mode) {
            AppPinMode.Enable,
            AppPinMode.ChangeNew,
            -> {
                if (prompt.firstPin == null) {
                    if (!AppPinRules.isValid(draft)) {
                        _state.update { it.copy(pinPrompt = prompt.copy(error = AppPinError.TooShort)) }
                    } else {
                        _state.update {
                            it.copy(
                                pinPrompt = prompt.copy(firstPin = draft, draft = "", error = null),
                            )
                        }
                    }
                } else if (draft != prompt.firstPin) {
                    _state.update {
                        it.copy(pinPrompt = prompt.copy(draft = "", error = AppPinError.Mismatch))
                    }
                } else {
                    viewModelScope.launch {
                        setAppPinUseCase(draft)
                        _state.update {
                            it.copy(
                                pinPrompt = null,
                                promptBiometricAfterPin = prompt.mode == AppPinMode.Enable,
                            )
                        }
                    }
                }
            }
            AppPinMode.Disable -> viewModelScope.launch {
                if (verifyAppPinUseCase(draft)) {
                    clearAppLockUseCase()
                    _state.update { it.copy(pinPrompt = null) }
                } else {
                    _state.update {
                        it.copy(pinPrompt = prompt.copy(draft = "", error = AppPinError.Wrong))
                    }
                }
            }
            AppPinMode.ChangeCurrent -> viewModelScope.launch {
                if (verifyAppPinUseCase(draft)) {
                    _state.update { it.copy(pinPrompt = AppPinPrompt(mode = AppPinMode.ChangeNew)) }
                } else {
                    _state.update {
                        it.copy(pinPrompt = prompt.copy(draft = "", error = AppPinError.Wrong))
                    }
                }
            }
        }
    }
}

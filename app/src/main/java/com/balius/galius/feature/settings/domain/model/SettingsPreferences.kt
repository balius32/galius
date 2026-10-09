package com.balius.galius.feature.settings.domain.model

import com.balius.galius.common.model.AccentOption
import com.balius.galius.common.model.ThemeMode

data class SettingsPreferences(
    val themeMode: ThemeMode = ThemeMode.Dark,
    val accent: AccentOption = AccentOption.ElectricIndigo,
    val appLockEnabled: Boolean = false,
    val biometricUnlockEnabled: Boolean = false,
)

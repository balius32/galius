package com.balius.galius.feature.settings.domain.repository

import com.balius.galius.common.model.AccentOption
import com.balius.galius.common.model.ThemeMode
import com.balius.galius.feature.settings.domain.model.SettingsPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observePreferences(): Flow<SettingsPreferences>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setAccent(accent: AccentOption)

    suspend fun setAppLockEnabled(enabled: Boolean)
}

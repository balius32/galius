package com.balius.galius.feature.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.balius.galius.common.model.AccentOption
import com.balius.galius.common.model.ThemeMode
import com.balius.galius.feature.settings.domain.model.SettingsPreferences
import com.balius.galius.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "galius_settings",
)

class SettingsRepositoryImpl(
    context: Context,
) : SettingsRepository {
    private val dataStore = context.settingsDataStore

    override fun observePreferences(): Flow<SettingsPreferences> =
        dataStore.data.map { prefs ->
            SettingsPreferences(
                themeMode = prefs[KEY_THEME_MODE]
                    ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.Dark,
                accent = prefs[KEY_ACCENT]
                    ?.let { runCatching { AccentOption.valueOf(it) }.getOrNull() }
                    ?: AccentOption.ElectricIndigo,
                appLockEnabled = prefs[KEY_APP_LOCK] ?: false,
            )
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    override suspend fun setAccent(accent: AccentOption) {
        dataStore.edit { it[KEY_ACCENT] = accent.name }
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_APP_LOCK] = enabled }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_ACCENT = stringPreferencesKey("accent")
        val KEY_APP_LOCK = booleanPreferencesKey("app_lock_enabled")
    }
}

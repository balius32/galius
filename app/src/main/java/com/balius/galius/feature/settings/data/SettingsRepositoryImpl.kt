package com.balius.galius.feature.settings.data

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.balius.galius.common.model.AccentOption
import com.balius.galius.common.model.ThemeMode
import com.balius.galius.feature.settings.domain.AppPinRules
import com.balius.galius.feature.settings.domain.model.SettingsPreferences
import com.balius.galius.feature.settings.domain.repository.SettingsRepository
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "galius_settings",
)

class SettingsRepositoryImpl(
    context: Context,
) : SettingsRepository {
    private val dataStore = context.settingsDataStore

    override fun observePreferences(): Flow<SettingsPreferences> =
        dataStore.data.map { prefs ->
            val hasPin = !prefs[KEY_PIN_HASH].isNullOrBlank()
            val lockEnabled = (prefs[KEY_APP_LOCK] ?: false) && hasPin
            SettingsPreferences(
                themeMode = prefs[KEY_THEME_MODE]
                    ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.Dark,
                accent = prefs[KEY_ACCENT]
                    ?.let { runCatching { AccentOption.valueOf(it) }.getOrNull() }
                    ?: AccentOption.ElectricIndigo,
                appLockEnabled = lockEnabled,
                biometricUnlockEnabled = (prefs[KEY_BIOMETRIC_UNLOCK] ?: false) && lockEnabled,
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

    override suspend fun setAppPin(pin: String) {
        check(AppPinRules.isValid(pin))
        val salt = ByteArray(SALT_BYTES).also { secureRandom.nextBytes(it) }
        val hash = withContext(Dispatchers.Default) { hashPin(pin, salt) }
        dataStore.edit { prefs ->
            prefs[KEY_PIN_SALT] = Base64.encodeToString(salt, Base64.NO_WRAP)
            prefs[KEY_PIN_HASH] = hash
            prefs[KEY_APP_LOCK] = true
        }
    }

    override suspend fun verifyAppPin(pin: String): Boolean = withContext(Dispatchers.Default) {
        val prefs = dataStore.data.first()
        val salt = prefs[KEY_PIN_SALT]?.let { Base64.decode(it, Base64.NO_WRAP) }
            ?: return@withContext false
        val expected = prefs[KEY_PIN_HASH]?.let { Base64.decode(it, Base64.NO_WRAP) }
            ?: return@withContext false
        val actual = Base64.decode(hashPin(pin, salt), Base64.NO_WRAP)
        MessageDigest.isEqual(expected, actual)
    }

    override suspend fun clearAppLock() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_PIN_SALT)
            prefs.remove(KEY_PIN_HASH)
            prefs[KEY_APP_LOCK] = false
            prefs[KEY_BIOMETRIC_UNLOCK] = false
        }
    }

    override suspend fun setBiometricUnlockEnabled(enabled: Boolean) {
        val hasPin = !dataStore.data.first()[KEY_PIN_HASH].isNullOrBlank()
        if (enabled && !hasPin) return
        dataStore.edit { it[KEY_BIOMETRIC_UNLOCK] = enabled }
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PIN_ITERATIONS, PIN_KEY_BITS)
        val hash = SecretKeyFactory.getInstance(PIN_ALGORITHM).generateSecret(spec).encoded
        spec.clearPassword()
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private companion object {
        const val SALT_BYTES = 16
        const val PIN_ITERATIONS = 120_000
        const val PIN_KEY_BITS = 256
        const val PIN_ALGORITHM = "PBKDF2WithHmacSHA256"

        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_ACCENT = stringPreferencesKey("accent")
        val KEY_APP_LOCK = booleanPreferencesKey("app_lock_enabled")
        val KEY_BIOMETRIC_UNLOCK = booleanPreferencesKey("biometric_unlock_enabled")
        val KEY_PIN_SALT = stringPreferencesKey("app_pin_salt")
        val KEY_PIN_HASH = stringPreferencesKey("app_pin_hash")

        val secureRandom = SecureRandom()
    }
}

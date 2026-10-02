package com.balius.galius.feature.settings.domain.usecase

import com.balius.galius.feature.settings.domain.model.SettingsPreferences
import com.balius.galius.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveSettingsPreferencesUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<SettingsPreferences> = settingsRepository.observePreferences()
}

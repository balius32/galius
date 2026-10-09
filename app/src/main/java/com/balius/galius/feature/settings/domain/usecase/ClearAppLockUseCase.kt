package com.balius.galius.feature.settings.domain.usecase

import com.balius.galius.feature.settings.domain.repository.SettingsRepository

class ClearAppLockUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke() {
        settingsRepository.clearAppLock()
    }
}

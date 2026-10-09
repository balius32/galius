package com.balius.galius.feature.settings.domain.usecase

import com.balius.galius.feature.settings.domain.repository.SettingsRepository

class SetBiometricUnlockUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.setBiometricUnlockEnabled(enabled)
    }
}

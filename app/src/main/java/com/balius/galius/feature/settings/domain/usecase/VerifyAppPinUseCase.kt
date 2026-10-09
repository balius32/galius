package com.balius.galius.feature.settings.domain.usecase

import com.balius.galius.feature.settings.domain.repository.SettingsRepository

class VerifyAppPinUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(pin: String): Boolean = settingsRepository.verifyAppPin(pin)
}

package com.balius.galius.feature.settings.domain.usecase

import com.balius.galius.feature.settings.domain.AppPinRules
import com.balius.galius.feature.settings.domain.repository.SettingsRepository

class SetAppPinUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(pin: String) {
        check(AppPinRules.isValid(pin))
        settingsRepository.setAppPin(pin)
    }
}

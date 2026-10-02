package com.balius.galius.feature.settings.domain.usecase

import com.balius.galius.common.model.AccentOption
import com.balius.galius.feature.settings.domain.repository.SettingsRepository

class SetAccentUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(accent: AccentOption) {
        settingsRepository.setAccent(accent)
    }
}

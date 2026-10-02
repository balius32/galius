package com.balius.galius.feature.settings.domain.usecase

import com.balius.galius.common.model.ThemeMode
import com.balius.galius.feature.settings.domain.repository.SettingsRepository

class SetThemeModeUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode)
    }
}

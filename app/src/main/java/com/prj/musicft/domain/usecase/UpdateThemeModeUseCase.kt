package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.model.ThemeMode
import com.prj.musicft.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateThemeModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(themeMode: ThemeMode) {
        settingsRepository.updateThemeMode(themeMode)
    }
}

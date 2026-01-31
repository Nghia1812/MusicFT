package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.model.AppSettings
import com.prj.musicft.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> {
        return settingsRepository.settings
    }
}

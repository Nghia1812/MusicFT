package com.prj.musicft.data.repository

import com.prj.musicft.data.local.dao.SettingsDao
import com.prj.musicft.data.mapper.toDomain
import com.prj.musicft.domain.model.AppSettings
import com.prj.musicft.domain.model.RepeatMode
import com.prj.musicft.domain.model.ThemeMode
import com.prj.musicft.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl @Inject constructor(private val settingsDao: SettingsDao) :
        SettingsRepository {

    override val settings: Flow<AppSettings> = settingsDao.getSettings().map { it.toDomain() }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        settingsDao.updateThemeMode(themeMode)
    }

    override suspend fun updateDynamicColor(enabled: Boolean) {
        settingsDao.updateDynamicColor(enabled)
    }

    override suspend fun updateShuffle(enabled: Boolean) {
        settingsDao.updateShuffleEnabled(enabled)
    }

    override suspend fun updateRepeatMode(repeatMode: RepeatMode) {
        settingsDao.updateRepeatMode(repeatMode)
    }
}

package com.prj.musicft.domain.repository

import com.prj.musicft.domain.model.AppSettings
import com.prj.musicft.domain.model.RepeatMode
import com.prj.musicft.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateDynamicColor(enabled: Boolean)
    suspend fun updateShuffle(enabled: Boolean)
    suspend fun updateRepeatMode(repeatMode: RepeatMode)
}

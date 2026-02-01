package com.prj.musicft.data.local.dao

import androidx.room.*
import com.prj.musicft.data.local.entity.AppSettingsEntity
import com.prj.musicft.domain.model.RepeatMode
import com.prj.musicft.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    // ========== QUERIES ==========

    @Query("SELECT * FROM app_settings WHERE id = 1") fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsOnce(): AppSettingsEntity?

    // ========== INSERTS ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(settings: AppSettingsEntity)

    // ========== UPDATES ==========

    @Update suspend fun update(settings: AppSettingsEntity)

    // Specific field updates (more efficient than full update)
    @Query("UPDATE app_settings SET theme_mode = :themeMode WHERE id = 1")
    suspend fun updateThemeMode(themeMode: ThemeMode)

    @Query("UPDATE app_settings SET use_dynamic_color = :enabled WHERE id = 1")
    suspend fun updateDynamicColor(enabled: Boolean)

    @Query("UPDATE app_settings SET shuffle_enabled = :enabled WHERE id = 1")
    suspend fun updateShuffleEnabled(enabled: Boolean)

    @Query("UPDATE app_settings SET repeat_mode = :repeatMode WHERE id = 1")
    suspend fun updateRepeatMode(repeatMode: RepeatMode)
}

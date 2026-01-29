package com.prj.musicft.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prj.musicft.domain.model.RepeatMode
import com.prj.musicft.domain.model.ThemeMode

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,  // Always 1 - single row design
    
    @ColumnInfo(name = "theme_mode")
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    
    @ColumnInfo(name = "use_dynamic_color")
    val useDynamicColor: Boolean = true,  // Material You (Android 12+)
    
    @ColumnInfo(name = "shuffle_enabled")
    val shuffleEnabled: Boolean = false,
    
    @ColumnInfo(name = "repeat_mode")
    val repeatMode: RepeatMode = RepeatMode.OFF
)

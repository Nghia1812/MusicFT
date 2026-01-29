
package com.prj.musicft.data.local.database

import androidx.room.TypeConverter
import com.prj.musicft.domain.model.RepeatMode
import com.prj.musicft.domain.model.ThemeMode

class Converters {
    
    @TypeConverter
    fun fromThemeMode(value: ThemeMode): String = value.name
    
    @TypeConverter
    fun toThemeMode(value: String): ThemeMode = 
        ThemeMode.valueOf(value)
    
    @TypeConverter
    fun fromRepeatMode(value: RepeatMode): String = value.name
    
    @TypeConverter
    fun toRepeatMode(value: String): RepeatMode = 
        RepeatMode.valueOf(value)
}

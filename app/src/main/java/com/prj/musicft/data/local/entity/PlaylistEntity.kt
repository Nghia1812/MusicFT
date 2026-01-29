package com.prj.musicft.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    indices = [
        Index(value = ["created_at"])  // Sort by creation date
    ]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,  // Playlist name (1-100 characters)
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()  // Creation timestamp
)

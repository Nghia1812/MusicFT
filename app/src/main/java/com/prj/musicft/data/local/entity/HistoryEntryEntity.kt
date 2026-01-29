package com.prj.musicft.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history_entries",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE  // Delete history when song deleted
        )
    ],
    indices = [
        Index(value = ["played_at"]),  // Chronological queries
        Index(value = ["song_id"])     // Song history lookup
    ]
)
data class HistoryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "song_id")
    val songId: Long,  // FK to SongEntity
    
    @ColumnInfo(name = "played_at")
    val playedAt: Long  // Timestamp (epoch milliseconds)
)

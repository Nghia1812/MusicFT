package com.prj.musicft.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlist_id", "song_id"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE  // Delete entries when playlist deleted
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE  // Delete entries when song deleted
        )
    ],
    indices = [
        Index(value = ["playlist_id"]),  // Fast playlist lookup
        Index(value = ["song_id"]),      // Fast song lookup
        Index(value = ["position"])      // Ordering
    ]
)
data class PlaylistSongCrossRef(
    @ColumnInfo(name = "playlist_id")
    val playlistId: Long,  // FK to PlaylistEntity
    
    @ColumnInfo(name = "song_id")
    val songId: Long,  // FK to SongEntity
    
    @ColumnInfo(name = "position")
    val position: Int,  // Order within playlist (0-indexed)
    
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis()  // When song was added to playlist
)

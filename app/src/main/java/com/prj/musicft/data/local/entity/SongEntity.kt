package com.prj.musicft.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.SET_DEFAULT
        ),
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["album_id"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ],
    indices = [
        Index(value = ["file_path"], unique = true),  // Prevent duplicates
        Index(value = ["artist_id"]),                 // Fast artist queries
        Index(value = ["album_id"]),                  // Fast album queries
        Index(value = ["is_favorite"]),               // Fast favorite filtering
        Index(value = ["added_at"])                   // Chronological sorting
    ]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "artist_id")
    val artistId: Long,  // FK to ArtistEntity
    
    @ColumnInfo(name = "album_id")
    val albumId: Long,  // FK to AlbumEntity
    
    @ColumnInfo(name = "duration")
    val duration: Long,  // Duration in milliseconds
    
    @ColumnInfo(name = "file_path")
    val filePath: String,  // Absolute path to audio file (unique)
    
    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "artwork_uri")
    val artworkUri: String? = null,  // URI to album art (nullable)
    
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis(),  // Scan timestamp
    
    // Additional metadata (optional)
    @ColumnInfo(name = "track_number")
    val trackNumber: Int? = null,
    
    @ColumnInfo(name = "year")
    val year: Int? = null,
    
    @ColumnInfo(name = "genre")
    val genre: String? = null,
    
    @ColumnInfo(name = "mime_type")
    val mimeType: String,  // "audio/mpeg", "audio/flac", etc.
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long  // File size in bytes
)

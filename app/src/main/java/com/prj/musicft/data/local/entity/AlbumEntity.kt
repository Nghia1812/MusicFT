package com.prj.musicft.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "albums",
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ],
    indices = [
        Index(value = ["name"]),      // Album name search
        Index(value = ["artist_id"])  // Artist's albums
    ]
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,  // Album name (e.g., "Abbey Road")
    
    @ColumnInfo(name = "artist_id", defaultValue = "1")
    val artistId: Long,  // FK to ArtistEntity (defaults to Unknown Artist)
    
    @ColumnInfo(name = "artwork_uri")
    val artworkUri: String? = null,  // Album cover art URI
    
    @ColumnInfo(name = "year")
    val year: Int? = null,  // Release year
    
    @ColumnInfo(name = "track_count")
    val trackCount: Int = 0  // Number of songs (computed)
)

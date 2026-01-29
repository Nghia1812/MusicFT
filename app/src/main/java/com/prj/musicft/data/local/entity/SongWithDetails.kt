package com.prj.musicft.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SongWithDetails(
    @Embedded 
    val song: SongEntity,
    
    @Relation(
        parentColumn = "artist_id",
        entityColumn = "id"
    )
    val artist: ArtistEntity,
    
    @Relation(
        parentColumn = "album_id",
        entityColumn = "id"
    )
    val album: AlbumEntity
)

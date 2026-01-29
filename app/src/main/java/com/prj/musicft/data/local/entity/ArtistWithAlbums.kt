package com.prj.musicft.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ArtistWithAlbums(
    @Embedded 
    val artist: ArtistEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "artist_id"
    )
    val albums: List<AlbumEntity>
)

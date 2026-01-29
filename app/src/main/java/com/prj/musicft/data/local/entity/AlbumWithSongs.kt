package com.prj.musicft.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class AlbumWithSongs(
    @Embedded 
    val album: AlbumEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "album_id"
    )
    val songs: List<SongEntity>
)

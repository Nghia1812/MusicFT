package com.prj.musicft.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class HistoryWithSong(
    @Embedded val history: HistoryEntryEntity,
    @Relation(
        parentColumn = "song_id",
        entityColumn = "id"
    )
    val song: SongEntity
)

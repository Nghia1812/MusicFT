package com.prj.musicft.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.prj.musicft.data.local.entity.PlaylistEntity

data class PlaylistWithSongCount(
    @Embedded val playlist: PlaylistEntity,
    @ColumnInfo(name = "song_count") val songCount: Int
)

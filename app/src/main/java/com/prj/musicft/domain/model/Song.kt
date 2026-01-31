package com.prj.musicft.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
        val id: Long,
        val title: String,
        val artistId: Long,
        val artistName: String,
        val albumId: Long,
        val albumName: String,
        val duration: Long,
        val filePath: String, // Needed for playback
        val artworkUri: String?,
        val isFavorite: Boolean,
        val trackNumber: Int? = null,
        val year: Int? = null,
        val genre: String? = null,
        val addedAt: Long = 0
) : Parcelable

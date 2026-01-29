package com.prj.musicft.domain.model

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
        val genre: String? = null
)

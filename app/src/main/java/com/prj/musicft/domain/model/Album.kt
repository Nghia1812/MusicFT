package com.prj.musicft.domain.model

data class Album(
        val id: Long,
        val name: String,
        val artistId: Long,
        val artistName: String, // Helpful to have resolved
        val artworkUri: String?,
        val year: Int?,
        val trackCount: Int
)

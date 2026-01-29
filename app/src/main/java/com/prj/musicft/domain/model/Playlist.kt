package com.prj.musicft.domain.model

data class Playlist(
        val id: Long,
        val name: String,
        val songCount: Int = 0, // Helpful for UI
        val createdAt: Long
)

package com.prj.musicft.domain.model

data class Artist(
        val id: Long,
        val name: String,
        val albumCount: Int = 0, // Could be computed or fetched
        val songCount: Int = 0 // Could be computed or fetched
)

package com.prj.musicft.domain.model

data class HistoryEntry(
        val id: Long,
        val songId: Long,
        val playedAt: Long,
        val song: Song? = null // Optional resolved song
)

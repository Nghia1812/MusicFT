package com.prj.musicft.domain.repository

import com.prj.musicft.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getRecentHistory(limit: Int = 50): Flow<List<HistoryEntry>>
    suspend fun recordListen(songId: Long)
    suspend fun clearHistory()
}

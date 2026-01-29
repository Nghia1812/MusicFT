package com.prj.musicft.data.repository

import com.prj.musicft.data.local.dao.HistoryDao
import com.prj.musicft.data.local.entity.HistoryEntryEntity
import com.prj.musicft.data.mapper.toDomain
import com.prj.musicft.domain.model.HistoryEntry
import com.prj.musicft.domain.repository.HistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl @Inject constructor(private val historyDao: HistoryDao) :
        HistoryRepository {

    override fun getRecentHistory(limit: Int): Flow<List<HistoryEntry>> {
        return historyDao.getRecentHistoryWithSongs(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun recordListen(songId: Long) {
        // Logic: Check if played recently (5 mins)
        val threshold = System.currentTimeMillis() - 300_000 // 5 mins
        val wasRecentlyPlayed = historyDao.wasPlayedRecently(songId, threshold)

        if (!wasRecentlyPlayed) {
            historyDao.insert(
                    HistoryEntryEntity(songId = songId, playedAt = System.currentTimeMillis())
            )
        }
    }

    override suspend fun clearHistory() {
        historyDao.clearHistory()
    }
}

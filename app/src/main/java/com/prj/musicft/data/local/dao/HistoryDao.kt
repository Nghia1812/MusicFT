package com.prj.musicft.data.local.dao

import androidx.room.*
import com.prj.musicft.data.local.entity.HistoryEntryEntity
import com.prj.musicft.data.local.entity.HistoryWithSong
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    // ========== QUERIES ==========

    @Query(
            """
        SELECT * FROM history_entries 
        ORDER BY played_at DESC 
        LIMIT :limit
    """
    )
    fun getRecentHistory(limit: Int = 50): Flow<List<HistoryEntryEntity>>

    @Query(
            """
        SELECT he.* FROM history_entries he
        WHERE he.song_id = :songId
        ORDER BY he.played_at DESC
    """
    )
    fun getSongHistory(songId: Long): Flow<List<HistoryEntryEntity>>

    // ========== INSERTS ==========

    @Insert suspend fun insert(entry: HistoryEntryEntity)

    // ========== DELETES ==========

    @Query("DELETE FROM history_entries WHERE played_at < :timestamp")
    suspend fun deleteOldEntries(timestamp: Long)

    @Query("DELETE FROM history_entries") suspend fun clearHistory()

    // ========== COUNTS ==========

    @Query("SELECT COUNT(*) FROM history_entries") suspend fun getHistoryCount(): Int

    // ========== CHECKS ==========

    @Query(
            """
        SELECT COUNT(*) > 0 FROM history_entries 
        WHERE song_id = :songId 
          AND played_at > :threshold
    """
    )
    suspend fun wasPlayedRecently(songId: Long, threshold: Long): Boolean

    // ========== WITH SONG DETAILS ==========

    @Transaction
    @Query(
            """
        SELECT he.*, s.* FROM history_entries he
        INNER JOIN songs s ON he.song_id = s.id
        ORDER BY he.played_at DESC
        LIMIT :limit
    """
    )
    fun getRecentHistoryWithSongs(limit: Int = 50): Flow<List<HistoryWithSong>>
}

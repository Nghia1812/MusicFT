package com.prj.musicft.data.local.dao

import androidx.room.*
import com.prj.musicft.data.local.entity.PlaylistEntity
import com.prj.musicft.data.local.entity.PlaylistSongCrossRef
import com.prj.musicft.data.local.entity.PlaylistWithSongs
import com.prj.musicft.data.local.model.PlaylistWithSongCount
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    // ========== QUERIES ==========

    @Transaction
    @Query(
        """
        SELECT p.*, COUNT(ps.song_id) as song_count
        FROM playlists p
        LEFT JOIN playlist_songs ps ON p.id = ps.playlist_id
        GROUP BY p.id
        ORDER BY p.created_at DESC
        """
    )
    fun getAllPlaylists(): Flow<List<PlaylistWithSongCount>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    // ========== INSERTS ==========

    @Insert suspend fun insert(playlist: PlaylistEntity): Long

    // ========== UPDATES ==========

    @Update suspend fun update(playlist: PlaylistEntity)

    // ========== DELETES ==========

    @Delete suspend fun delete(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists") suspend fun deleteAll()

    // ========== PLAYLIST-SONG ASSOCIATIONS ==========

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Delete suspend fun removeSongFromPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId AND song_id = :songId")
    suspend fun deleteSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    @Query(
            """
        SELECT COALESCE(MAX(position), -1) FROM playlist_songs 
        WHERE playlist_id = :playlistId
    """
    )
    suspend fun getMaxPosition(playlistId: Long): Int

    @Query(
            """
        UPDATE playlist_songs 
        SET position = position - 1 
        WHERE playlist_id = :playlistId AND position > :removedPosition
    """
    )
    suspend fun decrementPositionsAfter(playlistId: Long, removedPosition: Int)

    @Query(
            """
        SELECT * FROM playlist_songs 
        WHERE playlist_id = :playlistId 
        ORDER BY position ASC
    """
    )
    fun getPlaylistSongs(playlistId: Long): Flow<List<PlaylistSongCrossRef>>

    // ========== COUNTS ==========

    @Query("SELECT COUNT(*) FROM playlists") suspend fun getPlaylistCount(): Int

    @Query(
            """
        SELECT COUNT(*) FROM playlist_songs 
        WHERE playlist_id = :playlistId
    """
    )
    suspend fun getSongCount(playlistId: Long): Int

    @Query("SELECT playlist_id FROM playlist_songs WHERE song_id = :songId")
    fun getPlaylistsForSong(songId: Long): Flow<List<Long>>
}

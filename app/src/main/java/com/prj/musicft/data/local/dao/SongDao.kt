package com.prj.musicft.data.local.dao

import androidx.room.*
import com.prj.musicft.data.local.entity.SongEntity
import com.prj.musicft.data.local.entity.SongWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    // ========== QUERIES ==========

    @Query("SELECT * FROM songs ORDER BY title ASC") fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE is_favorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE album_id = :albumId ORDER BY track_number ASC, title ASC")
    fun getSongsByAlbum(albumId: Long): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE artist_id = :artistId ORDER BY title ASC")
    fun getSongsByArtist(artistId: Long): Flow<List<SongEntity>>

    @Query(
            """
        SELECT s.* FROM songs s
        LEFT JOIN artists a ON s.artist_id = a.id
        LEFT JOIN albums al ON s.album_id = al.id
        WHERE s.title LIKE '%' || :query || '%' 
           OR a.name LIKE '%' || :query || '%'
           OR al.name LIKE '%' || :query || '%'
        ORDER BY s.title ASC
    """
    )
    fun searchSongs(query: String): Flow<List<SongEntity>>

    // ========== INSERTS ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(song: SongEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(songs: List<SongEntity>)

    // ========== UPDATES ==========

    @Update suspend fun update(song: SongEntity)

    @Query("UPDATE songs SET is_favorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)

    // ========== DELETES ==========

    @Delete suspend fun delete(song: SongEntity)

    @Query("DELETE FROM songs") suspend fun deleteAll()

    @Query("DELETE FROM songs WHERE file_path = :filePath")
    suspend fun deleteByFilePath(filePath: String)

    // ========== COUNTS ==========

    @Query("SELECT COUNT(*) FROM songs") suspend fun getSongCount(): Int

    @Query("SELECT COUNT(*) FROM songs WHERE is_favorite = 1") suspend fun getFavoriteCount(): Int

    // ========== WITH DETAILS ==========

    @Transaction
    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongWithDetails(songId: Long): SongWithDetails?

    @Query("SELECT * FROM songs WHERE file_path = :filePath LIMIT 1")
    suspend fun getSongByFilePath(filePath: String): SongEntity?

    @Query("SELECT * FROM songs WHERE media_id = :mediaId LIMIT 1")
    suspend fun getSongByMediaId(mediaId: Long): SongEntity?

    @Transaction
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongsWithDetails(): Flow<List<SongWithDetails>>
}

package com.prj.musicft.data.local.dao

import androidx.room.*
import com.prj.musicft.data.local.entity.AlbumEntity
import com.prj.musicft.data.local.entity.AlbumWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    // ========== QUERIES ==========

    @Query("SELECT * FROM albums ORDER BY name ASC") fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :albumId")
    suspend fun getAlbumById(albumId: Long): AlbumEntity?

    @Query("SELECT * FROM albums WHERE artist_id = :artistId ORDER BY name ASC")
    fun getAlbumsByArtist(artistId: Long): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchAlbums(query: String): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE name = :name AND artist_id = :artistId LIMIT 1")
    suspend fun getAlbumByNameAndArtist(name: String, artistId: Long): AlbumEntity?

    // ========== INSERTS ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(album: AlbumEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(albums: List<AlbumEntity>)

    // ========== UPDATES ==========

    @Update suspend fun update(album: AlbumEntity)

    @Query(
            """
        UPDATE albums SET track_count = (
            SELECT COUNT(*) FROM songs WHERE album_id = albums.id
        )
    """
    )
    suspend fun updateAllTrackCounts()

    @Query(
            """
        UPDATE albums SET track_count = (
            SELECT COUNT(*) FROM songs WHERE album_id = :albumId
        )
        WHERE id = :albumId
    """
    )
    suspend fun updateTrackCount(albumId: Long)

    // ========== DELETES ==========

    @Delete suspend fun delete(album: AlbumEntity)

    @Query("DELETE FROM albums") suspend fun deleteAll()

    // ========== WITH SONGS ==========

    @Transaction
    @Query("SELECT * FROM albums WHERE id = :albumId")
    fun getAlbumWithSongs(albumId: Long): Flow<AlbumWithSongs?>

    @Transaction
    @Query("SELECT * FROM albums ORDER BY name ASC")
    fun getAllAlbumsWithSongs(): Flow<List<AlbumWithSongs>>
}

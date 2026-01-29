package com.prj.musicft.data.local.dao

import androidx.room.*
import com.prj.musicft.data.local.entity.ArtistEntity
import com.prj.musicft.data.local.entity.ArtistWithAlbums
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    // ========== QUERIES ==========

    @Query("SELECT * FROM artists ORDER BY name ASC") fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE id = :artistId")
    suspend fun getArtistById(artistId: Long): ArtistEntity?

    @Query("SELECT * FROM artists WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchArtists(query: String): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE name = :name LIMIT 1")
    suspend fun getArtistByName(name: String): ArtistEntity?

    // ========== INSERTS ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(artist: ArtistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(artists: List<ArtistEntity>)

    // ========== UPDATES ==========

    @Update suspend fun update(artist: ArtistEntity)

    // ========== DELETES ==========

    @Delete suspend fun delete(artist: ArtistEntity)

    @Query("DELETE FROM artists") suspend fun deleteAll()

    // ========== COUNTS ==========

    @Query("SELECT COUNT(*) FROM artists") suspend fun getArtistCount(): Int

    // ========== WITH ALBUMS ==========

    @Transaction
    @Query("SELECT * FROM artists WHERE id = :artistId")
    fun getArtistWithAlbums(artistId: Long): Flow<ArtistWithAlbums?>

    @Transaction
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtistsWithAlbums(): Flow<List<ArtistWithAlbums>>
}

package com.prj.musicft.domain.repository

import com.prj.musicft.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getFavoriteSongs(): Flow<List<Song>>
    suspend fun getSongById(songId: Long): Song?
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
    fun getSongsByArtist(artistId: Long): Flow<List<Song>>
    fun searchSongs(query: String): Flow<List<Song>>
    suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)
    suspend fun deleteSong(songId: Long)
}

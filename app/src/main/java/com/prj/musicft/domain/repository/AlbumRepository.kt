package com.prj.musicft.domain.repository

import com.prj.musicft.domain.model.Album
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun getAllAlbums(): Flow<List<Album>>
    suspend fun getAlbumById(albumId: Long): Album?
    fun getAlbumsByArtist(artistId: Long): Flow<List<Album>>
    fun searchAlbums(query: String): Flow<List<Album>>
}

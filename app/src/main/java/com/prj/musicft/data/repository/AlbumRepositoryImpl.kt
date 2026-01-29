package com.prj.musicft.data.repository

import com.prj.musicft.data.local.dao.AlbumDao
import com.prj.musicft.data.mapper.toDomain
import com.prj.musicft.domain.model.Album
import com.prj.musicft.domain.repository.AlbumRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlbumRepositoryImpl @Inject constructor(private val albumDao: AlbumDao) : AlbumRepository {

    override fun getAllAlbums(): Flow<List<Album>> {
        return albumDao.getAllAlbums().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAlbumById(albumId: Long): Album? {
        return albumDao.getAlbumById(albumId)?.toDomain()
    }

    override fun getAlbumsByArtist(artistId: Long): Flow<List<Album>> {
        return albumDao.getAlbumsByArtist(artistId).map { list -> list.map { it.toDomain() } }
    }

    override fun searchAlbums(query: String): Flow<List<Album>> {
        return albumDao.searchAlbums(query).map { list -> list.map { it.toDomain() } }
    }
}

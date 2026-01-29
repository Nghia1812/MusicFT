package com.prj.musicft.data.repository

import com.prj.musicft.data.local.dao.ArtistDao
import com.prj.musicft.data.mapper.toDomain
import com.prj.musicft.domain.model.Artist
import com.prj.musicft.domain.repository.ArtistRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ArtistRepositoryImpl @Inject constructor(private val artistDao: ArtistDao) :
        ArtistRepository {

    override fun getAllArtists(): Flow<List<Artist>> {
        return artistDao.getAllArtists().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getArtistById(artistId: Long): Artist? {
        return artistDao.getArtistById(artistId)?.toDomain()
    }

    override fun searchArtists(query: String): Flow<List<Artist>> {
        return artistDao.searchArtists(query).map { list -> list.map { it.toDomain() } }
    }
}

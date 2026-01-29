package com.prj.musicft.domain.repository

import com.prj.musicft.domain.model.Artist
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {
    fun getAllArtists(): Flow<List<Artist>>
    suspend fun getArtistById(artistId: Long): Artist?
    fun searchArtists(query: String): Flow<List<Artist>>
}

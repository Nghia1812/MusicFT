package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.model.Artist
import com.prj.musicft.domain.repository.ArtistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllArtistsUseCase @Inject constructor(
    private val artistRepository: ArtistRepository
) {
    operator fun invoke(): Flow<List<Artist>> {
        return artistRepository.getAllArtists()
    }
}

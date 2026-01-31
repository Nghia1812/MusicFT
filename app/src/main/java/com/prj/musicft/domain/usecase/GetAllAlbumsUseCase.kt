package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.model.Album
import com.prj.musicft.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllAlbumsUseCase @Inject constructor(
    private val albumRepository: AlbumRepository
) {
    operator fun invoke(): Flow<List<Album>> {
        return albumRepository.getAllAlbums()
    }
}

package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.model.Song
import com.prj.musicft.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteSongsUseCase @Inject constructor(
    private val songRepository: SongRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return songRepository.getFavoriteSongs()
    }
}

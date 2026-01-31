package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.model.Song
import com.prj.musicft.domain.repository.SongRepository
import javax.inject.Inject

class GetSongByIdUseCase @Inject constructor(
    private val songRepository: SongRepository
) {
    suspend operator fun invoke(songId: Long): Song? {
        return songRepository.getSongById(songId)
    }
}

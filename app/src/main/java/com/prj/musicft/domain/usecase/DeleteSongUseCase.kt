package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.repository.SongRepository
import javax.inject.Inject

class DeleteSongUseCase @Inject constructor(
    private val songRepository: SongRepository
) {
    suspend operator fun invoke(songId: Long) {
        songRepository.deleteSong(songId)
    }
}

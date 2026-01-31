package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.repository.PlaylistRepository
import javax.inject.Inject

class AddSongToPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, songId: Long) {
        playlistRepository.addSongToPlaylist(playlistId, songId)
    }
}

package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.repository.PlaylistRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(name: String, songToAddId: Long?): Long {
        val playlistId = playlistRepository.createPlaylist(name)
        if (songToAddId != null) {
            playlistRepository.addSongToPlaylist(playlistId, songToAddId)
        }
        return playlistId
    }
}

package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.model.Playlist
import com.prj.musicft.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Playlist>> {
        return playlistRepository.getAllPlaylists()
    }
}

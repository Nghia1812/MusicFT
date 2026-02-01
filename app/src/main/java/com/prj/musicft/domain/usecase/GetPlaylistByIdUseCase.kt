package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.model.Playlist
import com.prj.musicft.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistByIdUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long): Flow<Playlist?> {
        return repository.getPlaylistById(playlistId)
    }
}

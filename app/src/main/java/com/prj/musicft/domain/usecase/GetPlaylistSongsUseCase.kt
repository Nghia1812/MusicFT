package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.model.Song
import com.prj.musicft.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistSongsUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long): Flow<List<Song>> {
        return repository.getPlaylistSongs(playlistId)
    }
}

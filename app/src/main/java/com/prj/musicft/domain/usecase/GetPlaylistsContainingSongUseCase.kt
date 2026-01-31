package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistsContainingSongUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(songId: Long): Flow<List<Long>> {
        return playlistRepository.getPlaylistsContainingSong(songId)
    }
}

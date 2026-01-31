package com.prj.musicft.domain.repository

import com.prj.musicft.domain.model.Playlist
import com.prj.musicft.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getPlaylistById(playlistId: Long): Flow<Playlist?>
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>>

    suspend fun createPlaylist(name: String): Long
    suspend fun updatePlaylistName(playlistId: Long, newName: String)
    suspend fun deletePlaylist(playlistId: Long)

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    suspend fun reorderSongInPlaylist(
            playlistId: Long,
            fromPosition: Int,
            toPosition: Int
    ) // Future
    suspend fun clearPlaylist(playlistId: Long)
    fun getPlaylistsContainingSong(songId: Long): Flow<List<Long>>
}

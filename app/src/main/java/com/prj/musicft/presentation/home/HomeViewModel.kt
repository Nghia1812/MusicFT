package com.prj.musicft.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.musicft.domain.model.Song
import com.prj.musicft.domain.model.Playlist
import com.prj.musicft.domain.usecase.AddSongToPlaylistUseCase
import com.prj.musicft.domain.usecase.CreatePlaylistUseCase
import com.prj.musicft.domain.usecase.DeleteSongUseCase
import com.prj.musicft.domain.usecase.GetAllPlaylistsUseCase
import com.prj.musicft.domain.usecase.GetAllSongsUseCase
import com.prj.musicft.domain.usecase.GetPlaylistsContainingSongUseCase
import com.prj.musicft.domain.usecase.RemoveSongFromPlaylistUseCase
import com.prj.musicft.domain.usecase.UpdateFavoriteStatusUseCase
import com.prj.musicft.domain.usecase.RecordListenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.prj.musicft.presentation.common.UiState
import com.prj.musicft.presentation.service.connection.MusicServiceConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
    private val getPlaylistsContainingSongUseCase: GetPlaylistsContainingSongUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val removeSongFromPlaylistUseCase: RemoveSongFromPlaylistUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase,
    private val deleteSongUseCase: DeleteSongUseCase,
    private val recordListenUseCase: RecordListenUseCase,
    private val serviceConnection: MusicServiceConnection
) : ViewModel() {

    val uiState: StateFlow<UiState<List<Song>>> =
            getAllSongsUseCase()
                    .map { songs ->
                        if (songs.isEmpty()) {
                            Timber.i("empty list")
                            UiState.Empty
                        } else {
                            Timber.i("Songs: $songs")
                            UiState.Success(songs)
                        }
                    }
                    .onStart { emit(UiState.Loading) }
                    .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
                    .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            initialValue = UiState.Loading
                    )

    val playlists: StateFlow<List<Playlist>> = getAllPlaylistsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _playlistsContainingSong = MutableStateFlow<Set<Long>>(emptySet())
    val playlistsContainingSong: StateFlow<Set<Long>> = _playlistsContainingSong.asStateFlow()

    private var songToPlaylistJob: kotlinx.coroutines.Job? = null

    fun onPlayNext(song: Song) {
        serviceConnection.addSongToNext(song)
    }

    // Handled in UI state
    fun onAddToPlaylist(song: Song) {
        songToPlaylistJob?.cancel()
        songToPlaylistJob = viewModelScope.launch {
            getPlaylistsContainingSongUseCase(song.id).collect { ids ->
                _playlistsContainingSong.value = ids.toSet()
            }
        }
    }

    fun toggleSongInPlaylist(playlist: Playlist, song: Song, isCurrentlyAdded: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyAdded) {
                removeSongFromPlaylistUseCase(playlist.id, song.id)
                Timber.d("Removed ${song.title} from playlist ${playlist.name}")
            } else {
                addSongToPlaylistUseCase(playlist.id, song.id)
                Timber.d("Added ${song.title} to playlist ${playlist.name}")
            }
            // Status update happens automatically via flow collection
        }
    }

    fun createPlaylist(name: String, songToAdd: Song?) {
        viewModelScope.launch {
            createPlaylistUseCase(name, songToAdd?.id)
        }
    }

    fun onAddToFavorites(song: Song) {
        viewModelScope.launch {
            updateFavoriteStatusUseCase(song.id, !song.isFavorite)
        }
    }

    fun onShareSong(song: Song) {
        // TODO: Implement Share Intent
        Timber.d("Share song: ${song.title}")
    }

    fun onGoToAlbum(song: Song) {
         // TODO: Navigation to Album Details
         Timber.d("Go to album: ${song.albumName}")
    }

    fun onRemoveFromLibrary(song: Song) {
        viewModelScope.launch {
            deleteSongUseCase(song.id)
        }
    }

    fun recordListen(song: Song) {
        viewModelScope.launch {
            recordListenUseCase(song.id)
        }
    }
}

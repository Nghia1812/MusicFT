package com.prj.musicft.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.musicft.domain.model.Playlist
import com.prj.musicft.domain.model.Song
import com.prj.musicft.domain.usecase.AddSongToPlaylistUseCase
import com.prj.musicft.domain.usecase.CreatePlaylistUseCase
import com.prj.musicft.domain.usecase.DeleteSongUseCase
import com.prj.musicft.domain.usecase.GetAllPlaylistsUseCase
import com.prj.musicft.domain.usecase.GetAllSongsUseCase
import com.prj.musicft.domain.usecase.GetPlaylistsContainingSongUseCase
import com.prj.musicft.domain.usecase.RemoveSongFromPlaylistUseCase
import com.prj.musicft.domain.usecase.UpdateFavoriteStatusUseCase
import com.prj.musicft.presentation.common.UiState
import com.prj.musicft.presentation.service.connection.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase,
    private val getPlaylistsContainingSongUseCase: GetPlaylistsContainingSongUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val removeSongFromPlaylistUseCase: RemoveSongFromPlaylistUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val deleteSongUseCase: DeleteSongUseCase,
    private val serviceConnection: MusicServiceConnection
) : ViewModel() {

    private val _playlistsContainingSong = MutableStateFlow<Set<Long>>(emptySet())
    val playlistsContainingSong: StateFlow<Set<Long>> = _playlistsContainingSong.asStateFlow()

    private var songToPlaylistJob: kotlinx.coroutines.Job? = null

    val playlists: StateFlow<List<Playlist>> = getAllPlaylistsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Combining all sources to produce the UI state
    val uiState: StateFlow<UiState<List<Song>>> = combine(
        getAllSongsUseCase(),
        _searchQuery,
    ) { songs, query ->
        // Start filtering
        var filtered = songs
        
        // Filter by Query
        if (query.isNotBlank()) {
            filtered = filtered.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.artistName.contains(query, ignoreCase = true) 
            }
        }

        // Check for Initial state (no query, no genre) -> Idle
        if (query.isBlank()) {
            UiState.Idle
        } else if (filtered.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(filtered)
        }
    }
    .onStart { emit(UiState.Idle) }
    .catch { emit(UiState.Error(it.message ?: "Unknown Search Error")) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Idle
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearFilters() {
        _searchQuery.value = ""
    }
    
    // Actions
    fun onAddToFavorites(song: Song) {
        viewModelScope.launch {
            updateFavoriteStatusUseCase(song.id, !song.isFavorite)
        }
    }

    fun onPlayNext(song: Song) {
        serviceConnection.addSongToNext(song)
    }

    fun onAddToPlaylist(song: Song) {
        songToPlaylistJob?.cancel()
        songToPlaylistJob = viewModelScope.launch {
            getPlaylistsContainingSongUseCase(song.id).collect { ids ->
                _playlistsContainingSong.value = ids.toSet()
            }
        }
    }

    fun toggleSongInPlaylist(playlist: com.prj.musicft.domain.model.Playlist, song: Song, isCurrentlyAdded: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyAdded) {
                removeSongFromPlaylistUseCase(playlist.id, song.id)
            } else {
                addSongToPlaylistUseCase(playlist.id, song.id)
            }
        }
    }
    
    fun createPlaylist(name: String, songToAdd: Song?) {
        viewModelScope.launch {
            createPlaylistUseCase(name, songToAdd?.id)
        }
    }

    fun onShareSong(song: Song) {
        Timber.d("Share song: ${song.title}")
    }

    fun onGoToAlbum(song: Song) {
         Timber.d("Go to album: ${song.albumName}")
    }

    fun onRemoveFromLibrary(song: Song) {
        viewModelScope.launch {
            deleteSongUseCase(song.id)
        }
    }
}

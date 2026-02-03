package com.prj.musicft.presentation.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.musicft.domain.model.Playlist
import com.prj.musicft.domain.model.Song
import com.prj.musicft.domain.usecase.AddSongToPlaylistUseCase
import com.prj.musicft.domain.usecase.CreatePlaylistUseCase
import com.prj.musicft.domain.usecase.DeleteSongUseCase
import com.prj.musicft.domain.usecase.GetAllPlaylistsUseCase
import com.prj.musicft.domain.usecase.GetFavoriteSongsUseCase
import com.prj.musicft.domain.usecase.GetPlaylistByIdUseCase
import com.prj.musicft.domain.usecase.GetPlaylistSongsUseCase
import com.prj.musicft.domain.usecase.GetPlaylistsContainingSongUseCase
import com.prj.musicft.domain.usecase.GetRecentHistoryUseCase
import com.prj.musicft.domain.usecase.RemoveSongFromPlaylistUseCase
import com.prj.musicft.domain.usecase.UpdateFavoriteStatusUseCase
import com.prj.musicft.presentation.common.UiState
import com.prj.musicft.presentation.service.connection.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CollectionSongListViewModel @Inject constructor(
    private val getFavoriteSongsUseCase: GetFavoriteSongsUseCase,
    private val getRecentHistoryUseCase: GetRecentHistoryUseCase,
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
    private val getPlaylistsContainingSongUseCase: GetPlaylistsContainingSongUseCase,
    private val getPlaylistSongsUseCase: GetPlaylistSongsUseCase,
    private val getPlaylistByIdUseCase: GetPlaylistByIdUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val removeSongFromPlaylistUseCase: RemoveSongFromPlaylistUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase,
    private val deleteSongUseCase: DeleteSongUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val serviceConnection: MusicServiceConnection
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Song>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Song>>> = _uiState.asStateFlow()

    private val collectionType: String = checkNotNull(savedStateHandle["type"])
    private val playlistId: Long? = savedStateHandle.get<String>("playlistId")?.toLongOrNull() ?: savedStateHandle.get<Long>("playlistId")

    private val _title = MutableStateFlow("Collection")
    val title: StateFlow<String> = _title.asStateFlow()

    val playlists: StateFlow<List<Playlist>> = getAllPlaylistsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _playlistsContainingSong = MutableStateFlow<Set<Long>>(emptySet())
    val playlistsContainingSong: StateFlow<Set<Long>> = _playlistsContainingSong.asStateFlow()

    private var songToPlaylistJob: Job? = null

    init {
        loadSongs()
        updateTitle()
    }

    private fun updateTitle() {
        when (collectionType) {
            CollectionType.Favorites.name -> _title.value = "Favorites"
            CollectionType.History.name -> _title.value = "History"
            CollectionType.Playlists.name -> {
                if (playlistId != null) {
                    viewModelScope.launch {
                        getPlaylistByIdUseCase(playlistId).collectLatest { playlist ->
                            _title.value = playlist?.name ?: "Playlist"
                        }
                    }
                } else {
                    _title.value = "Playlist"
                }
            }
            else -> _title.value = collectionType
        }
    }

    private fun loadSongs() {
        viewModelScope.launch {
            try {
                when (collectionType) {
                    CollectionType.Favorites.name -> {
                        getFavoriteSongsUseCase().collectLatest { songs ->
                            _uiState.value = if (songs.isEmpty()) UiState.Empty else UiState.Success(songs)
                        }
                    }
                    CollectionType.History.name -> {
                        getRecentHistoryUseCase(limit = 100).collectLatest { entries ->
                            val songs = entries.mapNotNull { it.song }.distinctBy { it.id }
                            _uiState.value =
                                if (songs.isEmpty()) UiState.Empty else UiState.Success(songs)
                        }
                    }
                    CollectionType.Playlists.name -> {
                        if (playlistId != null) {
                            getPlaylistSongsUseCase(playlistId).collectLatest { songs ->
                                _uiState.value = if (songs.isEmpty()) UiState.Empty else UiState.Success(songs)
                            }
                        } else {
                             _uiState.value = UiState.Error("Invalid Playlist ID")
                        }
                    }
                    else -> {
                        _uiState.value = UiState.Empty
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onPlayAll() {
        val state = _uiState.value
        if (state is UiState.Success) {
            serviceConnection.setShuffleMode(false)
            serviceConnection.playQueue(state.data)
        }
    }

    fun onShuffle() {
        val state = _uiState.value
        if (state is UiState.Success) {
            val shuffledSongs = state.data.shuffled()
            serviceConnection.setShuffleMode(true)
            serviceConnection.playQueue(shuffledSongs)
        }
    }

    // --- Song Option Actions ---

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

    fun toggleSongInPlaylist(playlist: Playlist, song: Song, isCurrentlyAdded: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyAdded) {
                removeSongFromPlaylistUseCase(playlist.id, song.id)
                Timber.d("Removed ${song.title} from playlist ${playlist.name}")
            } else {
                addSongToPlaylistUseCase(playlist.id, song.id)
                Timber.d("Added ${song.title} to playlist ${playlist.name}")
            }
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
        // Placeholder for share
        Timber.d("Share song: ${song.title}")
    }

    fun onGoToAlbum(song: Song) {
        // Placeholder for navigation
        Timber.d("Go to album: ${song.albumName}")
    }

    fun onRemoveFromLibrary(song: Song) {
        viewModelScope.launch {
            deleteSongUseCase(song.id)
        }
    }
}

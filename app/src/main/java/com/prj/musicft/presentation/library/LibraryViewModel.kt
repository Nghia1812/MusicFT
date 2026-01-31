package com.prj.musicft.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.musicft.domain.model.Song
import com.prj.musicft.domain.usecase.ClearHistoryUseCase
import com.prj.musicft.domain.usecase.GetAllAlbumsUseCase
import com.prj.musicft.domain.usecase.GetAllArtistsUseCase
import com.prj.musicft.domain.usecase.GetAllPlaylistsUseCase
import com.prj.musicft.domain.usecase.GetFavoriteSongsUseCase
import com.prj.musicft.domain.usecase.GetRecentHistoryUseCase
import com.prj.musicft.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryData(
    val favoriteCount: Int = 0,
    val playlistCount: Int = 0,
    val albumCount: Int = 0,
    val artistCount: Int = 0,
    val recentHistory: List<Song> = emptyList()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getFavoriteSongsUseCase: GetFavoriteSongsUseCase,
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val getAllArtistsUseCase: GetAllArtistsUseCase,
    private val getRecentHistoryUseCase: GetRecentHistoryUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<LibraryData>> = combine(
        getFavoriteSongsUseCase(),
        getAllPlaylistsUseCase(),
        getAllAlbumsUseCase(),
        getAllArtistsUseCase(),
        getRecentHistoryUseCase(limit = 10)
    ) { favorites, playlists, albums, artists, history ->
        val recentSongs = history.mapNotNull { it.song }.distinctBy { it.id }.take(5)
        
        UiState.Success(
            LibraryData(
                favoriteCount = favorites.size,
                playlistCount = playlists.size,
                albumCount = albums.size,
                artistCount = artists.size,
                recentHistory = recentSongs
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    fun clearHistory() {
        viewModelScope.launch {
            clearHistoryUseCase()
        }
    }
}

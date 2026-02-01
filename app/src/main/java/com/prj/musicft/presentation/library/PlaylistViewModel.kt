package com.prj.musicft.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.musicft.domain.model.Playlist
import com.prj.musicft.domain.usecase.CreatePlaylistUseCase
import com.prj.musicft.domain.usecase.GetAllPlaylistsUseCase
import com.prj.musicft.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = getAllPlaylistsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

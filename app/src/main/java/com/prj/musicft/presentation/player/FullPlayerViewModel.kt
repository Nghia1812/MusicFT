package com.prj.musicft.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.musicft.domain.model.RepeatMode
import com.prj.musicft.domain.model.Song
import com.prj.musicft.domain.usecase.GetSongByIdUseCase
import com.prj.musicft.domain.usecase.RecordListenUseCase
import com.prj.musicft.domain.usecase.UpdateFavoriteStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.prj.musicft.presentation.common.UiState
import com.prj.musicft.presentation.service.connection.MusicServiceConnection

data class FullPlayerUiState(
    val song: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isFavorite: Boolean = false,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
)

@HiltViewModel
class FullPlayerViewModel @Inject constructor(
    private val getSongByIdUseCase: GetSongByIdUseCase,
    private val updateFavoriteStatusUseCase: UpdateFavoriteStatusUseCase,
    private val recordListenUseCase: RecordListenUseCase,
    private val serviceConnection: MusicServiceConnection
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<FullPlayerUiState>>(UiState.Loading)
    val uiState: StateFlow<UiState<FullPlayerUiState>> = _uiState.asStateFlow()

    init {
        // Observe current song from service
        viewModelScope.launch {
            serviceConnection.currentSong.collect { currentSong ->
                if (currentSong != null) {
                    recordListenUseCase(currentSong.id)
                    // Fetch fresh data from DB to ensure isFavorite is correct
                    // This handles cases where the player is reopened and the cached song in Service is stale
                    val freshSong = getSongByIdUseCase(currentSong.id) ?: currentSong
                    
                    _uiState.value = UiState.Success(
                        FullPlayerUiState(
                            song = freshSong,
                            isFavorite = freshSong.isFavorite,
                            duration = freshSong.duration,
                            isPlaying = serviceConnection.isPlaying.value, // Preserve current playback state
                            currentPosition = serviceConnection.getCurrentPosition()
                        )
                    )
                }
            }
        }
        
        // Observe Service State
        viewModelScope.launch {
            serviceConnection.isPlaying.collect { isPlaying ->
                updateState { it.copy(isPlaying = isPlaying) }
            }
        }
        
        // Poll for progress updates
        viewModelScope.launch {
            while (true) {
                val currentState = (_uiState.value as? UiState.Success)?.data
                if (currentState != null) {
                    val position = serviceConnection.getCurrentPosition()
                    val duration = serviceConnection.getDuration()
                    
                    updateState { 
                        it.copy(
                            currentPosition = position,
                            duration = if (duration > 0) duration else it.duration
                        )
                    }
                }
                delay(500) // Update twice per second
            }
        }
        
    }

    fun loadSong(song: Song, forcePlay: Boolean = true) {
        // Check if this is the same song already playing
        val currentSong = serviceConnection.currentSong.value
        
        if (currentSong?.id == song.id && !forcePlay) {
            // Same song, just sync the UI state without restarting playback
            _uiState.value = UiState.Success(
                FullPlayerUiState(
                    song = song,
                    isFavorite = song.isFavorite,
                    duration = song.duration,
                    isPlaying = serviceConnection.isPlaying.value,
                    currentPosition = serviceConnection.getCurrentPosition()
                )
            )
        } else {
            // Different song or forced play, start playback
            serviceConnection.playSong(song)
            
            _uiState.value = UiState.Success(
                FullPlayerUiState(
                    song = song,
                    isFavorite = song.isFavorite,
                    duration = song.duration,
                    isPlaying = true // Optimistic update
                )
            )
        }
    }

    private fun updateState(update: (FullPlayerUiState) -> FullPlayerUiState) {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            _uiState.value = UiState.Success(update(currentState.data))
        }
    }

    fun onPlayPauseClick() {
        serviceConnection.playPause()
        // State update handled by flow collection
    }

    fun onSeek(position: Long) {
        serviceConnection.seekTo(position)
        updateState { it.copy(currentPosition = position) }
    }

    fun onSkipNext() {
        serviceConnection.skipToNext()
    }

    fun onSkipPrevious() {
        serviceConnection.skipToPrevious()
    }

    fun onShuffleClick() {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        val newMode = !currentState.isShuffleEnabled
        serviceConnection.setShuffleMode(newMode)
        updateState { it.copy(isShuffleEnabled = newMode) }
    }

    fun onRepeatClick() {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        
        val newMode = when (currentState.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        
        // Map domain RepeatMode to Player RepeatMode
        val playerMode = when (newMode) {
             RepeatMode.OFF -> androidx.media3.common.Player.REPEAT_MODE_OFF
             RepeatMode.ALL -> androidx.media3.common.Player.REPEAT_MODE_ALL
             RepeatMode.ONE -> androidx.media3.common.Player.REPEAT_MODE_ONE
        }
        serviceConnection.setRepeatMode(playerMode)
        
        updateState {
            it.copy(repeatMode = newMode)
        }
    }

    fun onFavoriteClick() {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            val song = currentState.data.song ?: return
            val newStatus = !song.isFavorite
            
            viewModelScope.launch {
                updateFavoriteStatusUseCase(song.id, newStatus)
                updateState { it.copy(isFavorite = newStatus) }
            }
        }
    }
}

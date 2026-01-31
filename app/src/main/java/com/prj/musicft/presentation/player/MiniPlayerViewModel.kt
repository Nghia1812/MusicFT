package com.prj.musicft.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.musicft.domain.model.Song
import com.prj.musicft.domain.usecase.RecordListenUseCase
import com.prj.musicft.presentation.service.connection.MusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val serviceConnection: MusicServiceConnection,
    private val recordListenUseCase: RecordListenUseCase
) : ViewModel() {

    val currentSong: StateFlow<Song?> = serviceConnection.currentSong
    val isPlaying: StateFlow<Boolean> = serviceConnection.isPlaying
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    init {
        // Record listen history
        viewModelScope.launch {
            serviceConnection.currentSong.collect { song ->
                song?.let { recordListenUseCase(it.id) }
            }
        }

        // Poll for progress updates
        viewModelScope.launch {
            while (true) {
                val position = serviceConnection.getCurrentPosition()
                val duration = serviceConnection.getDuration()
                
                _progress.value = if (duration > 0) {
                    (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                
                delay(500) // Update twice per second
            }
        }
    }
    
    fun onPlayPauseClick() {
        serviceConnection.playPause()
    }
    
    fun onSkipNext() {
        serviceConnection.skipToNext()
    }
    
    fun onSkipPrevious() {
        serviceConnection.skipToPrevious()
    }
}

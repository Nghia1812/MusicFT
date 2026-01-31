package com.prj.musicft.presentation.service.connection

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.prj.musicft.domain.model.Song
import com.prj.musicft.presentation.service.PlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicServiceConnection @Inject constructor(
    @ApplicationContext context: Context
) {
    private var mediaController: MediaController? = null
    private val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
    private val controllerFuture: ListenableFuture<MediaController> =
        MediaController.Builder(context, sessionToken).buildAsync()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
        
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // When media item changes, update currentSong from extras
            val song = mediaItem?.mediaMetadata?.extras?.getParcelable<Song>("SONG_KEY")
            if (song != null) {
                _currentSong.value = song
            }
        }
    }

    init {
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(playerListener)
            // Initial Sync
            _isPlaying.value = mediaController?.isPlaying == true
            
            // Sync current song if playing
            val currentItem = mediaController?.currentMediaItem
            val song = currentItem?.mediaMetadata?.extras?.getParcelable<Song>("SONG_KEY")
            if (song != null) {
                _currentSong.value = song
            }
        }, MoreExecutors.directExecutor())
    }

    fun playSong(song: Song) {
        val controller = mediaController ?: return
        
        // Update current song state
        _currentSong.value = song
        
        val extras = android.os.Bundle().apply {
            putParcelable("SONG_KEY", song)
        }

        val mediaItem = MediaItem.Builder()
            .setMediaId(song.filePath) // Using path as ID for now
            .setUri(song.filePath)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artistName)
                    .setAlbumTitle(song.albumName)
                    .setArtworkUri(android.net.Uri.parse(song.artworkUri ?: ""))
                    .setExtras(extras)
                    .build()
            )
            .build()

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    fun playPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }
    
    fun setShuffleMode(enabled: Boolean) {
        mediaController?.shuffleModeEnabled = enabled
    }

    fun setRepeatMode(mode: Int) {
         mediaController?.repeatMode = mode
    }
    
    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }
    
    fun getDuration(): Long {
        return mediaController?.duration ?: 0L
    }

    fun addSongToNext(song: Song) {
        val controller = mediaController ?: return
        
        val extras = android.os.Bundle().apply {
            putParcelable("SONG_KEY", song)
        }
        
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.filePath)
            .setUri(song.filePath)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artistName)
                    .setAlbumTitle(song.albumName)
                    .setArtworkUri(android.net.Uri.parse(song.artworkUri ?: ""))
                    .setExtras(extras)
                    .build()
            )
            .build()
            
        val nextIndex = if (controller.playbackState != Player.STATE_IDLE) {
            controller.currentMediaItemIndex + 1
        } else {
            0
        }
        
        controller.addMediaItem(nextIndex, mediaItem)
    }

    fun playQueue(songs: List<Song>) {
        val controller = mediaController ?: return
        if (songs.isEmpty()) return

        val mediaItems = songs.map { song ->
            val extras = android.os.Bundle().apply {
                putParcelable("SONG_KEY", song)
            }
            MediaItem.Builder()
                .setMediaId(song.filePath)
                .setUri(song.filePath)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artistName)
                        .setAlbumTitle(song.albumName)
                        .setArtworkUri(android.net.Uri.parse(song.artworkUri ?: ""))
                        .setExtras(extras)
                        .build()
                )
                .build()
        }

        controller.setMediaItems(mediaItems)
        controller.prepare()
        controller.play()
        
        // Update current song to the first one immediately for UI responsiveness
        _currentSong.value = songs.first()
    }

    // Helper to release if needed, though Singleton usually lives with App
    fun release() {
        MediaController.releaseFuture(controllerFuture)
    }
}

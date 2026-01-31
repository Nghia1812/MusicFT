package com.prj.musicft.presentation.service

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MusicSessionCallback @Inject constructor() :
        MediaLibraryService.MediaLibrarySession.Callback {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @OptIn(UnstableApi::class)
    override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        val updatedMediaItems = mediaItems.map { it.buildUpon().setUri(it.mediaId).build() }
        return Futures.immediateFuture(updatedMediaItems)
    }

    // TODO: Implement other callback methods like onGetLibraryRoot, onGetChildren, etc. for Android
    // Auto logic later
}

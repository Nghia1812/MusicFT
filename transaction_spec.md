# Music-FT Transaction Specification
 
---
 
## T1. App Launch → Permission Check
 
**Screen (A):** SplashScreen  
**User Action:** App is launched  
 
**Logic Steps:**
1. Check if READ_MEDIA_AUDIO permission is granted.
2. If NOT granted:
   - Emit State: PermissionRequired
   - Navigate to: PermissionRequestScreen
3. If granted:
   - Proceed to media scan (background async)
   - Emit State: MediaScanStarted
   - On scan complete, navigate to: HomeScreen
 
**Side Effects:**
- May trigger permission dialog (if required)
- Initiate media scan (async)
 
**Emitted Events/States:**
- PermissionRequired
- MediaScanStarted
- NavigationEvent: To PermissionRequestScreen or HomeScreen
 
---
 
## T2. Navigate to Search
 
**Screen (A):** HomeScreen  
**User Action:** User taps search text field  
 
**Logic Steps:**
1. Emit NavigationEvent: OpenSearch
 
**Side Effects:**
- None
 
**Emitted Events/States:**
- NavigationEvent: SearchScreen
 
---
 
## T3. Scroll to All Songs Section
 
**Screen (A):** HomeScreen  
**User Action:** User taps "See all" in Recently Added  
 
**Logic Steps:**
1. Scroll view to All Songs section
2. Emit ScrollPositionUpdated
 
**Side Effects:**
- None
 
**Emitted Events/States:**
- ScrollPositionUpdated
 
---
 
## T5. Open Song Options
 
**Screen (A):** HomeScreen  
**User Action:** User taps song menu button  
 
**Logic Steps:**
1. Load song metadata
2. Query DB for available playlists
3. Emit SongMetadataLoaded and PlaylistsLoaded
 
**Side Effects:**
- None
 
**Emitted Events/States:**
- SongMetadataLoaded
- PlaylistsLoaded
- NavigationEvent: SongDetailOptionModal
 
---
 
## T6. Play Next
 
**Screen (A):** SongDetailOptionModal  
**User Action:** User taps "Play Next"  
 
**Logic Steps:**
1. Insert selected song at next position in playback queue
2. Update playback queue state
 
**Side Effects:**
- Updates playback queue in memory/service
 
**Emitted Events/States:**
- PlaybackQueueUpdated
- ModalClosed
- NavigationEvent: Return to HomeScreen
 
---
 
## T7. Add Song to Playlist
 
**Screen (A):** SongDetailOptionModal  
**User Action:** User taps "Add to Playlist"  
 
**Logic Steps:**
1. Query local database for available playlists.
2. Show playlist selection dialog/modal.
3. Await user selection:
    - If existing playlist:
        a. Insert cross-ref record (songId, playlistId) in DB.
        b. On success: Emit PlaylistSongAdded.
        c. On failure: Emit ErrorState (with reason).
    - If "Create New Playlist":
        a. Show create playlist dialog.
        b. Validate playlist name.
        c. Persist new playlist in DB.
        d. Insert cross-ref record (songId, newPlaylistId).
        e. On success: Emit PlaylistCreatedAndSongAdded.
        f. On failure: Emit ErrorState.
4. Show confirmation message (optional).
 
**Side Effects:**
- Updates playlist and playlist-song cross-ref tables in DB.
- May show error dialog or toast on failure.
 
**Emitted Events/States:**
- PlaylistSongAdded
- PlaylistCreatedAndSongAdded
- ErrorState
- NavigationEvent: Close Modal, return to HomeScreen (optional)
 
---
 
## T8. Add to Favorites
 
**Screen (A):** SongDetailOptionModal  
**User Action:** User taps "Add to Favorites"  
 
**Logic Steps:**
1. Set song.isFavorite = true in local DB.
2. On success:
    - Emit SongFavoriteUpdated (with updated song data)
    - Optionally, update UI optimistically
3. On failure:
    - Emit ErrorState (with reason)
    - Optionally, revert UI change
 
**Side Effects:**
- Updates song table in DB
- Shows snackbar/toast on success or failure
 
**Emitted Events/States:**
- SongFavoriteUpdated
- ErrorState
- NavigationEvent: Close Modal, return to HomeScreen (UI reflects favorite status)
 
---
 
## T11. Remove from Favorite
 
**Screen (A):** SongDetailOptionModal  
**User Action:** User taps "Remove from Favorite"  
 
**Logic Steps:**
1. Show confirmation dialog
2. If user confirms:
    a. Remove song from favorites in DB
    b. Emit LibraryUpdated
3. If user cancels:
    a. Emit ModalClosed
 
**Side Effects:**
- Updates favorites table in DB
 
**Emitted Events/States:**
- LibraryUpdated
- ModalClosed
- NavigationEvent: Return to HomeScreen
 
---
 
## T12. Select Song to Play
 
**Screen (A):** HomeScreen or CollectionSongListScreen  
**User Action:** User taps on a song  
 
**Logic Steps:**
1. ViewModel receives PlaySong(songId) event.
2. Prepare Media3 player with selected song.
3. Update playback queue and state (current song, queue position).
4. Emit PlaybackState (playing, paused, etc.)
5. Show SongPlayerOverlay (mini/full player).
 
**Side Effects:**
- Starts playback service if not running
- Updates playback history in DB
 
**Emitted Events/States:**
- PlaybackState
- Show SongPlayerOverlay
- PlaybackHistoryUpdated
 
---
 
## T13. Control Playback (Play / Pause)
 
**Screen (A):** SongPlayerOverlay  
**User Action:** User taps Play / Pause button  
 
**Logic Steps:**
1. Toggle playback state in Media3 player
2. Emit PlaybackStateUpdated
 
**Side Effects:**
- Updates playback state
 
**Emitted Events/States:**
- PlaybackStateUpdated
 
---
You sent
## T14. Skip Track (Next / Previous)
 
**Screen (A):** SongPlayerOverlay  
**User Action:** User taps Next or Previous  
 
**Logic Steps:**
1. Move player to next/previous song in queue
2. Update current song info
3. Add entry to playback history in DB
 
**Side Effects:**
- Updates playback queue and history
 
**Emitted Events/States:**
- PlaybackStateUpdated
- PlaybackHistoryUpdated
 
---
 
## T15. Background Playback
 
**Screen (A):** Any Screen  
**User Action:** User leaves app or locks device  
 
**Logic Steps:**
1. Ensure playback continues via foreground service
2. MediaSession maintains playback controls
 
**Side Effects:**
- Starts/maintains foreground playback service
 
**Emitted Events/States:**
- PlaybackContinuesInBackground
- MediaSessionActive
 
---
 
## T16. View Collection (Favorite/Playlist/Album/Artist)
 
**Screen (A):** LibraryScreen  
**User Action:** User taps a playlist/album/artist  
 
**Logic Steps:**
1. Query DB for collection songs
2. Observe for collection updates
3. Emit CollectionSongsLoaded
 
**Side Effects:**
- None
 
**Emitted Events/States:**
- CollectionSongsLoaded
- NavigationEvent: CollectionSongListScreen
 
---
 
## T17. Create Playlist
 
**Screen (A):** SongDetailOptionModal (within Add to Playlist flow)  
**User Action:** User taps "Create Playlist"  
 
**Logic Steps:**
1. Show create playlist dialog
2. Await user input
3. Validate playlist name
4. If valid:
    a. Persist new playlist in DB
    b. Add current song to new playlist
    c. Emit PlaylistCreatedAndSongAdded
5. If invalid:
    a. Emit ErrorState (with reason)
 
**Side Effects:**
- Updates playlists and playlist-song tables in DB
 
**Emitted Events/States:**
- PlaylistCreatedAndSongAdded
- ErrorState
- ConfirmationMessage (optional)
- ModalClosed
- NavigationEvent: Return to originating screen
 
---
 
## T18. View Collection Content (Favorites/History/Playlist)
 
**Screen (A):** LibraryScreen  
**User Action:** User taps "Favorites", "History", or other collection  
 
**Logic Steps:**
1. Query appropriate table (favorites/history/playlists)
2. Sort results (history: timestamp DESC, etc.)
3. Emit CollectionSongsLoaded
 
**Side Effects:**
- None
 
**Emitted Events/States:**
- CollectionSongsLoaded
- NavigationEvent: CollectionSongListScreen
 
---
 
## T19. Search Local Songs
 
**Screen (A):** SearchScreen  
**User Action:** User types keyword  
 
**Logic Steps:**
1. Query local DB using LIKE or FTS with search keyword
2. If results found:
    a. Emit FilteredSongList
3. If no results:
    a. Emit EmptySearch
 
**Side Effects:**
- None
 
**Emitted Events/States:**
- FilteredSongList
- EmptySearch
 
---
 
## T20. Select Song from Search
 
**Screen (A):** SearchScreen  
**User Action:** User taps a song from search results  
 
**Logic Steps:**
1. ViewModel sends PlaySong(songId)
2. Prepare Media3 player with selected song
3. Update playback state
 
**Side Effects:**
- Starts playback, updates playback state
 
**Emitted Events/States:**
- PlaybackState
- ShowSongPlayerOverlay
- RemainOnSearchScreen
 
---
 
## T21. Change Theme
 
**Screen (A):** SettingsScreen  
**User Action:** User selects Light / Dark / System theme  
 
**Logic Steps:**
1. Persist new theme preference in local storage.
2. Emit ThemeChanged (with theme value).
3. Trigger UI recomposition to apply theme immediately.
 
**Side Effects:**
- Updates user settings/preferences table or DataStore
 
**Emitted Events/States:**
- ThemeChanged
- UIThemeRecomposition
 
---
 
## T22. Navigate to Settings
 
**Screen (A):** HomeScreen or LibraryScreen  
**User Action:** User taps Settings icon  
 
**Logic Steps:**
1. Emit NavigationEvent: OpenSettings
 
**Side Effects:**
- None
 
**Emitted Events/States:**
- NavigationEvent: SettingsScreen
 
---
 
## T23. Switch Library Views
 
**Screen (A):** LibraryScreen  
**User Action:** User switches between tabs (Songs/Albums/Artists/Playlists)  
 
**Logic Steps:**
1. Update selected tab state
2. Query and load appropriate data for tab
3. Emit TabContentUpdated
 
**Side Effects:**
- None
 
**Emitted Events/States:**
- TabContentUpdated
 
---
 
## T26. Permission Denied with Rationale
 
**Screen (A):** PermissionRequestScreen  
**User Action:** User denies permission (but rationale available)  
 
**Logic Steps:**
1. Detect shouldShowRationale == true
2. Show rationale dialog
3. On user confirmation, retry permission request
 
**Side Effects:**
- May trigger permission dialog again
 
**Emitted Events/States:**
- RationaleDialogShown
- PermissionRequestRetried
 
---
 
## T27. Media Scan Failure or Empty Library
 
**Screen (A):** SplashScreen or HomeScreen  
**User Action:** App launches and media scan runs (implicit)  
 
**Logic Steps:**
1. Start media scan (async).
2. If scan fails (e.g., storage error, permission revoked):
    - Emit ErrorState: MediaScanFailed (with reason)
    - Show error dialog or empty state UI
    - Optionally, prompt user to retry or check permissions
3. If scan succeeds but no media found:
    - Emit State: NoMediaFound
    - Show empty library UI with guidance (e.g., "No music found. Add music to your device.")
 
**Side Effects:**
- None (unless user chooses to retry)
 
**Emitted Events/States:**
- MediaScanFailed
- NoMediaFound
- ErrorDialogShown
- EmptyLibraryUI
 
---
 
## T28. Playback Error Handling
 
**Screen (A):** SongPlayerOverlay (any playback context)  
**User Action:** Playback starts or continues (implicit)  
 
**Logic Steps:**
1. Media3 player fails to play song (corrupt/unsupported).
2. Emit PlaybackError event (with error reason and song info).
3. Optionally, auto-skip to next track in queue.
4. Show error snackbar/toast to user.
 
**Side Effects:**
- Optionally updates playback queue (skip)
 
**Emitted Events/States:**
- PlaybackError
- PlaybackStateUpdated (if skipping)
- ErrorSnackbarShown
 
---
 
## T29. Delete Playlist
 
**Screen (A):** CollectionSongListScreen or LibraryScreen  
**User Action:** User selects "Delete Playlist" from options  
 
**Logic Steps:**
1. Show confirmation dialog.
2. If user confirms:
    a. Delete playlist and associated song cross-refs from DB.
    b. Emit PlaylistDeleted.
3. If user cancels:
    a. Emit ModalClosed.
 
**Side Effects:**
- Updates playlist and playlist-song tables in DB
 
**Emitted Events/States:**
- PlaylistDeleted
- ModalClosed
- LibraryUpdated
 
---
 
## T30. Clear Listening History
 
**Screen (A):** LibraryScreen (History Tab)  
**User Action:** User taps "Clear History"  
 
**Logic Steps:**
1. Show confirmation dialog.
2. If user confirms:
    a. Delete all entries from history table.
    b. Emit HistoryCleared.
3. If user cancels:
    a. Emit ModalClosed.
 
**Side Effects:**
- Updates history table in DB
 
**Emitted Events/States:**
- HistoryCleared
- ModalClosed
- LibraryUpdated
 
---
 
| Screen                    | Transactions                                                                                  |
|---------------------------|----------------------------------------------------------------------------------------------|
| SplashScreen              | T1, T27                                                                                      |
| PermissionRequestScreen   | T1, T26                                                                                      |
| HomeScreen                | T1, T2, T3, T5, T12, T22, T27                                                                |
| SongDetailOptionModal     | T5, T6, T7, T8, T11, T17                                                                     |
| LibraryScreen             | T16, T18, T22, T23, T29, T30                                                                 |
| CollectionSongListScreen  | T12, T29                                                                                     |
| SearchScreen              | T2, T19, T20                                                                                 |
| SongPlayerOverlay         | T12, T13, T14, T15, T28                                                                      |
| SettingsScreen            | T21                                                                                          |
| Any Screen                | T15                                                                                          |
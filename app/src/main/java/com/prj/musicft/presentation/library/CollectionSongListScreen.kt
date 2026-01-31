package com.prj.musicft.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prj.musicft.domain.model.Song
import com.prj.musicft.presentation.common.SongListItem
import com.prj.musicft.presentation.common.UiState
import com.prj.musicft.presentation.home.AddToPlaylistDialog
import com.prj.musicft.presentation.home.CreatePlaylistDialog
import com.prj.musicft.presentation.home.SongDetailOptionModal
import com.prj.musicft.presentation.theme.CyberpunkTeal
import com.prj.musicft.presentation.theme.DarkBackground
import com.prj.musicft.presentation.theme.NeonGradientEnd
import com.prj.musicft.presentation.theme.NeonGradientStart
import com.prj.musicft.presentation.theme.SurfaceSlate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionSongListScreen(
    onNavigateUp: () -> Unit,
    onSongClick: (Song) -> Unit,
    viewModel: CollectionSongListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // --- Dialog/Modal State ---
    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }
    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var isCreatingPlaylist by remember { mutableStateOf(false) }
    
    val playlists by viewModel.playlists.collectAsState()
    val addedPlaylistIds by viewModel.playlistsContainingSong.collectAsState()

    // --- Song Option Modal ---
    if (selectedSongForOptions != null) {
        val song = selectedSongForOptions!!
        SongDetailOptionModal(
            song = song,
            onDismissRequest = { selectedSongForOptions = null },
            onPlayNext = {
                viewModel.onPlayNext(song)
                selectedSongForOptions = null
            },
            onAddToPlaylist = {
                viewModel.onAddToPlaylist(song) // Fetches status
                songToAddToPlaylist = song
                selectedSongForOptions = null
            },
            onAddToFavorites = {
                viewModel.onAddToFavorites(song)
                selectedSongForOptions = null
            },
            onShareSong = {
                viewModel.onShareSong(song)
                selectedSongForOptions = null
            },
            onGoToAlbum = {
                viewModel.onGoToAlbum(song)
                selectedSongForOptions = null
            },
            onRemoveFromLibrary = {
                viewModel.onRemoveFromLibrary(song)
                selectedSongForOptions = null
            }
        )
    }

    // --- Add To Playlist / Create Playlist Dialogs ---
    if (songToAddToPlaylist != null) {
        if (isCreatingPlaylist) {
            CreatePlaylistDialog(
                onConfirm = { name ->
                    viewModel.createPlaylist(name, songToAddToPlaylist)
                    isCreatingPlaylist = false
                    songToAddToPlaylist = null
                },
                onDismissRequest = { isCreatingPlaylist = false }
            )
        } else {
            AddToPlaylistDialog(
                song = songToAddToPlaylist!!,
                playlists = playlists,
                addedPlaylistIds = addedPlaylistIds,
                onPlaylistClick = { playlist ->
                    val isAdded = addedPlaylistIds.contains(playlist.id)
                    viewModel.toggleSongInPlaylist(playlist, songToAddToPlaylist!!, isAdded)
                },
                onCreateNewClick = { isCreatingPlaylist = true },
                onDismissRequest = { songToAddToPlaylist = null }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collection", color = Color.White) }, // Ideally dynamic title
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground,
        floatingActionButton = {
            if (uiState is UiState.Success) {
                FloatingActionButton(
                    onClick = { viewModel.onPlayAll() },
                    containerColor = CyberpunkTeal,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play All", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = CyberpunkTeal
                    )
                }
                is UiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Empty -> {
                    Text(
                        text = "No songs found in this collection.",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 100.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header with gradient or visual
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                NeonGradientStart.copy(alpha = 0.3f),
                                                DarkBackground
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Text(
                                    text = "${state.data.size} Songs",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        items(state.data) { song ->
                            SongListItem(
                                song = song,
                                onClick = { onSongClick(song) },
                                onOptionClick = { selectedSongForOptions = song }
                            )
                        }
                    }
                }
                UiState.Idle -> {}
            }
        }
    }
}

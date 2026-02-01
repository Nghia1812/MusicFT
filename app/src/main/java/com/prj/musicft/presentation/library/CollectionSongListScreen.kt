package com.prj.musicft.presentation.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.prj.musicft.R
import com.prj.musicft.domain.model.Song
import com.prj.musicft.presentation.common.SongListItem
import com.prj.musicft.presentation.common.UiState
import com.prj.musicft.presentation.home.AddToPlaylistDialog
import com.prj.musicft.presentation.home.CreatePlaylistDialog
import com.prj.musicft.presentation.home.SongDetailOptionModal
import com.prj.musicft.presentation.theme.CyberpunkTeal
import com.prj.musicft.presentation.theme.DarkBackground


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionSongListScreen(
    onNavigateUp: () -> Unit,
    onSongClick: (Song) -> Unit,
    viewModel: CollectionSongListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val title by viewModel.title.collectAsState()
    
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 100.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header Section
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp)
                            ) {
                                Text(
                                    text = "YOUR COLLECTION",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = CyberpunkTeal
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${state.data.size} Tracks",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.onPlayAll() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyberpunkTeal,
                                            contentColor = Color.White // Text color on Teal
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Play All",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { /* TODO: Shuffle Logic */ viewModel.onPlayAll() }, // Assuming Shuffle is play all for now or check viewModel
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color.White
                                        ),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_shuffle),
                                            tint = Color.White.copy(alpha = 0.2f),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Shuffle",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
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

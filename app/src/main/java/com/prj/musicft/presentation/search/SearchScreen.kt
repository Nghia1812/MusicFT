package com.prj.musicft.presentation.search

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prj.musicft.domain.model.Song
import com.prj.musicft.presentation.common.SongListItem
import com.prj.musicft.presentation.common.UiState
import com.prj.musicft.presentation.home.AddToPlaylistDialog
import com.prj.musicft.presentation.home.CreatePlaylistDialog
import com.prj.musicft.presentation.home.SongDetailOptionModal
import com.prj.musicft.presentation.theme.CyberpunkTeal
import com.prj.musicft.presentation.theme.DarkBackground
import com.prj.musicft.presentation.theme.SurfaceSlate

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(), onSongClick: (Song) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val playlists by viewModel.playlists.collectAsState()
    val addedPlaylistIds by viewModel.playlistsContainingSong.collectAsState()

    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }
    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var isCreatingPlaylist by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

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
                viewModel.onAddToPlaylist(song)
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
            })
    }

    if (songToAddToPlaylist != null) {
        if (isCreatingPlaylist) {
            CreatePlaylistDialog(onConfirm = { name ->
                viewModel.createPlaylist(name, songToAddToPlaylist)
                isCreatingPlaylist = false
                songToAddToPlaylist = null
            }, onDismissRequest = { isCreatingPlaylist = false })
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
                onDismissRequest = { songToAddToPlaylist = null })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        // 1. Header with "Search" and Profile Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = {
                Text(
                    text = "Search songs, artists...",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            trailingIcon = {

                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.Gray
                    )
                }

            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceSlate),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceSlate,
                unfocusedContainerColor = SurfaceSlate,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = CyberpunkTeal,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Results Section
        if (uiState !is UiState.Idle) {
            Text(
                text = "TOP RESULTS",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        color = CyberpunkTeal, modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 50.dp), // Adjust for visual balance
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Play what you love",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Search for songs, artists, and playlists",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                is UiState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.3f), // Subtle dark circle
                                    shape = CircleShape
                                )
                        ) {
                           Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        color = Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .border(2.dp, CyberpunkTeal, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = CyberpunkTeal,
                                    modifier = Modifier.size(48.dp)
                                )
                                // Cross "x" overlay
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = CyberpunkTeal,
                                    modifier = Modifier.size(24.dp).offset(x = 12.dp, y = 12.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Try searching for something\nelse or explore new genres.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.clearFilters() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CyberpunkTeal
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberpunkTeal),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "CLEAR ALL FILTERS",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 100.dp), // Space for MiniPlayer/BottomNav
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data) { song ->
                            SongListItem(
                                song = song,
                                onClick = { onSongClick(song) },
                                onOptionClick = { selectedSongForOptions = song })
                        }
                    }
                }
            }
        }
    }
}

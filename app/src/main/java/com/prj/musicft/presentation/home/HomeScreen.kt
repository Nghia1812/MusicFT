package com.prj.musicft.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.prj.musicft.domain.model.Song
import com.prj.musicft.presentation.common.UiState
import com.prj.musicft.presentation.common.SongListItem
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.prj.musicft.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSongClick: (Song) -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }
    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var isCreatingPlaylist by remember { mutableStateOf(false) }
    val playlists by viewModel.playlists.collectAsState()
    val addedPlaylistIds by viewModel.playlistsContainingSong.collectAsState()

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

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { HomeTopBar() }, containerColor = MaterialTheme.colorScheme.background) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is UiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_songs_found),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is UiState.Success -> {
                HomeContent(
                    songs = state.data,
                    padding = padding,
                    onSongClick = { song ->
                        viewModel.recordListen(song)
                        onSongClick(song)
                    },
                    onSongOptionsClick = { song -> selectedSongForOptions = song },
                    onSearchClick = onSearchClick
                )
            }

            UiState.Idle -> {
                // Do nothing
            }
        }
    }
}

@Composable
fun HomeContent(
    songs: List<Song>,
    padding: PaddingValues,
    onSongClick: (Song) -> Unit,
    onSongOptionsClick: (Song) -> Unit,
    onSearchClick: () -> Unit
) {
    // Sort for "Recently Added"
    val recentlyAdded = remember(songs) { songs.sortedBy { it.addedAt }.take(5) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Search Bar
        item { SearchBar(onClick = onSearchClick) }

        // Recently Added Section
        item {
            SectionHeader(
                title = stringResource(R.string.recently_added),
                actionText = stringResource(R.string.see_all),
                onActionClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(2)
                    }
                }
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) { items(recentlyAdded) { song -> RecentlyAddedItem(song = song) } }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // All Songs Section
        item { SectionHeader(title = stringResource(R.string.all_songs), actionText = null) }

        items(songs) { song ->
            SongListItem(
                song = song,
                onClick = { onSongClick(song) },
                onOptionClick = { onSongOptionsClick(song) }
            )
        }
    }
}

@Composable
fun HomeTopBar() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.discovery),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SearchBar(onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String?, onActionClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        if (actionText != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onActionClick?.invoke() }
            )
        }
    }
}

@Composable
fun RecentlyAddedItem(song: Song) {
    Column(modifier = Modifier.width(140.dp)) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.size(140.dp)) {
            AsyncImage(
                model = song.artworkUri ?: android.R.drawable.ic_menu_gallery,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
        Text(
            text = song.artistName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}



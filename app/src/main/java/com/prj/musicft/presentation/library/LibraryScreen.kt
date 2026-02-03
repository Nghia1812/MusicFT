package com.prj.musicft.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite

import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.vectorResource
import com.prj.musicft.R
import androidx.compose.ui.res.stringResource
import com.prj.musicft.domain.model.Song
import com.prj.musicft.presentation.common.SongListItem
import com.prj.musicft.presentation.common.UiState


enum class CollectionType {
    Favorites, Playlists, Albums, Artists, History
}

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onNavigateToCollection: (CollectionType) -> Unit,
    onSongClick: (Song) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { LibraryTopBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is UiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Success -> {
                    LibraryContent(
                        data = state.data,
                        onNavigateToCollection = onNavigateToCollection,
                        onSongClick = onSongClick
                    )
                }
                UiState.Idle -> {}
                UiState.Empty -> {}
            }
        }
    }
}

@Composable
fun LibraryContent(
    data: LibraryData,
    onNavigateToCollection: (CollectionType) -> Unit,
    onSongClick: (Song) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Grid of Cards
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LibraryCard(
                        title = stringResource(R.string.favorites),
                        count = stringResource(R.string.tracks_count, data.favoriteCount),
                        icon = Icons.Default.Favorite,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCollection(CollectionType.Favorites) }
                    )
                    LibraryCard(
                        title = stringResource(R.string.playlists),
                        count = stringResource(R.string.playlists_created_count, data.playlistCount),
                        icon = Icons.AutoMirrored.Filled.List,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCollection(CollectionType.Playlists) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LibraryCard(
                        title = stringResource(R.string.albums),
                        count = stringResource(R.string.albums_saved_count, data.albumCount),
                        icon = ImageVector.vectorResource(R.drawable.ic_album),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCollection(CollectionType.Albums) }
                    )
                    LibraryCard(
                        title = stringResource(R.string.artists),
                        count = stringResource(R.string.artists_following_count, data.artistCount),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCollection(CollectionType.Artists) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Recently Played Header
        item {
            LibrarySectionHeader(
                title = stringResource(R.string.recently_played),
                actionText = stringResource(R.string.see_all),
                onActionClick = { onNavigateToCollection(CollectionType.History) }
            )
        }

        // Recently Played List
        items(data.recentHistory) { song ->
            SongListItem(
                song = song,
                onClick = { onSongClick(song) },
                onOptionClick = { /* TODO: Open options */ }
            )
        }
    }
}

@Composable
fun LibraryTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.library),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun LibraryCard(
    title: String,
    count: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconBgColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = count,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LibrarySectionHeader(title: String, actionText: String?, onActionClick: (() -> Unit)? = null) {
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

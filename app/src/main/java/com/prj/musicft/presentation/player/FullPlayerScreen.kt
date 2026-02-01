package com.prj.musicft.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.prj.musicft.domain.model.RepeatMode
import com.prj.musicft.domain.model.Song
import androidx.compose.ui.res.vectorResource
import com.prj.musicft.R

import com.prj.musicft.presentation.common.UiState

@Composable
fun FullPlayerScreen(
    onCollapse: () -> Unit,
    viewModel: FullPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is UiState.Empty -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                         text = "No song playing",
                         style = MaterialTheme.typography.titleMedium,
                         color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onCollapse) {
                         Text("Back")
                    }
                }
            }
        }
        is UiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                     text = state.message,
                     color = MaterialTheme.colorScheme.error,
                     modifier = Modifier.padding(16.dp)
                )
            }
        }
        is UiState.Success -> {
            // We assume that if we are in Success state, we have a song.
            // However, FullPlayerUiState.song is nullable.
            // If it's null, we can treat it as Loading or Empty, but theoretically
            // our ViewModel only emits Success when a song is loaded.
            val song = state.data.song
            if (song != null) {
                FullPlayerContent(
                    state = state.data,
                    song = song,
                    onCollapse = onCollapse,
                    viewModel = viewModel
                )
            } else {
                // Fallback should not happen based on ViewModel logic
                 Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        UiState.Idle -> {
            // Do nothing
        }
    }
}

@Composable
fun FullPlayerContent(
    state: FullPlayerUiState,
    song: Song,
    onCollapse: () -> Unit,
    viewModel: FullPlayerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            IconButton(onClick = { /* More Options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // --- Artwork ---
        Card(
            modifier = Modifier
                .size(300.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
             AsyncImage(
                model = song.artworkUri ?: android.R.drawable.ic_menu_gallery, // Fallback
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // --- Title & Artist Checking ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall, // H5 equivalent nearby
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary, // Artist gets accent color
                    maxLines = 1
                )
            }
            
            IconButton(onClick = viewModel::onFavoriteClick) {
                Icon(
                    imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (state.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Seekbar ---
        Column {
            Slider(
                value = state.currentPosition.toFloat(),
                onValueChange = { viewModel.onSeek(it.toLong()) },
                valueRange = 0f..state.duration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(state.currentPosition),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDuration(state.duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Controls ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = viewModel::onShuffleClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_shuffle),
                    contentDescription = "Shuffle",
                    tint = if (state.isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Prev
            IconButton(
                onClick = viewModel::onSkipPrevious,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_previous),
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Play/Pause (Big)
            FilledIconButton(
                onClick = viewModel::onPlayPauseClick,
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (state.isPlaying) ImageVector.vectorResource(R.drawable.ic_pause) else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Next
            IconButton(
                onClick = viewModel::onSkipNext,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_next),
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Repeat
            IconButton(onClick = viewModel::onRepeatClick) {
                 val icon = when(state.repeatMode) {
                     RepeatMode.ONE -> ImageVector.vectorResource(R.drawable.ic_replay)
                     else -> ImageVector.vectorResource(R.drawable.ic_replay)
                 }
                 val tint = if (state.repeatMode == RepeatMode.OFF) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                 
                Icon(
                    imageVector = icon,
                    contentDescription = "Repeat",
                    tint = tint
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // --- Bottom Sheet / Queue Hint (Optional for Phase 1, but nice visual) ---
        // Maybe just a "Lyrics" or "Up Next" button
    }
}

// Helper to format MM:SS
fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

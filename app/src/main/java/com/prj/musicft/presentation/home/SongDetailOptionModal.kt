package com.prj.musicft.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.res.vectorResource
import com.prj.musicft.R
import com.prj.musicft.domain.model.Song
import com.prj.musicft.presentation.theme.*
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailOptionModal(
    song: Song,
    onDismissRequest: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onAddToFavorites: () -> Unit,
    onShareSong: () -> Unit,
    onGoToAlbum: () -> Unit,
    onRemoveFromLibrary: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant, width = 40.dp, height = 4.dp) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.artworkUri ?: android.R.drawable.ic_menu_gallery,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = song.artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            OptionItem(
                icon = Icons.Default.PlayArrow,
                text = stringResource(R.string.play_next),
                color = MaterialTheme.colorScheme.primary,
                onClick = onPlayNext
            )
            OptionItem(
                icon = Icons.Default.Add,
                text = stringResource(R.string.add_to_playlist),
                color = MaterialTheme.colorScheme.primary,
                onClick = onAddToPlaylist
            )
            OptionItem(
                icon = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                text = stringResource(R.string.add_to_favorites),
                color = MaterialTheme.colorScheme.primary,
                onClick = onAddToFavorites
            )
            OptionItem(
                icon = Icons.Default.Share,
                text = stringResource(R.string.share_song),
                color = MaterialTheme.colorScheme.primary,
                onClick = onShareSong
            )
            OptionItem(
                icon = ImageVector.vectorResource(R.drawable.ic_album),
                text = stringResource(R.string.go_to_album),
                color = MaterialTheme.colorScheme.primary,
                onClick = onGoToAlbum
            )
            
            // Spacer/Divider if needed? 
            Spacer(modifier = Modifier.height(16.dp))
            
            OptionItem(
                icon = Icons.Default.Delete,
                text = stringResource(R.string.remove_from_library),
                color = MaterialTheme.colorScheme.error,
                onClick = onRemoveFromLibrary
            )
        }
    }
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (color == MaterialTheme.colorScheme.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

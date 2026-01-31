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
import com.prj.musicft.domain.model.Song
import com.prj.musicft.presentation.theme.*

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
        containerColor = SurfaceSlate,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray, width = 40.dp, height = 4.dp) }
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
                        color = LightText
                    )
                    Text(
                        text = song.artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            OptionItem(
                icon = Icons.Default.PlayArrow,
                text = "Play Next",
                color = CyberpunkTeal,
                onClick = onPlayNext
            )
            OptionItem(
                icon = Icons.Default.Add,
                text = "Add to Playlist",
                color = CyberpunkTeal,
                onClick = onAddToPlaylist
            )
            OptionItem(
                icon = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                text = "Add to Favorites",
                color = CyberpunkTeal,
                onClick = onAddToFavorites
            )
            OptionItem(
                icon = Icons.Default.Share,
                text = "Share Song",
                color = CyberpunkTeal,
                onClick = onShareSong
            )
            OptionItem(
                icon = Icons.Default.Star, //TODO: Replace with icons manually
                text = "Go to Album",
                color = CyberpunkTeal,
                onClick = onGoToAlbum
            )
            
            // Spacer/Divider if needed? 
            Spacer(modifier = Modifier.height(16.dp))
            
            OptionItem(
                icon = Icons.Default.Delete,
                text = "Remove from Library",
                color = ErrorRed,
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
            color = if (color == ErrorRed) ErrorRed else LightText
        )
    }
}

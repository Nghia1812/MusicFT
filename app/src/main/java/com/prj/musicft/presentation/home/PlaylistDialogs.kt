package com.prj.musicft.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.prj.musicft.domain.model.Playlist
import com.prj.musicft.domain.model.Song
import com.prj.musicft.presentation.theme.CyberpunkTeal
import com.prj.musicft.presentation.theme.SurfaceSlate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistDialog(
    song: Song,
    playlists: List<Playlist>,
    addedPlaylistIds: Set<Long>,
    onPlaylistClick: (Playlist) -> Unit,
    onCreateNewClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = SurfaceSlate,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray, width = 40.dp, height = 4.dp) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Add to Playlist",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Song Preview Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = song.artworkUri ?: android.R.drawable.ic_menu_gallery,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.DarkGray, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 1,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${song.artistName} • ${formatDuration(song.duration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = CyberpunkTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // New Playlist Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCreateNewClick)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = CyberpunkTeal
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "New Playlist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Create a fresh collection",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "YOUR PLAYLISTS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.0.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Playlists List
            LazyColumn(
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                items(playlists) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        isAdded = addedPlaylistIds.contains(playlist.id),
                        onClick = { onPlaylistClick(playlist) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Create Button (Bottom) - kept for consistency with design, could just rely on list item
            Button(
                onClick = onCreateNewClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberpunkTeal,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Create New Playlist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PlaylistItem(playlist: Playlist, isAdded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder for playlist art
        Box(
             modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
             contentAlignment = Alignment.Center
        ) {
             Icon(
                 // Use a generic music icon or the first letter
                 imageVector = Icons.Default.AccountCircle, // TODO: Placeholder
                 contentDescription = null,
                 tint = Color.Gray
             )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isAdded) CyberpunkTeal else Color.White, // Highlight name if added
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${playlist.songCount} tracks",
                style = MaterialTheme.typography.bodySmall,
                color = CyberpunkTeal
            )
        }
        
        Icon(
            imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
            contentDescription = null,
            tint = if (isAdded) CyberpunkTeal else Color.Gray
        )
    }
}

// Helper
private fun formatDuration(durationMs: Long): String {
    val minutes = (durationMs / 1000) / 60
    val seconds = (durationMs / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
}

// CreatePlaylistDialog remains the same (as a standard Dialog) but styling should be consistent if needed.
// For now, keeping it as is since request focused on the main sheet.
@Composable
fun CreatePlaylistDialog(
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "New Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                         focusedTextColor = Color.White,
                         unfocusedTextColor = Color.White,
                         focusedBorderColor = CyberpunkTeal,
                         unfocusedBorderColor = Color.Gray,
                         focusedLabelColor = CyberpunkTeal,
                         unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            if (playlistName.isNotBlank()) {
                                onConfirm(playlistName)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberpunkTeal),
                        enabled = playlistName.isNotBlank()
                    ) {
                        Text("Create", color = Color.Black)
                    }
                }
            }
        }
    }
}

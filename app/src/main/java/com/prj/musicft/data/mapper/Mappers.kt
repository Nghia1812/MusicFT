package com.prj.musicft.data.mapper

import com.prj.musicft.data.local.entity.*
import com.prj.musicft.domain.model.*

fun SongWithDetails.toDomain(): Song {
    return Song(
            id = song.id,
            title = song.title,
            artistId = 0,
            artistName = "",
            albumId = 0,
            albumName = "",
            duration = song.duration,
            filePath = song.filePath,
            artworkUri = song.artworkUri,
            isFavorite = song.isFavorite,
            trackNumber = song.trackNumber,
            year = song.year,
            genre = song.genre,
            addedAt = song.addedAt
    )
}

// Fallback mapper for when we only have the Entity (e.g. from search results if not using joins)
// Note: This produces "Unknown" for names if not provided, ideal to use SongWithDetails always.
fun SongEntity.toDomain(
        artistName: String = "Unknown Artist",
        albumName: String = "Unknown Album"
): Song {
    return Song(
            id = id,
            title = title,
            artistId = artistId,
            artistName = artistName,
            albumId = albumId,
            albumName = albumName,
            duration = duration,
            filePath = filePath,
            artworkUri = artworkUri,
            isFavorite = isFavorite,
            trackNumber = trackNumber,
            year = year,
            genre = genre,
            addedAt = addedAt
    )
}

fun AlbumEntity.toDomain(artistName: String = "Unknown Artist"): Album {
    return Album(
            id = id,
            name = name,
            artistId = artistId,
            artistName = artistName, // Should be filled by join
            artworkUri = artworkUri,
            year = year,
            trackCount = trackCount
    )
}

fun AlbumWithSongs.toDomain(): Album {
    // This maps the Album entity itself, but logic might need to handle the list separately
    // depending on the UseCase. Here we map the core Album data.
    return album.toDomain(
            "Unknown Artist"
    ) // Limitation: AlbumWithSongs doesn't include ArtistEntity directly in relation
}

fun ArtistEntity.toDomain(): Artist {
    return Artist(
            id = id,
            name = name,
            albumCount = 0, // Need to compute
            songCount = 0 // Need to compute
    )
}

fun ArtistWithAlbums.toDomain(): Artist {
    return Artist(
            id = artist.id,
            name = artist.name,
            albumCount = albums.size,
            songCount = 0 // Still unknown
    )
}

fun PlaylistEntity.toDomain(songCount: Int = 0): Playlist {
    return Playlist(id = id, name = name, songCount = songCount, createdAt = createdAt)
}

fun PlaylistWithSongs.toDomain(): Playlist {
    return Playlist(
            id = playlist.id,
            name = playlist.name,
            songCount = songs.size,
            createdAt = playlist.createdAt
    )
}

fun HistoryWithSong.toDomain(): HistoryEntry {
    return HistoryEntry(
            id = history.id,
            songId = history.songId,
            playedAt = history.playedAt,
            song = song.toDomain() // Using basic mapper, names might be "Unknown" if not strictly
            // joined with artist/album
            )
}

fun AppSettingsEntity.toDomain(): AppSettings {
    return AppSettings(
            themeMode = themeMode,
            useDynamicColor = useDynamicColor,
            shuffleEnabled = shuffleEnabled,
            repeatMode = repeatMode
    )
}

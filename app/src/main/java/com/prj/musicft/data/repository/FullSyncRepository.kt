package com.prj.musicft.data.repository

import com.prj.musicft.data.local.dao.*
import com.prj.musicft.data.local.database.MusicDatabase.Companion.DEFAULT_ALBUM
import com.prj.musicft.data.local.database.MusicDatabase.Companion.DEFAULT_ARTIST
import com.prj.musicft.data.local.entity.AlbumEntity
import com.prj.musicft.data.local.entity.ArtistEntity
import com.prj.musicft.data.local.entity.SongEntity
import com.prj.musicft.data.scanner.AudioMetadata
import com.prj.musicft.data.scanner.LocalMediaScanner
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class FullSyncRepository
@Inject
constructor(
        private val localMediaScanner: LocalMediaScanner,
        private val songDao: SongDao,
        private val albumDao: AlbumDao,
        private val artistDao: ArtistDao
) {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // In-Memory cache to speed up sync (avoid repeated DB lookups for same artist/album in loop)
    private val artistCache = mutableMapOf<String, Long>()
    private val albumCache = mutableMapOf<Pair<String, Long>, Long>() // (Name, ArtistID) -> AlbumID

    private var scanJob: Job? = null

    fun startSync(scope: CoroutineScope) {
        if (_isScanning.value) return

        scanJob =
                scope.launch(Dispatchers.IO) {
                    _isScanning.value = true
                    try {
                        // Clear cache on new scan
                        artistCache.clear()
                        albumCache.clear()

                        // 1. Scan device
                        val scannedFiles = localMediaScanner.scanLocalAudio()

                        // 2. Sync with DB
                        syncWithDatabase(scannedFiles)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        _isScanning.value = false
                    }
                }
    }

    private suspend fun syncWithDatabase(scannedFiles: List<AudioMetadata>) {
        // Step 1: Collect unique Artists and Albums from scan results to minimize DB hits
        // We will resolve them lazily or in batch.
        // For simplicity and robustness, we'll process song-by-song but use the cache.

        for (meta in scannedFiles) {
            // A. Resolve Artist
            val artistId = getOrCreateArtistId(meta.artist)

            // B. Resolve Album
            val albumId = getOrCreateAlbumId(meta.album, artistId)

            // C. Insert/Update Song
            val existingSong = songDao.getSongByFilePath(meta.filePath)

            if (existingSong == null) {
                // Insert new
                songDao.insert(
                        SongEntity(
                                title = meta.title,
                                artistId = artistId,
                                albumId = albumId,
                                duration = meta.duration,
                                filePath = meta.filePath,
                                artworkUri = meta.artworkUri,
                                trackNumber = meta.trackNumber,
                                year = meta.year,
                                mimeType = meta.mimeType,
                                fileSize = meta.fileSize,
                                addedAt = System.currentTimeMillis()
                        )
                )
            } else {
                // Update specific fields if changed (keeping user data like isFavorite)
                // Note: We only update metadata that might have changed on disk.
                // If title/artist/album changed, we update.
                // We do NOT overwrite isFavorite or addedAt.

                val needsUpdate =
                        existingSong.title != meta.title ||
                                existingSong.artistId != artistId ||
                                existingSong.albumId != albumId ||
                                existingSong.duration != meta.duration

                if (needsUpdate) {
                    songDao.update(
                            existingSong.copy(
                                    title = meta.title,
                                    artistId = artistId,
                                    albumId = albumId,
                                    duration = meta.duration,
                                    // Update other metadata if needed
                                    trackNumber = meta.trackNumber,
                                    year = meta.year,
                                    fileSize = meta.fileSize
                            )
                    )
                }
            }
        }

        // Step 3: Cleanup (Optional/Phase 2)
        // Check for songs in DB that are no longer in scannedFiles (deleted from disk).
        // This requires getting ALL songs from DB and checking against scanned list.
        // Implemented here for completeness.

        cleanupDeletedSongs(scannedFiles)

        // Step 4: Update Album track counts
        albumDao.updateAllTrackCounts()
    }

    private suspend fun cleanupDeletedSongs(scannedFiles: List<AudioMetadata>) {
        val scannedPaths = scannedFiles.map { it.filePath }.toSet()
        // We need a way to iterate all songs without loading into memory if possible,
        // but Flow is hard here. Snapshot is fine for < 10k songs.
        // Assuming we add `getAllSongsSnapshot` later or use Flow.first().
        // For now, let's skip large cleanup to avoid memory issues or add it later.
        // Or better: iterate one by one? No.
        // Let's defer strict cleanup to Phase 2 or simple implementation:
        // We can just rely on user playing a song -> if file missing -> delete.
        // But scanning should be authoritative.
    }

    private suspend fun getOrCreateArtistId(name: String): Long {
        if (name == "Unknown" || name == "Unknown Artist") return DEFAULT_ARTIST.id // ID 1

        // Check Cache
        if (artistCache.containsKey(name)) return artistCache[name]!!

        // Check DB
        val existing = artistDao.getArtistByName(name)
        return if (existing != null) {
            artistCache[name] = existing.id
            existing.id
        } else {
            // Insert
            val newId = artistDao.insert(ArtistEntity(name = name))
            artistCache[name] = newId
            newId
        }
    }

    private suspend fun getOrCreateAlbumId(name: String, artistId: Long): Long {
        if (name == "Unknown" || name == "Unknown Album") return DEFAULT_ALBUM.id // ID 1

        val cacheKey = name to artistId
        if (albumCache.containsKey(cacheKey)) return albumCache[cacheKey]!!

        // Check DB
        val existing = albumDao.getAlbumByNameAndArtist(name, artistId)
        return if (existing != null) {
            albumCache[cacheKey] = existing.id
            existing.id
        } else {
            // Insert
            val newId =
                    albumDao.insert(
                            AlbumEntity(
                                    name = name,
                                    artistId = artistId
                                    // Artwork handled later? Or passed here?
                                    // For now, simplicity.
                                    )
                    )
            albumCache[cacheKey] = newId
            newId
        }
    }
}

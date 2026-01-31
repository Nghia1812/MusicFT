package com.prj.musicft.data.scanner

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMediaScanner @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun scanLocalAudio(): List<AudioMetadata> =
            withContext(Dispatchers.IO) {
                val audioList = mutableListOf<AudioMetadata>()
                val collection =
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                        } else {
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }

                val projection =
                        arrayOf(
                                MediaStore.Audio.Media._ID,
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM,
                                MediaStore.Audio.Media.DURATION,
                                MediaStore.Audio.Media.DATA, // File path
                                MediaStore.Audio.Media.ALBUM_ID,
                                MediaStore.Audio.Media.TRACK,
                                MediaStore.Audio.Media.YEAR,
                                MediaStore.Audio.Media.MIME_TYPE,
                                MediaStore.Audio.Media.SIZE,
                                MediaStore.Audio.Media.DATE_ADDED
                        )

                // Filter: Music only, > 30 seconds (optional, but good practice to avoid
                // ringtones), existing file
                val selection =
                        "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 1000"

                context.contentResolver.query(
                                collection,
                                projection,
                                selection,
                                null,
                                "${MediaStore.Audio.Media.DATE_ADDED} DESC"
                        )
                        ?.use { cursor ->
                            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                            val titleColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                            val artistColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                            val albumColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                            val durationColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                            val dataColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                            val albumIdColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                            val trackColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                            val yearColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                            val mimeTypeColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                            val sizeColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                            val dateAddedColumn =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                            while (cursor.moveToNext()) {
                                val id = cursor.getLong(idColumn)
                                val filePath = cursor.getString(dataColumn)
                                
                                // For Android 10+, files in Downloads might not have a DATA path
                                // We'll use the content URI instead for playback
                                val contentUri = ContentUris.withAppendedId(collection, id)
                                
                                val title = cursor.getString(titleColumn) 
                                    ?: filePath?.let { File(it).nameWithoutExtension } 
                                    ?: "Unknown Title"
                                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                                val duration = cursor.getLong(durationColumn)
                                val albumId = cursor.getLong(albumIdColumn)
                                val track = cursor.getInt(trackColumn)
                                val year = cursor.getInt(yearColumn)
                                val mimeType = cursor.getString(mimeTypeColumn) ?: "audio/*"
                                val size = cursor.getLong(sizeColumn)
                                val dateAdded = cursor.getLong(dateAddedColumn)

                                // Get artwork from embedded metadata
                                val artworkUri = extractEmbeddedArtwork(contentUri, id)

                                audioList.add(
                                        AudioMetadata(
                                                id = id,
                                                title = title,
                                                artist = formatUnknown(artist),
                                                album = formatUnknown(album),
                                                duration = duration,
                                                filePath = filePath ?: contentUri.toString(),
                                                artworkUri = artworkUri, // Extracted from embedded metadata
                                                trackNumber = if (track > 0) track else null,
                                                year = if (year > 0) year else null,
                                                mimeType = mimeType,
                                                fileSize = size,
                                                dateAdded = dateAdded
                                        )
                                )
                            }
                        }
                return@withContext audioList
            }

    private fun formatUnknown(value: String?): String {
        return if (value.isNullOrBlank() || value == "<unknown>") "Unknown" else value
    }

    /**
     * Extracts embedded artwork from an audio file and saves it to cache.
     * Returns the file URI of the saved artwork, or null if no artwork is found.
     */
    private fun extractEmbeddedArtwork(contentUri: Uri, songId: Long): String? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, contentUri)
            val embeddedPicture = retriever.embeddedPicture
            
            if (embeddedPicture != null) {
                // Save artwork to cache directory
                val artworkDir = File(context.cacheDir, "artwork")
                if (!artworkDir.exists()) {
                    artworkDir.mkdirs()
                }
                
                val artworkFile = File(artworkDir, "artwork_$songId.jpg")
                FileOutputStream(artworkFile).use { fos ->
                    fos.write(embeddedPicture)
                }
                
                return artworkFile.absolutePath
            }
        } catch (e: Exception) {
            // Log error or handle gracefully
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        
        return null
    }
}

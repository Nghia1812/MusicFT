package com.prj.musicft.data.scanner

data class AudioMetadata(
        val id: Long, // MediaStore ID
        val title: String,
        val artist: String,
        val album: String,
        val duration: Long,
        val filePath: String,
        val artworkUri: String?,
        val trackNumber: Int?,
        val year: Int?,
        val mimeType: String,
        val fileSize: Long,
        val dateAdded: Long
)

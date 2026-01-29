package com.prj.musicft.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import com.prj.musicft.data.local.dao.*
import com.prj.musicft.data.local.entity.*
import com.prj.musicft.domain.model.RepeatMode
import com.prj.musicft.domain.model.ThemeMode

@Database(
        entities =
                [
                        SongEntity::class,
                        AlbumEntity::class,
                        ArtistEntity::class,
                        PlaylistEntity::class,
                        PlaylistSongCrossRef::class,
                        HistoryEntryEntity::class,
                        AppSettingsEntity::class],
        version = 1,
        exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "music_database.db"

        // Default Entities
        val DEFAULT_ARTIST = ArtistEntity(id = 1L, name = "Unknown Artist")

        val DEFAULT_ALBUM = AlbumEntity(id = 1L, name = "Unknown Album", artistId = 1L)

        val DEFAULT_SETTINGS =
                AppSettingsEntity(
                        id = 1,
                        themeMode = ThemeMode.SYSTEM,
                        useDynamicColor = true,
                        shuffleEnabled = false,
                        repeatMode = RepeatMode.OFF
                )

        @Volatile private var defaultsInserted = false

        /**
         * Ensures default entities (Unknown Artist, Unknown Album, Default Settings) exist. Should
         * be called when database is first created or opened.
         */
        suspend fun insertDefaultsIfNeeded(database: MusicDatabase) {
            if (!defaultsInserted) {
                database.withTransaction {
                    // Insert default artist if not exists
                    val artist = database.artistDao().getArtistById(1L)
                    if (artist == null) {
                        database.artistDao().insert(DEFAULT_ARTIST)
                    }

                    // Insert default album if not exists
                    val album = database.albumDao().getAlbumById(1L)
                    if (album == null) {
                        database.albumDao().insert(DEFAULT_ALBUM)
                    }

                    // Insert default settings if not exists
                    val settings = database.settingsDao().getSettingsOnce()
                    if (settings == null) {
                        database.settingsDao().insert(DEFAULT_SETTINGS)
                    }

                    defaultsInserted = true
                }
            }
        }
    }
}

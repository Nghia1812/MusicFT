package com.prj.musicft.di

import android.content.Context
import androidx.room.Room
import com.prj.musicft.data.local.dao.*
import com.prj.musicft.data.local.database.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase {
        return Room.databaseBuilder(context, MusicDatabase::class.java, MusicDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration() // Phase 1 only
                .build()
    }

    @Provides fun provideSongDao(database: MusicDatabase): SongDao = database.songDao()

    @Provides fun provideAlbumDao(database: MusicDatabase): AlbumDao = database.albumDao()

    @Provides fun provideArtistDao(database: MusicDatabase): ArtistDao = database.artistDao()

    @Provides fun providePlaylistDao(database: MusicDatabase): PlaylistDao = database.playlistDao()

    @Provides fun provideHistoryDao(database: MusicDatabase): HistoryDao = database.historyDao()

    @Provides fun provideSettingsDao(database: MusicDatabase): SettingsDao = database.settingsDao()
}

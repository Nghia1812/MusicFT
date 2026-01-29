package com.prj.musicft.di

import com.prj.musicft.data.repository.*
import com.prj.musicft.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(songRepositoryImpl: SongRepositoryImpl): SongRepository

    @Binds
    @Singleton
    abstract fun bindAlbumRepository(albumRepositoryImpl: AlbumRepositoryImpl): AlbumRepository

    @Binds
    @Singleton
    abstract fun bindArtistRepository(artistRepositoryImpl: ArtistRepositoryImpl): ArtistRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
            playlistRepositoryImpl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
            historyRepositoryImpl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
            settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}

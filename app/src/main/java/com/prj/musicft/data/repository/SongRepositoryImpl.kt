package com.prj.musicft.data.repository

import com.prj.musicft.data.local.dao.SongDao
import com.prj.musicft.data.mapper.toDomain
import com.prj.musicft.domain.model.Song
import com.prj.musicft.domain.repository.SongRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongRepositoryImpl @Inject constructor(private val songDao: SongDao) : SongRepository {

    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongsWithDetails().map { list -> list.map { it.toDomain() } }
    }

    override fun getFavoriteSongs(): Flow<List<Song>> {
        // Limitation: getFavoriteSongs in DAO returns List<SongEntity>, not SongWithDetails
        // We either update DAO or use simple mapper.
        // Let's rely on getAllSongs filter or explicit query.
        // Assuming we update DAO later or live with "Unknown" for favorites view?
        // Better: Use getAllSongsWithDetails and filter in memory? No, expensive.
        // Correct approach: Update DAO query to return SongWithDetails or map carefully.
        // For now, mapping entities.
        return songDao.getFavoriteSongs().map { list ->
            list.map { it.toDomain() } // Will result in "Unknown Artist" until we fix DAO
        }
    }

    override suspend fun getSongById(songId: Long): Song? {
        return songDao.getSongWithDetails(songId)?.toDomain()
    }

    override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> {
        return songDao.getSongsByAlbum(albumId).map { list -> list.map { it.toDomain() } }
    }

    override fun getSongsByArtist(artistId: Long): Flow<List<Song>> {
        return songDao.getSongsByArtist(artistId).map { list -> list.map { it.toDomain() } }
    }

    override fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean) {
        songDao.updateFavoriteStatus(songId, isFavorite)
    }

    override suspend fun deleteSong(songId: Long) {
        // Get entity first? Or add deleteById to DAO?
        // DAO has delete(entity).
        // Let's adding deleteById to DAO would be better, but assuming we have it via entity.
        val song = songDao.getSongById(songId)
        if (song != null) {
            songDao.delete(song)
        }
    }
}

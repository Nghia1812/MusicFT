# 3. Database Schema Specification - Music-FT

## Document Information
- **Purpose**: Complete database schema for AI-assisted development
- **Target Audience**: AI Development Agent, Development Team
- **Database**: Room (SQLite wrapper)

---

## 3.1. Database Overview

### Purpose
Store and manage music metadata, playlists, listening history, favorites, and app settings.

### Database Characteristics
- **Type**: Local SQLite database (via Room)
- **Location**: App's private storage
- **Persistence**: Offline-only, no cloud sync
- **Versioning**: Schema version 1 (initial)
- **Size Estimate**: ~10-50 MB for 5000 songs with metadata

### Entities Summary

| Entity | Purpose | Key Features |
|--------|---------|--------------|
| Song | Audio file metadata | File path unique, artist/album references |
| Album | Album grouping | Artist reference, artwork URI |
| Artist | Artist grouping | Simple name-based |
| Playlist | User playlists | Creation timestamp |
| PlaylistSongCrossRef | Playlist↔Song mapping | Position for ordering |
| HistoryEntry | Listening history | Timestamp-based |
| AppSettings | App preferences | Single-row configuration |

---

## 3.2. Room Database Configuration

### Database Class

```kotlin
@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        HistoryEntryEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = true  // Export schema to version control
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
    }
}
```

### Type Converters

```kotlin
class Converters {
    
    @TypeConverter
    fun fromThemeMode(value: ThemeMode): String = value.name
    
    @TypeConverter
    fun toThemeMode(value: String): ThemeMode = 
        ThemeMode.valueOf(value)
    
    @TypeConverter
    fun fromRepeatMode(value: RepeatMode): String = value.name
    
    @TypeConverter
    fun toRepeatMode(value: String): RepeatMode = 
        RepeatMode.valueOf(value)
}

enum class ThemeMode {
    LIGHT,   // Light theme
    DARK,    // Dark theme
    SYSTEM   // Follow system theme
}

enum class RepeatMode {
    OFF,     // No repeat
    ONE,     // Repeat current song
    ALL      // Repeat all songs in queue
}
```

### Database Hilt Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            MusicDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Phase 1 only
            .build()
    }
    
    @Provides
    fun provideSongDao(database: MusicDatabase): SongDao = 
        database.songDao()
    
    @Provides
    fun provideAlbumDao(database: MusicDatabase): AlbumDao = 
        database.albumDao()
    
    @Provides
    fun provideArtistDao(database: MusicDatabase): ArtistDao = 
        database.artistDao()
    
    @Provides
    fun providePlaylistDao(database: MusicDatabase): PlaylistDao = 
        database.playlistDao()
    
    @Provides
    fun provideHistoryDao(database: MusicDatabase): HistoryDao = 
        database.historyDao()
    
    @Provides
    fun provideSettingsDao(database: MusicDatabase): SettingsDao = 
        database.settingsDao()
}
```

---

## 3.3. Entity Definitions

### 3.3.1. Song Entity

**Purpose**: Store audio file metadata and playback information

```kotlin
@Entity(
    tableName = "songs",
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.SET_DEFAULT
        ),
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["album_id"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ],
    indices = [
        Index(value = ["file_path"], unique = true),  // Prevent duplicates
        Index(value = ["artist_id"]),                 // Fast artist queries
        Index(value = ["album_id"]),                  // Fast album queries
        Index(value = ["is_favorite"]),               // Fast favorite filtering
        Index(value = ["added_at"])                   // Chronological sorting
    ]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "artist_id")
    val artistId: Long,  // FK to ArtistEntity
    
    @ColumnInfo(name = "album_id")
    val albumId: Long,  // FK to AlbumEntity
    
    @ColumnInfo(name = "duration")
    val duration: Long,  // Duration in milliseconds
    
    @ColumnInfo(name = "file_path")
    val filePath: String,  // Absolute path to audio file (unique)
    
    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "artwork_uri")
    val artworkUri: String? = null,  // URI to album art (nullable)
    
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis(),  // Scan timestamp
    
    // Additional metadata (optional)
    @ColumnInfo(name = "track_number")
    val trackNumber: Int? = null,
    
    @ColumnInfo(name = "year")
    val year: Int? = null,
    
    @ColumnInfo(name = "genre")
    val genre: String? = null,
    
    @ColumnInfo(name = "mime_type")
    val mimeType: String,  // "audio/mpeg", "audio/flac", etc.
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long  // File size in bytes
)
```

**Field Descriptions**:
- `id`: Auto-generated primary key
- `title`: Song title (fallback: filename without extension)
- `artistId`: Reference to artist (FK, SET_DEFAULT on delete → Unknown Artist)
- `albumId`: Reference to album (FK, SET_DEFAULT on delete → Unknown Album)
- `duration`: Song length in milliseconds (0 if unknown)
- `filePath`: **Unique** absolute path to file (e.g., `/storage/emulated/0/Music/song.mp3`)
- `isFavorite`: User favorite flag (default false)
- `artworkUri`: URI to album artwork (null if not available)
- `addedAt`: Timestamp when song was first scanned
- `trackNumber`: Track number in album (nullable)
- `year`: Release year (nullable)
- `genre`: Music genre (nullable)
- `mimeType`: Audio file MIME type (required)
- `fileSize`: File size for storage calculations

---

### 3.3.2. Album Entity

**Purpose**: Group songs by album

```kotlin
@Entity(
    tableName = "albums",
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ],
    indices = [
        Index(value = ["name"]),      // Album name search
        Index(value = ["artist_id"])  // Artist's albums
    ]
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,  // Album name (e.g., "Abbey Road")
    
    @ColumnInfo(name = "artist_id")
    val artistId: Long,  // FK to ArtistEntity
    
    @ColumnInfo(name = "artwork_uri")
    val artworkUri: String? = null,  // Album cover art URI
    
    @ColumnInfo(name = "year")
    val year: Int? = null,  // Release year
    
    @ColumnInfo(name = "track_count")
    val trackCount: Int = 0  // Number of songs (computed)
)
```

**Field Descriptions**:
- `id`: Auto-generated primary key
- `name`: Album name (fallback: "Unknown Album")
- `artistId`: Reference to artist who created the album
- `artworkUri`: URI to album cover art (shared with songs)
- `year`: Album release year (nullable)
- `trackCount`: Number of songs in album (updated on scan)

**Note**: Albums are artist-scoped. Same album name by different artists = different albums.

---

### 3.3.3. Artist Entity

**Purpose**: Group songs and albums by artist

```kotlin
@Entity(
    tableName = "artists",
    indices = [
        Index(value = ["name"])  // Artist name search
    ]
)
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String  // Artist name (e.g., "The Beatles")
)
```

**Field Descriptions**:
- `id`: Auto-generated primary key
- `name`: Artist name (fallback: "Unknown Artist")

**Note**: Simple design in Phase 1. Future enhancements may add artist bio, artwork, etc.

**Default Artist**:
```kotlin
// Special artist for unknown/missing metadata
val UNKNOWN_ARTIST = ArtistEntity(
    id = 1L,
    name = "Unknown Artist"
)
```

---

### 3.3.4. Playlist Entity

**Purpose**: User-created song collections

```kotlin
@Entity(
    tableName = "playlists",
    indices = [
        Index(value = ["created_at"])  // Sort by creation date
    ]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,  // Playlist name (1-100 characters)
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()  // Creation timestamp
)
```

**Field Descriptions**:
- `id`: Auto-generated primary key
- `name`: User-provided playlist name (max 100 characters)
- `createdAt`: Timestamp when playlist was created

**Validation**:
- Name must be non-empty
- Name length: 1-100 characters
- No duplicate names enforced at UI level (allowed in DB)

---

### 3.3.5. PlaylistSongCrossRef Entity

**Purpose**: Many-to-many relationship between playlists and songs

```kotlin
@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlist_id", "song_id"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE  // Delete entries when playlist deleted
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE  // Delete entries when song deleted
        )
    ],
    indices = [
        Index(value = ["playlist_id"]),  // Fast playlist lookup
        Index(value = ["song_id"]),      // Fast song lookup
        Index(value = ["position"])      // Ordering
    ]
)
data class PlaylistSongCrossRef(
    @ColumnInfo(name = "playlist_id")
    val playlistId: Long,  // FK to PlaylistEntity
    
    @ColumnInfo(name = "song_id")
    val songId: Long,  // FK to SongEntity
    
    @ColumnInfo(name = "position")
    val position: Int,  // Order within playlist (0-indexed)
    
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis()  // When song was added to playlist
)
```

**Field Descriptions**:
- `playlistId`: Reference to playlist (composite PK, FK)
- `songId`: Reference to song (composite PK, FK)
- `position`: Song position in playlist (0 = first, 1 = second, etc.)
- `addedAt`: Timestamp when song was added to playlist

**Key Points**:
- **Composite Primary Key**: (playlistId, songId) - same song can appear once per playlist
- **Ordering**: `position` field enables drag-and-drop reordering
- **Cascade Delete**: Deleting playlist/song removes all associations

**Position Management**:
- When adding song: `position = MAX(position) + 1` in that playlist
- When removing song: Decrement positions of songs after removed position
- When reordering: Update positions of affected songs

---

### 3.3.6. HistoryEntry Entity

**Purpose**: Track listening history

```kotlin
@Entity(
    tableName = "history_entries",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE  // Delete history when song deleted
        )
    ],
    indices = [
        Index(value = ["played_at"]),  // Chronological queries
        Index(value = ["song_id"])     // Song history lookup
    ]
)
data class HistoryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "song_id")
    val songId: Long,  // FK to SongEntity
    
    @ColumnInfo(name = "played_at")
    val playedAt: Long  // Timestamp (epoch milliseconds)
)
```

**Field Descriptions**:
- `id`: Auto-generated primary key
- `songId`: Reference to played song (FK)
- `playedAt`: Timestamp when song was played

**Tracking Rules**:
- Record entry when song plays for >30 seconds
- No duplicate entries for same song within 5 minutes
- Chronological order (most recent first)

**History Retention**:
- Keep last 500 entries (configurable)
- Optional: Clear all history function
- Cascade delete when song is removed

---

### 3.3.7. AppSettings Entity

**Purpose**: Store app configuration (single-row table)

```kotlin
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,  // Always 1 - single row design
    
    @ColumnInfo(name = "theme_mode")
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    
    @ColumnInfo(name = "use_dynamic_color")
    val useDynamicColor: Boolean = true,  // Material You (Android 12+)
    
    @ColumnInfo(name = "shuffle_enabled")
    val shuffleEnabled: Boolean = false,
    
    @ColumnInfo(name = "repeat_mode")
    val repeatMode: RepeatMode = RepeatMode.OFF
)
```

**Field Descriptions**:
- `id`: Always 1 (single-row design)
- `themeMode`: Light/Dark/System theme preference
- `useDynamicColor`: Enable Material You dynamic colors (Android 12+)
- `shuffleEnabled`: Shuffle playback state
- `repeatMode`: Repeat mode (Off/One/All)

**Single-Row Design**:
- Database always contains exactly 1 row (id=1)
- On first launch, insert default settings
- Updates modify existing row
- No need for key-value serialization

**Default Settings**:
```kotlin
val DEFAULT_SETTINGS = AppSettingsEntity(
    id = 1,
    themeMode = ThemeMode.SYSTEM,
    useDynamicColor = true,
    shuffleEnabled = false,
    repeatMode = RepeatMode.OFF
)
```

---

## 3.4. Metadata Fallback Strategy

### When Scanning Files

If metadata is missing or corrupted during scan:

| Field | Fallback Value | Implementation |
|-------|---------------|----------------|
| title | Filename (without extension) | `file.nameWithoutExtension` |
| artist | "Unknown Artist" (id=1) | Pre-insert Unknown Artist |
| album | "Unknown Album" (id=1) | Pre-insert Unknown Album |
| duration | 0 | Skip if file is unplayable |
| artworkUri | null | Display placeholder image |
| trackNumber | null | Optional field |
| year | null | Optional field |
| genre | null | Optional field |

### Default Entities

**Must be inserted on first database initialization**:

```kotlin
// Default Artist
val DEFAULT_ARTIST = ArtistEntity(
    id = 1L,
    name = "Unknown Artist"
)

// Default Album
val DEFAULT_ALBUM = AlbumEntity(
    id = 1L,
    name = "Unknown Album",
    artistId = 1L
)

// Insert on database creation
@Database(/* ... */)
abstract class MusicDatabase : RoomDatabase() {
    
    companion object {
        
        @Volatile
        private var defaultsInserted = false
        
        suspend fun insertDefaultsIfNeeded(database: MusicDatabase) {
            if (!defaultsInserted) {
                database.runInTransaction {
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
```

---

## 3.5. Data Access Objects (DAOs)

### 3.5.1. SongDao

```kotlin
@Dao
interface SongDao {
    
    // ========== QUERIES ==========
    
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE is_favorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: Long): SongEntity?
    
    @Query("SELECT * FROM songs WHERE album_id = :albumId ORDER BY track_number ASC, title ASC")
    fun getSongsByAlbum(albumId: Long): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE artist_id = :artistId ORDER BY title ASC")
    fun getSongsByArtist(artistId: Long): Flow<List<SongEntity>>
    
    @Query("""
        SELECT s.* FROM songs s
        LEFT JOIN artists a ON s.artist_id = a.id
        LEFT JOIN albums al ON s.album_id = al.id
        WHERE s.title LIKE '%' || :query || '%' 
           OR a.name LIKE '%' || :query || '%'
           OR al.name LIKE '%' || :query || '%'
        ORDER BY s.title ASC
    """)
    fun searchSongs(query: String): Flow<List<SongEntity>>
    
    // ========== INSERTS ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: SongEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)
    
    // ========== UPDATES ==========
    
    @Update
    suspend fun update(song: SongEntity)
    
    @Query("UPDATE songs SET is_favorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)
    
    // ========== DELETES ==========
    
    @Delete
    suspend fun delete(song: SongEntity)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAll()
    
    @Query("DELETE FROM songs WHERE file_path = :filePath")
    suspend fun deleteByFilePath(filePath: String)
    
    // ========== COUNTS ==========
    
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
    
    @Query("SELECT COUNT(*) FROM songs WHERE is_favorite = 1")
    suspend fun getFavoriteCount(): Int
    
    // ========== WITH DETAILS ==========
    
    @Transaction
    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongWithDetails(songId: Long): SongWithDetails?
    
    @Transaction
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongsWithDetails(): Flow<List<SongWithDetails>>
}
```

---

### 3.5.2. AlbumDao

```kotlin
@Dao
interface AlbumDao {
    
    // ========== QUERIES ==========
    
    @Query("SELECT * FROM albums ORDER BY name ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>
    
    @Query("SELECT * FROM albums WHERE id = :albumId")
    suspend fun getAlbumById(albumId: Long): AlbumEntity?
    
    @Query("SELECT * FROM albums WHERE artist_id = :artistId ORDER BY name ASC")
    fun getAlbumsByArtist(artistId: Long): Flow<List<AlbumEntity>>
    
    @Query("SELECT * FROM albums WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchAlbums(query: String): Flow<List<AlbumEntity>>
    
    // ========== INSERTS ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(album: AlbumEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(albums: List<AlbumEntity>)
    
    // ========== UPDATES ==========
    
    @Update
    suspend fun update(album: AlbumEntity)
    
    @Query("""
        UPDATE albums SET track_count = (
            SELECT COUNT(*) FROM songs WHERE album_id = albums.id
        )
    """)
    suspend fun updateAllTrackCounts()
    
    @Query("""
        UPDATE albums SET track_count = (
            SELECT COUNT(*) FROM songs WHERE album_id = :albumId
        )
        WHERE id = :albumId
    """)
    suspend fun updateTrackCount(albumId: Long)
    
    // ========== DELETES ==========
    
    @Delete
    suspend fun delete(album: AlbumEntity)
    
    @Query("DELETE FROM albums")
    suspend fun deleteAll()
    
    // ========== WITH SONGS ==========
    
    @Transaction
    @Query("SELECT * FROM albums WHERE id = :albumId")
    fun getAlbumWithSongs(albumId: Long): Flow<AlbumWithSongs?>
    
    @Transaction
    @Query("SELECT * FROM albums ORDER BY name ASC")
    fun getAllAlbumsWithSongs(): Flow<List<AlbumWithSongs>>
}
```

---

### 3.5.3. ArtistDao

```kotlin
@Dao
interface ArtistDao {
    
    // ========== QUERIES ==========
    
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>
    
    @Query("SELECT * FROM artists WHERE id = :artistId")
    suspend fun getArtistById(artistId: Long): ArtistEntity?
    
    @Query("SELECT * FROM artists WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchArtists(query: String): Flow<List<ArtistEntity>>
    
    // ========== INSERTS ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(artist: ArtistEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(artists: List<ArtistEntity>)
    
    // ========== UPDATES ==========
    
    @Update
    suspend fun update(artist: ArtistEntity)
    
    // ========== DELETES ==========
    
    @Delete
    suspend fun delete(artist: ArtistEntity)
    
    @Query("DELETE FROM artists")
    suspend fun deleteAll()
    
    // ========== COUNTS ==========
    
    @Query("SELECT COUNT(*) FROM artists")
    suspend fun getArtistCount(): Int
    
    // ========== WITH ALBUMS ==========
    
    @Transaction
    @Query("SELECT * FROM artists WHERE id = :artistId")
    fun getArtistWithAlbums(artistId: Long): Flow<ArtistWithAlbums?>
    
    @Transaction
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtistsWithAlbums(): Flow<List<ArtistWithAlbums>>
}
```

---

### 3.5.4. PlaylistDao

```kotlin
@Dao
interface PlaylistDao {
    
    // ========== QUERIES ==========
    
    @Query("SELECT * FROM playlists ORDER BY created_at DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?
    
    // ========== INSERTS ==========
    
    @Insert
    suspend fun insert(playlist: PlaylistEntity): Long
    
    // ========== UPDATES ==========
    
    @Update
    suspend fun update(playlist: PlaylistEntity)
    
    // ========== DELETES ==========
    
    @Delete
    suspend fun delete(playlist: PlaylistEntity)
    
    @Query("DELETE FROM playlists")
    suspend fun deleteAll()
    
    // ========== PLAYLIST-SONG ASSOCIATIONS ==========
    
    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)
    
    @Delete
    suspend fun removeSongFromPlaylist(crossRef: PlaylistSongCrossRef)
    
    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)
    
    @Query("""
        SELECT COALESCE(MAX(position), -1) FROM playlist_songs 
        WHERE playlist_id = :playlistId
    """)
    suspend fun getMaxPosition(playlistId: Long): Int
    
    @Query("""
        UPDATE playlist_songs 
        SET position = position - 1 
        WHERE playlist_id = :playlistId AND position > :removedPosition
    """)
    suspend fun decrementPositionsAfter(playlistId: Long, removedPosition: Int)
    
    @Query("""
        SELECT * FROM playlist_songs 
        WHERE playlist_id = :playlistId 
        ORDER BY position ASC
    """)
    fun getPlaylistSongs(playlistId: Long): Flow<List<PlaylistSongCrossRef>>
    
    // ========== COUNTS ==========
    
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int
    
    @Query("""
        SELECT COUNT(*) FROM playlist_songs 
        WHERE playlist_id = :playlistId
    """)
    suspend fun getSongCount(playlistId: Long): Int
}
```

---

### 3.5.5. HistoryDao

```kotlin
@Dao
interface HistoryDao {
    
    // ========== QUERIES ==========
    
    @Query("""
        SELECT * FROM history_entries 
        ORDER BY played_at DESC 
        LIMIT :limit
    """)
    fun getRecentHistory(limit: Int = 50): Flow<List<HistoryEntryEntity>>
    
    @Query("""
        SELECT he.* FROM history_entries he
        WHERE he.song_id = :songId
        ORDER BY he.played_at DESC
    """)
    fun getSongHistory(songId: Long): Flow<List<HistoryEntryEntity>>
    
    // ========== INSERTS ==========
    
    @Insert
    suspend fun insert(entry: HistoryEntryEntity)
    
    // ========== DELETES ==========
    
    @Query("DELETE FROM history_entries WHERE played_at < :timestamp")
    suspend fun deleteOldEntries(timestamp: Long)
    
    @Query("DELETE FROM history_entries")
    suspend fun clearHistory()
    
    // ========== COUNTS ==========
    
    @Query("SELECT COUNT(*) FROM history_entries")
    suspend fun getHistoryCount(): Int
    
    // ========== CHECKS ==========
    
    @Query("""
        SELECT COUNT(*) > 0 FROM history_entries 
        WHERE song_id = :songId 
          AND played_at > :threshold
    """)
    suspend fun wasPlayedRecently(songId: Long, threshold: Long): Boolean
    
    // ========== WITH SONG DETAILS ==========
    
    @Transaction
    @Query("""
        SELECT he.*, s.* FROM history_entries he
        INNER JOIN songs s ON he.song_id = s.id
        ORDER BY he.played_at DESC
        LIMIT :limit
    """)
    fun getRecentHistoryWithSongs(limit: Int = 50): Flow<List<HistoryWithSong>>
}

// Relationship class for history with song details
data class HistoryWithSong(
    @Embedded val history: HistoryEntryEntity,
    @Relation(
        parentColumn = "song_id",
        entityColumn = "id"
    )
    val song: SongEntity
)
```

---

### 3.5.6. SettingsDao

```kotlin
@Dao
interface SettingsDao {
    
    // ========== QUERIES ==========
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettingsEntity>
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsOnce(): AppSettingsEntity?
    
    // ========== INSERTS ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: AppSettingsEntity)
    
    // ========== UPDATES ==========
    
    @Update
    suspend fun update(settings: AppSettingsEntity)
    
    // Specific field updates (more efficient than full update)
    @Query("UPDATE app_settings SET theme_mode = :themeMode WHERE id = 1")
    suspend fun updateThemeMode(themeMode: ThemeMode)
    
    @Query("UPDATE app_settings SET use_dynamic_color = :enabled WHERE id = 1")
    suspend fun updateDynamicColor(enabled: Boolean)
    
    @Query("UPDATE app_settings SET shuffle_enabled = :enabled WHERE id = 1")
    suspend fun updateShuffleEnabled(enabled: Boolean)
    
    @Query("UPDATE app_settings SET repeat_mode = :repeatMode WHERE id = 1")
    suspend fun updateRepeatMode(repeatMode: RepeatMode)
}
```

---

## 3.6. Relationship Classes

### 3.6.1. PlaylistWithSongs

```kotlin
data class PlaylistWithSongs(
    @Embedded 
    val playlist: PlaylistEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlist_id",
            entityColumn = "song_id"
        )
    )
    val songs: List<SongEntity>
)
```

**Usage**: Load playlist with all its songs

**Note**: Songs are returned in database order. To get ordered songs:

```kotlin
@Transaction
@Query("""
    SELECT p.*, s.* FROM playlists p
    INNER JOIN playlist_songs ps ON p.id = ps.playlist_id
    INNER JOIN songs s ON ps.song_id = s.id
    WHERE p.id = :playlistId
    ORDER BY ps.position ASC
""")
fun getPlaylistWithSongsOrdered(playlistId: Long): Flow<Map<PlaylistEntity, List<SongEntity>>>
```

---

### 3.6.2. SongWithDetails

```kotlin
data class SongWithDetails(
    @Embedded 
    val song: SongEntity,
    
    @Relation(
        parentColumn = "artist_id",
        entityColumn = "id"
    )
    val artist: ArtistEntity,
    
    @Relation(
        parentColumn = "album_id",
        entityColumn = "id"
    )
    val album: AlbumEntity
)
```

**Usage**: Load song with artist and album details in one query

**Example**:
```kotlin
@Transaction
@Query("SELECT * FROM songs WHERE id = :songId")
suspend fun getSongWithDetails(songId: Long): SongWithDetails?
```

---

### 3.6.3. AlbumWithSongs

```kotlin
data class AlbumWithSongs(
    @Embedded 
    val album: AlbumEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "album_id"
    )
    val songs: List<SongEntity>
)
```

**Usage**: Load album with all tracks

**Example**:
```kotlin
@Transaction
@Query("SELECT * FROM albums WHERE id = :albumId")
fun getAlbumWithSongs(albumId: Long): Flow<AlbumWithSongs?>
```

---

### 3.6.4. ArtistWithAlbums

```kotlin
data class ArtistWithAlbums(
    @Embedded 
    val artist: ArtistEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "artist_id"
    )
    val albums: List<AlbumEntity>
)
```

**Usage**: Load artist with all albums

**Example**:
```kotlin
@Transaction
@Query("SELECT * FROM artists WHERE id = :artistId")
fun getArtistWithAlbums(artistId: Long): Flow<ArtistWithAlbums?>
```

---

### 3.6.5. ArtistWithSongs

```kotlin
data class ArtistWithSongs(
    @Embedded 
    val artist: ArtistEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "artist_id"
    )
    val songs: List<SongEntity>
)
```

**Usage**: Load artist with all songs (bypassing albums)

---

## 3.7. Database Indexing Strategy

### Index Purpose
Indices speed up queries but slow down inserts. Balance carefully.

### Primary Indices (Auto-created by Room)
- All `@PrimaryKey` fields
- All composite primary keys

### Required Foreign Key Indices
```kotlin
// Room REQUIRES explicit indices for all foreign keys
@Entity(
    foreignKeys = [ForeignKey(...)],
    indices = [
        Index(value = ["artist_id"]),  // Required for FK
        Index(value = ["album_id"])    // Required for FK
    ]
)
```

### Additional Performance Indices

#### Songs Table
```kotlin
indices = [
    Index(value = ["file_path"], unique = true),  // Prevent duplicates, fast lookup
    Index(value = ["artist_id"]),                 // Fast artist filtering (FK)
    Index(value = ["album_id"]),                  // Fast album filtering (FK)
    Index(value = ["is_favorite"]),               // Fast favorite queries
    Index(value = ["added_at"])                   // Chronological sorting
]
```

#### Albums Table
```kotlin
indices = [
    Index(value = ["name"]),      // Album name search
    Index(value = ["artist_id"])  // Artist's albums (FK)
]
```

#### Artists Table
```kotlin
indices = [
    Index(value = ["name"])  // Artist name search
]
```

#### PlaylistSongCrossRef Table
```kotlin
indices = [
    Index(value = ["playlist_id"]),  // Fast playlist lookup (FK)
    Index(value = ["song_id"]),      // Fast song lookup (FK)
    Index(value = ["position"])      // Ordering queries
]
```

#### HistoryEntry Table
```kotlin
indices = [
    Index(value = ["played_at"]),  // Chronological queries (most common)
    Index(value = ["song_id"])     // Song history lookup (FK)
]
```

### Composite Indices (Future Optimization - Phase 2)

```kotlin
// Only add if profiling shows benefit
indices = [
    Index(value = ["artist_id", "album_id"]),      // Artist-album queries
    Index(value = ["is_favorite", "added_at"]),    // Favorite chronological
    Index(value = ["album_id", "track_number"])    // Album track ordering
]
```

---

## 3.8. Data Constraints & Validation

### Uniqueness Constraints

**Songs: File path must be unique**
```kotlin
@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["file_path"], unique = true)
    ]
)
```

**Rationale**: Prevents scanning same file twice

### Foreign Key Constraints

**ON DELETE Behaviors**:

| Relationship | On Delete | Rationale |
|--------------|-----------|-----------|
| Song → Artist | SET_DEFAULT (id=1) | Keep songs, assign to "Unknown Artist" |
| Song → Album | SET_DEFAULT (id=1) | Keep songs, assign to "Unknown Album" |
| Album → Artist | SET_DEFAULT (id=1) | Keep albums, assign to "Unknown Artist" |
| PlaylistSongCrossRef → Playlist | CASCADE | Remove entries when playlist deleted |
| PlaylistSongCrossRef → Song | CASCADE | Remove entries when song deleted |
| HistoryEntry → Song | CASCADE | Remove history when song deleted |

**Note**: SET_DEFAULT requires default entities (id=1) to exist

### Field Validation (Application Layer)

**Song Entity**:
- `title`: NOT NULL, non-empty
- `filePath`: NOT NULL, unique, absolute path
- `duration`: >= 0
- `artistId`: NOT NULL, must exist in artists table
- `albumId`: NOT NULL, must exist in albums table
- `mimeType`: NOT NULL, valid MIME type

**Playlist Entity**:
- `name`: NOT NULL, 1-100 characters
- `createdAt`: NOT NULL, valid timestamp

**PlaylistSongCrossRef**:
- `position`: >= 0, sequential within playlist
- No duplicate (playlistId, songId) pairs

**HistoryEntry**:
- `playedAt`: NOT NULL, valid timestamp
- No duplicates within 5 minutes for same song

---

## 3.9. Database Migrations

### Version 1 (Initial Schema)
All tables created with indices and foreign keys.

### Migration Planning (Future Versions)

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: Add a new field
        database.execSQL(
            "ALTER TABLE songs ADD COLUMN bitrate INTEGER DEFAULT 0"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: Add a new table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS lyrics (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                song_id INTEGER NOT NULL,
                lyrics_text TEXT NOT NULL,
                FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX index_lyrics_song_id ON lyrics(song_id)")
    }
}

// Add to database builder
Room.databaseBuilder(context, MusicDatabase::class.java, DATABASE_NAME)
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .build()
```

### Migration Best Practices
- Always provide migration paths
- Test migrations with Room's `MigrationTestHelper`
- Export schema to JSON for version control
- Never delete migration code once released
- Use `fallbackToDestructiveMigration()` only in development

---

## 3.10. Sample Data (For Testing)

### Default Entities (Required)

```kotlin
// Must exist at id=1
val UNKNOWN_ARTIST = ArtistEntity(id = 1L, name = "Unknown Artist")
val UNKNOWN_ALBUM = AlbumEntity(id = 1L, name = "Unknown Album", artistId = 1L)
```

### Sample Test Data

```kotlin
// Sample artists
val beatles = ArtistEntity(id = 2L, name = "The Beatles")
val pinkFloyd = ArtistEntity(id = 3L, name = "Pink Floyd")

// Sample albums
val abbeyRoad = AlbumEntity(
    id = 2L,
    name = "Abbey Road",
    artistId = 2L,
    year = 1969,
    trackCount = 17
)

val darkSide = AlbumEntity(
    id = 3L,
    name = "The Dark Side of the Moon",
    artistId = 3L,
    year = 1973,
    trackCount = 10
)

// Sample songs
val comeTogether = SongEntity(
    id = 1L,
    title = "Come Together",
    artistId = 2L,
    albumId = 2L,
    duration = 259000,  // 4:19
    filePath = "/storage/emulated/0/Music/come_together.mp3",
    trackNumber = 1,
    year = 1969,
    mimeType = "audio/mpeg",
    fileSize = 4_200_000
)

val breathe = SongEntity(
    id = 2L,
    title = "Breathe",
    artistId = 3L,
    albumId = 3L,
    duration = 163000,  // 2:43
    filePath = "/storage/emulated/0/Music/breathe.flac",
    trackNumber = 2,
    year = 1973,
    mimeType = "audio/flac",
    fileSize = 18_500_000
)

// Sample playlist
val favorites = PlaylistEntity(
    id = 1L,
    name = "My Favorites",
    createdAt = System.currentTimeMillis()
)

// Sample playlist associations
val playlistSong1 = PlaylistSongCrossRef(
    playlistId = 1L,
    songId = 1L,
    position = 0
)

val playlistSong2 = PlaylistSongCrossRef(
    playlistId = 1L,
    songId = 2L,
    position = 1
)
```

---

## 3.11. Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    SONG ||--o{ PLAYLIST_SONG_CROSS_REF : "in"
    PLAYLIST ||--o{ PLAYLIST_SONG_CROSS_REF : "contains"
    SONG }o--|| ARTIST : "by"
    SONG }o--|| ALBUM : "in"
    ALBUM }o--|| ARTIST : "by"
    SONG ||--o{ HISTORY_ENTRY : "played"
    
    SONG {
        Long id PK
        String title
        Long artistId FK
        Long albumId FK
        Long duration
        String filePath UK
        Boolean isFavorite
        String artworkUri
        Long addedAt
        Int trackNumber
        Int year
        String genre
        String mimeType
        Long fileSize
    }
    
    ARTIST {
        Long id PK
        String name
    }
    
    ALBUM {
        Long id PK
        String name
        Long artistId FK
        String artworkUri
        Int year
        Int trackCount
    }
    
    PLAYLIST {
        Long id PK
        String name
        Long createdAt
    }
    
    PLAYLIST_SONG_CROSS_REF {
        Long playlistId PK-FK
        Long songId PK-FK
        Int position
        Long addedAt
    }
    
    HISTORY_ENTRY {
        Long id PK
        Long songId FK
        Long playedAt
    }
    
    APP_SETTINGS {
        Int id PK
        String themeMode
        Boolean useDynamicColor
        Boolean shuffleEnabled
        String repeatMode
    }
```

---

## 3.12. Database Operations Examples

### Adding a Song (with Artist and Album)

```kotlin
suspend fun addSongToDatabase(
    songDao: SongDao,
    artistDao: ArtistDao,
    albumDao: AlbumDao,
    filePath: String,
    metadata: AudioMetadata
) {
    // 1. Get or create artist
    val artistId = artistDao.getArtistByName(metadata.artist)?.id
        ?: artistDao.insert(ArtistEntity(name = metadata.artist))
    
    // 2. Get or create album
    val albumId = albumDao.getAlbumByNameAndArtist(metadata.album, artistId)?.id
        ?: albumDao.insert(AlbumEntity(
            name = metadata.album,
            artistId = artistId,
            year = metadata.year
        ))
    
    // 3. Insert song
    songDao.insert(SongEntity(
        title = metadata.title,
        artistId = artistId,
        albumId = albumId,
        duration = metadata.duration,
        filePath = filePath,
        mimeType = metadata.mimeType,
        fileSize = metadata.fileSize
    ))
    
    // 4. Update album track count
    albumDao.updateTrackCount(albumId)
}
```

### Adding Song to Playlist

```kotlin
suspend fun addSongToPlaylist(
    playlistDao: PlaylistDao,
    playlistId: Long,
    songId: Long
) {
    // Get next position
    val maxPosition = playlistDao.getMaxPosition(playlistId)
    val nextPosition = maxPosition + 1
    
    // Add to playlist
    playlistDao.addSongToPlaylist(
        PlaylistSongCrossRef(
            playlistId = playlistId,
            songId = songId,
            position = nextPosition
        )
    )
}
```

### Removing Song from Playlist

```kotlin
suspend fun removeSongFromPlaylist(
    playlistDao: PlaylistDao,
    playlistId: Long,
    songId: Long
) {
    // Get song position
    val songs = playlistDao.getPlaylistSongs(playlistId).first()
    val crossRef = songs.find { it.songId == songId } ?: return
    
    // Remove song
    playlistDao.removeSongFromPlaylist(crossRef)
    
    // Decrement positions of songs after removed song
    playlistDao.decrementPositionsAfter(playlistId, crossRef.position)
}
```

### Recording Listen History

```kotlin
suspend fun recordListenHistory(
    historyDao: HistoryDao,
    songId: Long
) {
    // Check if already played recently (within 5 minutes)
    val fiveMinutesAgo = System.currentTimeMillis() - 300_000
    val wasRecentlyPlayed = historyDao.wasPlayedRecently(songId, fiveMinutesAgo)
    
    if (!wasRecentlyPlayed) {
        historyDao.insert(
            HistoryEntryEntity(
                songId = songId,
                playedAt = System.currentTimeMillis()
            )
        )
    }
}
```

---

## 3.13. Performance Considerations

### Query Optimization
- Use indices for frequently filtered/sorted columns
- Prefer `Flow` for UI updates (efficient, lifecycle-aware)
- Use `@Transaction` for multi-table operations
- Limit result sets with `LIMIT` clause

### Batch Operations
```kotlin
// ✅ Good: Batch insert
songDao.insertAll(listOf(song1, song2, song3))

// ❌ Bad: Individual inserts in loop
songs.forEach { song -> songDao.insert(song) }
```

### Memory Management
- Don't load all songs at once for large libraries
- Use pagination for very large result sets (Phase 2)
- Release resources properly

### Database Size Management
- Periodically clean old history entries
- Limit history to 500 entries
- Consider vacuum operations (Phase 2)

---

## 3.14. Quick Reference Checklist

Before implementing database:

- [ ] Room dependency added to build.gradle
- [ ] KSP plugin configured
- [ ] All entity classes defined with @Entity
- [ ] All DAOs defined with @Dao
- [ ] Database class created with @Database
- [ ] Type converters implemented
- [ ] Foreign key indices added
- [ ] Unique constraints on file_path
- [ ] Default entities (Unknown Artist/Album) insertion logic
- [ ] Hilt module for database injection
- [ ] Migration strategy planned
- [ ] exportSchema enabled
- [ ] Unit tests written for DAOs

---

**End of Database Schema Specification Document**
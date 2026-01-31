package com.prj.musicft.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 1 to version 2
 * Adds media_id column to songs table and updates indices
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Create a new temporary table with the new schema
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS songs_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                media_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                artist_id INTEGER NOT NULL,
                album_id INTEGER NOT NULL,
                duration INTEGER NOT NULL,
                file_path TEXT NOT NULL,
                is_favorite INTEGER NOT NULL DEFAULT 0,
                artwork_uri TEXT,
                added_at INTEGER NOT NULL,
                track_number INTEGER,
                year INTEGER,
                genre TEXT,
                mime_type TEXT NOT NULL,
                file_size INTEGER NOT NULL,
                FOREIGN KEY(artist_id) REFERENCES artists(id) ON DELETE SET DEFAULT,
                FOREIGN KEY(album_id) REFERENCES albums(id) ON DELETE SET DEFAULT
            )
        """.trimIndent())

        // Step 2: Copy data from old table to new table
        // For existing songs without media_id, we'll use a placeholder value (0)
        // These will be re-scanned and updated with correct media_id
        database.execSQL("""
            INSERT INTO songs_new (
                id, media_id, title, artist_id, album_id, duration, file_path,
                is_favorite, artwork_uri, added_at, track_number, year, genre,
                mime_type, file_size
            )
            SELECT 
                id, 0 as media_id, title, artist_id, album_id, duration, file_path,
                is_favorite, artwork_uri, added_at, track_number, year, genre,
                mime_type, file_size
            FROM songs
        """.trimIndent())

        // Step 3: Drop the old table
        database.execSQL("DROP TABLE songs")

        // Step 4: Rename the new table
        database.execSQL("ALTER TABLE songs_new RENAME TO songs")

        // Step 5: Create indices
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_songs_media_id ON songs(media_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_songs_file_path ON songs(file_path)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_songs_artist_id ON songs(artist_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_songs_album_id ON songs(album_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_songs_is_favorite ON songs(is_favorite)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_songs_added_at ON songs(added_at)")
    }
}

/**
 * Migration from version 2 to version 3
 * Adds default values to foreign key columns
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // SQLite doesn't support ALTER COLUMN to add DEFAULT values
        // We need to recreate the tables with the new schema
        
        // 1. Recreate albums table with default value for artist_id
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS albums_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                artist_id INTEGER NOT NULL DEFAULT 1,
                artwork_uri TEXT,
                year INTEGER,
                track_count INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(artist_id) REFERENCES artists(id) ON DELETE SET DEFAULT
            )
        """.trimIndent())
        
        database.execSQL("""
            INSERT INTO albums_new (id, name, artist_id, artwork_uri, year, track_count)
            SELECT id, name, artist_id, artwork_uri, year, track_count FROM albums
        """.trimIndent())
        
        database.execSQL("DROP TABLE albums")
        database.execSQL("ALTER TABLE albums_new RENAME TO albums")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_albums_name ON albums(name)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_albums_artist_id ON albums(artist_id)")
        
        // 2. Recreate songs table with default values for artist_id and album_id
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS songs_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                media_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                artist_id INTEGER NOT NULL DEFAULT 1,
                album_id INTEGER NOT NULL DEFAULT 1,
                duration INTEGER NOT NULL,
                file_path TEXT NOT NULL,
                is_favorite INTEGER NOT NULL DEFAULT 0,
                artwork_uri TEXT,
                added_at INTEGER NOT NULL,
                track_number INTEGER,
                year INTEGER,
                genre TEXT,
                mime_type TEXT NOT NULL,
                file_size INTEGER NOT NULL,
                FOREIGN KEY(artist_id) REFERENCES artists(id) ON DELETE SET DEFAULT,
                FOREIGN KEY(album_id) REFERENCES albums(id) ON DELETE SET DEFAULT
            )
        """.trimIndent())
        
        database.execSQL("""
            INSERT INTO songs_new (
                id, media_id, title, artist_id, album_id, duration, file_path,
                is_favorite, artwork_uri, added_at, track_number, year, genre,
                mime_type, file_size
            )
            SELECT 
                id, media_id, title, artist_id, album_id, duration, file_path,
                is_favorite, artwork_uri, added_at, track_number, year, genre,
                mime_type, file_size
            FROM songs
        """.trimIndent())
        
        database.execSQL("DROP TABLE songs")
        database.execSQL("ALTER TABLE songs_new RENAME TO songs")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_songs_media_id ON songs(media_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_songs_file_path ON songs(file_path)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_songs_artist_id ON songs(artist_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_songs_album_id ON songs(album_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_songs_is_favorite ON songs(is_favorite)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_songs_added_at ON songs(added_at)")
    }
}

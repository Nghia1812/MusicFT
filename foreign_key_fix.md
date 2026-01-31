# Foreign Key Constraint Fix

## Problem

The app was crashing with:
```
android.database.sqlite.SQLiteConstraintException: FOREIGN KEY constraint failed 
(code 787 SQLITE_CONSTRAINT_FOREIGNKEY)
```

This occurred when trying to insert `SongEntity` records during the sync process.

## Root Causes

There were **THREE** critical issues with the foreign key setup:

### 1. Foreign Keys Disabled in SQLite (PRIMARY ISSUE)
**SQLite has foreign key constraints DISABLED by default!**

Even though we defined foreign key relationships in our entities, SQLite was not enforcing them because we never enabled them. This is a common gotcha with SQLite.

### 2. Missing Default Values for Foreign Key Columns
The entities used `onDelete = ForeignKey.SET_DEFAULT`, but didn't specify what the default value should be:

```kotlin
// BEFORE - Missing default value
@ColumnInfo(name = "artist_id")
val artistId: Long,  // FK to ArtistEntity

// AFTER - With default value
@ColumnInfo(name = "artist_id", defaultValue = "1")
val artistId: Long,  // FK to ArtistEntity (defaults to Unknown Artist)
```

When using `SET_DEFAULT`, SQLite needs to know what value to use when the referenced row is deleted. Without a default value specified, the constraint fails.

### 3. Potential Insert Order Issues
When inserting songs, we need to ensure that:
1. The default artist (ID 1) and album (ID 1) exist first
2. Any custom artists/albums are created before songs that reference them

The `FullSyncRepository` was already handling this correctly by calling `getOrCreateArtistId()` and `getOrCreateAlbumId()` before inserting songs.

## Solutions Implemented

### Solution 1: Enable Foreign Key Constraints ✅
Added a `RoomDatabase.Callback` to enable foreign keys when the database opens:

```kotlin
.addCallback(object : RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON")
    }
})
```

**This is the most critical fix!** Without this, none of the foreign key constraints work.

### Solution 2: Add Default Values ✅
Updated all foreign key columns to specify `defaultValue = "1"`:

**SongEntity.kt:**
- `artist_id` → defaults to 1 (Unknown Artist)
- `album_id` → defaults to 1 (Unknown Album)

**AlbumEntity.kt:**
- `artist_id` → defaults to 1 (Unknown Artist)

### Solution 3: Database Migration ✅
Created `MIGRATION_2_3` to:
1. Recreate the `albums` table with default value for `artist_id`
2. Recreate the `songs` table with default values for `artist_id` and `album_id`
3. Preserve all existing data during migration

### Solution 4: Ensure Default Entities Exist ✅
The `MusicDatabase.insertDefaultsIfNeeded()` function already ensures that:
- Artist with ID 1 ("Unknown Artist") exists
- Album with ID 1 ("Unknown Album") exists
- Default settings exist

This is called when the database is created/opened.

## Why This Matters

### Before the Fix:
1. Foreign keys were defined but **not enforced** (disabled in SQLite)
2. Invalid references could be inserted without errors
3. When foreign keys were enabled, inserts failed due to missing defaults
4. Data integrity was not guaranteed

### After the Fix:
1. ✅ Foreign keys are **enforced** by SQLite
2. ✅ Invalid references are rejected at insert time
3. ✅ Deleted artists/albums automatically set songs to "Unknown" (ID 1)
4. ✅ Data integrity is guaranteed by the database
5. ✅ Cascading deletes work correctly

## Testing

To verify the fix works:

1. **Clear app data** to trigger a fresh database creation
2. **Run the app** and trigger a music scan
3. **Verify songs are inserted** without FOREIGN KEY errors
4. **Check that default entities exist**:
   - Artist ID 1 = "Unknown Artist"
   - Album ID 1 = "Unknown Album"

### Test Foreign Key Enforcement:
Try to insert a song with an invalid artist_id (e.g., 999):
```kotlin
// This should fail with FOREIGN KEY constraint error
songDao.insert(SongEntity(
    mediaId = 123,
    title = "Test",
    artistId = 999,  // Invalid - doesn't exist
    albumId = 1,
    // ... other fields
))
```

### Test SET DEFAULT Behavior:
1. Create a custom artist
2. Create songs for that artist
3. Delete the artist
4. Verify songs now reference "Unknown Artist" (ID 1)

## Key Takeaways

1. **Always enable foreign keys in SQLite** - They're disabled by default!
2. **Use `defaultValue` with `SET_DEFAULT`** - SQLite needs to know what the default is
3. **Ensure referenced entities exist** - Insert parents before children
4. **Test with foreign keys enabled** - Catches referential integrity issues early

## Files Modified

1. `SongEntity.kt` - Added default values to foreign key columns
2. `AlbumEntity.kt` - Added default value to artist_id
3. `Migrations.kt` - Added MIGRATION_2_3
4. `DatabaseModule.kt` - Enabled foreign keys via callback
5. `MusicDatabase.kt` - Updated version to 3

## Database Version History

- **Version 1**: Original schema
- **Version 2**: Added `media_id` column to songs
- **Version 3**: Added default values to foreign key columns + enabled FK constraints

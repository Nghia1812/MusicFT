## *Entities Overview*
 
- Songs (with metadata)
- Playlists (with many-to-many between playlists and songs)
- Listening history (chronological, with timestamps)
- Favorites (can be a flag in Song or a dedicated table)
- Albums and Artists (for organization/filtering)
- App settings (theme, etc.)
 
---
 
## *Class Diagram*
 
```
┌─────────────┐        ┌─────────────┐        ┌─────────────┐
│   Artist    │◄───┐   │    Album    │        │    Song     │
│─────────────│    │   │─────────────│        │─────────────│
│ id (PK)     │    │   │ id (PK)     │◄───┐   │ id (PK)     │
│ name        │    └───┤ artist_id   │    └───┤ artist_id   │
└─────────────┘        │ name        │        │ album_id    │
                       │ artwork_uri │        │ title       │
                       │ year        │        │ duration    │
                       │ track_count │        │ file_path   │
                       └─────────────┘        │ is_favorite │
                                              │ artwork_uri │
                                              │ added_at    │
┌─────────────┐                              └──────┬──────┘
│  Playlist   │                                     │
│─────────────│        ┌──────────────────┐         │
│ id (PK)     │◄───────┤ PlaylistSong     │◄────────┘
│ name        │        │ CrossRef         │
│ created_at  │        │──────────────────│
└─────────────┘        │ playlist_id (FK) │
                       │ song_id (FK)     │
        ┌──────────────│ position         │
        │              │ added_at         │
        │              └──────────────────┘
        │              
        │              ┌─────────────────┐
        │              │ HistoryEntry    │
        │              │─────────────────│
        └──────────────┤ song_id (FK)    │
                       │ played_at       │
                       └─────────────────┘
                       
                       ┌─────────────────┐
                       │  AppSettings    │
                       │─────────────────│
                       │ id = 1          │
                       │ theme_mode      │
                       │ use_dynamic_clr │
                       │ shuffle_enabled │
                       │ repeat_mode     │
                       └─────────────────┘
```
 
## *Explanation*
 
### *1. Song*
- Stores core metadata (title, artist, album, duration, filePath, artworkUri)
- isFavorite: a boolean flag, as per your specs (can also be a cross-table, but this is simpler and efficient for local-only)
- addedAt: when the song was first scanned/added
 
### *2. Playlist & PlaylistSongCrossRef*
- Playlists are stored in Playlist
- Many-to-many relationship with songs via PlaylistSongCrossRef
- This allows for:
  - Songs in multiple playlists
  - Order in playlist (add an order: Int field if you want to support sorting)
 
### *3. Album & Artist*
- Normalized entities for lookup/filtering/sorting
- Songs reference these by ID
 
### *4. HistoryEntry*
- Stores every play with timestamp (playedAt)
- References the played song
 
### *5. AppSetting*
- Generic key-value table for app settings (theme, etc.)
 
---

## *Diagram in PlantUML Syntax (for Visualization Tools)*
 
plantuml
@startuml
entity Song {
  Long id PK
  String title
  Long artistId
  Long albumId
  Long duration
  String filePath
  Boolean isFavorite
  String artworkUri
  Long addedAt
}
entity Playlist {
  Long id PK
  String name
  Long createdAt
}
entity PlaylistSongCrossRef {
  Long playlistId PK, FK
  Long songId PK, FK
}
entity Album {
  Long id PK
  String name
}
entity Artist {
  Long id PK
  String name
}
entity HistoryEntry {
  Long id PK
  Long songId FK
  Long playedAt
}
entity AppSetting {
  String key PK
  String value
}
 
Song }o--|| Artist : artistId
Song }o--|| Album : albumId
Song ||--o{ PlaylistSongCrossRef : songId
Playlist ||--o{ PlaylistSongCrossRef : playlistId
Song ||--o{ HistoryEntry : id
@enduml
 
---
 
## *Summary Table*
 
| Entity                | Description                                      |
|-----------------------|--------------------------------------------------|
| Song                  | Local audio file metadata + favorite flag        |
| Playlist              | User playlists                                   |
| PlaylistSongCrossRef  | Songs in playlists (many-to-many)                |
| Album                 | Albums, for grouping/filtering                   |
| Artist                | Artists, for grouping/filtering                  |
| HistoryEntry          | Listening history (song + timestamp)             |
| AppSetting            | Key-value for app settings (e.g., theme)         |
 
---
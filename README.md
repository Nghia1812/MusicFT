# Music-FT 🎵

**Music-FT** (Music File Tracker) is a modern, fully offline Android music player that provides a fast, smooth listening experience using Material 3 Design principles. Built with Kotlin and Jetpack Compose, it scans local audio files and organizes them intelligently without requiring any internet connection.

---

## 📱 Tech Stack

### Core Technologies
- **Language**: Kotlin 100%
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Build Tool**: Gradle with Kotlin DSL

### UI Framework
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material 3** - Google's latest design system with dynamic color support
- **Coil** - Efficient image loading for album artwork

### Architecture & Libraries
- **Architecture Pattern**: Clean Architecture + MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt (Dagger-based)
- **Database**: Room - SQLite object mapping library
- **Async Programming**: Kotlin Coroutines + Flow
- **Navigation**: Jetpack Navigation Compose
- **Build Configuration**: KSP (Kotlin Symbol Processing)

### Media Playback
- **Media3 ExoPlayer** - Modern media playback engine
- **Media3 Session** - MediaSession integration for system controls
- **Media3 UI** - Playback UI components

### Development Tools
- **Timber** - Logging framework for debugging
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17 (required for AGP 8.x+)

---

## 🏗️ Architecture

The application follows **Clean Architecture** principles with clear separation of concerns across three layers:

### Layer Structure

```
┌──────────────────────────────────────────┐
│     Presentation Layer (UI)              │
│  • Jetpack Compose screens              │
│  • ViewModels for state management      │
│  • UI State classes                     │
├──────────────────────────────────────────┤
│     Domain Layer (Business Logic)        │
│  • Use Cases (Interactors)              │
│  • Domain Models                        │
│  • Repository Interfaces                │
├──────────────────────────────────────────┤
│     Data Layer (Data Sources)            │
│  • Repository Implementations           │
│  • Room Database (DAOs)                 │
│  • Data Models (Entities)                │
│  • MediaStore Scanner                    │
└──────────────────────────────────────────┘
```

### Directory Organization

```
app/src/main/java/com/prj/musicft/
├── presentation/          # UI Layer - Compose screens & ViewModels
│   ├── home/             # Home/Library screen
│   ├── player/           # Full player & Mini player
│   ├── playlist/         # Playlist management
│   ├── search/           # Search functionality
│   ├── library/          # Library views (albums, artists)
│   ├── settings/         # Settings screen
│   └── splash/           # Splash & permission screens
├── domain/               # Business Logic Layer (Pure Kotlin)
│   ├── model/           # Domain models
│   ├── usecase/         # Business logic operations
│   └── repository/      # Repository interfaces
├── data/                 # Data Source Layer
│   ├── local/           # Room database
│   │   ├── dao/         # Data Access Objects
│   │   ├── entity/      # Database entities
│   │   └── database/    # Database configuration
│   └── repository/      # Repository implementations
├── di/                   # Dependency Injection modules
│   ├── DatabaseModule   # Database & DAO providers
│   ├── RepositoryModule # Repository bindings
│   └── MediaModule      # ExoPlayer & MediaSession
└── ui/                   # Shared UI components & theme
```

### Key Architectural Principles

#### 1. **Unidirectional Data Flow (UDF)**
```
User Action → ViewModel → Update State → Recompose UI
```
- State flows down from ViewModels to Composables
- Events flow up from UI to ViewModels
- Single source of truth for each screen

#### 2. **Dependency Rule**
- **Domain layer** has NO dependencies (Pure Kotlin)
- **Data layer** depends on Domain (implements interfaces)
- **Presentation layer** depends on Domain (uses Use Cases)

#### 3. **Reactive Programming**
- Database queries return `Flow<T>` for reactive updates
- ViewModels use `StateFlow` for UI state
- Automatic UI updates when data changes

#### 4. **Separation of Concerns**
- **Composables**: UI rendering only (stateless where possible)
- **ViewModels**: State management and UI logic
- **Use Cases**: Single-responsibility business operations
- **Repositories**: Data access abstraction
- **DAOs**: Database operations

---

## 🗄️ Database Relationships

### Entity-Relationship Diagram

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

### Core Entities

#### 1. **Song** (Main Entity)
- **Primary Key**: `id` (auto-generated)
- **Foreign Keys**: 
  - `artist_id` → Artist (ON DELETE SET_DEFAULT)
  - `album_id` → Album (ON DELETE SET_DEFAULT)
- **Unique Constraint**: `file_path` (prevents duplicate scans)
- **Indices**: artist_id, album_id, is_favorite, added_at
- **Purpose**: Stores audio file metadata and playback information

#### 2. **Artist**
- **Primary Key**: `id`
- **No Foreign Keys**
- **Purpose**: Groups songs and albums by artist
- **Special**: Artist with `id=1` is "Unknown Artist" (default)

#### 3. **Album**
- **Primary Key**: `id`
- **Foreign Key**: `artist_id` → Artist (ON DELETE SET_DEFAULT)
- **Purpose**: Groups songs by album
- **Special**: Album with `id=1` is "Unknown Album" (default)

#### 4. **Playlist**
- **Primary Key**: `id`
- **No Foreign Keys**
- **Purpose**: User-created song collections

#### 5. **PlaylistSongCrossRef** (Junction Table)
- **Composite Primary Key**: `(playlist_id, song_id)`
- **Foreign Keys**:
  - `playlist_id` → Playlist (ON DELETE CASCADE)
  - `song_id` → Song (ON DELETE CASCADE)
- **Additional Fields**: `position` (ordering), `added_at` (timestamp)
- **Purpose**: Many-to-many relationship between playlists and songs

#### 6. **HistoryEntry**
- **Primary Key**: `id` (auto-generated)
- **Foreign Key**: `song_id` → Song (ON DELETE CASCADE)
- **Purpose**: Track listening history
- **Tracking Rule**: Record when song plays >30 seconds

#### 7. **AppSettings** (Single-Row Table)
- **Primary Key**: `id` (always = 1)
- **Purpose**: Store app configuration (theme, playback settings)
- **Design**: Single row updated for preferences

### Relationship Types

#### One-to-Many Relationships
1. **Artist → Albums** (One artist has many albums)
2. **Artist → Songs** (One artist has many songs)
3. **Album → Songs** (One album has many songs)
4. **Song → HistoryEntries** (One song appears in history multiple times)

#### Many-to-Many Relationship
- **Playlist ↔ Songs** (via `PlaylistSongCrossRef`)
  - A playlist contains many songs
  - A song can belong to many playlists
  - Position field enables custom ordering per playlist

### Foreign Key Behaviors

#### ON DELETE CASCADE
- **PlaylistSongCrossRef**: When playlist deleted → removes all associations
- **PlaylistSongCrossRef**: When song deleted → removes from all playlists
- **HistoryEntry**: When song deleted → removes all history entries

#### ON DELETE SET_DEFAULT
- **Song.artist_id**: When artist deleted → set to "Unknown Artist" (id=1)
- **Song.album_id**: When album deleted → set to "Unknown Album" (id=1)
- **Album.artist_id**: When artist deleted → set to "Unknown Artist" (id=1)

### Data Flow

#### Music Scanning Flow
```
MediaStore API
     ↓
Extract Metadata
     ↓
Create/Find Artist Entity
     ↓
Create/Find Album Entity
     ↓
Create Song Entity (with FKs)
     ↓
Store in Room Database
```

#### Playlist Management Flow
```
User creates Playlist
     ↓
Playlist Entity inserted
     ↓
User adds Songs
     ↓
PlaylistSongCrossRef entries created
  (with position = MAX(position) + 1)
     ↓
Reorder/Remove updates position field
```

---

## 🚀 Key Features

- ✅ **100% Offline** - No internet required, complete privacy
- ✅ **Local Music Scanning** - Automatically discovers and catalogs audio files
- ✅ **Background Playback** - Music continues when app is minimized
- ✅ **Material 3 Design** - Modern UI with dynamic color support
- ✅ **Playlist Management** - Create, edit, and organize custom playlists
- ✅ **Favorites** - Quick access to your preferred songs
- ✅ **Listening History** - Track recently played songs
- ✅ **Search** - Find songs, artists, and albums instantly
- ✅ **Mini Player** - Persistent controls during navigation
- ✅ **System Integration** - Lock screen controls, notification controls
- ✅ **Theme Support** - Light, Dark, and System themes

---

## 📋 Requirements

- **Android Device**: Android 7.0 (API 24) or higher
- **Permissions**: 
  - Storage access (for scanning audio files)
  - Notification (for playback controls)
- **Storage**: Minimum 50MB for app + database

---

## 🔧 Building the Project

1. **Clone the repository**
```bash
git clone <repository-url>
cd musicFT
```

2. **Open in Android Studio**
   - Use Android Studio Hedgehog (2023.1.1) or later
   - Ensure JDK 17 is installed

3. **Sync Gradle**
   - Let Android Studio download dependencies
   - Build should complete without errors

4. **Run on Device/Emulator**
   - Connect Android device or start emulator
   - Click Run (Shift+F10)

---

## 📚 Additional Documentation

- [`product_spec.md`](product_spec.md) - Complete product specifications
- [`tech_stack.md`](tech_stack.md) - Detailed technical stack documentation
- [`DB_schema.md`](DB_relation.md) - Comprehensive database schema
- [`transaction_spec.md`](transaction_spec.md) - Transaction and workflow specifications

---

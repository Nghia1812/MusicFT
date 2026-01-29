# 1. Product Specifications - Music-FT

## Document Information
- **Purpose**: Complete product specification for AI-assisted development
- **Target Audience**: AI Development Agent, Development Team

---

## 1.1. Product Overview

### Application Name
**Music-FT** (Working title - Music File Tracker)

### Product Type
Offline Music Player for Android

### Primary Goal
Provide a **fast, modern, fully offline music player** that scans local audio files, organizes them intelligently, and offers a smooth listening experience using **Material 3 UI principles**.

### Target Platform
- **Operating System**: Android
- **Minimum SDK**: API 26 (Android 8.0) - Covers ~95% of active devices
- **Target SDK**: API 34 (Android 14) - Required by Google Play Store
- **Compile SDK**: API 35 (or latest stable at build time)

### Target Users
- Users who store music locally on their device
- Users who prefer offline playback over streaming services
- Power users who manage playlists and libraries manually
- Privacy-conscious users who want no internet dependency
- Users in areas with limited or expensive internet connectivity

---

## 1.2. Core Value Proposition

- **100% Offline**: No internet required after installation - complete privacy
- **Local-first**: Scans and manages music stored on device
- **Modern UX**: Material 3 design, smooth animations, mini player
- **Fast & Lightweight**: Optimized performance, minimal battery drain
- **Developer-grade architecture**: Scalable, testable, maintainable codebase

---

## 1.3. Functional Specifications

### A. Core Playback Features

#### A1. Local Audio Scanning
**Purpose**: Discover and catalog all music files on the device

**Supported Audio Formats**:
- **Phase 1**: 
  - `mp3` (MPEG Audio Layer 3)
  - `flac` (Free Lossless Audio Codec)
- **Future extensible**: `wav`, `aac`, `ogg`, `m4a`

**Metadata Extraction** (using MediaMetadataRetriever):
- Title (fallback: filename without extension)
- Artist (fallback: "Unknown Artist")
- Album (fallback: "Unknown Album")
- Duration (milliseconds)
- Album artwork (fallback: placeholder image)
- Track number (optional)
- Year (optional)
- Genre (optional)

**Scanning Behavior**:
- Initial scan on first launch
- Manual re-scan option in settings
- Incremental scan for new files (future enhancement)
- Background thread execution (non-blocking UI)
- Progress indicator during scan

**Persistence**:
- All scanned metadata stored in local Room database
- File path stored for playback reference
- Duplicate prevention via unique file path constraint

#### A2. Audio Playback Controls
**Basic Controls**:
- Play
- Pause
- Resume (from last position)
- Next track
- Previous track
- Seek (scrubbing timeline with real-time position updates)

**Advanced Controls** (Phase 1):
- Shuffle mode (on/off)
- Repeat mode (off/one/all)

**Playback Modes**:
- Play single song
- Play from album
- Play from artist
- Play from playlist
- Play from favorites
- Play from queue

#### A3. Playback Engine
**Media Engine**: Media3 (ExoPlayer)

**Architecture**:
- Foreground Service for background playback
- MediaSession integration for system controls
- MediaNotification for lock screen and notification shade
- Audio focus handling (pause on phone calls, duck on notifications)

**Supported Features**:
- Background playback (music continues when app minimized)
- Playback while screen is off
- Notification media controls (play/pause/next/previous)
- Lock screen controls
- Bluetooth/headphone controls support
- Audio focus management
- Lifecycle-aware playback state restoration

**State Persistence**:
- Save current song position
- Restore playback queue after app restart
- Remember shuffle/repeat settings

---

### B. Library Management

#### B1. Music Library
**Display Options**:
- **Songs View**: List of all scanned songs
- **Albums View**: Grid/list of albums with artwork
- **Artists View**: List of artists
- **Playlists View**: User-created playlists

**Sorting Options** (per view):
- **Songs**: Title (A-Z), Artist, Album, Date Added, Duration
- **Albums**: Title (A-Z), Artist, Year, Date Added
- **Artists**: Name (A-Z), Number of songs

**Filtering**:
- Search by song title, artist, or album
- Filter favorites
- Local-only search (no network)

**Display Information**:
- Song: Title, Artist, Album, Duration
- Album: Album art, Album name, Artist, Track count, Year
- Artist: Artist name, Album count, Song count

#### B2. Favorites
**Purpose**: Quick access to frequently played or preferred songs

**Functionality**:
- Mark/unmark songs as "Favorite" (star icon)
- Favorites persist in database (boolean flag on Song entity)
- Dedicated "Favorites" screen/tab
- Filter/sort favorites independently

**User Experience**:
- Single tap to toggle favorite status
- Visual indicator (filled/outlined star icon)
- Immediate UI update with smooth animation

#### B3. Listening History
**Purpose**: Track what user has listened to recently

**Tracking**:
- Record listening event when song plays for >30 seconds
- Store: Song ID, Timestamp (epoch milliseconds)
- No duplicate entries for same song within 5 minutes

**Display**:
- "Recent" or "History" screen
- Chronological order (most recent first)
- Show up to 50 recent entries
- Display: Song details, "Played X minutes/hours/days ago"

**Privacy**:
- Local storage only
- Optional: Clear history function

---

### C. Playlist Management

#### C1. Playlist CRUD
**Create Playlist**:
- User provides name (1-100 characters)
- Creates empty playlist
- Auto-generated creation timestamp

**Rename Playlist**:
- Edit playlist name
- Validate non-empty, max 100 characters

**Delete Playlist**:
- Confirm before deletion
- Cascade delete playlist-song associations
- Does NOT delete actual songs

**View Playlist Details**:
- Display playlist name, creation date, song count
- Show all songs in playlist with order

#### C2. Playlist Content Management
**Add Song(s) to Playlist**:
- Single song: Long-press → "Add to playlist"
- Multiple songs: Select mode → "Add to playlist"
- Choose target playlist from dialog
- Songs added at end of playlist (highest position + 1)

**Remove Song from Playlist**:
- Swipe to delete or menu option
- Does NOT delete song from library
- Updates positions of remaining songs

**Reorder Songs**:
- Drag and drop to reorder
- Update position values in database
- Smooth animation during reorder

---

### D. User Interface Features

#### D1. Themes
**Theme Options**:
- **Light Mode**: Light background, dark text
- **Dark Mode**: Dark background, light text
- **System Theme**: Follow Android system setting (default)

**Dynamic Color** (Android 12+):
- Material You integration
- Extract colors from wallpaper
- User can enable/disable in settings

**Theme Persistence**:
- Save user preference in database
- Apply theme immediately on selection

#### D2. Mini Player
**Purpose**: Persistent playback control during navigation

**Visibility**:
- Shown at bottom of screen when music is playing
- Hidden when no active playback
- Visible across all screens (except full player)

**Display Information**:
- Album artwork (small thumbnail)
- Song title (scrolling if too long)
- Artist name

**Controls**:
- Play/Pause button
- (Optional) Next button

**Interaction**:
- Tap anywhere on mini player → Open full player
- Swipe down to dismiss → Pause playback (optional)

#### D3. Full Player Screen (Now Playing)
**Layout**:
- Large album artwork (centered, with shadow/elevation)
- Song title (large, bold)
- Artist name (medium, secondary color)
- Album name (small, secondary color)

**Playback Progress**:
- Seek bar (scrubber)
- Current time (left)
- Total duration (right)
- Real-time position updates

**Controls** (Material 3 icon buttons):
- Shuffle toggle
- Previous track
- Play/Pause (large, prominent)
- Next track
- Repeat mode toggle

**Additional Features**:
- Favorite toggle (star icon)
- "Add to playlist" button
- Queue management (view/edit upcoming songs)

**Animations**:
- Smooth fade-in on open
- Album art rotation on play (optional)
- Button press ripple effects
- Seek bar thumb scaling
- Material Motion guidelines

**Gesture Support** (optional):
- Swipe down to minimize to mini player
- Swipe left/right to skip tracks

---

## 1.4. Non-Functional Requirements

### A. Performance
**Response Times**:
- Initial music scan: Background, non-blocking (show progress)
- Playback actions (play/pause): <100ms response
- Screen navigation: <200ms transition
- Search/filter: <300ms for 1000+ songs

**Smoothness**:
- 60 FPS UI rendering (no frame drops)
- Smooth animations (Material Motion)
- No jank during scrolling

**Memory**:
- Efficient bitmap handling for album art
- Lazy loading for lists
- Proper caching strategy

**Battery**:
- Minimal drain during background playback
- Efficient sensor usage
- Wake lock only when necessary

### B. Reliability
**Playback Continuity**:
- Music continues reliably in background
- Handles audio focus changes correctly
- Resumes after phone calls

**State Recovery**:
- Restore playback state after app killed
- Restore queue after configuration changes (rotation)
- Handle storage permission changes gracefully

**Error Handling**:
- Graceful handling of corrupted files
- Recovery from missing files
- User-friendly error messages

### C. Compatibility
**Android Versions**:
- Full support: API 26-35 (Android 8.0 - 15+)
- Graceful degradation for older features

**Permissions**:
- Android 13+ permission model (READ_MEDIA_AUDIO)
- Scoped Storage compliance (Android 10+)
- Runtime permission requests with rationale

**Screen Sizes**:
- Portrait-first design for phones
- Functional on tablets (not optimized in Phase 1)
- Responsive layouts using Compose

### D. Architecture Constraints
**Must Use**:
- Android Architecture Components (ViewModel, LiveData/Flow, Room)
- Lifecycle-aware components
- Repository pattern
- Dependency Injection (Hilt)

**Must Support**:
- Unit testability (domain layer >70% coverage)
- Separation of concerns (Clean Architecture)
- Future modularization capability

---

## 1.5. System Integration

### A. Permissions Required

**Runtime Permissions** (must request):
- `READ_MEDIA_AUDIO` (Android 13+) - Access audio files
- `POST_NOTIFICATIONS` (Android 13+) - Show playback notifications

**Normal Permissions** (auto-granted):
- `FOREGROUND_SERVICE` - Background playback
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK` (Android 14+) - Media service type
- `WAKE_LOCK` - Keep CPU awake during playback

**Legacy Permissions** (if supporting API 26-32):
- `READ_EXTERNAL_STORAGE` (Android 6-12) - Access audio files

### B. Permission Handling Strategy
1. Check permissions on app launch
2. Request `READ_MEDIA_AUDIO` with rationale if denied
3. Request `POST_NOTIFICATIONS` on first playback
4. Handle denial gracefully (show explanation)
5. Provide settings deep-link for manual permission grant
6. Periodic permission check (user may revoke)

### C. Storage Access
- Use **MediaStore API** for file discovery
- Scoped storage compliant (no MANAGE_EXTERNAL_STORAGE needed)
- Respect user privacy (only access audio files)

---

## 1.6. Technology Stack Summary

### Core Technologies
- **Language**: Kotlin 100%
- **UI**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture + MVVM
- **Database**: Room
- **Playback**: Media3 (ExoPlayer)
- **DI**: Hilt (Dagger)
- **Async**: Coroutines + Flow
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil

### Build System
- Gradle (Kotlin DSL)
- KSP (Kotlin Symbol Processing)
- Version Catalog for dependencies

---

## 1.7. Constraints & Assumptions

### Constraints
- **Offline-only**: No streaming, no cloud sync, no network calls
- **Local database only**: Room (SQLite)
- **Single-user**: No multi-user support, no user accounts
- **Android-only**: No iOS, no cross-platform

### Assumptions
- User has granted required media permissions
- Audio files contain valid metadata (fallbacks provided)
- Device has sufficient storage for database
- User understands basic music player concepts

---

## 1.8. Out of Scope (Phase 1)

The following features are **explicitly excluded** from Phase 1:

- ❌ Online streaming services
- ❌ Lyrics fetching or display
- ❌ Audio equalizer / DSP effects
- ❌ Cross-device sync
- ❌ User accounts / authentication
- ❌ Social features (sharing, recommendations)
- ❌ Podcast support
- ❌ Radio stations
- ❌ Music download / purchase
- ❌ Advanced audio effects (bass boost, virtualizer)
- ❌ Sleep timer
- ❌ Gapless playback
- ❌ ReplayGain support
- ❌ Tablet-optimized layouts

---

## 1.9. Success Criteria (Phase 1)

### Functional Success
- ✅ App can scan local music from device storage
- ✅ App can play audio files reliably with Media3
- ✅ User can create, edit, and delete playlists
- ✅ User can mark songs as favorites
- ✅ App tracks listening history accurately
- ✅ Mini player and full player work correctly
- ✅ Background playback continues reliably
- ✅ Notification controls function properly

### Technical Success
- ✅ Architecture follows Clean Architecture principles
- ✅ UI matches Material 3 design guidelines
- ✅ No memory leaks or ANR (Application Not Responding)
- ✅ Unit test coverage >70% for domain layer
- ✅ Code is documented and maintainable
- ✅ Build succeeds without errors/warnings

### User Experience Success
- ✅ App launches in <2 seconds on average device
- ✅ Music scanning doesn't block UI
- ✅ Smooth animations at 60 FPS
- ✅ Intuitive navigation (users can find features without help)
- ✅ Accessible UI (content descriptions, readable text)

---

## 1.10. Development Phases

### Phase 1: Foundation (Weeks 1-2)
**Goals**: Set up project, core data layer, basic scanning

**Deliverables**:
- Project setup with Gradle, dependencies
- Database schema (Room entities, DAOs)
- Media scanner implementation
- Basic repository layer
- Hilt DI setup

### Phase 2: Playback Core (Weeks 3-4)
**Goals**: Implement playback engine, basic UI

**Deliverables**:
- Media3 playback service
- Foreground service with notification
- Basic library UI (songs list)
- ViewModel layer
- Play/pause functionality

### Phase 3: Library Features (Weeks 5-6)
**Goals**: Complete library management, playlists

**Deliverables**:
- Albums/Artists views
- Search and filtering
- Playlist CRUD
- Favorites functionality
- History tracking

### Phase 4: Player UI (Week 7)
**Goals**: Full player screen, mini player

**Deliverables**:
- Full player screen with controls
- Mini player component
- Seek bar with scrubbing
- Queue management

### Phase 5: Polish (Week 8)
**Goals**: Animations, themes, testing

**Deliverables**:
- Material Motion animations
- Theme support (light/dark/system)
- Dynamic color (Android 12+)
- Error handling
- Unit tests
- Bug fixes

---

## 1.11. Screen Structure & Navigation

### Navigation Graph
```
MainActivity (Single Activity)
├── BottomNavigation
│   ├── Library Tab (default)
│   │   ├── Songs (default)
│   │   ├── Albums
│   │   ├── Artists
│   │   └── Playlists
│   ├── Favorites Tab
│   └── History Tab
├── Playlist Detail Screen
├── Album Detail Screen
├── Artist Detail Screen
├── Now Playing (Full Player)
└── Settings Screen
```

### Persistent UI Components
- **Bottom Navigation Bar** (always visible except full player)
- **Mini Player** (shown when music is playing, bottom above nav bar)
- **Top App Bar** (screen title, search, menu)

---

## 1.12. Error Handling Scenarios

### User-Facing Errors
| Scenario | Handling |
|----------|----------|
| No audio files found | Show empty state with helpful message |
| Corrupted audio file | Skip file, log error, show toast |
| Missing metadata | Use fallback values (see A1) |
| Permission denied | Show rationale, deep-link to settings |
| File deleted while playing | Skip to next, show toast |
| Storage full | Show error during scan |
| Database error | Show error, offer to reset database |

### System Errors
| Scenario | Handling |
|----------|----------|
| Audio focus loss | Pause playback |
| Phone call | Pause playback, resume when call ends |
| Headphones unplugged | Pause playback (user preference) |
| Low battery | Continue playback (system handles) |
| App killed by system | Save state, restore on next launch |

---

## 1.13. Accessibility Requirements

### Must Support
- **TalkBack**: Screen reader support
- **Content Descriptions**: All icons and images
- **Minimum Touch Target**: 48dp for all interactive elements
- **Color Contrast**: WCAG AA compliance
- **Text Scaling**: Support system font size settings
- **Keyboard Navigation**: For external keyboard users

### Nice to Have (Phase 2+)
- Haptic feedback for actions
- Voice commands
- Large text mode

---

## 1.14. Privacy & Security

### Data Privacy
- **No data collection**: Zero telemetry, analytics, or tracking
- **No network access**: Offline-only app
- **Local storage only**: All data stored on device
- **No cloud backup**: User manages backups via Android

### Security
- **No authentication**: Single-user, local app
- **File access**: Limited to audio files via MediaStore
- **Database**: SQLite with no encryption (local user data)

---

## 1.15. Future Enhancements (Phase 2+)

**Potential features for future releases**:
- Lyrics display (synced/unsynced)
- Audio equalizer with presets
- Sleep timer
- Gapless playback
- Podcast support
- Tablet-optimized layouts
- Widget support
- Auto-playlists (smart playlists)
- Advanced search (by year, genre, duration)
- Crossfade between tracks
- Chromecast support
- File format conversion
- Tag editing
- Folder browsing

---

## Appendix: Glossary

**Terms used in this document**:

- **Media3**: Android's modern media playback framework (successor to ExoPlayer)
- **Room**: Android's SQLite object mapping library
- **Hilt**: Dependency injection framework for Android
- **Material 3**: Google's latest design system
- **Scoped Storage**: Android 10+ storage isolation model
- **MediaStore**: Android system API for accessing media files
- **Foreground Service**: Android service type for long-running tasks with notification
- **Audio Focus**: Android system for managing audio playback between apps
- **Clean Architecture**: Software design pattern separating concerns into layers

---

**End of Product Specifications Document**
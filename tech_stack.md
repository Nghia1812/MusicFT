# 2. Technical Stack Specification - Music-FT

## Document Information
- **Version**: 1.0
- **Purpose**: Complete technical specification for AI-assisted development
- **Target Audience**: AI Development Agent, Development Team

---

## 2.1. Platform & Environment

### Target Platform
- **Operating System**: Android
- **Minimum SDK**: API 26 (Android 8.0)
  - **Rationale**: Covers ~95% of active devices, supports modern APIs
  - **Trade-off**: Excludes Android 7 and below (~5% of users)
- **Target SDK**: API 34 (Android 14)
  - **Rationale**: Required by Google Play Store (August 2023+)
- **Compile SDK**: API 35 (or latest stable at build time)

### Device Scope
- **Primary**: Smartphones (portrait-first design)
- **Secondary**: Tablets (functional but not optimized in Phase 1)
  - Layouts must remain functional (no breakage)
  - Tablet optimization deferred to Phase 2

### Build Environment
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: JDK 17 (required for AGP 8.x+)
- **Gradle**: 8.2+

---

## 2.2. Programming Language

### Language
- **Kotlin** (mandatory, 100% Kotlin codebase)
- **Kotlin Version**: 1.9.22 or 2.0.0+ (prefer stable releases)

### Language Constraints & Best Practices

#### Idiomatic Kotlin
Use modern Kotlin features:
```kotlin
// ✅ Prefer data classes for immutable data
data class Song(val id: Long, val title: String)

// ✅ Use sealed classes for state modeling
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Song>) : UiState()
    data class Error(val message: String) : UiState()
}

// ✅ Use value classes for type-safe IDs (inline classes)
@JvmInline
value class SongId(val value: Long)

// ✅ Extension functions for reusable logic
fun Long.toFormattedDuration(): String { ... }
```

#### Null Safety
```kotlin
// ✅ Embrace null safety
val title: String? = song.metadata?.title

// ⚠️ Avoid !! operator (justify if used)
val title = song.title!! // Only if guaranteed non-null

// ✅ Use safe calls and Elvis operator
val title = song.title ?: "Unknown"
```

#### Immutability
```kotlin
// ✅ Prefer val over var
val songs = listOf(song1, song2)

// ✅ Use immutable collections
val playlist: List<Song> = getPlaylist() // Not MutableList

// ⚠️ Use var only when necessary
var currentPosition = 0L
```

#### Avoid Java Interop
- No Java code unless required by external libraries
- Prefer Kotlin-first libraries (e.g., MockK over Mockito)
- Use Kotlin coroutines over RxJava

---

## 2.3. Application Architecture

### Architecture Pattern
**Clean Architecture + MVVM (Model-View-ViewModel)**

### Layered Structure (Mandatory)

```
┌─────────────────────────────────────┐
│     Presentation Layer (UI)         │  ← Jetpack Compose + ViewModel
├─────────────────────────────────────┤
│     Domain Layer (Business Logic)   │  ← Use Cases + Models
├─────────────────────────────────────┤
│     Data Layer (Data Sources)       │  ← Repositories + Room + Scanner
└─────────────────────────────────────┘
```

### Layer Descriptions

#### A. Presentation Layer (UI)
**Technology**: Jetpack Compose + Material 3

**Components**:
- **Composables**: UI components (screens, components)
- **ViewModels**: State management, UI logic
- **UI State**: Immutable data classes representing UI state
- **UI Events**: User interactions

**Rules**:
- No business logic in Composables
- No direct data source access
- State-driven UI (unidirectional data flow)
- ViewModels use domain layer (use cases)

**Example Structure**:
```kotlin
// UI State
data class LibraryUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ViewModel
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getSongsUseCase: GetSongsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()
    
    init {
        loadSongs()
    }
    
    private fun loadSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getSongsUseCase().collect { songs ->
                _uiState.update { 
                    it.copy(songs = songs, isLoading = false) 
                }
            }
        }
    }
}

// Composable
@Composable
fun LibraryScreen(viewModel: LibraryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.error != null -> ErrorMessage(uiState.error!!)
        else -> SongList(uiState.songs)
    }
}
```

#### B. Domain Layer (Business Logic)
**Technology**: Pure Kotlin (no Android dependencies)

**Components**:
- **Use Cases (Interactors)**: Business logic operations
- **Domain Models**: Business entities (different from database entities)
- **Repository Interfaces**: Data access contracts

**Rules**:
- Pure Kotlin (testable without Android framework)
- No Android imports (android.*, androidx.*)
- Defines repository interfaces (implemented in data layer)
- Single Responsibility Principle per use case

**Example Structure**:
```kotlin
// Domain Model
data class Song(
    val id: SongId,
    val title: String,
    val artist: String,
    val duration: Duration
)

// Repository Interface
interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>
    suspend fun getSongById(id: SongId): Song?
    suspend fun updateFavoriteStatus(id: SongId, isFavorite: Boolean)
}

// Use Case
class GetSongsUseCase @Inject constructor(
    private val repository: SongRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return repository.getAllSongs()
    }
}

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: SongRepository
) {
    suspend operator fun invoke(songId: SongId) {
        val song = repository.getSongById(songId)
        song?.let {
            repository.updateFavoriteStatus(songId, !it.isFavorite)
        }
    }
}
```

#### C. Data Layer (Data Sources)
**Technology**: Room, MediaStore, File System

**Components**:
- **Repository Implementations**: Implement domain interfaces
- **Data Sources**: Room DAOs, Media Scanner
- **Data Models**: Database entities (different from domain models)
- **Mappers**: Convert between data and domain models

**Rules**:
- Implements repository interfaces from domain layer
- Maps data models ↔ domain models
- No business logic (only data operations)
- Handle data errors, provide fallbacks

**Example Structure**:
```kotlin
// Data Entity (Room)
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artistId: Long,
    val duration: Long // milliseconds
)

// Mapper
fun SongEntity.toDomain(): Song = Song(
    id = SongId(id),
    title = title,
    artist = "..." // fetch from artist table,
    duration = Duration.ofMillis(duration)
)

// Repository Implementation
class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao
) : SongRepository {
    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs()
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    override suspend fun getSongById(id: SongId): Song? {
        return songDao.getSongById(id.value)?.toDomain()
    }
}
```

---

## 2.4. UI Framework

### UI Toolkit
- **Jetpack Compose** (BOM version 2024.02.00 or later)
- **Material 3** (Material Design 3)

### UI Principles

#### Declarative UI
```kotlin
// ✅ Declarative - UI is a function of state
@Composable
fun SongItem(song: Song, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(song.title) },
        supportingContent = { Text(song.artist) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
```

#### Unidirectional Data Flow (UDF)
```
User Action → ViewModel → Update State → Recompose UI
    ↑                                         ↓
    └─────────────────────────────────────────┘
```

#### State Hoisting
```kotlin
// ✅ Hoist state to make composables reusable
@Composable
fun SearchBar(
    query: String,              // State comes from caller
    onQueryChange: (String) -> Unit  // Events sent to caller
) {
    TextField(
        value = query,
        onValueChange = onQueryChange
    )
}
```

### Theming

#### Material 3 Color Scheme
```kotlin
// Define color schemes
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    // ... other colors
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    // ... other colors
)

// Theme composable
@Composable
fun MusicFTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

#### Typography
```kotlin
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    // ... other text styles
)
```

### No XML Layouts
- **Zero XML layouts** - pure Compose
- Migration from XML not required (new project)
- XML only for resources (strings, colors, dimensions)

---

## 2.5. Dependency Injection

### DI Framework
**Hilt** (Dagger-based)

### Scope Rules
```kotlin
// @Singleton - App-wide, single instance
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            "music_database"
        ).build()
    }
}

// @ViewModelScoped - Lives with ViewModel
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackUseCase: PlaybackUseCase
) : ViewModel()

// @ActivityRetainedScoped - Lives across config changes
@Module
@InstallIn(ActivityRetainedComponent::class)
object PlaybackModule {
    @Provides
    @ActivityRetainedScoped
    fun provideMediaController(): MediaController = ...
}
```

### Hilt Module Organization
```kotlin
// DatabaseModule - Room database and DAOs
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase
    
    @Provides
    fun provideSongDao(db: MusicDatabase): SongDao = db.songDao()
}

// RepositoryModule - Repository bindings
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindSongRepository(
        impl: SongRepositoryImpl
    ): SongRepository
}

// MediaModule - ExoPlayer, MediaSession
@Module
@InstallIn(SingletonComponent::class)
object MediaModule {
    @Provides @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }
}
```

### Constraints
- ❌ No manual service locators
- ❌ No global singletons outside DI
- ✅ All dependencies injected via constructor
- ✅ Use `@Inject` constructor for ViewModels

---

## 2.6. Asynchronous & Reactive Programming

### Concurrency
**Kotlin Coroutines**

### Reactive Streams
**Kotlin Flow**

### Usage Rules

#### Use Flow for Streams
```kotlin
// ✅ Database streams
@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<SongEntity>>
}

// ✅ UI state streams
val uiState: StateFlow<UiState> = ...

// ✅ Playback state streams
val playbackState: Flow<PlaybackState> = ...
```

#### Use suspend for One-Shot Operations
```kotlin
// ✅ Single result operations
@Dao
interface PlaylistDao {
    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
}
```

#### Coroutine Dispatchers
```kotlin
// IO - Database, file system, network
withContext(Dispatchers.IO) {
    songDao.insertSong(song)
}

// Main - UI updates
withContext(Dispatchers.Main) {
    updateUI()
}

// Default - CPU-intensive work
withContext(Dispatchers.Default) {
    processAudioMetadata()
}
```

#### Structured Concurrency
```kotlin
// ✅ Use viewModelScope for ViewModel coroutines
class PlayerViewModel : ViewModel() {
    fun playSong(song: Song) {
        viewModelScope.launch {
            playbackUseCase.play(song)
        }
    }
}

// ✅ Use lifecycleScope for Activity/Fragment
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            // Lifecycle-aware work
        }
    }
}
```

### No RxJava
- ❌ Do not use RxJava (RxAndroid, RxKotlin)
- ✅ Use Kotlin Flow for all reactive streams

---

## 2.7. Local Data Storage

### Database
**Room Database** (version 2.6.x+)

### Configuration
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
    exportSchema = true // Export schema for version control
)
@TypeConverters(Converters::class)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
    abstract fun settingsDao(): SettingsDao
}
```

### Data Characteristics
- **Offline-only**: Single local database, no sync
- **Schema versioned**: Room migrations for updates
- **Backup**: User-managed via Android Auto Backup

### Access Rules
- ✅ Database access **only** via DAO
- ✅ DAOs injected via Hilt
- ❌ No direct database access from UI or ViewModel
- ✅ Repository pattern mediates access

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
```

---

## 2.8. Media Playback Engine

### Media Engine
**Media3 (ExoPlayer)** - Version 1.2.x+

### Playback Architecture
```kotlin
// Foreground Service
class PlaybackService : MediaLibraryService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaLibrarySession
    
    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaLibrarySession.Builder(this, player, callback)
            .build()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaSession
    }
}
```

### Supported Features
- Background playback (foreground service)
- MediaSession integration
- Notification controls (play/pause/next/previous)
- Lock screen controls
- Audio focus handling (pause on phone call)
- Bluetooth/headphone controls
- Lifecycle-aware playback state

### Audio Focus
```kotlin
// Handle audio focus changes
private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
    .setOnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> player.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> player.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> 
                player.volume = 0.3f // Lower volume
            AudioManager.AUDIOFOCUS_GAIN -> {
                player.volume = 1.0f
                // Resume if was playing
            }
        }
    }
    .build()
```

---

## 2.9. Android System Integration

### Permissions

#### Required Permissions
```xml
<!-- Runtime permissions (must request) -->
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" /> <!-- API 33+ -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" /> <!-- API 33+ -->

<!-- Normal permissions (auto-granted) -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" /> <!-- API 34+ -->
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Legacy permissions (for API 26-32) -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

#### Permission Handling
```kotlin
// Check and request permission
private fun checkAudioPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

// Request with rationale
val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        // Permission granted
        startMusicScan()
    } else {
        // Show rationale and settings link
        showPermissionRationale()
    }
}
```

### Services
**Foreground Service** for playback:
```xml
<service
    android:name=".playback.PlaybackService"
    android:foregroundServiceType="mediaPlayback"
    android:exported="true">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaLibraryService" />
    </intent-filter>
</service>
```

### Storage Access
- **MediaStore API**: Discover audio files
- **ContentResolver**: Read file metadata
- **Scoped Storage**: Compliant with Android 10+ restrictions

```kotlin
// Query audio files via MediaStore
val projection = arrayOf(
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.TITLE,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.ALBUM,
    MediaStore.Audio.Media.DURATION,
    MediaStore.Audio.Media.DATA // File path
)

val cursor = contentResolver.query(
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    projection,
    "${MediaStore.Audio.Media.IS_MUSIC} != 0",
    null,
    "${MediaStore.Audio.Media.TITLE} ASC"
)
```

---

## 2.10. Navigation

### Navigation Framework
**Jetpack Navigation Compose**

### Navigation Architecture
- Single Activity (MainActivity)
- Composable-based destinations
- Type-safe navigation arguments (via Kotlin Serialization)

### Route Structure
```kotlin
// Define destinations
sealed class Screen(val route: String) {
    object Library : Screen("library")
    object Favorites : Screen("favorites")
    object History : Screen("history")
    data object NowPlaying : Screen("now_playing")
    object Settings : Screen("settings")
    
    // With arguments
    data class PlaylistDetail(val playlistId: Long) : Screen("playlist/$playlistId") {
        companion object {
            const val route = "playlist/{playlistId}"
        }
    }
}

// Navigation setup
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController, startDestination = Screen.Library.route) {
        composable(Screen.Library.route) { LibraryScreen() }
        composable(Screen.Favorites.route) { FavoritesScreen() }
        composable(Screen.History.route) { HistoryScreen() }
        composable(Screen.NowPlaying.route) { NowPlayingScreen() }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId")
            PlaylistDetailScreen(playlistId!!)
        }
    }
}
```

### Navigation State
- Navigation state hoisted to top-level composable
- Deep linking support (future enhancement)
- Back stack management

---

## 2.11. Image Loading

### Library
**Coil 2.x** (Compose-native, Kotlin coroutines)

### Usage
```kotlin
// Load album artwork
@Composable
fun AlbumArtwork(
    artworkUri: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(artworkUri)
            .crossfade(true)
            .error(R.drawable.placeholder_album) // Fallback
            .placeholder(R.drawable.placeholder_album) // Loading
            .build(),
        contentDescription = "Album artwork",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}
```

### Features
- Async image loading
- Memory caching
- Disk caching
- Crossfade animations
- Placeholder/error handling

---

## 2.12. Build System

### Build Tool
- **Gradle** (Kotlin DSL) - Version 8.2+
- **Android Gradle Plugin (AGP)**: 8.2.x+
- **Kotlin Gradle Plugin**: Matches Kotlin version (1.9.22)

### Build Configuration
```kotlin
// build.gradle.kts (Project level)
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}

// build.gradle.kts (App level)
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.musicft"
    compileSdk = 35
    
    defaultConfig {
        applicationId = "com.example.musicft"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}
```

### Gradle Plugins
- **Android Gradle Plugin** (AGP)
- **Kotlin Gradle Plugin**
- **Hilt Gradle Plugin**
- **KSP** (Kotlin Symbol Processing) - for Room & Hilt

### Dependency Management
**Version Catalog** (libs.versions.toml):
```toml
[versions]
kotlin = "1.9.22"
compose-bom = "2024.02.00"
room = "2.6.1"
hilt = "2.50"
media3 = "1.2.1"
coil = "2.5.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.12.0" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version = "2.7.0" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }

androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }

media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
media3-session = { group = "androidx.media3", name = "media3-session", version.ref = "media3" }

coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

[plugins]
android-application = { id = "com.android.application", version = "8.2.0" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "1.9.22-1.0.17" }
```

---

## 2.13. Testing

### Unit Testing (Mandatory)

#### Frameworks
- **JUnit 5** (JUnit Jupiter) - Modern testing framework
- **MockK** - Kotlin-friendly mocking library
- **Coroutines Test** - kotlinx-coroutines-test
- **Turbine** - Flow testing library

#### Test Scope
**Must Test** (>70% coverage):
- Use cases (domain layer)
- Repository implementations
- ViewModel business logic
- Data transformations (mappers)

#### Example Unit Test
```kotlin
class GetSongsUseCaseTest {
    private lateinit var repository: SongRepository
    private lateinit var useCase: GetSongsUseCase
    
    @Before
    fun setup() {
        repository = mockk()
        useCase = GetSongsUseCase(repository)
    }
    
    @Test
    fun `invoke returns songs from repository`() = runTest {
        // Given
        val songs = listOf(
            Song(SongId(1), "Song 1", "Artist 1", Duration.ofMinutes(3)),
            Song(SongId(2), "Song 2", "Artist 2", Duration.ofMinutes(4))
        )
        coEvery { repository.getAllSongs() } returns flowOf(songs)
        
        // When
        val result = useCase().first()
        
        // Then
        assertEquals(songs, result)
        coVerify { repository.getAllSongs() }
    }
}
```

### UI Testing (Basic)

#### Framework
- **Compose UI Test** - Testing Compose UI

#### Scope
- Critical user flows only (Phase 1)
- Play/pause functionality
- Navigation between screens

#### Example UI Test
```kotlin
@HiltAndroidTest
class LibraryScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun songList_displaysCorrectly() {
        // Given
        val songs = listOf(
            Song(SongId(1), "Song 1", "Artist 1", Duration.ofMinutes(3))
        )
        
        // When
        composeTestRule.setContent {
            MusicFTTheme {
                SongList(songs = songs, onSongClick = {})
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Song 1").assertExists()
        composeTestRule.onNodeWithText("Artist 1").assertExists()
    }
}
```

### Integration Testing (Optional)
- Room database operations
- Media playback service

### Test Structure
```
app/
├── src/
│   ├── test/          # Unit tests (run on JVM)
│   │   └── kotlin/
│   │       ├── domain/
│   │       ├── data/
│   │       └── presentation/
│   └── androidTest/   # Instrumented tests (run on device/emulator)
│       └── kotlin/
│           └── ui/
```

### Testing Best Practices
- Use fakes over mocks for repositories when possible
- Use test doubles for external dependencies
- Follow Given-When-Then structure
- Test behavior, not implementation
- One assertion per test (when possible)

---

## 2.14. Code Optimization (Release Build)

### R8 Configuration
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### ProGuard Rules
```proguard
# proguard-rules.pro

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Coil
-dontwarn coil.**
-keep class coil.** { *; }
```

---

## 2.15. Code Quality Standards

### Mandatory Practices
- ✅ Null safety (avoid `!!` operator except when justified)
- ✅ Immutable data structures (prefer `val` over `var`)
- ✅ Extension functions for reusable logic
- ✅ Sealed classes for state modeling
- ✅ Proper error handling (no empty catch blocks)
- ✅ Meaningful variable names (no single letters except iterators)

### Code Style
- Follow **Kotlin Coding Conventions**
- Use **ktlint** or **detekt** (optional but recommended)
- Max function length: ~30 lines (guideline, not hard limit)
- Max class length: ~300 lines (guideline, not hard limit)

### Naming Conventions
- **Classes**: PascalCase (`SongRepository`)
- **Functions/Variables**: camelCase (`getAllSongs`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_CACHE_SIZE`)
- **Composables**: PascalCase, React-style (`LibraryScreen`)
- **Package names**: lowercase, no underscores (`com.example.musicft`)

### Documentation
```kotlin
/**
 * Retrieves all songs from the local database.
 *
 * @return Flow of song list, updates when database changes
 */
fun getAllSongs(): Flow<List<Song>>
```

---

## 2.16. Dependency Versions (Reference)

### Core Versions
- **Kotlin**: 1.9.22 (or 2.0.0 if stable)
- **Compose BOM**: 2024.02.00
- **Media3**: 1.2.1+
- **Room**: 2.6.1
- **Hilt**: 2.50
- **Coroutines**: 1.8.0
- **Coil**: 2.5.0

### Update Policy
- Use stable releases only (no alpha/beta in production)
- Review dependency updates quarterly
- Pin major versions to avoid breaking changes
- Test thoroughly after updates

---

## 2.17. Module Structure

### Phase 1: Single Module
```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/prj/musicft/
│   │   │       ├── data/
│   │   │       │   ├── local/
│   │   │       │   │   ├── dao/
│   │   │       │   │   ├── entity/
│   │   │       │   │   └── database/
│   │   │       │   ├── repository/
│   │   │       │   └── scanner/
│   │   │       ├── domain/
│   │   │       │   ├── model/
│   │   │       │   ├── repository/
│   │   │       │   └── usecase/
│   │   │       ├── presentation/
│   │   │       │   ├── player/
│   │   │       │   ├── library/
│   │   │       │   ├── playlist/
│   │   │       │   ├── theme/
│   │   │       │   └── common/
│   │   │       └── di/
│   │   └── res/
│   ├── test/
│   └── androidTest/
```

### Future Modularization (Phase 2+)
```
:app
:core:common
:core:ui
:core:data
:core:domain
:feature:player
:feature:library
:feature:playlist
```

---

## 2.18. Development Tools

### Logging
- **Timber** (recommended) or Android Logcat
- Log levels:
  - **Debug builds**: VERBOSE
  - **Release builds**: ERROR only

```kotlin
// Setup Timber
class MusicFTApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

// Usage
Timber.d("Loading songs from database")
Timber.e(exception, "Failed to play song")
```

### Debugging Tools
- Android Studio Profiler (CPU, Memory, Network)
- Layout Inspector (Compose UI hierarchy)
- Database Inspector (Room database)
- Logcat filtering

### Crash Reporting (Phase 2)
- Not included in Phase 1
- Consider: Firebase Crashlytics or Sentry

---

## 2.19. Explicit Exclusions

**The following are NOT allowed in Phase 1:**

- ❌ XML layouts
- ❌ MVP / MVC architecture patterns
- ❌ RxJava (use Kotlin Flow)
- ❌ Java-based View system
- ❌ Third-party media engines (use Media3)
- ❌ Online APIs / networking libraries (Retrofit, OkHttp)
- ❌ Firebase (Analytics, Crashlytics, etc.)
- ❌ Third-party analytics
- ❌ In-app purchases / billing
- ❌ Ads

---

## 2.20. Quick Reference Checklist

Before starting development, ensure:

- [ ] Android Studio Hedgehog+ installed
- [ ] JDK 17 configured
- [ ] Kotlin 1.9.22+ configured
- [ ] Gradle 8.2+ configured
- [ ] Dependencies defined in Version Catalog
- [ ] Hilt plugin added
- [ ] KSP plugin added
- [ ] Compose enabled in build.gradle
- [ ] Minimum SDK set to 26
- [ ] Target SDK set to 34
- [ ] ProGuard rules added
- [ ] Package structure planned
- [ ] Git repository initialized

---

**End of Technical Stack Specification Document**
package com.prj.musicft

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prj.musicft.domain.model.Song
import com.prj.musicft.presentation.common.AppBottomNavigation
import com.prj.musicft.presentation.home.HomeScreen
import com.prj.musicft.presentation.navigation.Screen
import com.prj.musicft.presentation.player.FullPlayerScreen
import com.prj.musicft.presentation.player.FullPlayerViewModel
import com.prj.musicft.presentation.player.MiniPlayer
import com.prj.musicft.presentation.splash.SplashScreen
import com.prj.musicft.presentation.theme.MusicFTTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

import androidx.compose.runtime.collectAsState
import com.prj.musicft.domain.model.ThemeMode
import com.prj.musicft.presentation.library.CollectionSongListScreen
import com.prj.musicft.presentation.library.LibraryScreen
import com.prj.musicft.presentation.search.SearchScreen
import com.prj.musicft.presentation.settings.SettingsScreen
import com.prj.musicft.presentation.settings.SettingsViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.settingsState.collectAsState()
            val isDarkTheme = settingsState?.themeMode == ThemeMode.DARK

            MusicFTTheme(darkTheme = isDarkTheme) { MusicFTApp() }
        }
        Timber.plant(MyTimberTree())
    }
}

@Composable
fun MusicFTApp() {
    val navController = rememberNavController()
    // In a real app, observe "current song" flow to decide if MiniPlayer is visible
    val showMiniPlayer = true

    // Check current route to hide bottom bar on Splash
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Splash.route && currentRoute != Screen.PermissionRequest.route
    
    // Hide MiniPlayer when on Player screen
    val showMiniPlayerBar = showBottomBar && currentRoute != Screen.Player.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    when (targetState.destination.route) {
                        Screen.Player.route -> {
                            slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(1000, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(500))
                        }
                        else -> fadeIn(animationSpec = tween(400))
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Screen.Player.route -> {
                            fadeOut(animationSpec = tween(400))
                        }
                        else -> fadeOut(animationSpec = tween(400))
                    }
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(400))
                },
                popExitTransition = {
                    when (initialState.destination.route) {
                        Screen.Player.route -> {
                            slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(1000, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(500))
                        }
                        else -> fadeOut(animationSpec = tween(400))
                    }
                }
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        onNavigateToPermission = {
                            navController.navigate(Screen.PermissionRequest.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.PermissionRequest.route) {
                    com.prj.musicft.presentation.splash.PermissionRequestScreen(
                        onPermissionGranted = {
                            // Navigate back to Splash check flow
                            navController.navigate(Screen.Splash.route) {
                                popUpTo(0) // Clear entire stack to restart from Splash
                            }
                        },
                        onDenyOrIgnore = {
                            // Go to Home (empty state)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) // Clear entire stack so Back button exits app
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        onSongClick = { song ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("song", song)
                            navController.currentBackStackEntry?.savedStateHandle?.set("forcePlay", true)
                            navController.navigate(Screen.Player.route)
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        }
                    )
                }

                composable(Screen.Library.route) {
                    LibraryScreen(
                        onNavigateToCollection = { type ->
                            navController.navigate(Screen.CollectionList.createRoute(type.name))
                        },
                        onSongClick = { song ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("song", song)
                            navController.currentBackStackEntry?.savedStateHandle?.set("forcePlay", true)
                            navController.navigate(Screen.Player.route)
                        }
                    )
                }

                composable(Screen.CollectionList.route) {
                    CollectionSongListScreen(
                        onNavigateUp = { navController.popBackStack() },
                        onSongClick = { song ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("song", song)
                            navController.currentBackStackEntry?.savedStateHandle?.set("forcePlay", true)
                            navController.navigate(Screen.Player.route)
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        onSongClick = { song ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("song", song)
                            navController.currentBackStackEntry?.savedStateHandle?.set("forcePlay", true)
                            navController.navigate(Screen.Player.route)
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        // No onNavigateBack needed for main tab usually, but if needed:
                        // onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Player.route) {
                    val viewModel: FullPlayerViewModel = hiltViewModel()

                    val previousEntry = navController.previousBackStackEntry
                    val song = previousEntry?.savedStateHandle?.get<Song>("song")
                    val forcePlay = previousEntry?.savedStateHandle?.get<Boolean>("forcePlay") ?: false

                    // Only load song if explicitly passed (from song list)
                    // If no song passed (from MiniPlayer), ViewModel will use current service state
                    LaunchedEffect(song) {
                        if (song != null) {
                            viewModel.loadSong(song, forcePlay = forcePlay)
                            // Clear after loading to prevent re-trigger
                            previousEntry.savedStateHandle.remove<Song>("song")
                            previousEntry.savedStateHandle.remove<Boolean>("forcePlay")
                        }
                    }

                    FullPlayerScreen(
                        onCollapse = { navController.popBackStack() },
                        viewModel = viewModel
                    )
                }
            }

            // Floating MiniPlayer - positioned above the bottom nav bar
            // Hidden when on Player screen
            if (showMiniPlayerBar && showMiniPlayer) {
                MiniPlayer(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    onClick = {
                        // Clear any previous song data to prevent restart
                        navController.currentBackStackEntry?.savedStateHandle?.remove<Song>("song")
                        navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("forcePlay")
                        navController.navigate(Screen.Player.route)
                    }
                )
            }
        }
    }
}

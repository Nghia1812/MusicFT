package com.prj.musicft.presentation.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prj.musicft.data.repository.FullSyncRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(private val syncRepository: FullSyncRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun onPermissionsGranted() {
        viewModelScope.launch {
            _uiState.value = SplashUiState.Scanning
            // Start sync
            syncRepository.startSync(this)

            // Wait for sync or minimum delay
            // Ideally, we observe syncRepository.isScanning
            // For now, let's simulate minimum branding time + sync wait
            delay(1500)

            // Simple check (in real app, observe existing flow)
            // Assuming sync is fast or happens in background.
            // We transition to Home.
            _uiState.value = SplashUiState.Completed
        }
    }
}

sealed class SplashUiState {
    object Loading : SplashUiState()
    object Scanning : SplashUiState()
    object Completed : SplashUiState()
}

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToPermission: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Check permissions on start
    val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    LaunchedEffect(Unit) {
        val allGranted =
            permissions.all {
                ContextCompat.checkSelfPermission(context, it) ==
                        PackageManager.PERMISSION_GRANTED
            }
        if (allGranted) {
            viewModel.onPermissionsGranted()
        } else {
            // Delay slightly for branding impact or just go straight
            delay(500)
            onNavigateToPermission()
        }
    }

    LaunchedEffect(state) {
        if (state is SplashUiState.Completed) {
            onNavigateToHome()
        }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animate Logo
            val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
            val scale by
            infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                label = "Scale"
            )

            // Logo Placeholder (Gradient Circle)
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .scale(scale)
                        .background(
                            brush =
                                Brush.linearGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.tertiary,
                                            MaterialTheme.colorScheme.primary
                                        )
                                ),
                            shape = CircleShape
                        ),
                contentAlignment = Alignment.Center
            ) {
                // Icon or Initial
                Text(
                    text = "FT",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Cyberpunk Audio",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state is SplashUiState.Scanning) {
                LinearProgressIndicator(
                    modifier = Modifier.width(150.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Syncing Library...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

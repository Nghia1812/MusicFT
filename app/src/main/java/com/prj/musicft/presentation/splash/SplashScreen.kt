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
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.prj.musicft.data.repository.FullSyncRepository
import com.prj.musicft.presentation.theme.*

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
            _uiState.value = SplashUiState.Scanning(0f)
            // Start sync
            syncRepository.startSync(this)

            // Observe progress
            val progressJob = launch {
                syncRepository.progress.collect { progress ->
                    // Only update if we are still in scanning state
                    if (_uiState.value is SplashUiState.Scanning) {
                        _uiState.value = SplashUiState.Scanning(progress)
                    }
                }
            }

            // Wait for sync or minimum delay
            // Ideally, we observe syncRepository.isScanning
            // For now, let's simulate minimum branding time + sync wait
            delay(1500)

            // Simple check (in real app, observe existing flow)
            // Assuming sync is fast or happens in background.
            // We transition to Home.
            progressJob.cancel()
            _uiState.value = SplashUiState.Completed
        }
    }
}

sealed class SplashUiState {
    object Loading : SplashUiState()
    data class Scanning(val progress: Float) : SplashUiState()
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
            delay(1500)
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
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(TealGlow, DeepDarkBackground),
                    center = Offset.Unspecified, // Center
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo Container
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        color = DarkSurface,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                    )
                    // Add a subtle border or glow if possible, but basic shape first
                    .border(
                        width = 1.dp,
                        color = DarkBorder,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                 // Using the requested icon
                 Icon(
                     painter = androidx.compose.ui.res.painterResource(id = com.prj.musicft.R.drawable.ic_app_launcher),
                     contentDescription = "Logo",
                     tint = Color.Unspecified,
                     modifier = Modifier.size(64.dp)
                 )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Title
            Text(
                text = "SONUS",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle with lines
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    color = Color.DarkGray,
                    modifier = Modifier.width(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "THE FUTURE OF SOUND",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 2.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    ),
                    color = SonusTeal
                )
                Spacer(modifier = Modifier.width(12.dp))
                Divider(
                    color = Color.DarkGray,
                    modifier = Modifier.width(40.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.8f))

            // Progress Section
            // Calculate progress for animation
            val currentProgress = (state as? SplashUiState.Scanning)?.progress ?: 0f
            val animatedProgress by animateFloatAsState(
                targetValue = currentProgress,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                label = "ProgressAnimation"
            )
            val percentage = (animatedProgress * 100).toInt()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "INITIALIZING ENGINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = SlateGrey
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ),
                        color = SonusTeal
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = SonusTeal,
                    trackColor = DarkTrack
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Footer
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PRECISION AUDIO ARCHITECTURE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    ),
                    color = SlateGrey.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

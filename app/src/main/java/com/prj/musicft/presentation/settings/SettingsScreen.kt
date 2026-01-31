package com.prj.musicft.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prj.musicft.presentation.theme.CyberpunkTeal
import com.prj.musicft.presentation.theme.DarkBackground
import com.prj.musicft.presentation.theme.GrayText
import com.prj.musicft.presentation.theme.LightText
import com.prj.musicft.presentation.theme.SurfaceSlate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsState by viewModel.settingsState.collectAsState()
    val isDark = settingsState?.themeMode == com.prj.musicft.domain.model.ThemeMode.DARK

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // General Section
            SettingsSection(title = "GENERAL") {
                SettingsGroup {
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = "Theme",
                        subtitle = if (isDark) "Dark Mode" else "Light Mode",
                        trailingContent = {
                            ThemeSwitcher(
                                isDark = isDark,
                                onThemeChange = { viewModel.onThemeChange(it) }
                            )
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Translate,
                        title = "Language",
                        subtitle = "English (US)",
                        showChevron = true
                    )
                }
            }

            // Audio Section
            SettingsSection(title = "AUDIO") {
                SettingsGroup {
                    SettingsItem(
                        icon = Icons.Default.Tune,
                        title = "Equalizer",
                        subtitle = "Custom preset active",
                        showChevron = true
                    )
                    SettingsItem(
                        icon = Icons.Default.Shuffle,
                        title = "Crossfade",
                        subtitle = "5 seconds overlap",
                        trailingContent = {
                            Switch(
                                checked = true,
                                onCheckedChange = { },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CyberpunkTeal,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = SurfaceSlate
                                )
                            )
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.GraphicEq,
                        title = "Audio Quality",
                        subtitle = "Lossless (High)",
                        trailingContent = {
                            Badge(
                                containerColor = CyberpunkTeal.copy(alpha = 0.2f),
                                contentColor = CyberpunkTeal
                            ) {
                                Text(
                                    text = "FLAC",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    )
                }
            }

            // About Section
            SettingsSection(title = "ABOUT") {
                SettingsGroup {
                    SettingsItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        showChevron = true
                    )
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "Built for audiophiles",
                        trailingContent = {
                            Text(
                                text = "v2.4.0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrayText
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(50.dp)) // Bottom padding
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = CyberpunkTeal,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsGroup(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceSlate)
            .padding(vertical = 8.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    showChevron: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyberpunkTeal,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText
                )
            }
        }

        // Trailing
        if (trailingContent != null) {
            trailingContent()
        } else if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = GrayText
            )
        }
    }
}

@Composable
fun ThemeSwitcher(
    isDark: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dark Option
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f) // Ensure equal width
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onThemeChange(true) }
                .then(
                    if (isDark) Modifier
                        .background(SurfaceSlate) // Active
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Dark",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Color.White else GrayText
            )
        }
        
        // Light Option
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onThemeChange(false) }
                .then(
                    if (!isDark) Modifier
                        .background(SurfaceSlate) // Active
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Light",
                style = MaterialTheme.typography.labelSmall,
                color = if (!isDark) Color.White else GrayText
            )
        }
    }
}

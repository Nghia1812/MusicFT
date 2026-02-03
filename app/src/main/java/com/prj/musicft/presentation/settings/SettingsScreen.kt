package com.prj.musicft.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.vectorResource
import com.prj.musicft.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsState by viewModel.settingsState.collectAsState()
    val isDark = settingsState?.themeMode == com.prj.musicft.domain.model.ThemeMode.DARK
    var showLanguageDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val appLocales = AppCompatDelegate.getApplicationLocales()
    val isSystemDefault = appLocales.isEmpty
    
    // For display, use the configuration (resolved language)
    val currentLocale = configuration.locales[0]
    val displayLanguage = remember(currentLocale) {
        currentLocale.getDisplayName(currentLocale)
    }
    
    // Privacy Policy Dialog State
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // Version Name
    val context = androidx.compose.ui.platform.LocalContext.current
    val versionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${packageInfo.versionName}"
        } catch (e: Exception) {
            context.getString(R.string.unknown)
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguageCode = if (isSystemDefault) "" else (appLocales[0]?.language ?: "en"),
            onDismissRequest = { showLanguageDialog = false },
            onLanguageSelected = { code ->
                viewModel.onLanguageChange(code)
                showLanguageDialog = false
            }
        )
    }
    
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            onDismissRequest = { showPrivacyDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
            SettingsSection(title = stringResource(R.string.general)) {
                SettingsGroup {
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.ic_theme),
                        title = stringResource(R.string.theme),
                        subtitle = if (isDark) stringResource(R.string.dark_mode) else stringResource(R.string.light_mode),
                        trailingContent = {
                            ThemeSwitcher(
                                isDark = isDark,
                                onThemeChange = { viewModel.onThemeChange(it) }
                            )
                        }
                    )
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.ic_language),
                        title = stringResource(R.string.language),
                        subtitle = displayLanguage, // Display current language name
                        showChevron = true,
                        onClick = { showLanguageDialog = true }
                    )
                }
            }

            // Audio Section
            SettingsSection(title = stringResource(R.string.audio)) {
                SettingsGroup {
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.ic_stat),
                        title = stringResource(R.string.equalizer),
                        subtitle = stringResource(R.string.custom_preset_active),
                        showChevron = true
                    )
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.ic_settings),
                        title = stringResource(R.string.crossfade),
                        subtitle = stringResource(R.string.crossfade_desc),
                        trailingContent = {
                            Switch(
                                checked = true,
                                onCheckedChange = { },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    )
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.ic_album),
                        title = stringResource(R.string.audio_quality),
                        subtitle = stringResource(R.string.lossless_high),
                        trailingContent = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = stringResource(R.string.flac),
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
            SettingsSection(title = stringResource(R.string.about)) {
                SettingsGroup {
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.ic_privacy),
                        title = stringResource(R.string.privacy_policy),
                        showChevron = true,
                        onClick = { showPrivacyDialog = true }
                    )
                    SettingsItem(
                        icon = ImageVector.vectorResource(R.drawable.ic_app_launcher), // Using app launcher as version icon
                        title = stringResource(R.string.version),
                        subtitle = stringResource(R.string.audiophile_desc),
                        trailingContent = {
                            Text(
                                text = versionName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            color = MaterialTheme.colorScheme.primary,
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
            .background(MaterialTheme.colorScheme.surface)
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
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Trailing - wrap in Box with wrapContentWidth to prevent expansion
        if (trailingContent != null) {
            Box(modifier = Modifier.wrapContentWidth()) {
                trailingContent()
            }
        } else if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            .width(120.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
                        .background(MaterialTheme.colorScheme.surface) // Active
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.dark),
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
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
                        .background(MaterialTheme.colorScheme.surface) // Active
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.light),
                style = MaterialTheme.typography.labelSmall,
                color = if (!isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onDismissRequest: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.select_language)) },
        text = {
            Column {
                val languages = listOf(
                    stringResource(R.string.system_default) to "",
                    stringResource(R.string.english) to "en",
                    stringResource(R.string.vietnamese) to "vi",
                    stringResource(R.string.spanish) to "es",
                    stringResource(R.string.french) to "fr"
                )
                languages.forEach { (name, code) ->
                    val isSelected = if (code.isEmpty()) {
                        currentLanguageCode.isEmpty()
                    } else {
                        currentLanguageCode == code
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null // Handled by Row clickable
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun PrivacyPolicyDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.privacy_policy))
        },
        text = {
            Column(
                 modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.privacy_policy_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

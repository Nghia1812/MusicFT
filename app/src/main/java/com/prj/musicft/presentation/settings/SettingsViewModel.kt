package com.prj.musicft.presentation.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.prj.musicft.domain.model.AppSettings
import com.prj.musicft.domain.model.ThemeMode
import com.prj.musicft.domain.usecase.GetAppSettingsUseCase
import com.prj.musicft.domain.usecase.UpdateThemeModeUseCase
import com.prj.musicft.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val updateThemeModeUseCase: UpdateThemeModeUseCase
) : ViewModel() {

    // Expose the raw AppSettings or map to a UI state
    // For simplicity, we expose AppSettings directly wrapped in UiState or just the Flow
    val settingsState: StateFlow<AppSettings?> = getAppSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onThemeChange(isDark: Boolean) {
        viewModelScope.launch {
            val newMode = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
            updateThemeModeUseCase(newMode)
        }
    }

    fun onLanguageChange(languageCode: String) {
        val appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(languageCode)
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)
    }
}

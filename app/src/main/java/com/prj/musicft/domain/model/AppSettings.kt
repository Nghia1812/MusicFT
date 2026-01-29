package com.prj.musicft.domain.model

data class AppSettings(
        val themeMode: ThemeMode,
        val useDynamicColor: Boolean,
        val shuffleEnabled: Boolean,
        val repeatMode: RepeatMode
)

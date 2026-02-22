package com.example.playlistmakerr.domain.api

interface SettingsInteractor {
    fun isDarkTheme(): Boolean
    fun switchTheme(isDark: Boolean)
}

package com.example.playlistmakerr.domain.impl

import com.example.playlistmakerr.domain.api.SettingsInteractor
import com.example.playlistmakerr.domain.api.SettingsRepository

class SettingsInteractorImpl(private val repository: SettingsRepository) : SettingsInteractor {

    override fun isDarkTheme(): Boolean = repository.isDarkTheme()

    override fun switchTheme(isDark: Boolean) {
        repository.setDarkTheme(isDark)
    }
}

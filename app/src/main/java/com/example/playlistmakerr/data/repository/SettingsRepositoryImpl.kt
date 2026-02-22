package com.example.playlistmakerr.data.repository

import android.content.SharedPreferences
import com.example.playlistmakerr.domain.api.SettingsRepository

class SettingsRepositoryImpl(private val sharedPreferences: SharedPreferences) : SettingsRepository {

    override fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_THEME, false)
    }

    override fun setDarkTheme(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }

    companion object {
        const val PREFERENCES_NAME = "theme_preferences"
        const val KEY_DARK_THEME = "dark_theme"
    }
}

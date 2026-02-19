package com.example.playlistmakerr

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class PlaylistMakerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applyTheme()
    }

    private fun applyTheme() {
        val sharedPreferences = getSharedPreferences(SettingsActivity.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val isDarkTheme = sharedPreferences.getBoolean(SettingsActivity.KEY_DARK_THEME, false)
        
        val mode = if (isDarkTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}

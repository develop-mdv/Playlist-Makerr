package com.example.playlistmakerr

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmakerr.creator.Creator

class PlaylistMakerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applyTheme()
    }

    private fun applyTheme() {
        val settingsInteractor = Creator.provideSettingsInteractor(this)
        val isDarkTheme = settingsInteractor.isDarkTheme()

        val mode = if (isDarkTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}

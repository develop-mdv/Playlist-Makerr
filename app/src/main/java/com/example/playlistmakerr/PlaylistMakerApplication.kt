package com.example.playlistmakerr

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmakerr.di.dataModule
import com.example.playlistmakerr.di.interactorModule
import com.example.playlistmakerr.di.repositoryModule
import com.example.playlistmakerr.di.viewModelModule
import com.example.playlistmakerr.domain.api.SettingsInteractor
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PlaylistMakerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PlaylistMakerApplication)
            modules(dataModule, repositoryModule, interactorModule, viewModelModule)
        }
        applyTheme()
    }

    private fun applyTheme() {
        val settingsInteractor: SettingsInteractor = get()
        val isDarkTheme = settingsInteractor.isDarkTheme()

        val mode = if (isDarkTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}

package com.example.playlistmakerr.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmakerr.domain.api.SettingsInteractor

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor,
) : ViewModel() {

    private val _isDarkTheme = MutableLiveData<Boolean>()
    val isDarkTheme: LiveData<Boolean> = _isDarkTheme

    init {
        _isDarkTheme.value = settingsInteractor.isDarkTheme()
    }

    fun switchTheme(isDark: Boolean) {
        settingsInteractor.switchTheme(isDark)
        _isDarkTheme.value = isDark
    }
}

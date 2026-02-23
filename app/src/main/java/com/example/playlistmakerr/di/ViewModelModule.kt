package com.example.playlistmakerr.di

import com.example.playlistmakerr.presentation.player.PlayerViewModel
import com.example.playlistmakerr.presentation.search.SearchViewModel
import com.example.playlistmakerr.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        SearchViewModel(get(), get())
    }

    viewModel {
        PlayerViewModel()
    }

    viewModel {
        SettingsViewModel(get())
    }
}

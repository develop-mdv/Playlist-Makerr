package com.example.playlistmakerr.di

import com.example.playlistmakerr.presentation.library.FavoritesViewModel
import com.example.playlistmakerr.presentation.library.PlaylistsViewModel
import com.example.playlistmakerr.presentation.newplaylist.CreatePlaylistViewModel
import com.example.playlistmakerr.presentation.player.PlayerViewModel
import com.example.playlistmakerr.presentation.search.SearchViewModel
import com.example.playlistmakerr.presentation.settings.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        SearchViewModel(get(), get())
    }

    viewModel {
        PlayerViewModel(get(), get(), get())
    }

    viewModel {
        SettingsViewModel(get())
    }

    viewModel {
        FavoritesViewModel(get())
    }

    viewModel {
        PlaylistsViewModel(get())
    }

    viewModel {
        CreatePlaylistViewModel(androidApplication(), get())
    }
}

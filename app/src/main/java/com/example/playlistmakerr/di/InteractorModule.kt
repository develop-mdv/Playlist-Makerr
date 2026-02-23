package com.example.playlistmakerr.di

import com.example.playlistmakerr.domain.api.SearchHistoryInteractor
import com.example.playlistmakerr.domain.api.SettingsInteractor
import com.example.playlistmakerr.domain.api.TracksInteractor
import com.example.playlistmakerr.domain.impl.SearchHistoryInteractorImpl
import com.example.playlistmakerr.domain.impl.SettingsInteractorImpl
import com.example.playlistmakerr.domain.impl.TracksInteractorImpl
import org.koin.dsl.module

val interactorModule = module {

    single<TracksInteractor> {
        TracksInteractorImpl(get())
    }

    single<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(get())
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }
}

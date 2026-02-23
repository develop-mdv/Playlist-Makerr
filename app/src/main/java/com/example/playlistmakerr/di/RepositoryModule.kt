package com.example.playlistmakerr.di

import com.example.playlistmakerr.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmakerr.data.repository.SettingsRepositoryImpl
import com.example.playlistmakerr.data.repository.TracksRepositoryImpl
import com.example.playlistmakerr.domain.api.SearchHistoryRepository
import com.example.playlistmakerr.domain.api.SettingsRepository
import com.example.playlistmakerr.domain.api.TracksRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {

    single<TracksRepository> {
        TracksRepositoryImpl(get())
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(get(named("search_history")), get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get(named("theme")))
    }
}

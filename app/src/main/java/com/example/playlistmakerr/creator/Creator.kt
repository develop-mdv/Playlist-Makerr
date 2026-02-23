package com.example.playlistmakerr.creator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmakerr.data.network.ItunesApi
import com.example.playlistmakerr.data.network.RetrofitNetworkClient
import com.example.playlistmakerr.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmakerr.data.repository.SettingsRepositoryImpl
import com.example.playlistmakerr.data.repository.TracksRepositoryImpl
import com.example.playlistmakerr.domain.api.SearchHistoryInteractor
import com.example.playlistmakerr.domain.api.SearchHistoryRepository
import com.example.playlistmakerr.domain.api.SettingsInteractor
import com.example.playlistmakerr.domain.api.SettingsRepository
import com.example.playlistmakerr.domain.api.TracksInteractor
import com.example.playlistmakerr.domain.api.TracksRepository
import com.example.playlistmakerr.domain.impl.SearchHistoryInteractorImpl
import com.example.playlistmakerr.domain.impl.SettingsInteractorImpl
import com.example.playlistmakerr.domain.impl.TracksInteractorImpl
import com.example.playlistmakerr.presentation.player.PlayerViewModel
import com.example.playlistmakerr.presentation.search.SearchViewModel
import com.example.playlistmakerr.presentation.settings.SettingsViewModel
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {

    private const val ITUNES_BASE_URL = "https://itunes.apple.com"
    private const val SEARCH_HISTORY_PREFERENCES = "search_history_preferences"
    private const val THEME_PREFERENCES = "theme_preferences"

    private val retrofit = Retrofit.Builder()
        .baseUrl(ITUNES_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesApi = retrofit.create(ItunesApi::class.java)
    private val networkClient = RetrofitNetworkClient(itunesApi)
    private val gson = Gson()

    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(networkClient)
    }

    private fun getSearchHistoryRepository(context: Context): SearchHistoryRepository {
        val sharedPreferences =
            context.getSharedPreferences(SEARCH_HISTORY_PREFERENCES, Context.MODE_PRIVATE)
        return SearchHistoryRepositoryImpl(sharedPreferences, gson)
    }

    private fun getSettingsRepository(context: Context): SettingsRepository {
        val sharedPreferences =
            context.getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE)
        return SettingsRepositoryImpl(sharedPreferences)
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(getSearchHistoryRepository(context))
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        return SettingsInteractorImpl(getSettingsRepository(context))
    }

    fun provideSearchViewModelFactory(context: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(
                    provideTracksInteractor(),
                    provideSearchHistoryInteractor(context),
                ) as T
            }
        }
    }

    fun providePlayerViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PlayerViewModel() as T
            }
        }
    }

    fun provideSettingsViewModelFactory(context: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(
                    provideSettingsInteractor(context),
                ) as T
            }
        }
    }
}

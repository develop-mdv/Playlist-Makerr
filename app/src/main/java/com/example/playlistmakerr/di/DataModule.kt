package com.example.playlistmakerr.di

import android.content.Context
import android.media.MediaPlayer
import androidx.room.Room
import com.example.playlistmakerr.data.db.AppDatabase
import com.example.playlistmakerr.data.network.ItunesApi
import com.example.playlistmakerr.data.network.NetworkClient
import com.example.playlistmakerr.data.network.RetrofitNetworkClient
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {

    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "playlist_maker.db"
        ).build()
    }

    single<ItunesApi> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApi::class.java)
    }

    single(named("search_history")) {
        androidContext()
            .getSharedPreferences("search_history_preferences", Context.MODE_PRIVATE)
    }

    single(named("theme")) {
        androidContext()
            .getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)
    }

    factory { Gson() }

    factory { MediaPlayer() }

    single<NetworkClient> {
        RetrofitNetworkClient(get())
    }
}

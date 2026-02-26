package com.example.playlistmakerr.di

import android.content.Context
import android.media.MediaPlayer
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS playlists (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                coverImagePath TEXT,
                trackIds TEXT NOT NULL,
                trackCount INTEGER NOT NULL DEFAULT 0
            )"""
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS playlist_tracks (
                trackId INTEGER PRIMARY KEY NOT NULL,
                trackName TEXT,
                artistName TEXT,
                trackTimeMillis INTEGER,
                artworkUrl100 TEXT,
                collectionName TEXT,
                releaseDate TEXT,
                primaryGenreName TEXT,
                country TEXT,
                previewUrl TEXT
            )"""
        )
    }
}

val dataModule = module {

    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "playlist_maker.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
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

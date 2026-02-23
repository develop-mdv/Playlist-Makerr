package com.example.playlistmakerr.data.repository

import android.content.SharedPreferences
import com.example.playlistmakerr.data.dto.TrackDto
import com.example.playlistmakerr.domain.api.SearchHistoryRepository
import com.example.playlistmakerr.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : SearchHistoryRepository {

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null) ?: return emptyList()
        val type = object : TypeToken<ArrayList<TrackDto>>() {}.type
        val dtoList: ArrayList<TrackDto> = gson.fromJson(json, type) ?: return emptyList()
        return dtoList.map { it.toDomain() }
    }

    override fun addTrack(track: Track) {
        val history = getHistoryDtoList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track.toDto())
        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.size - 1)
        }
        saveHistory(history)
    }

    override fun clearHistory() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
    }

    private fun getHistoryDtoList(): ArrayList<TrackDto> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null) ?: return arrayListOf()
        val type = object : TypeToken<ArrayList<TrackDto>>() {}.type
        return gson.fromJson(json, type) ?: arrayListOf()
    }

    private fun saveHistory(history: ArrayList<TrackDto>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString(SEARCH_HISTORY_KEY, json).apply()
    }

    private fun TrackDto.toDomain() = Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )

    private fun Track.toDto() = TrackDto(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )

    companion object {
        const val SEARCH_HISTORY_KEY = "search_history"
        const val MAX_HISTORY_SIZE = 10
    }
}

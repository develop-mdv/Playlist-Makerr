package com.example.playlistmakerr

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()

    fun getHistory(): ArrayList<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null) ?: return arrayListOf()
        val type = object : TypeToken<ArrayList<Track>>() {}.type
        return gson.fromJson(json, type) ?: arrayListOf()
    }

    fun addTrack(track: Track) {
        val history = getHistory()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.size - 1)
        }
        saveHistory(history)
    }

    fun clearHistory() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
    }

    private fun saveHistory(history: ArrayList<Track>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString(SEARCH_HISTORY_KEY, json).apply()
    }

    companion object {
        const val SEARCH_HISTORY_KEY = "search_history"
        const val MAX_HISTORY_SIZE = 10
    }
}

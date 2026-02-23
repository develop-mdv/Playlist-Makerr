package com.example.playlistmakerr.domain.api

import com.example.playlistmakerr.domain.models.Track

interface SearchHistoryRepository {
    fun getHistory(): List<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}

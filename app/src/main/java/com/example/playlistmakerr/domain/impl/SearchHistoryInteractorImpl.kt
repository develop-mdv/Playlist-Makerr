package com.example.playlistmakerr.domain.impl

import com.example.playlistmakerr.domain.api.SearchHistoryInteractor
import com.example.playlistmakerr.domain.api.SearchHistoryRepository
import com.example.playlistmakerr.domain.models.Track

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {

    override fun getHistory(): List<Track> = repository.getHistory()

    override fun addTrack(track: Track) = repository.addTrack(track)

    override fun clearHistory() = repository.clearHistory()
}

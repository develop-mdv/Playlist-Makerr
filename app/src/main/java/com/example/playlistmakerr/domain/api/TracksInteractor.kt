package com.example.playlistmakerr.domain.api

import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface TracksInteractor {
    fun searchTracks(expression: String): Flow<Pair<List<Track>?, String?>>
}

package com.example.playlistmakerr.domain.api

import com.example.playlistmakerr.domain.models.Resource
import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    fun searchTracks(expression: String): Flow<Resource<List<Track>>>
}

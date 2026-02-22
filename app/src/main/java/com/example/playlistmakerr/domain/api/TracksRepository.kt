package com.example.playlistmakerr.domain.api

import com.example.playlistmakerr.domain.models.Resource
import com.example.playlistmakerr.domain.models.Track

interface TracksRepository {
    fun searchTracks(expression: String): Resource<List<Track>>
}

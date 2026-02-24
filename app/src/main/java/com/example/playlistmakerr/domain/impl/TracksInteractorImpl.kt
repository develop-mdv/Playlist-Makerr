package com.example.playlistmakerr.domain.impl

import com.example.playlistmakerr.domain.api.TracksInteractor
import com.example.playlistmakerr.domain.api.TracksRepository
import com.example.playlistmakerr.domain.models.Resource
import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {

    override fun searchTracks(expression: String): Flow<Pair<List<Track>?, String?>> {
        return repository.searchTracks(expression)
            .map { resource ->
                when (resource) {
                    is Resource.Success -> Pair(resource.data, null)
                    is Resource.Error -> Pair(null, resource.message)
                }
            }
            .flowOn(Dispatchers.IO)
    }
}

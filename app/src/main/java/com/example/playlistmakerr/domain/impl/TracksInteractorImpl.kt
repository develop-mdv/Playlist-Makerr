package com.example.playlistmakerr.domain.impl

import com.example.playlistmakerr.domain.api.TracksInteractor
import com.example.playlistmakerr.domain.api.TracksRepository
import com.example.playlistmakerr.domain.models.Resource
import java.util.concurrent.Executors

class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun searchTracks(expression: String, consumer: TracksInteractor.TracksConsumer) {
        executor.execute {
            when (val resource = repository.searchTracks(expression)) {
                is Resource.Success -> consumer.consume(resource.data, null)
                is Resource.Error -> consumer.consume(null, resource.message)
            }
        }
    }
}

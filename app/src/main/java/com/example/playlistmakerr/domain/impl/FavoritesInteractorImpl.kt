package com.example.playlistmakerr.domain.impl

import com.example.playlistmakerr.domain.api.FavoritesInteractor
import com.example.playlistmakerr.domain.api.FavoritesRepository
import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class FavoritesInteractorImpl(
    private val favoritesRepository: FavoritesRepository,
) : FavoritesInteractor {

    override suspend fun addTrack(track: Track) {
        favoritesRepository.addTrack(track)
    }

    override suspend fun removeTrack(track: Track) {
        favoritesRepository.removeTrack(track)
    }

    override fun getTracks(): Flow<List<Track>> {
        return favoritesRepository.getTracks().flowOn(Dispatchers.IO)
    }

    override suspend fun isFavorite(trackId: Long): Boolean {
        return favoritesRepository.isFavorite(trackId)
    }
}

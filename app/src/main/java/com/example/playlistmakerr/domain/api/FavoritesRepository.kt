package com.example.playlistmakerr.domain.api

import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun addTrack(track: Track)
    suspend fun removeTrack(track: Track)
    fun getTracks(): Flow<List<Track>>
    suspend fun isFavorite(trackId: Long): Boolean
}

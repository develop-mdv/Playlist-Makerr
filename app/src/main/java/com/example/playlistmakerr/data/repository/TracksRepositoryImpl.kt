package com.example.playlistmakerr.data.repository

import com.example.playlistmakerr.data.dto.TracksSearchRequest
import com.example.playlistmakerr.data.dto.TracksSearchResponse
import com.example.playlistmakerr.data.network.NetworkClient
import com.example.playlistmakerr.domain.api.TracksRepository
import com.example.playlistmakerr.domain.models.Resource
import com.example.playlistmakerr.domain.models.Track

class TracksRepositoryImpl(private val networkClient: NetworkClient) : TracksRepository {

    override fun searchTracks(expression: String): Resource<List<Track>> {
        val response = networkClient.doRequest(TracksSearchRequest(expression))
        return when (response.resultCode) {
            -1 -> Resource.Error("Проблемы со связью")
            200 -> {
                val results = (response as TracksSearchResponse).results
                Resource.Success(results.map { dto ->
                    Track(
                        trackId = dto.trackId,
                        trackName = dto.trackName,
                        artistName = dto.artistName,
                        trackTimeMillis = dto.trackTimeMillis,
                        artworkUrl100 = dto.artworkUrl100,
                        collectionName = dto.collectionName,
                        releaseDate = dto.releaseDate,
                        primaryGenreName = dto.primaryGenreName,
                        country = dto.country,
                        previewUrl = dto.previewUrl
                    )
                })
            }
            else -> Resource.Error("Ошибка сервера")
        }
    }
}

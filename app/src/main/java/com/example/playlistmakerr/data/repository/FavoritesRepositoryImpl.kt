package com.example.playlistmakerr.data.repository

import com.example.playlistmakerr.data.db.AppDatabase
import com.example.playlistmakerr.data.db.FavoriteTrackEntity
import com.example.playlistmakerr.domain.api.FavoritesRepository
import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val appDatabase: AppDatabase,
) : FavoritesRepository {

    override suspend fun addTrack(track: Track) {
        appDatabase.favoriteTrackDao().insertTrack(track.toEntity())
    }

    override suspend fun removeTrack(track: Track) {
        appDatabase.favoriteTrackDao().deleteTrack(track.toEntity())
    }

    override fun getTracks(): Flow<List<Track>> {
        return appDatabase.favoriteTrackDao().getTracks().map { tracks ->
            tracks.map { it.toDomain() }
        }
    }

    override suspend fun isFavorite(trackId: Long): Boolean {
        return appDatabase.favoriteTrackDao().isFavorite(trackId)
    }

    private fun Track.toEntity(): FavoriteTrackEntity = FavoriteTrackEntity(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl,
    )

    private fun FavoriteTrackEntity.toDomain(): Track = Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl,
        isFavorite = true,
    )
}

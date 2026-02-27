package com.example.playlistmakerr.data.repository

import com.example.playlistmakerr.data.db.AppDatabase
import com.example.playlistmakerr.data.db.PlaylistEntity
import com.example.playlistmakerr.data.db.PlaylistTrackEntity
import com.example.playlistmakerr.domain.api.PlaylistRepository
import com.example.playlistmakerr.domain.models.Playlist
import com.example.playlistmakerr.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val gson: Gson,
) : PlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist) {
        appDatabase.playlistDao().insertPlaylist(playlist.toEntity())
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        appDatabase.playlistDao().updatePlaylist(playlist.toEntity())
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return appDatabase.playlistDao().getPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        appDatabase.playlistTrackDao().insertTrack(track.toPlaylistTrackEntity())

        val updatedTrackIds = playlist.trackIds + track.trackId
        val updatedPlaylist = playlist.copy(
            trackIds = updatedTrackIds,
            trackCount = updatedTrackIds.size,
        )
        appDatabase.playlistDao().updatePlaylist(updatedPlaylist.toEntity())
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return appDatabase.playlistDao().getPlaylistById(playlistId)?.toDomain()
    }

    override fun getTracksByIds(trackIds: List<Long>): Flow<List<Track>> = flow {
        val allTracks = appDatabase.playlistTrackDao().getAllTracks()
        val trackMap = allTracks.associateBy { it.trackId }
        val result = trackIds.reversed().mapNotNull { id ->
            trackMap[id]?.toDomain()
        }
        emit(result)
    }

    override suspend fun removeTrackFromPlaylist(trackId: Long, playlist: Playlist) {
        val updatedTrackIds = playlist.trackIds.filter { it != trackId }
        val updatedPlaylist = playlist.copy(
            trackIds = updatedTrackIds,
            trackCount = updatedTrackIds.size,
        )
        appDatabase.playlistDao().updatePlaylist(updatedPlaylist.toEntity())
        removeOrphanTrack(trackId)
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        appDatabase.playlistDao().deletePlaylistById(playlist.id)
        playlist.trackIds.forEach { trackId ->
            removeOrphanTrack(trackId)
        }
    }

    private suspend fun removeOrphanTrack(trackId: Long) {
        val playlists = appDatabase.playlistDao().getPlaylists().first()
        val isUsed = playlists.any { entity ->
            val type = object : TypeToken<List<Long>>() {}.type
            val ids: List<Long> = if (entity.trackIds.isBlank()) emptyList()
            else gson.fromJson(entity.trackIds, type)
            ids.contains(trackId)
        }
        if (!isUsed) {
            appDatabase.playlistTrackDao().deleteTrackById(trackId)
        }
    }

    private fun Playlist.toEntity(): PlaylistEntity = PlaylistEntity(
        id = id,
        name = name,
        description = description,
        coverImagePath = coverImagePath,
        trackIds = gson.toJson(trackIds),
        trackCount = trackCount,
    )

    private fun PlaylistEntity.toDomain(): Playlist {
        val type = object : TypeToken<List<Long>>() {}.type
        val ids: List<Long> = if (trackIds.isBlank()) emptyList() else gson.fromJson(trackIds, type)
        return Playlist(
            id = id,
            name = name,
            description = description,
            coverImagePath = coverImagePath,
            trackIds = ids,
            trackCount = trackCount,
        )
    }

    private fun PlaylistTrackEntity.toDomain(): Track = Track(
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

    private fun Track.toPlaylistTrackEntity(): PlaylistTrackEntity = PlaylistTrackEntity(
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
}

package com.example.playlistmakerr.domain.impl

import com.example.playlistmakerr.domain.api.PlaylistInteractor
import com.example.playlistmakerr.domain.api.PlaylistRepository
import com.example.playlistmakerr.domain.models.Playlist
import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class PlaylistInteractorImpl(
    private val playlistRepository: PlaylistRepository,
) : PlaylistInteractor {

    override suspend fun createPlaylist(playlist: Playlist) {
        playlistRepository.createPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistRepository.updatePlaylist(playlist)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistRepository.getPlaylists().flowOn(Dispatchers.IO)
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        playlistRepository.addTrackToPlaylist(track, playlist)
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return playlistRepository.getPlaylistById(playlistId)
    }

    override fun getTracksForIds(trackIds: List<Long>): Flow<List<Track>> {
        return playlistRepository.getTracksForIds(trackIds).flowOn(Dispatchers.IO)
    }

    override suspend fun removeTrackFromPlaylist(trackId: Long, playlist: Playlist) {
        playlistRepository.removeTrackFromPlaylist(trackId, playlist)
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistRepository.deletePlaylist(playlist)
    }
}

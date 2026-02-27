package com.example.playlistmakerr.domain.api

import com.example.playlistmakerr.domain.models.Playlist
import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistInteractor {
    suspend fun createPlaylist(playlist: Playlist)
    suspend fun updatePlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    fun getTracksByIds(trackIds: List<Long>): Flow<List<Track>>
    suspend fun removeTrackFromPlaylist(trackId: Long, playlist: Playlist)
    suspend fun deletePlaylist(playlist: Playlist)
}

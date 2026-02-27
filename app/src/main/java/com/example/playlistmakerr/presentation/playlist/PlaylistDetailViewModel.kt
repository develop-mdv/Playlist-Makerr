package com.example.playlistmakerr.presentation.playlist

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmakerr.domain.api.PlaylistInteractor
import com.example.playlistmakerr.domain.models.Playlist
import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistDetailViewModel(
    application: Application,
    private val playlistInteractor: PlaylistInteractor,
) : AndroidViewModel(application) {

    private val _playlist = MutableLiveData<Playlist?>()
    val playlist: LiveData<Playlist?> = _playlist

    private val _tracks = MutableLiveData<List<Track>>(emptyList())
    val tracks: LiveData<List<Track>> = _tracks

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            val loadedPlaylist = playlistInteractor.getPlaylistById(playlistId)
            _playlist.postValue(loadedPlaylist)
            loadedPlaylist?.let { loadTracks(it) }
        }
    }

    private fun loadTracks(playlist: Playlist) {
        viewModelScope.launch {
            playlistInteractor.getTracksByIds(playlist.trackIds).collect { trackList ->
                _tracks.postValue(trackList)
            }
        }
    }

    fun removeTrack(trackId: Long) {
        val currentPlaylist = _playlist.value ?: return
        viewModelScope.launch {
            playlistInteractor.removeTrackFromPlaylist(trackId, currentPlaylist)
            loadPlaylist(currentPlaylist.id)
        }
    }

    fun deletePlaylist() {
        val currentPlaylist = _playlist.value ?: return
        viewModelScope.launch {
            playlistInteractor.deletePlaylist(currentPlaylist)
        }
    }

    fun sharePlaylist(): Intent? {
        val currentPlaylist = _playlist.value ?: return null
        val currentTracks = _tracks.value ?: return null
        if (currentTracks.isEmpty()) return null

        val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())
        val sb = StringBuilder()
        sb.appendLine(currentPlaylist.name)
        if (currentPlaylist.description.isNotBlank()) {
            sb.appendLine(currentPlaylist.description)
        }
        sb.appendLine("${currentTracks.size} ${getTracksCountSuffix(currentTracks.size)}")

        currentTracks.forEachIndexed { index, track ->
            val duration = if (track.trackTimeMillis != null) {
                timeFormatter.format(track.trackTimeMillis)
            } else {
                "00:00"
            }
            sb.appendLine("${index + 1}. ${track.artistName ?: ""} - ${track.trackName ?: ""} ($duration)")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString().trim())
        }
        return Intent.createChooser(shareIntent, null)
    }

    private fun getTracksCountSuffix(count: Int): String {
        val mod100 = count % 100
        val mod10 = count % 10
        return when {
            mod100 in 11..19 -> "треков"
            mod10 == 1 -> "трек"
            mod10 in 2..4 -> "трека"
            else -> "треков"
        }
    }
}

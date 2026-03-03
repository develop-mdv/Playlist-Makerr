package com.example.playlistmakerr.presentation.playlistdetails

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmakerr.domain.api.PlaylistInteractor
import com.example.playlistmakerr.domain.models.Playlist
import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistDetailsViewModel(
    application: Application,
    private val playlistInteractor: PlaylistInteractor,
) : AndroidViewModel(application) {

    private val _playlist = MutableLiveData<Playlist?>()
    val playlist: LiveData<Playlist?> = _playlist

    private val _tracks = MutableLiveData<List<Track>>(emptyList())
    val tracks: LiveData<List<Track>> = _tracks

    private val _totalDurationMinutes = MutableLiveData(0)
    val totalDurationMinutes: LiveData<Int> = _totalDurationMinutes

    private val _navigateBack = MutableLiveData(false)
    val navigateBack: LiveData<Boolean> = _navigateBack
    private var tracksJob: Job? = null

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            val pl = playlistInteractor.getPlaylistById(playlistId) ?: return@launch
            _playlist.postValue(pl)
            loadTracks(pl.trackIds)
        }
    }

    private fun loadTracks(trackIds: List<Long>) {
        tracksJob?.cancel()
        tracksJob = viewModelScope.launch {
            playlistInteractor.getTracksForIds(trackIds).collect { trackList ->
                _tracks.postValue(trackList)
                val totalMillis = trackList.sumOf { it.trackTimeMillis ?: 0L }
                val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(totalMillis).toIntOrNull() ?: 0
                _totalDurationMinutes.postValue(minutes)
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
            _navigateBack.postValue(true)
        }
    }

    fun sharePlaylist(): Intent? {
        val currentPlaylist = _playlist.value ?: return null
        val trackList = _tracks.value ?: return null
        if (trackList.isEmpty()) return null

        val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())
        val sb = StringBuilder()
        sb.appendLine(currentPlaylist.name)
        if (currentPlaylist.description.isNotBlank()) {
            sb.appendLine(currentPlaylist.description)
        }
        sb.appendLine("${trackList.size} треков")
        trackList.forEachIndexed { index, track ->
            val duration = if (track.trackTimeMillis != null) {
                timeFormatter.format(track.trackTimeMillis)
            } else {
                "00:00"
            }
            sb.appendLine("${index + 1}. ${track.artistName ?: ""} - ${track.trackName ?: ""} ($duration)")
        }

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString().trim())
        }
    }

}

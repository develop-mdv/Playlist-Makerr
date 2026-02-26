package com.example.playlistmakerr.presentation.player

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmakerr.domain.api.FavoritesInteractor
import com.example.playlistmakerr.domain.api.PlaylistInteractor
import com.example.playlistmakerr.domain.models.Playlist
import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    private val mediaPlayer: MediaPlayer,
    private val favoritesInteractor: FavoritesInteractor,
    private val playlistInteractor: PlaylistInteractor,
) : ViewModel() {

    companion object {
        private const val PLAYBACK_UPDATE_DELAY = 300L
    }

    private val _playerState = MutableLiveData<PlayerScreenState>(PlayerScreenState.Default())
    val playerState: LiveData<PlayerScreenState> = _playerState

    private val _playlists = MutableLiveData<List<Playlist>>(emptyList())
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _addTrackToPlaylistResult = MutableLiveData<AddTrackToPlaylistResult?>()
    val addTrackToPlaylistResult: LiveData<AddTrackToPlaylistResult?> = _addTrackToPlaylistResult

    private var currentTrack: Track? = null
    private var isPrepared = false
    private var updateTimeJob: Job? = null
    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())

    fun prepare(url: String?) {
        if (isPrepared) return
        if (url.isNullOrEmpty()) return

        isPrepared = true
        mediaPlayer.apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                _playerState.value = PlayerScreenState.Prepared(currentFavoriteValue())
            }
            setOnCompletionListener {
                updateTimeJob?.cancel()
                _playerState.value = PlayerScreenState.Prepared(currentFavoriteValue())
            }
        }
    }

    fun setTrack(track: Track) {
        currentTrack = track
        viewModelScope.launch {
            val isTrackFavorite = favoritesInteractor.isFavorite(track.trackId)
            track.isFavorite = isTrackFavorite
            updateFavoriteInState(isTrackFavorite)
        }
    }

    fun onFavoriteClicked() {
        val track = currentTrack ?: return
        viewModelScope.launch {
            if (track.isFavorite) {
                favoritesInteractor.removeTrack(track)
            } else {
                favoritesInteractor.addTrack(track)
            }
            track.isFavorite = !track.isFavorite
            updateFavoriteInState(track.isFavorite)
        }
    }

    fun playbackControl() {
        when (_playerState.value) {
            is PlayerScreenState.Playing -> pause()
            is PlayerScreenState.Prepared, is PlayerScreenState.Paused -> play()
            else -> {}
        }
    }

    fun pause() {
        mediaPlayer.pause()
        val position = timeFormatter.format(mediaPlayer.currentPosition)
        _playerState.value = PlayerScreenState.Paused(position, currentFavoriteValue())
        updateTimeJob?.cancel()
    }

    private fun play() {
        mediaPlayer.start()
        val position = timeFormatter.format(mediaPlayer.currentPosition)
        _playerState.value = PlayerScreenState.Playing(position, currentFavoriteValue())
        startTrackProgressUpdates()
    }

    private fun startTrackProgressUpdates() {
        updateTimeJob?.cancel()
        updateTimeJob = viewModelScope.launch {
            while (isActive && mediaPlayer.isPlaying) {
                delay(PLAYBACK_UPDATE_DELAY)
                if (mediaPlayer.isPlaying) {
                    val position = timeFormatter.format(mediaPlayer.currentPosition)
                    _playerState.value = PlayerScreenState.Playing(position, currentFavoriteValue())
                }
            }
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getPlaylists().collect { list ->
                _playlists.postValue(list)
            }
        }
    }

    fun addTrackToPlaylist(playlist: Playlist) {
        val track = currentTrack ?: return
        if (playlist.trackIds.contains(track.trackId)) {
            _addTrackToPlaylistResult.value =
                AddTrackToPlaylistResult.AlreadyExists(playlist.name)
        } else {
            viewModelScope.launch {
                playlistInteractor.addTrackToPlaylist(track, playlist)
                _addTrackToPlaylistResult.postValue(
                    AddTrackToPlaylistResult.Added(playlist.name)
                )
            }
        }
    }

    fun clearAddTrackResult() {
        _addTrackToPlaylistResult.value = null
    }

    private fun currentFavoriteValue(): Boolean {
        return _playerState.value?.isFavorite ?: false
    }

    private fun updateFavoriteInState(isFavorite: Boolean) {
        _playerState.value = when (val state = _playerState.value ?: PlayerScreenState.Default()) {
            is PlayerScreenState.Default -> state.copy(isFavorite = isFavorite)
            is PlayerScreenState.Prepared -> state.copy(isFavorite = isFavorite)
            is PlayerScreenState.Playing -> state.copy(isFavorite = isFavorite)
            is PlayerScreenState.Paused -> state.copy(isFavorite = isFavorite)
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateTimeJob?.cancel()
        mediaPlayer.release()
    }
}

sealed class AddTrackToPlaylistResult {
    data class Added(val playlistName: String) : AddTrackToPlaylistResult()
    data class AlreadyExists(val playlistName: String) : AddTrackToPlaylistResult()
}

package com.example.playlistmakerr.presentation.player

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(private val mediaPlayer: MediaPlayer) : ViewModel() {

    companion object {
        private const val PLAYBACK_UPDATE_DELAY = 300L
    }

    private val _playerState = MutableLiveData<PlayerScreenState>(PlayerScreenState.Default)
    val playerState: LiveData<PlayerScreenState> = _playerState

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
                _playerState.value = PlayerScreenState.Prepared
            }
            setOnCompletionListener {
                updateTimeJob?.cancel()
                _playerState.value = PlayerScreenState.Prepared
            }
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
        _playerState.value = PlayerScreenState.Paused(position)
        updateTimeJob?.cancel()
    }

    private fun play() {
        mediaPlayer.start()
        val position = timeFormatter.format(mediaPlayer.currentPosition)
        _playerState.value = PlayerScreenState.Playing(position)
        startTrackProgressUpdates()
    }

    private fun startTrackProgressUpdates() {
        updateTimeJob?.cancel()
        updateTimeJob = viewModelScope.launch {
            while (isActive && mediaPlayer.isPlaying) {
                delay(PLAYBACK_UPDATE_DELAY)
                if (mediaPlayer.isPlaying) {
                    val position = timeFormatter.format(mediaPlayer.currentPosition)
                    _playerState.value = PlayerScreenState.Playing(position)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateTimeJob?.cancel()
        mediaPlayer.release()
    }
}

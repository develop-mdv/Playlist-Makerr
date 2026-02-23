package com.example.playlistmakerr.presentation.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel : ViewModel() {

    companion object {
        private const val PLAYBACK_UPDATE_DELAY = 300L
        private const val DEFAULT_POSITION = "00:00"
    }

    private val _playerState = MutableLiveData<PlayerScreenState>(PlayerScreenState.Default)
    val playerState: LiveData<PlayerScreenState> = _playerState

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.getDefault())

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            if (_playerState.value is PlayerScreenState.Playing) {
                val position = mediaPlayer?.currentPosition?.let { timeFormatter.format(it) }
                    ?: DEFAULT_POSITION
                _playerState.value = PlayerScreenState.Playing(position)
                handler.postDelayed(this, PLAYBACK_UPDATE_DELAY)
            }
        }
    }

    fun prepare(url: String?) {
        if (mediaPlayer != null) return
        if (url.isNullOrEmpty()) return

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                _playerState.value = PlayerScreenState.Prepared
            }
            setOnCompletionListener {
                handler.removeCallbacks(updateTimeRunnable)
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
        mediaPlayer?.pause()
        val position = mediaPlayer?.currentPosition?.let { timeFormatter.format(it) }
            ?: DEFAULT_POSITION
        _playerState.value = PlayerScreenState.Paused(position)
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun play() {
        mediaPlayer?.start()
        val position = mediaPlayer?.currentPosition?.let { timeFormatter.format(it) }
            ?: DEFAULT_POSITION
        _playerState.value = PlayerScreenState.Playing(position)
        handler.post(updateTimeRunnable)
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(updateTimeRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

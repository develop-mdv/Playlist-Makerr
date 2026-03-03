package com.example.playlistmakerr.presentation.newplaylist

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmakerr.domain.api.PlaylistInteractor
import com.example.playlistmakerr.domain.models.Playlist
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    application: Application,
    private val playlistInteractor: PlaylistInteractor,
) : CreatePlaylistViewModel(application, playlistInteractor) {

    private val _playlistData = MutableLiveData<Playlist?>()
    val playlistData: LiveData<Playlist?> = _playlistData

    private var editingPlaylist: Playlist? = null

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(playlistId)
            editingPlaylist = playlist
            _playlistData.postValue(playlist)
            if (playlist?.coverImagePath != null) {
                coverImageUri = Uri.parse(playlist.coverImagePath)
            }
        }
    }

    fun savePlaylist(name: String, description: String, onComplete: () -> Unit) {
        val playlist = editingPlaylist ?: return
        viewModelScope.launch {
            var coverPath = playlist.coverImagePath
            if (coverImageUri != null && coverImageUri.toString() != playlist.coverImagePath) {
                coverPath = saveImageToStorage(coverImageUri!!)
            }

            val updatedPlaylist = playlist.copy(
                name = name,
                description = description,
                coverImagePath = coverPath,
            )
            playlistInteractor.updatePlaylist(updatedPlaylist)
            onComplete()
        }
    }
}

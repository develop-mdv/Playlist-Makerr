package com.example.playlistmakerr.presentation.newplaylist

import android.app.Application
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

    private val _editPlaylist = MutableLiveData<Playlist?>()
    val editPlaylist: LiveData<Playlist?> = _editPlaylist

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(playlistId)
            _editPlaylist.postValue(playlist)
        }
    }

    fun updatePlaylist(name: String, description: String, onComplete: () -> Unit) {
        val current = _editPlaylist.value ?: return
        viewModelScope.launch {
            var coverPath = current.coverImagePath
            if (coverImageUri != null) {
                coverPath = saveImageToPrivateStoragePublic(coverImageUri!!)
            }

            val updatedPlaylist = current.copy(
                name = name,
                description = description,
                coverImagePath = coverPath ?: current.coverImagePath,
            )
            playlistInteractor.updatePlaylist(updatedPlaylist)
            onComplete()
        }
    }
}

package com.example.playlistmakerr.presentation.newplaylist

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmakerr.domain.api.PlaylistInteractor
import com.example.playlistmakerr.domain.models.Playlist
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class CreatePlaylistViewModel(
    application: Application,
    private val playlistInteractor: PlaylistInteractor,
) : AndroidViewModel(application) {

    var coverImageUri: Uri? = null
    private var savedCoverPath: String? = null

    fun createPlaylist(name: String, description: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (coverImageUri != null) {
                savedCoverPath = saveImageToPrivateStorage(coverImageUri!!)
            }

            val playlist = Playlist(
                name = name,
                description = description,
                coverImagePath = savedCoverPath,
            )
            playlistInteractor.createPlaylist(playlist)
            onComplete()
        }
    }

    private fun saveImageToPrivateStorage(uri: Uri): String? {
        val context = getApplication<Application>()
        val filePath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "playlist_covers"
        )
        if (!filePath.exists()) {
            filePath.mkdirs()
        }
        val file = File(filePath, "cover_${System.currentTimeMillis()}.jpg")
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        BitmapFactory
            .decodeStream(inputStream)
            .compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
        inputStream?.close()
        outputStream.close()
        return file.absolutePath
    }

    fun hasUnsavedData(name: String, description: String): Boolean {
        return coverImageUri != null || name.isNotBlank() || description.isNotBlank()
    }
}

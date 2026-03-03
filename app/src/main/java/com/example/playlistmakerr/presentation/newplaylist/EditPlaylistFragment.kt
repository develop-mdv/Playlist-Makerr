package com.example.playlistmakerr.presentation.newplaylist

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmakerr.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPlaylistFragment : CreatePlaylistFragment() {

    override val viewModel by viewModel<EditPlaylistViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = getString(R.string.edit_playlist_title)
        createButton.text = getString(R.string.save_button)

        val playlistId = arguments?.getLong(ARG_PLAYLIST_ID, -1L) ?: -1L
        if (playlistId == -1L) {
            findNavController().navigateUp()
            return
        }

        viewModel.loadPlaylist(playlistId)

        viewModel.playlistData.observe(viewLifecycleOwner) { playlist ->
            if (playlist == null) return@observe
            nameEditText.setText(playlist.name)
            descriptionEditText.setText(playlist.description)
            createButton.isEnabled = playlist.name.isNotBlank()

            if (!playlist.coverImagePath.isNullOrEmpty()) {
                val cornerRadius = resources.getDimensionPixelSize(R.dimen.create_playlist_cover_corner_radius)
                coverImage.background = null
                Glide.with(this)
                    .load(playlist.coverImagePath)
                    .transform(CenterCrop(), RoundedCorners(cornerRadius))
                    .into(coverImage)
            }
        }

        createButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            viewModel.savePlaylist(name, description) {
                findNavController().navigateUp()
            }
        }
    }

    override fun handleBack() {
        findNavController().navigateUp()
    }

    companion object {
        const val ARG_PLAYLIST_ID = "playlist_id"
    }
}

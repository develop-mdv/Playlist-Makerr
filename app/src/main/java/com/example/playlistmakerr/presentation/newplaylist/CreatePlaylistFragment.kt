package com.example.playlistmakerr.presentation.newplaylist

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmakerr.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.koin.androidx.viewmodel.ext.android.viewModel

open class CreatePlaylistFragment : Fragment() {

    open val viewModel by viewModel<CreatePlaylistViewModel>()

    protected lateinit var coverImage: ImageView
    protected lateinit var nameEditText: TextInputEditText
    protected lateinit var descriptionEditText: TextInputEditText
    protected lateinit var createButton: Button
    protected lateinit var toolbar: MaterialToolbar

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.coverImageUri = uri
                showCoverImage(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_create_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        coverImage = view.findViewById(R.id.coverImage)
        nameEditText = view.findViewById(R.id.nameEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        createButton = view.findViewById(R.id.createButton)
        toolbar = view.findViewById(R.id.toolbar)

        coverImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                createButton.isEnabled = !s.isNullOrBlank()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        createButton.setOnClickListener {
            onCreateButtonClick()
        }

        toolbar.setNavigationOnClickListener {
            handleBack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBack()
                }
            }
        )

        if (viewModel.coverImageUri != null) {
            showCoverImage(viewModel.coverImageUri!!)
        }
    }

    protected open fun onCreateButtonClick() {
        val name = nameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        viewModel.createPlaylist(name, description) {
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_created, name),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigateUp()
        }
    }

    protected open fun handleBack() {
        val name = nameEditText.text.toString()
        val description = descriptionEditText.text.toString()
        if (viewModel.hasUnsavedData(name, description)) {
            showConfirmDialog()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.finish_creating_dialog_title))
            .setMessage(getString(R.string.finish_creating_dialog_message))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.finish)) { _, _ ->
                findNavController().navigateUp()
            }
            .show()
    }

    private fun showCoverImage(uri: Uri) {
        val cornerRadius =
            resources.getDimensionPixelSize(R.dimen.create_playlist_cover_corner_radius)
        coverImage.background = null
        Glide.with(this)
            .load(uri)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))
            .into(coverImage)
    }
}

package com.example.playlistmakerr.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmakerr.R
import com.example.playlistmakerr.presentation.playlistdetails.PlaylistDetailsFragment
import com.google.android.material.button.MaterialButton
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private val viewModel by viewModel<PlaylistsViewModel>()

    private lateinit var newPlaylistButton: MaterialButton
    private lateinit var playlistsRecyclerView: RecyclerView
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView

    private val adapter = PlaylistGridAdapter { playlist ->
        val bundle = Bundle().apply {
            putLong(PlaylistDetailsFragment.ARG_PLAYLIST_ID, playlist.id)
        }
        findNavController().navigate(R.id.action_libraryFragment_to_playlistDetailsFragment, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_playlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newPlaylistButton = view.findViewById(R.id.newPlaylistButton)
        playlistsRecyclerView = view.findViewById(R.id.playlistsRecyclerView)
        placeholderImage = view.findViewById(R.id.placeholderImage)
        placeholderText = view.findViewById(R.id.placeholderText)

        playlistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        playlistsRecyclerView.adapter = adapter

        newPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_createPlaylistFragment)
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            if (playlists.isEmpty()) {
                playlistsRecyclerView.visibility = View.GONE
                placeholderImage.visibility = View.VISIBLE
                placeholderText.visibility = View.VISIBLE
            } else {
                playlistsRecyclerView.visibility = View.VISIBLE
                placeholderImage.visibility = View.GONE
                placeholderText.visibility = View.GONE
                adapter.updatePlaylists(playlists)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists()
    }

    companion object {
        fun newInstance(): PlaylistsFragment {
            return PlaylistsFragment()
        }
    }
}

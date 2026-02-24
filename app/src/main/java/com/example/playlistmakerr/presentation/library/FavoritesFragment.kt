package com.example.playlistmakerr.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmakerr.R
import com.example.playlistmakerr.domain.models.Track
import com.example.playlistmakerr.presentation.player.PlayerFragment
import com.example.playlistmakerr.presentation.search.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private val viewModel by viewModel<FavoritesViewModel>()
    private val tracks = ArrayList<Track>()
    private lateinit var trackAdapter: TrackAdapter

    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var tracksRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        placeholderImage = view.findViewById(R.id.placeholderImage)
        placeholderText = view.findViewById(R.id.placeholderText)
        tracksRecyclerView = view.findViewById(R.id.rvFavoritesTracks)

        trackAdapter = TrackAdapter(tracks) { track ->
            openPlayer(track)
        }

        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tracksRecyclerView.adapter = trackAdapter

        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun render(state: FavoritesScreenState) {
        when (state) {
            is FavoritesScreenState.Empty -> {
                placeholderImage.visibility = View.VISIBLE
                placeholderText.visibility = View.VISIBLE
                tracksRecyclerView.visibility = View.GONE
                tracks.clear()
                trackAdapter.notifyDataSetChanged()
            }
            is FavoritesScreenState.Content -> {
                placeholderImage.visibility = View.GONE
                placeholderText.visibility = View.GONE
                tracksRecyclerView.visibility = View.VISIBLE
                tracks.clear()
                tracks.addAll(state.tracks)
                trackAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun openPlayer(track: Track) {
        val bundle = Bundle().apply {
            putSerializable(PlayerFragment.EXTRA_TRACK, track)
        }
        findNavController().navigate(R.id.action_libraryFragment_to_playerFragment, bundle)
    }

    companion object {
        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }
}

package com.example.playlistmakerr.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmakerr.R
import com.example.playlistmakerr.domain.models.Track
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerFragment : Fragment() {

    companion object {
        const val EXTRA_TRACK = "track"
    }

    private val viewModel by viewModel<PlayerViewModel>()

    private lateinit var playButton: ImageButton
    private lateinit var favoriteButton: ImageButton
    private lateinit var addToPlaylistButton: ImageButton
    private lateinit var playTimeText: TextView

    private lateinit var overlay: View
    private lateinit var bottomSheetContainer: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var playlistsRecyclerView: RecyclerView
    private lateinit var newPlaylistButtonBs: MaterialButton
    private lateinit var playlistAdapter: PlaylistBottomSheetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val track = arguments?.let { BundleCompat.getParcelable(it, EXTRA_TRACK, Track::class.java) }

        if (track == null) {
            findNavController().navigateUp()
            return
        }

        setupTrackInfo(view, track)

        playButton = view.findViewById(R.id.playButton)
        favoriteButton = view.findViewById(R.id.favoriteButton)
        addToPlaylistButton = view.findViewById(R.id.addToPlaylistButton)
        playTimeText = view.findViewById(R.id.playTime)

        setupBottomSheet(view)

        playButton.setOnClickListener {
            viewModel.playbackControl()
        }

        favoriteButton.setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        addToPlaylistButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            viewModel.loadPlaylists()
        }

        newPlaylistButtonBs.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.action_playerFragment_to_createPlaylistFragment)
        }

        viewModel.playerState.observe(viewLifecycleOwner) { state ->
            renderPlayerState(state)
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistAdapter.updatePlaylists(playlists)
        }

        viewModel.addTrackToPlaylistResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe
            when (result) {
                is AddTrackToPlaylistResult.Added -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.track_added_to_playlist, result.playlistName),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is AddTrackToPlaylistResult.AlreadyExists -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.track_already_in_playlist, result.playlistName),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            viewModel.clearAddTrackResult()
        }

        viewModel.setTrack(track)
        viewModel.prepare(track.previewUrl)
    }

    private fun setupBottomSheet(view: View) {
        overlay = view.findViewById(R.id.overlay)
        bottomSheetContainer = view.findViewById(R.id.playlistsBottomSheet)
        playlistsRecyclerView = view.findViewById(R.id.playlistsRecyclerViewBs)
        newPlaylistButtonBs = view.findViewById(R.id.newPlaylistButtonBs)

        playlistAdapter = PlaylistBottomSheetAdapter { playlist ->
            viewModel.addTrackToPlaylist(playlist)
        }
        playlistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        playlistsRecyclerView.adapter = playlistAdapter

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        overlay.visibility = View.GONE
                    }
                    else -> {
                        overlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                overlay.alpha = (slideOffset + 1f) / 2f
            }
        })
    }

    private fun setupTrackInfo(view: View, track: Track) {
        val coverImage = view.findViewById<ImageView>(R.id.cover)
        val trackNameText = view.findViewById<TextView>(R.id.trackName)
        val artistNameText = view.findViewById<TextView>(R.id.artistName)
        val durationValue = view.findViewById<TextView>(R.id.durationValue)
        val albumValue = view.findViewById<TextView>(R.id.albumValue)
        val yearValue = view.findViewById<TextView>(R.id.yearValue)
        val genreValue = view.findViewById<TextView>(R.id.genreValue)
        val countryValue = view.findViewById<TextView>(R.id.countryValue)
        val albumGroup = view.findViewById<Group>(R.id.albumGroup)
        val yearGroup = view.findViewById<Group>(R.id.yearGroup)

        trackNameText.text = track.trackName ?: ""
        artistNameText.text = track.artistName ?: ""

        durationValue.text = if (track.trackTimeMillis != null) {
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
        } else {
            ""
        }

        if (track.collectionName.isNullOrEmpty()) {
            albumGroup.visibility = View.GONE
        } else {
            albumValue.text = track.collectionName
        }

        if (track.releaseDate.isNullOrEmpty()) {
            yearGroup.visibility = View.GONE
        } else {
            yearValue.text = track.releaseDate.substring(0, 4)
        }

        genreValue.text = track.primaryGenreName ?: ""
        countryValue.text = track.country ?: ""

        val cornerRadius = resources.getDimensionPixelSize(R.dimen.player_cover_corner_radius)
        Glide.with(this)
            .load(track.getCoverArtwork())
            .transform(CenterCrop(), RoundedCorners(cornerRadius))
            .placeholder(R.drawable.player_cover_placeholder)
            .error(R.drawable.player_cover_placeholder)
            .into(coverImage)
    }

    private fun renderPlayerState(state: PlayerScreenState) {
        renderFavoriteState(state.isFavorite)
        when (state) {
            is PlayerScreenState.Default -> {
                playButton.setImageResource(R.drawable.ic_play_arrow)
                playTimeText.text = getString(R.string.default_play_time)
            }
            is PlayerScreenState.Prepared -> {
                playButton.setImageResource(R.drawable.ic_play_arrow)
                playTimeText.text = getString(R.string.default_play_time)
            }
            is PlayerScreenState.Playing -> {
                playButton.setImageResource(R.drawable.ic_pause)
                playTimeText.text = state.currentPosition
            }
            is PlayerScreenState.Paused -> {
                playButton.setImageResource(R.drawable.ic_play_arrow)
                playTimeText.text = state.currentPosition
            }
        }
    }

    private fun renderFavoriteState(isFavorite: Boolean) {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite)
            favoriteButton.imageTintList =
                android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.yp_red)
                )
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border)
            favoriteButton.imageTintList =
                android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.player_secondary_icon_tint)
                )
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.playerState.value is PlayerScreenState.Playing) {
            viewModel.pause()
        }
    }
}

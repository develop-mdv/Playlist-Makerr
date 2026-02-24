package com.example.playlistmakerr.presentation.player

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmakerr.R
import com.example.playlistmakerr.domain.models.Track
import com.google.android.material.appbar.MaterialToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerFragment : Fragment() {

    companion object {
        const val EXTRA_TRACK = "track"
    }

    private val viewModel by viewModel<PlayerViewModel>()

    private lateinit var playButton: ImageButton
    private lateinit var playTimeText: TextView

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

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(EXTRA_TRACK, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(EXTRA_TRACK) as? Track
        }

        if (track == null) {
            findNavController().navigateUp()
            return
        }

        setupTrackInfo(view, track)

        playButton = view.findViewById(R.id.playButton)
        playTimeText = view.findViewById(R.id.playTime)

        playButton.setOnClickListener {
            viewModel.playbackControl()
        }

        viewModel.playerState.observe(viewLifecycleOwner) { state ->
            renderPlayerState(state)
        }

        viewModel.prepare(track.previewUrl)
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

    override fun onPause() {
        super.onPause()
        if (viewModel.playerState.value is PlayerScreenState.Playing) {
            viewModel.pause()
        }
    }
}

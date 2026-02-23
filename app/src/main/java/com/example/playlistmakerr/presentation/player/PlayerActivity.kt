package com.example.playlistmakerr.presentation.player

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmakerr.R
import com.example.playlistmakerr.domain.models.Track
import com.google.android.material.appbar.MaterialToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACK = "track"
    }

    private val viewModel by viewModel<PlayerViewModel>()

    private lateinit var playButton: ImageButton
    private lateinit var playTimeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_TRACK, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_TRACK) as? Track
        }

        if (track == null) {
            finish()
            return
        }

        setupTrackInfo(track)

        playButton = findViewById(R.id.playButton)
        playTimeText = findViewById(R.id.playTime)

        playButton.setOnClickListener {
            viewModel.playbackControl()
        }

        viewModel.playerState.observe(this) { state ->
            renderPlayerState(state)
        }

        viewModel.prepare(track.previewUrl)
    }

    private fun setupTrackInfo(track: Track) {
        val coverImage = findViewById<ImageView>(R.id.cover)
        val trackNameText = findViewById<TextView>(R.id.trackName)
        val artistNameText = findViewById<TextView>(R.id.artistName)
        val durationValue = findViewById<TextView>(R.id.durationValue)
        val albumValue = findViewById<TextView>(R.id.albumValue)
        val yearValue = findViewById<TextView>(R.id.yearValue)
        val genreValue = findViewById<TextView>(R.id.genreValue)
        val countryValue = findViewById<TextView>(R.id.countryValue)
        val albumGroup = findViewById<Group>(R.id.albumGroup)
        val yearGroup = findViewById<Group>(R.id.yearGroup)

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

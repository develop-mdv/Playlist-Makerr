package com.example.playlistmakerr.presentation.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.example.playlistmakerr.R
import com.example.playlistmakerr.domain.models.Track
import com.example.playlistmakerr.presentation.player.PlayerFragment
import com.example.playlistmakerr.presentation.search.TrackAdapter
import com.example.playlistmakerr.presentation.util.MinuteCountFormatter
import com.example.playlistmakerr.presentation.util.TrackCountFormatter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistDetailFragment : Fragment() {

    companion object {
        const val EXTRA_PLAYLIST_ID = "playlist_id"
    }

    private val viewModel by viewModel<PlaylistDetailViewModel>()

    private lateinit var coverImage: ImageView
    private lateinit var playlistName: TextView
    private lateinit var playlistDescription: TextView
    private lateinit var playlistInfo: TextView
    private lateinit var shareButton: ImageView
    private lateinit var menuButton: ImageView
    private lateinit var noTracksMessage: TextView
    private lateinit var tracksRecyclerView: RecyclerView

    private lateinit var menuBottomSheet: LinearLayout
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var overlay: View
    private lateinit var menuPlaylistCover: ImageView
    private lateinit var menuPlaylistName: TextView
    private lateinit var menuPlaylistTrackCount: TextView

    private lateinit var tracksBottomSheet: LinearLayout
    private lateinit var tracksBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var trackAdapter: TrackAdapter? = null
    private val trackList = mutableListOf<Track>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_playlist_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupBottomSheets()
        setupListeners()

        val playlistId = arguments?.getLong(EXTRA_PLAYLIST_ID, -1L) ?: -1L
        if (playlistId == -1L) {
            findNavController().navigateUp()
            return
        }

        trackAdapter = TrackAdapter(trackList,
            onItemClick = { track -> openPlayer(track) },
            onLongClick = { track -> showDeleteTrackDialog(track) }
        )
        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tracksRecyclerView.adapter = trackAdapter

        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            if (playlist == null) return@observe
            renderPlaylist(playlist)
        }

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            trackList.clear()
            trackList.addAll(tracks)
            trackAdapter?.notifyDataSetChanged()

            if (tracks.isEmpty()) {
                noTracksMessage.visibility = View.VISIBLE
                tracksRecyclerView.visibility = View.GONE
            } else {
                noTracksMessage.visibility = View.GONE
                tracksRecyclerView.visibility = View.VISIBLE
            }

            viewModel.playlist.value?.let { renderPlaylist(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        val playlistId = arguments?.getLong(EXTRA_PLAYLIST_ID, -1L) ?: -1L
        if (playlistId != -1L) {
            viewModel.loadPlaylist(playlistId)
        }
    }

    private fun initViews(view: View) {
        coverImage = view.findViewById(R.id.playlistCover)
        playlistName = view.findViewById(R.id.playlistName)
        playlistDescription = view.findViewById(R.id.playlistDescription)
        playlistInfo = view.findViewById(R.id.playlistInfo)
        shareButton = view.findViewById(R.id.shareButton)
        menuButton = view.findViewById(R.id.menuButton)
        noTracksMessage = view.findViewById(R.id.noTracksMessage)
        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView)

        overlay = view.findViewById(R.id.overlay)
        menuBottomSheet = view.findViewById(R.id.menuBottomSheet)
        menuPlaylistCover = view.findViewById(R.id.menuPlaylistCover)
        menuPlaylistName = view.findViewById(R.id.menuPlaylistName)
        menuPlaylistTrackCount = view.findViewById(R.id.menuPlaylistTrackCount)

        tracksBottomSheet = view.findViewById(R.id.tracksBottomSheet)

        view.findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupBottomSheets() {
        tracksBottomSheetBehavior = BottomSheetBehavior.from(tracksBottomSheet)

        coverImage.post {
            val screenHeight = requireView().height
            val coverHeight = coverImage.height
            tracksBottomSheetBehavior.peekHeight = screenHeight - coverHeight
        }

        menuBottomSheetBehavior = BottomSheetBehavior.from(menuBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        menuBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> overlay.visibility = View.GONE
                    else -> overlay.visibility = View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                overlay.alpha = (slideOffset + 1f) / 2f
            }
        })
    }

    private fun setupListeners() {
        shareButton.setOnClickListener { handleShare() }

        menuButton.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        overlay.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        menuBottomSheet.findViewById<TextView>(R.id.menuShare).setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            handleShare()
        }

        menuBottomSheet.findViewById<TextView>(R.id.menuEdit).setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val playlist = viewModel.playlist.value ?: return@setOnClickListener
            findNavController().navigate(
                R.id.action_playlistDetailFragment_to_editPlaylistFragment,
                bundleOf("playlist_id" to playlist.id)
            )
        }

        menuBottomSheet.findViewById<TextView>(R.id.menuDelete).setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            showDeletePlaylistDialog()
        }
    }

    private fun renderPlaylist(playlist: com.example.playlistmakerr.domain.models.Playlist) {
        playlistName.text = playlist.name

        if (playlist.description.isBlank()) {
            playlistDescription.visibility = View.GONE
        } else {
            playlistDescription.visibility = View.VISIBLE
            playlistDescription.text = playlist.description
        }

        val tracks = viewModel.tracks.value ?: emptyList()
        val totalDurationMs = tracks.sumOf { it.trackTimeMillis ?: 0L }
        val totalMinutes = SimpleDateFormat("mm", Locale.getDefault()).format(totalDurationMs).toIntOrNull() ?: 0
        val minutesText = MinuteCountFormatter.format(totalMinutes, requireContext())
        val tracksCountText = TrackCountFormatter.format(playlist.trackCount, requireContext())
        playlistInfo.text = "$minutesText \u00B7 $tracksCountText"

        if (!playlist.coverImagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(playlist.coverImagePath)
                .transform(CenterCrop())
                .placeholder(R.drawable.placeholder_playlist_cover)
                .error(R.drawable.placeholder_playlist_cover)
                .into(coverImage)
        } else {
            coverImage.setImageResource(R.drawable.placeholder_playlist_cover)
        }

        menuPlaylistName.text = playlist.name
        menuPlaylistTrackCount.text = tracksCountText

        if (!playlist.coverImagePath.isNullOrEmpty()) {
            val cornerRadius = resources.getDimensionPixelSize(R.dimen.playlist_bs_item_cover_corner_radius)
            Glide.with(this)
                .load(playlist.coverImagePath)
                .transform(CenterCrop(), com.bumptech.glide.load.resource.bitmap.RoundedCorners(cornerRadius))
                .placeholder(R.drawable.placeholder_playlist_cover)
                .error(R.drawable.placeholder_playlist_cover)
                .into(menuPlaylistCover)
        } else {
            menuPlaylistCover.setImageResource(R.drawable.placeholder_playlist_cover)
        }
    }

    private fun handleShare() {
        val tracks = viewModel.tracks.value
        if (tracks.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.no_tracks_to_share),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val intent = viewModel.sharePlaylist()
        if (intent != null) {
            startActivity(intent)
        }
    }

    private fun openPlayer(track: Track) {
        findNavController().navigate(
            R.id.action_playlistDetailFragment_to_playerFragment,
            bundleOf(PlayerFragment.EXTRA_TRACK to track)
        )
    }

    private fun showDeleteTrackDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.delete_track_dialog_message))
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.removeTrack(track.trackId)
            }
            .show()
    }

    private fun showDeletePlaylistDialog() {
        val playlist = viewModel.playlist.value ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_playlist_dialog_title))
            .setMessage(getString(R.string.delete_playlist_dialog_message, playlist.name))
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deletePlaylist()
                findNavController().navigateUp()
            }
            .show()
    }
}

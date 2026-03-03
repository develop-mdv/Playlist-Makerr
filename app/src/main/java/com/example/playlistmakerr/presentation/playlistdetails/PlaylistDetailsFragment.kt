package com.example.playlistmakerr.presentation.playlistdetails

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmakerr.R
import com.example.playlistmakerr.domain.models.Track
import com.example.playlistmakerr.presentation.player.PlayerFragment
import com.example.playlistmakerr.presentation.search.TrackAdapter
import com.example.playlistmakerr.presentation.util.MinutesFormatter
import com.example.playlistmakerr.presentation.util.TrackCountFormatter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistDetailsFragment : Fragment() {

    private val viewModel by viewModel<PlaylistDetailsViewModel>()

    private lateinit var coverImage: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var playlistName: TextView
    private lateinit var playlistDescription: TextView
    private lateinit var playlistInfo: TextView
    private lateinit var shareButton: ImageButton
    private lateinit var menuButton: ImageButton

    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var emptyTracksMessage: TextView
    private lateinit var tracksBottomSheet: LinearLayout
    private lateinit var tracksBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var overlay: View
    private lateinit var menuBottomSheet: LinearLayout
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var menuPlaylistCover: ImageView
    private lateinit var menuPlaylistName: TextView
    private lateinit var menuPlaylistTrackCount: TextView
    private lateinit var menuShare: TextView
    private lateinit var menuEdit: TextView
    private lateinit var menuDelete: TextView

    private val tracks = mutableListOf<Track>()
    private lateinit var trackAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_playlist_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupTracksBottomSheet()
        setupMenuBottomSheet()
        setupClickListeners()
        observeViewModel()

        val playlistId = arguments?.getLong(ARG_PLAYLIST_ID, -1L) ?: -1L
        if (playlistId == -1L) {
            findNavController().navigateUp()
            return
        }
        viewModel.loadPlaylist(playlistId)
    }

    override fun onResume() {
        super.onResume()
        val playlistId = arguments?.getLong(ARG_PLAYLIST_ID, -1L) ?: -1L
        if (playlistId != -1L) {
            viewModel.loadPlaylist(playlistId)
        }
    }

    private fun initViews(view: View) {
        coverImage = view.findViewById(R.id.coverImage)
        backButton = view.findViewById(R.id.backButton)
        playlistName = view.findViewById(R.id.playlistName)
        playlistDescription = view.findViewById(R.id.playlistDescription)
        playlistInfo = view.findViewById(R.id.playlistInfo)
        shareButton = view.findViewById(R.id.shareButton)
        menuButton = view.findViewById(R.id.menuButton)

        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView)
        emptyTracksMessage = view.findViewById(R.id.emptyTracksMessage)
        tracksBottomSheet = view.findViewById(R.id.tracksBottomSheet)

        overlay = view.findViewById(R.id.overlay)
        menuBottomSheet = view.findViewById(R.id.menuBottomSheet)
        menuPlaylistCover = view.findViewById(R.id.menuPlaylistCover)
        menuPlaylistName = view.findViewById(R.id.menuPlaylistName)
        menuPlaylistTrackCount = view.findViewById(R.id.menuPlaylistTrackCount)
        menuShare = view.findViewById(R.id.menuShare)
        menuEdit = view.findViewById(R.id.menuEdit)
        menuDelete = view.findViewById(R.id.menuDelete)

        trackAdapter = TrackAdapter(tracks,
            onItemClick = { track -> openPlayer(track) },
            onLongClick = { track -> showDeleteTrackDialog(track) }
        )
        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tracksRecyclerView.adapter = trackAdapter
    }

    private fun setupTracksBottomSheet() {
        tracksBottomSheetBehavior = BottomSheetBehavior.from(tracksBottomSheet).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
        // Keep action buttons fully visible above the sheet on all screen heights.
        view?.doOnLayout { updateTracksBottomSheetPeekHeight() }
    }

    private fun updateTracksBottomSheetPeekHeight() {
        val root = view ?: return
        if (!::shareButton.isInitialized || !::menuButton.isInitialized) return

        val buttonsBottom = maxOf(shareButton.bottom, menuButton.bottom)
        if (buttonsBottom <= 0) return

        val density = resources.displayMetrics.density
        val gapPx = (32 * density).toInt()
        val minPeekPx = (220 * density).toInt()
        val desiredTop = buttonsBottom + gapPx
        val dynamicPeekHeight = (root.height - desiredTop).coerceAtLeast(minPeekPx)

        tracksBottomSheetBehavior.peekHeight = dynamicPeekHeight
        tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setupMenuBottomSheet() {
        menuBottomSheetBehavior = BottomSheetBehavior.from(menuBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        menuBottomSheetBehavior.addBottomSheetCallback(object :
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

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        shareButton.setOnClickListener {
            sharePlaylist()
        }

        menuButton.setOnClickListener {
            updateMenuInfo()
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        menuShare.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            sharePlaylist()
        }

        menuEdit.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val playlist = viewModel.playlist.value ?: return@setOnClickListener
            val bundle = Bundle().apply {
                putLong(ARG_PLAYLIST_ID, playlist.id)
            }
            findNavController().navigate(R.id.action_playlistDetailsFragment_to_editPlaylistFragment, bundle)
        }

        menuDelete.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            showDeletePlaylistDialog()
        }

        overlay.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun observeViewModel() {
        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            if (playlist == null) return@observe
            playlistName.text = playlist.name
            if (playlist.description.isNotBlank()) {
                playlistDescription.text = playlist.description
                playlistDescription.visibility = View.VISIBLE
            } else {
                playlistDescription.visibility = View.GONE
            }

            if (!playlist.coverImagePath.isNullOrEmpty()) {
                coverImage.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this)
                    .load(playlist.coverImagePath)
                    .centerCrop()
                    .placeholder(R.drawable.playlist_details_cover_placeholder)
                    .error(R.drawable.playlist_details_cover_placeholder)
                    .into(coverImage)
            } else {
                Glide.with(this).clear(coverImage)
                coverImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
                coverImage.setImageResource(R.drawable.playlist_details_cover_placeholder)
            }
            view?.post { updateTracksBottomSheetPeekHeight() }
        }

        viewModel.tracks.observe(viewLifecycleOwner) { trackList ->
            tracks.clear()
            tracks.addAll(trackList)
            trackAdapter.notifyDataSetChanged()

            if (trackList.isEmpty()) {
                emptyTracksMessage.visibility = View.VISIBLE
                tracksRecyclerView.visibility = View.GONE
            } else {
                emptyTracksMessage.visibility = View.GONE
                tracksRecyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.totalDurationMinutes.observe(viewLifecycleOwner) { minutes ->
            updatePlaylistInfo(minutes)
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigateUp()
            }
        }
    }

    private fun updatePlaylistInfo(minutes: Int) {
        val playlist = viewModel.playlist.value ?: return
        val minutesText = MinutesFormatter.format(minutes, requireContext())
        val tracksText = TrackCountFormatter.format(playlist.trackCount, requireContext())
        playlistInfo.text = "$minutesText \u00B7 $tracksText"
    }

    private fun updateMenuInfo() {
        val playlist = viewModel.playlist.value ?: return
        menuPlaylistName.text = playlist.name
        menuPlaylistTrackCount.text = TrackCountFormatter.format(playlist.trackCount, requireContext())

        val cornerRadius = resources.getDimensionPixelSize(R.dimen.playlist_bs_item_cover_corner_radius)
        Glide.with(this)
            .load(playlist.coverImagePath)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))
            .placeholder(R.drawable.placeholder_playlist_cover)
            .error(R.drawable.placeholder_playlist_cover)
            .into(menuPlaylistCover)
    }

    private fun openPlayer(track: Track) {
        val bundle = Bundle().apply {
            putSerializable(PlayerFragment.EXTRA_TRACK, track)
        }
        findNavController().navigate(R.id.action_playlistDetailsFragment_to_playerFragment, bundle)
    }

    private fun showDeleteTrackDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.delete_track_dialog_message))
            .setNegativeButton(getString(R.string.dialog_no)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                viewModel.removeTrack(track.trackId)
            }
            .show()
    }

    private fun showDeletePlaylistDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_playlist_dialog_title))
            .setMessage(getString(R.string.delete_playlist_dialog_message))
            .setNegativeButton(getString(R.string.delete_playlist_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.delete_playlist_confirm)) { _, _ ->
                viewModel.deletePlaylist()
            }
            .show()
    }

    private fun sharePlaylist() {
        val intent = viewModel.sharePlaylist()
        if (intent == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_share_empty),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            startActivity(Intent.createChooser(intent, null))
        }
    }

    companion object {
        const val ARG_PLAYLIST_ID = "playlist_id"
    }
}

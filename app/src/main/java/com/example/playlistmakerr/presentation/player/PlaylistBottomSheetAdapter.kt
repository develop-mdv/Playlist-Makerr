package com.example.playlistmakerr.presentation.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmakerr.R
import com.example.playlistmakerr.domain.models.Playlist
import com.example.playlistmakerr.presentation.util.TrackCountFormatter

class PlaylistBottomSheetAdapter(
    private val playlists: MutableList<Playlist> = mutableListOf(),
    private val onItemClick: (Playlist) -> Unit,
) : RecyclerView.Adapter<PlaylistBottomSheetAdapter.PlaylistBsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistBsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_bottom_sheet, parent, false)
        return PlaylistBsViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistBsViewHolder, position: Int) {
        holder.bind(playlists[position])
        holder.itemView.setOnClickListener {
            onItemClick(playlists[position])
        }
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newPlaylists)
        notifyDataSetChanged()
    }

    class PlaylistBsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coverImage: ImageView = itemView.findViewById(R.id.playlistCover)
        private val nameText: TextView = itemView.findViewById(R.id.playlistName)
        private val trackCountText: TextView = itemView.findViewById(R.id.playlistTrackCount)

        fun bind(playlist: Playlist) {
            nameText.text = playlist.name
            trackCountText.text = TrackCountFormatter.format(playlist.trackCount, itemView.context)

            val cornerRadius =
                itemView.resources.getDimensionPixelSize(R.dimen.playlist_bs_item_cover_corner_radius)

            Glide.with(itemView)
                .load(playlist.coverImagePath)
                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                .placeholder(R.drawable.placeholder_playlist_cover)
                .error(R.drawable.placeholder_playlist_cover)
                .into(coverImage)
        }
    }
}

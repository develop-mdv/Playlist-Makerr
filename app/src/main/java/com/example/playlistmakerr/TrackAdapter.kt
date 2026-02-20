package com.example.playlistmakerr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class TrackAdapter(
    private val tracks: List<Track>,
    private val onItemClick: ((Track) -> Unit)? = null
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(tracks[position])
        }
    }

    override fun getItemCount(): Int = tracks.size

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val coverImageView: ImageView = itemView.findViewById(R.id.iv_cover)
        private val trackNameTextView: TextView = itemView.findViewById(R.id.tv_track_name)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.tv_artist_name)
        private val trackTimeTextView: TextView = itemView.findViewById(R.id.tv_track_time)

        fun bind(track: Track) {
            trackNameTextView.text = track.trackName ?: ""
            artistNameTextView.text = track.artistName ?: ""
            trackTimeTextView.text = if (track.trackTimeMillis != null) {
                SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
            } else {
                ""
            }

            val radius = itemView.resources.getDimensionPixelSize(R.dimen.track_cover_corner_radius)

            Glide.with(itemView)
                .load(track.artworkUrl100)
                .transform(CenterCrop(), RoundedCorners(radius))
                .placeholder(R.drawable.track_cover_placeholder)
                .error(R.drawable.track_cover_placeholder)
                .into(coverImageView)
        }
    }
}

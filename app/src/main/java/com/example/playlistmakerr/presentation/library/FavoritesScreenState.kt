package com.example.playlistmakerr.presentation.library

import com.example.playlistmakerr.domain.models.Track

sealed interface FavoritesScreenState {
    data object Empty : FavoritesScreenState
    data class Content(val tracks: List<Track>) : FavoritesScreenState
}

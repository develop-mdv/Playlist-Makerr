package com.example.playlistmakerr.presentation.search

import com.example.playlistmakerr.domain.models.Track

sealed class SearchScreenState {
    object Loading : SearchScreenState()
    data class Content(val tracks: List<Track>) : SearchScreenState()
    object NothingFound : SearchScreenState()
    object ConnectionError : SearchScreenState()
    data class History(val tracks: List<Track>) : SearchScreenState()
    object Empty : SearchScreenState()
}

package com.example.playlistmakerr.presentation.player

sealed class PlayerScreenState {
    object Default : PlayerScreenState()
    object Prepared : PlayerScreenState()
    data class Playing(val currentPosition: String) : PlayerScreenState()
    data class Paused(val currentPosition: String) : PlayerScreenState()
}

package com.example.playlistmakerr.presentation.player

sealed class PlayerScreenState(open val isFavorite: Boolean) {
    data class Default(override val isFavorite: Boolean = false) : PlayerScreenState(isFavorite)
    data class Prepared(override val isFavorite: Boolean = false) : PlayerScreenState(isFavorite)
    data class Playing(
        val currentPosition: String,
        override val isFavorite: Boolean = false,
    ) : PlayerScreenState(isFavorite)
    data class Paused(
        val currentPosition: String,
        override val isFavorite: Boolean = false,
    ) : PlayerScreenState(isFavorite)
}

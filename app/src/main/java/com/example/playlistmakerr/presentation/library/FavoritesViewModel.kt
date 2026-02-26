package com.example.playlistmakerr.presentation.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmakerr.domain.api.FavoritesInteractor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesInteractor: FavoritesInteractor,
) : ViewModel() {

    private val _screenState = MutableLiveData<FavoritesScreenState>(FavoritesScreenState.Empty)
    val screenState: LiveData<FavoritesScreenState> = _screenState

    init {
        viewModelScope.launch {
            favoritesInteractor.getTracks().collect { tracks ->
                _screenState.value = if (tracks.isEmpty()) {
                    FavoritesScreenState.Empty
                } else {
                    FavoritesScreenState.Content(tracks)
                }
            }
        }
    }
}

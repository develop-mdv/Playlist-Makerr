package com.example.playlistmakerr.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmakerr.domain.api.SearchHistoryInteractor
import com.example.playlistmakerr.domain.api.TracksInteractor
import com.example.playlistmakerr.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor,
) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    private val _screenState = MutableLiveData<SearchScreenState>(SearchScreenState.Empty)
    val screenState: LiveData<SearchScreenState> = _screenState

    private var lastQuery: String = ""
    private var searchDebounceJob: Job? = null
    private var searchJob: Job? = null

    fun searchDebounce(query: String) {
        lastQuery = query
        searchDebounceJob?.cancel()
        if (query.isNotEmpty()) {
            searchDebounceJob = viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_DELAY)
                search(query)
            }
        }
    }

    fun search(query: String) {
        if (query.isEmpty()) return

        searchDebounceJob?.cancel()
        searchJob?.cancel()
        lastQuery = query
        _screenState.value = SearchScreenState.Loading

        searchJob = viewModelScope.launch {
            tracksInteractor.searchTracks(query).collect { (foundTracks, _) ->
                if (foundTracks != null) {
                    if (foundTracks.isEmpty()) {
                        _screenState.value = SearchScreenState.NothingFound
                    } else {
                        _screenState.value = SearchScreenState.Content(foundTracks)
                    }
                } else {
                    _screenState.value = SearchScreenState.ConnectionError
                }
            }
        }
    }

    fun showHistory(hasFocus: Boolean, text: CharSequence?) {
        if (hasFocus && text.isNullOrEmpty()) {
            val history = searchHistoryInteractor.getHistory()
            if (history.isNotEmpty()) {
                _screenState.value = SearchScreenState.History(history)
            } else {
                _screenState.value = SearchScreenState.Empty
            }
        } else if (text.isNullOrEmpty()) {
            _screenState.value = SearchScreenState.Empty
        }
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryInteractor.addTrack(track)
    }

    fun clearHistory() {
        searchHistoryInteractor.clearHistory()
        _screenState.value = SearchScreenState.Empty
    }

    fun clearSearch() {
        searchDebounceJob?.cancel()
        searchJob?.cancel()
        lastQuery = ""
    }

    fun refreshSearch() {
        if (lastQuery.isNotEmpty()) {
            search(lastQuery)
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchDebounceJob?.cancel()
        searchJob?.cancel()
    }
}

package com.example.playlistmakerr.presentation.search

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmakerr.domain.api.SearchHistoryInteractor
import com.example.playlistmakerr.domain.api.TracksInteractor
import com.example.playlistmakerr.domain.models.Track

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
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable {
        if (lastQuery.isNotEmpty()) {
            search(lastQuery)
        }
    }

    fun searchDebounce(query: String) {
        lastQuery = query
        handler.removeCallbacks(searchRunnable)
        if (query.isNotEmpty()) {
            handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
        }
    }

    fun search(query: String) {
        handler.removeCallbacks(searchRunnable)
        lastQuery = query
        _screenState.value = SearchScreenState.Loading

        tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
                if (foundTracks != null) {
                    if (foundTracks.isEmpty()) {
                        _screenState.postValue(SearchScreenState.NothingFound)
                    } else {
                        _screenState.postValue(SearchScreenState.Content(foundTracks))
                    }
                } else {
                    _screenState.postValue(SearchScreenState.ConnectionError)
                }
            }
        })
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
        handler.removeCallbacks(searchRunnable)
        lastQuery = ""
    }

    fun refreshSearch() {
        if (lastQuery.isNotEmpty()) {
            search(lastQuery)
        }
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(searchRunnable)
    }
}

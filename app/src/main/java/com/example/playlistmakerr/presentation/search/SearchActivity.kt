package com.example.playlistmakerr.presentation.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmakerr.R
import com.example.playlistmakerr.creator.Creator
import com.example.playlistmakerr.domain.api.SearchHistoryInteractor
import com.example.playlistmakerr.domain.api.TracksInteractor
import com.example.playlistmakerr.domain.models.Track
import com.example.playlistmakerr.presentation.player.PlayerActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var placeholderLayout: LinearLayout
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var searchContentFrame: View

    private lateinit var historyLayout: View
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var clearHistoryButton: MaterialButton

    private lateinit var tracksInteractor: TracksInteractor
    private lateinit var searchHistoryInteractor: SearchHistoryInteractor

    private var searchText: String = ""
    private val tracks = ArrayList<Track>()
    private val historyTracks = ArrayList<Track>()
    private var lastQuery: String = ""

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable {
        val query = searchEditText.text.toString()
        if (query.isNotEmpty()) {
            search(query)
        }
    }

    private var isClickAllowed = true
    private val clickRunnable = Runnable { isClickAllowed = true }

    companion object {
        const val SEARCH_QUERY = "SEARCH_QUERY"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        tracksInteractor = Creator.provideTracksInteractor()
        searchHistoryInteractor = Creator.provideSearchHistoryInteractor(applicationContext)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        searchEditText = findViewById(R.id.et_search)
        clearButton = findViewById(R.id.btn_clear)
        tracksRecyclerView = findViewById(R.id.rv_tracks)
        placeholderLayout = findViewById(R.id.placeholder_layout)
        placeholderImage = findViewById(R.id.iv_placeholder)
        placeholderText = findViewById(R.id.tv_placeholder)
        refreshButton = findViewById(R.id.btn_refresh)
        progressBar = findViewById(R.id.progressBar)
        searchContentFrame = findViewById(R.id.searchContentFrame)

        historyLayout = findViewById(R.id.history_layout)
        historyRecyclerView = findViewById(R.id.rv_history)
        clearHistoryButton = findViewById(R.id.btn_clear_history)

        trackAdapter = TrackAdapter(tracks) { track ->
            if (clickDebounce()) {
                searchHistoryInteractor.addTrack(track)
                updateHistoryList()
                openPlayer(track)
            }
        }
        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        tracksRecyclerView.adapter = trackAdapter

        historyAdapter = TrackAdapter(historyTracks) { track ->
            if (clickDebounce()) {
                searchHistoryInteractor.addTrack(track)
                updateHistoryList()
                openPlayer(track)
            }
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        clearHistoryButton.setOnClickListener {
            searchHistoryInteractor.clearHistory()
            historyTracks.clear()
            historyAdapter.notifyDataSetChanged()
            historyLayout.visibility = View.GONE
        }

        updateClearButtonVisibility(searchEditText.text)

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            showHistoryIfNeeded(hasFocus, searchEditText.text)
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                updateClearButtonVisibility(s)
                showHistoryIfNeeded(searchEditText.hasFocus(), s)
                searchDebounce()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        searchEditText.addTextChangedListener(simpleTextWatcher)

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handler.removeCallbacks(searchRunnable)
                if (searchEditText.text.isNotEmpty()) {
                    search(searchEditText.text.toString())
                }
                true
            } else {
                false
            }
        }

        clearButton.setOnClickListener {
            searchEditText.setText("")
            hideKeyboard()
            tracks.clear()
            trackAdapter.notifyDataSetChanged()
            searchContentFrame.visibility = View.GONE
            placeholderLayout.visibility = View.GONE
            handler.removeCallbacks(searchRunnable)
            showHistoryIfNeeded(hasFocus = true, text = "")
        }

        refreshButton.setOnClickListener {
            if (lastQuery.isNotEmpty()) {
                search(lastQuery)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRunnable)
        handler.removeCallbacks(clickRunnable)
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        if (searchEditText.text.isNotEmpty()) {
            handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.removeCallbacks(clickRunnable)
            handler.postDelayed(clickRunnable, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun showHistoryIfNeeded(hasFocus: Boolean, text: CharSequence?) {
        val history = searchHistoryInteractor.getHistory()
        if (hasFocus && text.isNullOrEmpty() && history.isNotEmpty()) {
            historyTracks.clear()
            historyTracks.addAll(history)
            historyAdapter.notifyDataSetChanged()
            historyLayout.visibility = View.VISIBLE
            searchContentFrame.visibility = View.GONE
            placeholderLayout.visibility = View.GONE
        } else {
            historyLayout.visibility = View.GONE
        }
    }

    private fun updateHistoryList() {
        val history = searchHistoryInteractor.getHistory()
        historyTracks.clear()
        historyTracks.addAll(history)
        historyAdapter.notifyDataSetChanged()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(SEARCH_QUERY, "")
        searchEditText.setText(searchText)
        searchEditText.setSelection(searchEditText.text?.length ?: 0)
    }

    private fun search(query: String) {
        lastQuery = query
        historyLayout.visibility = View.GONE
        showLoading()

        tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
                handler.post {
                    progressBar.visibility = View.GONE
                    if (foundTracks != null) {
                        tracks.clear()
                        tracks.addAll(foundTracks)
                        trackAdapter.notifyDataSetChanged()
                        if (foundTracks.isEmpty()) {
                            showPlaceholder(PlaceholderType.NOTHING_FOUND)
                        } else {
                            showContent()
                        }
                    } else {
                        showPlaceholder(PlaceholderType.CONNECTION_ERROR)
                    }
                }
            }
        })
    }

    private fun showLoading() {
        searchContentFrame.visibility = View.VISIBLE
        tracksRecyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        placeholderLayout.visibility = View.GONE
        historyLayout.visibility = View.GONE
    }

    private fun showContent() {
        searchContentFrame.visibility = View.VISIBLE
        tracksRecyclerView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        placeholderLayout.visibility = View.GONE
        historyLayout.visibility = View.GONE
    }

    private fun showPlaceholder(type: PlaceholderType) {
        tracks.clear()
        trackAdapter.notifyDataSetChanged()
        searchContentFrame.visibility = View.GONE
        placeholderLayout.visibility = View.VISIBLE
        historyLayout.visibility = View.GONE

        when (type) {
            PlaceholderType.NOTHING_FOUND -> {
                placeholderImage.setImageResource(R.drawable.il_nothing_found)
                placeholderText.text = getString(R.string.nothing_found)
                refreshButton.visibility = View.GONE
            }
            PlaceholderType.CONNECTION_ERROR -> {
                placeholderImage.setImageResource(R.drawable.il_connection_error)
                placeholderText.text = getString(R.string.connection_problem)
                refreshButton.visibility = View.VISIBLE
            }
        }
    }

    private fun updateClearButtonVisibility(s: CharSequence?) {
        if (s.isNullOrEmpty()) {
            clearButton.visibility = View.GONE
        } else {
            clearButton.visibility = View.VISIBLE
        }
    }

    private fun openPlayer(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(PlayerActivity.EXTRA_TRACK, track)
        startActivity(intent)
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    private enum class PlaceholderType {
        NOTHING_FOUND,
        CONNECTION_ERROR
    }
}

package com.example.playlistmakerr.presentation.search

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmakerr.R
import com.example.playlistmakerr.domain.models.Track
import com.example.playlistmakerr.presentation.player.PlayerFragment
import com.google.android.material.button.MaterialButton
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

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

    private val viewModel by viewModel<SearchViewModel>()

    private var searchText: String = ""
    private val tracks = ArrayList<Track>()
    private val historyTracks = ArrayList<Track>()

    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    private val clickRunnable = Runnable { isClickAllowed = true }

    companion object {
        const val SEARCH_QUERY = "SEARCH_QUERY"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.et_search)
        clearButton = view.findViewById(R.id.btn_clear)
        tracksRecyclerView = view.findViewById(R.id.rv_tracks)
        placeholderLayout = view.findViewById(R.id.placeholder_layout)
        placeholderImage = view.findViewById(R.id.iv_placeholder)
        placeholderText = view.findViewById(R.id.tv_placeholder)
        refreshButton = view.findViewById(R.id.btn_refresh)
        progressBar = view.findViewById(R.id.progressBar)
        searchContentFrame = view.findViewById(R.id.searchContentFrame)

        historyLayout = view.findViewById(R.id.history_layout)
        historyRecyclerView = view.findViewById(R.id.rv_history)
        clearHistoryButton = view.findViewById(R.id.btn_clear_history)

        trackAdapter = TrackAdapter(tracks) { track ->
            if (clickDebounce()) {
                viewModel.addTrackToHistory(track)
                openPlayer(track)
            }
        }
        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tracksRecyclerView.adapter = trackAdapter

        historyAdapter = TrackAdapter(historyTracks) { track ->
            if (clickDebounce()) {
                viewModel.addTrackToHistory(track)
                openPlayer(track)
            }
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyRecyclerView.adapter = historyAdapter

        clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }

        updateClearButtonVisibility(searchEditText.text)

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.showHistory(hasFocus, searchEditText.text)
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                updateClearButtonVisibility(s)
                viewModel.showHistory(searchEditText.hasFocus(), s)
                viewModel.searchDebounce(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        searchEditText.addTextChangedListener(simpleTextWatcher)

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchEditText.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.search(query)
                }
                true
            } else {
                false
            }
        }

        clearButton.setOnClickListener {
            searchEditText.setText("")
            hideKeyboard()
            viewModel.clearSearch()
            viewModel.showHistory(hasFocus = true, text = "")
        }

        refreshButton.setOnClickListener {
            viewModel.refreshSearch()
        }

        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    override fun onResume() {
        super.onResume()
        isClickAllowed = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(clickRunnable)
    }

    private fun render(state: SearchScreenState) {
        when (state) {
            is SearchScreenState.Empty -> {
                searchContentFrame.visibility = View.GONE
                placeholderLayout.visibility = View.GONE
                historyLayout.visibility = View.GONE
            }
            is SearchScreenState.Loading -> {
                searchContentFrame.visibility = View.VISIBLE
                tracksRecyclerView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                placeholderLayout.visibility = View.GONE
                historyLayout.visibility = View.GONE
            }
            is SearchScreenState.Content -> {
                tracks.clear()
                tracks.addAll(state.tracks)
                trackAdapter.notifyDataSetChanged()
                searchContentFrame.visibility = View.VISIBLE
                tracksRecyclerView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                placeholderLayout.visibility = View.GONE
                historyLayout.visibility = View.GONE
            }
            is SearchScreenState.NothingFound -> {
                tracks.clear()
                trackAdapter.notifyDataSetChanged()
                searchContentFrame.visibility = View.GONE
                placeholderLayout.visibility = View.VISIBLE
                placeholderImage.setImageResource(R.drawable.il_nothing_found)
                placeholderText.text = getString(R.string.nothing_found)
                refreshButton.visibility = View.GONE
                historyLayout.visibility = View.GONE
            }
            is SearchScreenState.ConnectionError -> {
                tracks.clear()
                trackAdapter.notifyDataSetChanged()
                searchContentFrame.visibility = View.GONE
                placeholderLayout.visibility = View.VISIBLE
                placeholderImage.setImageResource(R.drawable.il_connection_error)
                placeholderText.text = getString(R.string.connection_problem)
                refreshButton.visibility = View.VISIBLE
                historyLayout.visibility = View.GONE
            }
            is SearchScreenState.History -> {
                historyTracks.clear()
                historyTracks.addAll(state.tracks)
                historyAdapter.notifyDataSetChanged()
                searchContentFrame.visibility = View.GONE
                placeholderLayout.visibility = View.GONE
                historyLayout.visibility = View.VISIBLE
            }
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

    private fun updateClearButtonVisibility(s: CharSequence?) {
        clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    private fun openPlayer(track: Track) {
        val bundle = Bundle().apply {
            putSerializable(PlayerFragment.EXTRA_TRACK, track)
        }
        findNavController().navigate(R.id.action_searchFragment_to_playerFragment, bundle)
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }
}

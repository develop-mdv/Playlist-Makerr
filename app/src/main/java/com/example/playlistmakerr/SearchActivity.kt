package com.example.playlistmakerr

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchInputLayout: TextInputLayout
    private var searchText: String = ""

    companion object {
        const val SEARCH_QUERY = "SEARCH_QUERY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        searchInputLayout = findViewById(R.id.til_search)
        searchEditText = findViewById(R.id.et_search)

        // Configure initial clear button visibility
        updateClearButtonVisibility(searchEditText.text)

        // TextWatcher
        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                updateClearButtonVisibility(s)
                // Stub for future search logic
            }

            override fun afterTextChanged(s: Editable?) {
                // empty
            }
        }
        searchEditText.addTextChangedListener(simpleTextWatcher)

        // Clear button logic
        searchInputLayout.setEndIconOnClickListener {
            searchEditText.setText("")
            hideKeyboard()
            searchEditText.clearFocus()
        }
        
        // Focus listener to show/hide keyboard or handle focus specific logic if needed
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            // Optionally handle focus changes
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(SEARCH_QUERY, "")
        searchEditText.setText(searchText)
        // Курсор в конец строки
        searchEditText.setSelection(searchEditText.text?.length ?: 0)
    }

    private fun updateClearButtonVisibility(s: CharSequence?) {
        if (s.isNullOrEmpty()) {
            searchInputLayout.isEndIconVisible = false
        } else {
            searchInputLayout.isEndIconVisible = true
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }
}

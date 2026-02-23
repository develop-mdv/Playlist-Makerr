package com.example.playlistmakerr.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmakerr.R
import com.example.playlistmakerr.presentation.library.LibraryActivity
import com.example.playlistmakerr.presentation.search.SearchActivity
import com.example.playlistmakerr.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn_search).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivity(Intent(this@MainActivity, SearchActivity::class.java))
            }
        })

        findViewById<View>(R.id.btn_library).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }

        findViewById<View>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}

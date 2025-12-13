package com.example.playlistmakerr

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Поиск — анонимный класс
        findViewById<View>(R.id.btn_search).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivity(Intent(this@MainActivity, SearchActivity::class.java))
            }
        })

        // Медиатека — лямбда-выражение
        findViewById<View>(R.id.btn_library).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }

        // Настройки — любой способ (лямбда)
        findViewById<View>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
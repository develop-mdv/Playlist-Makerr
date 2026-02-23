package com.example.playlistmakerr.presentation.library

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmakerr.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2

class LibraryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        val adapter = LibraryViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        val tabTitles = arrayOf(
            getString(R.string.tab_favorite_tracks),
            getString(R.string.tab_playlists),
        )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}

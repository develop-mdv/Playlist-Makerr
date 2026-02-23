package com.example.playlistmakerr.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmakerr.R
import com.example.playlistmakerr.creator.Creator
import com.example.playlistmakerr.domain.api.SettingsInteractor
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsInteractor: SettingsInteractor
    private lateinit var switchDarkTheme: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingsInteractor = Creator.provideSettingsInteractor(applicationContext)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        switchDarkTheme = findViewById(R.id.switch_dark_theme)

        val isDarkTheme = settingsInteractor.isDarkTheme()
        switchDarkTheme.isChecked = isDarkTheme

        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            settingsInteractor.switchTheme(isChecked)
            applyTheme(isChecked)
        }

        val tvShareApp: MaterialTextView = findViewById(R.id.tv_share_app)
        tvShareApp.setOnClickListener {
            shareApp()
        }

        val tvWriteToSupport: MaterialTextView = findViewById(R.id.tv_write_to_support)
        tvWriteToSupport.setOnClickListener {
            writeToSupport()
        }

        val tvUserAgreement: MaterialTextView = findViewById(R.id.tv_user_agreement)
        tvUserAgreement.setOnClickListener {
            openUserAgreement()
        }
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        val mode = if (isDarkTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun writeToSupport() {
        val email = getString(R.string.support_email)
        val subject = getString(R.string.support_subject)
        val body = getString(R.string.support_body)

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            val chooser = Intent.createChooser(emailIntent, null)
            startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openUserAgreement() {
        val url = getString(R.string.offer_url)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(browserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package com.example.playlistmakerr

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView

class SettingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchDarkTheme: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        switchDarkTheme = findViewById(R.id.switch_dark_theme)
        
        // Загружаем сохраненное состояние темы
        val isDarkTheme = sharedPreferences.getBoolean(KEY_DARK_THEME, false)
        switchDarkTheme.isChecked = isDarkTheme

        // Обработчик переключения темы
        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            saveThemePreference(isChecked)
            applyTheme(isChecked)
        }

        // Обработчик кнопки "Поделиться приложением"
        val tvShareApp: MaterialTextView = findViewById(R.id.tv_share_app)
        tvShareApp.setOnClickListener {
            shareApp()
        }

        // Обработчик кнопки "Написать в поддержку"
        val tvWriteToSupport: MaterialTextView = findViewById(R.id.tv_write_to_support)
        tvWriteToSupport.setOnClickListener {
            writeToSupport()
        }

        // Обработчик кнопки "Пользовательское соглашение"
        val tvUserAgreement: MaterialTextView = findViewById(R.id.tv_user_agreement)
        tvUserAgreement.setOnClickListener {
            openUserAgreement()
        }
    }

    private fun saveThemePreference(isDarkTheme: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_DARK_THEME, isDarkTheme)
            .apply()
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
        
        // Используем Intent.ACTION_SEND с типом message/rfc822 для открытия только email приложений
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        
        try {
            // Создаем chooser только для email приложений
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

    companion object {
        const val PREFERENCES_NAME = "theme_preferences"
        const val KEY_DARK_THEME = "dark_theme"
    }
}



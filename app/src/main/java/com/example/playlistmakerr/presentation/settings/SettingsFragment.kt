package com.example.playlistmakerr.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.playlistmakerr.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val viewModel by viewModel<SettingsViewModel>()
    private lateinit var switchDarkTheme: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchDarkTheme = view.findViewById(R.id.switch_dark_theme)

        viewModel.isDarkTheme.observe(viewLifecycleOwner) { isDark ->
            switchDarkTheme.setOnCheckedChangeListener(null)
            switchDarkTheme.isChecked = isDark
            switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
                viewModel.switchTheme(isChecked)
            }
            applyTheme(isDark)
        }

        val tvShareApp: MaterialTextView = view.findViewById(R.id.tv_share_app)
        tvShareApp.setOnClickListener {
            shareApp()
        }

        val tvWriteToSupport: MaterialTextView = view.findViewById(R.id.tv_write_to_support)
        tvWriteToSupport.setOnClickListener {
            writeToSupport()
        }

        val tvUserAgreement: MaterialTextView = view.findViewById(R.id.tv_user_agreement)
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

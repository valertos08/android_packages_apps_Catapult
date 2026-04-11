/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.os.Bundle
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.slider.Slider
import org.lineageos.tv.launcher.ext.recentAppsCardSize
import org.lineageos.tv.launcher.ext.recentAppsEnabled
import org.lineageos.tv.launcher.ext.recentAppsShowThumbnails

class RecentAppsSettingsActivity : ModalActivity(R.layout.activity_recent_apps_settings) {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSettings()
    }

    private fun setupSettings() {
        val enabledSwitch = findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.recent_apps_enabled_switch)!!
        val sizeSlider = findViewById<Slider>(R.id.recent_apps_size_slider)!!
        val sizeValue = findViewById<TextView>(R.id.recent_apps_size_value)!!
        val thumbnailsSwitch = findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.recent_apps_thumbnails_switch)!!

        enabledSwitch.isChecked = prefs.recentAppsEnabled
        enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.recentAppsEnabled = isChecked
        }

        sizeSlider.value = prefs.recentAppsCardSize.coerceIn(20, 50).toFloat()
        updateSizeLabel(sizeValue, sizeSlider.value.toInt())
        sizeSlider.addOnChangeListener { _, value, fromUser ->
            updateSizeLabel(sizeValue, value.toInt())
            if (fromUser) {
                prefs.recentAppsCardSize = value.toInt()
            }
        }

        thumbnailsSwitch.isChecked = prefs.recentAppsShowThumbnails
        thumbnailsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.recentAppsShowThumbnails = isChecked
        }
    }

    private fun updateSizeLabel(textView: TextView, value: Int) {
        textView.text = getString(R.string.recent_apps_size_percent, value)
    }
}

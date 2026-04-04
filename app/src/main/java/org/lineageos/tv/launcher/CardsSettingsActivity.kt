/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.materialswitch.MaterialSwitch
import org.lineageos.tv.launcher.ext.allAppsGrid
import org.lineageos.tv.launcher.ext.allAppsGridColumns
import org.lineageos.tv.launcher.ext.appCardSize
import org.lineageos.tv.launcher.ext.favoriteCardSize
import org.lineageos.tv.launcher.ext.cardCornerRadius
import org.lineageos.tv.launcher.ext.watchNextCardSize

class CardsSettingsActivity : ModalActivity(R.layout.activity_cards_settings) {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    private val cardSizeValues = listOf(60, 70, 80, 90, 100, 110, 120, 130, 140)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupCardSizeSettings()
    }

    private fun setupCardSizeSettings() {
        val allAppsGridSwitch = findViewById<MaterialSwitch>(R.id.all_apps_grid_switch)!!
        val gridColumnsSection = findViewById<LinearLayout>(R.id.grid_columns_section)!!
        val gridColumnsSeekBar = findViewById<SeekBar>(R.id.grid_columns_seekbar)!!
        val gridColumnsValue = findViewById<TextView>(R.id.grid_columns_value)!!
        val appCardSizeSection = findViewById<LinearLayout>(R.id.app_card_size_section)!!

        allAppsGridSwitch.isChecked = prefs.allAppsGrid
        
        // Grid mode: show columns, hide card size
        // Carousel mode: hide columns, show card size
        updateSettingsVisibility(prefs.allAppsGrid)

        allAppsGridSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.allAppsGrid = isChecked
            updateSettingsVisibility(isChecked)
        }

        gridColumnsSeekBar.progress = prefs.allAppsGridColumns - 4
        gridColumnsValue.text = prefs.allAppsGridColumns.toString()

        gridColumnsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress + 4
                gridColumnsValue.text = value.toString()
                if (fromUser) {
                    prefs.allAppsGridColumns = value
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val appCardSeekBar = findViewById<SeekBar>(R.id.app_card_size_seekbar)!!
        val appCardValue = findViewById<TextView>(R.id.app_card_size_value)!!

        val favoriteCardSeekBar = findViewById<SeekBar>(R.id.favorite_card_size_seekbar)!!
        val favoriteCardValue = findViewById<TextView>(R.id.favorite_card_size_value)!!

        val watchNextCardSeekBar = findViewById<SeekBar>(R.id.watch_next_card_size_seekbar)!!
        val watchNextCardValue = findViewById<TextView>(R.id.watch_next_card_size_value)!!

        appCardSeekBar.progress = cardSizeValues.indexOf(prefs.appCardSize.coerceIn(60, 140))
        favoriteCardSeekBar.progress = cardSizeValues.indexOf(prefs.favoriteCardSize.coerceIn(60, 140))
        watchNextCardSeekBar.progress = cardSizeValues.indexOf(prefs.watchNextCardSize.coerceIn(60, 140))

        updateCardSizeLabel(appCardValue, cardSizeValues[appCardSeekBar.progress])
        updateCardSizeLabel(favoriteCardValue, cardSizeValues[favoriteCardSeekBar.progress])
        updateCardSizeLabel(watchNextCardValue, cardSizeValues[watchNextCardSeekBar.progress])

        appCardSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = cardSizeValues[progress]
                updateCardSizeLabel(appCardValue, value)
                if (fromUser) {
                    prefs.appCardSize = value
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        favoriteCardSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = cardSizeValues[progress]
                updateCardSizeLabel(favoriteCardValue, value)
                if (fromUser) {
                    prefs.favoriteCardSize = value
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        watchNextCardSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = cardSizeValues[progress]
                updateCardSizeLabel(watchNextCardValue, value)
                if (fromUser) {
                    prefs.watchNextCardSize = value
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val cornerRadiusSeekBar = findViewById<SeekBar>(R.id.card_corner_radius_seekbar)!!
        val cornerRadiusValue = findViewById<TextView>(R.id.card_corner_radius_value)!!

        cornerRadiusSeekBar.progress = prefs.cardCornerRadius.coerceIn(0, 20)
        cornerRadiusValue.text = "${cornerRadiusSeekBar.progress}dp"

        cornerRadiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                cornerRadiusValue.text = "${progress}dp"
                if (fromUser) {
                    prefs.cardCornerRadius = progress
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateCardSizeLabel(textView: TextView, value: Int) {
        textView.text = getString(R.string.card_size_percent, value)
    }

    private fun updateSettingsVisibility(isGridMode: Boolean) {
        val gridColumnsSection = findViewById<LinearLayout>(R.id.grid_columns_section)!!
        val appCardSizeSection = findViewById<LinearLayout>(R.id.app_card_size_section)!!
        
        // Grid mode: show columns, hide card size
        // Carousel mode: hide columns, show card size
        gridColumnsSection.visibility = if (isGridMode) View.VISIBLE else View.GONE
        appCardSizeSection.visibility = if (isGridMode) View.GONE else View.VISIBLE
    }
}

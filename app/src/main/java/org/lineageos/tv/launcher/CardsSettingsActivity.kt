/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.PreferenceManager
import org.lineageos.tv.launcher.ext.appCardSize
import org.lineageos.tv.launcher.ext.favoriteCardSize
import org.lineageos.tv.launcher.ext.watchNextCardSize

class CardsSettingsActivity : ModalActivity(R.layout.activity_cards_settings) {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    private val cardSizeValues = listOf(60, 70, 80, 90, 100, 110, 120, 130, 140)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupCardSizeSettings()
    }

    private fun setupCardSizeSettings() {
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
    }

    private fun updateCardSizeLabel(textView: TextView, value: Int) {
        textView.text = getString(R.string.card_size_percent, value)
    }
}

/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import org.lineageos.tv.launcher.ext.allAppsGrid
import org.lineageos.tv.launcher.ext.allAppsGridColumns
import org.lineageos.tv.launcher.ext.appCardSize
import org.lineageos.tv.launcher.ext.favoriteCardSize
import org.lineageos.tv.launcher.ext.cardCornerRadius
import org.lineageos.tv.launcher.ext.watchNextCardSize

class CardsSettingsActivity : ModalActivity(R.layout.activity_cards_settings) {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupCardSizeSettings()
    }

    private fun setupCardSizeSettings() {
        val allAppsGridSwitch = findViewById<MaterialSwitch>(R.id.all_apps_grid_switch)!!
        val gridColumnsSlider = findViewById<Slider>(R.id.grid_columns_slider)!!
        val gridColumnsValue = findViewById<TextView>(R.id.grid_columns_value)!!

        allAppsGridSwitch.isChecked = prefs.allAppsGrid
        
        updateSettingsVisibility(prefs.allAppsGrid)

        allAppsGridSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.allAppsGrid = isChecked
            updateSettingsVisibility(isChecked)
        }

        gridColumnsSlider.value = prefs.allAppsGridColumns.toFloat()
        gridColumnsValue.text = prefs.allAppsGridColumns.toString()

        gridColumnsSlider.addOnChangeListener { _, value, fromUser ->
            val intValue = value.toInt()
            gridColumnsValue.text = intValue.toString()
            if (fromUser) {
                prefs.allAppsGridColumns = intValue
            }
        }

        val appCardSlider = findViewById<Slider>(R.id.app_card_size_slider)!!
        val appCardValue = findViewById<TextView>(R.id.app_card_size_value)!!

        val favoriteCardSlider = findViewById<Slider>(R.id.favorite_card_size_slider)!!
        val favoriteCardValue = findViewById<TextView>(R.id.favorite_card_size_value)!!

        val watchNextCardSlider = findViewById<Slider>(R.id.watch_next_card_size_slider)!!
        val watchNextCardValue = findViewById<TextView>(R.id.watch_next_card_size_value)!!

        appCardSlider.value = prefs.appCardSize.coerceIn(60, 140).toFloat()
        favoriteCardSlider.value = prefs.favoriteCardSize.coerceIn(60, 140).toFloat()
        watchNextCardSlider.value = prefs.watchNextCardSize.coerceIn(60, 140).toFloat()

        updateCardSizeLabel(appCardValue, appCardSlider.value.toInt())
        updateCardSizeLabel(favoriteCardValue, favoriteCardSlider.value.toInt())
        updateCardSizeLabel(watchNextCardValue, watchNextCardSlider.value.toInt())

        appCardSlider.addOnChangeListener { _, value, fromUser ->
            updateCardSizeLabel(appCardValue, value.toInt())
            if (fromUser) {
                prefs.appCardSize = value.toInt()
            }
        }

        favoriteCardSlider.addOnChangeListener { _, value, fromUser ->
            updateCardSizeLabel(favoriteCardValue, value.toInt())
            if (fromUser) {
                prefs.favoriteCardSize = value.toInt()
            }
        }

        watchNextCardSlider.addOnChangeListener { _, value, fromUser ->
            updateCardSizeLabel(watchNextCardValue, value.toInt())
            if (fromUser) {
                prefs.watchNextCardSize = value.toInt()
            }
        }

        val cornerRadiusSlider = findViewById<Slider>(R.id.card_corner_radius_slider)!!
        val cornerRadiusValue = findViewById<TextView>(R.id.card_corner_radius_value)!!

        cornerRadiusSlider.value = prefs.cardCornerRadius.coerceIn(0, 20).toFloat()
        cornerRadiusValue.text = "${cornerRadiusSlider.value.toInt()}dp"

        cornerRadiusSlider.addOnChangeListener { _, value, fromUser ->
            val intValue = value.toInt()
            cornerRadiusValue.text = "${intValue}dp"
            if (fromUser) {
                prefs.cardCornerRadius = intValue
            }
        }
    }

    private fun updateCardSizeLabel(textView: TextView, value: Int) {
        textView.text = getString(R.string.card_size_percent, value)
    }

    private fun updateSettingsVisibility(isGridMode: Boolean) {
        val gridColumnsSection = findViewById<View>(R.id.grid_columns_section)!!
        val appCardSizeSection = findViewById<View>(R.id.app_card_size_section)!!
        
        gridColumnsSection.visibility = if (isGridMode) View.VISIBLE else View.GONE
        appCardSizeSection.visibility = if (isGridMode) View.GONE else View.VISIBLE
    }
}

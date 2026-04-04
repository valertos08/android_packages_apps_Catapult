/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.materialswitch.MaterialSwitch
import org.lineageos.tv.launcher.ext.allAppsGrid
import org.lineageos.tv.launcher.ext.allAppsGridColumns
import org.lineageos.tv.launcher.ext.appCardSize
import org.lineageos.tv.launcher.ext.favoriteCardSize
import org.lineageos.tv.launcher.ext.cardCornerRadius
import org.lineageos.tv.launcher.ext.cardBackgroundType
import org.lineageos.tv.launcher.ext.cardBackgroundColor
import org.lineageos.tv.launcher.ext.cardBackgroundGradientStart
import org.lineageos.tv.launcher.ext.cardBackgroundGradientEnd
import org.lineageos.tv.launcher.ext.cardBackgroundGradientMode
import org.lineageos.tv.launcher.ext.cardBackgroundIconGradientMode
import org.lineageos.tv.launcher.ext.watchNextCardSize
import org.lineageos.tv.launcher.utils.CardBackgroundHelper
import org.lineageos.tv.launcher.utils.GradientDirection

class CardsSettingsActivity : ModalActivity(R.layout.activity_cards_settings) {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    private val cardSizeValues = listOf(60, 70, 80, 90, 100, 110, 120, 130, 140)

    private var currentColorPicker: ColorPickerDialog? = null

    private lateinit var cardBackgroundPreview: View
    private lateinit var backgroundTypeGroup: RadioGroup
    private lateinit var bgThemeRadio: RadioButton
    private lateinit var bgCustomRadio: RadioButton
    private lateinit var bgGradientRadio: RadioButton
    private lateinit var bgIconRadio: RadioButton
    private lateinit var customOptions: LinearLayout
    private lateinit var customColorPreview: View
    private lateinit var gradientOptions: LinearLayout
    private lateinit var gradientStartPreview: View
    private lateinit var gradientEndPreview: View
    private lateinit var gradientDirectionSpinner: Spinner
    private lateinit var iconOptions: LinearLayout
    private lateinit var iconGradientDirectionSpinner: Spinner

    private val gradientDirectionNames = listOf(
        R.string.gradient_vertical_tb,
        R.string.gradient_vertical_bt,
        R.string.gradient_horizontal_lr,
        R.string.gradient_horizontal_rl,
        R.string.gradient_diagonal_tlbr,
        R.string.gradient_diagonal_trbl,
        R.string.gradient_diagonal_bltr,
        R.string.gradient_diagonal_brtl
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupCardSizeSettings()
        setupCardBackgroundSettings()
    }

    private fun setupCardSizeSettings() {
        val allAppsGridSwitch = findViewById<MaterialSwitch>(R.id.all_apps_grid_switch)!!
        val gridColumnsSeekBar = findViewById<SeekBar>(R.id.grid_columns_seekbar)!!
        val gridColumnsValue = findViewById<TextView>(R.id.grid_columns_value)!!

        allAppsGridSwitch.isChecked = prefs.allAppsGrid
        
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
                    updateBackgroundPreview()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupCardBackgroundSettings() {
        cardBackgroundPreview = findViewById(R.id.card_background_preview)!!
        backgroundTypeGroup = findViewById(R.id.card_background_type_group)!!
        bgThemeRadio = findViewById(R.id.card_bg_theme)!!
        bgCustomRadio = findViewById(R.id.card_bg_custom)!!
        bgGradientRadio = findViewById(R.id.card_bg_gradient)!!
        bgIconRadio = findViewById(R.id.card_bg_icon)!!
        customOptions = findViewById(R.id.card_bg_custom_options)!!
        customColorPreview = findViewById(R.id.custom_color_preview)!!
        gradientOptions = findViewById(R.id.card_bg_gradient_options)!!
        gradientStartPreview = findViewById(R.id.gradient_start_preview)!!
        gradientEndPreview = findViewById(R.id.gradient_end_preview)!!
        gradientDirectionSpinner = findViewById(R.id.gradient_direction_spinner)!!
        iconOptions = findViewById(R.id.card_bg_icon_options)!!
        iconGradientDirectionSpinner = findViewById(R.id.icon_gradient_direction_spinner)!!

        setupSpinners()

        val bgType = prefs.cardBackgroundType
        when (bgType) {
            CardBackgroundHelper.BACKGROUND_TYPE_THEME -> bgThemeRadio.isChecked = true
            CardBackgroundHelper.BACKGROUND_TYPE_CUSTOM -> bgCustomRadio.isChecked = true
            CardBackgroundHelper.BACKGROUND_TYPE_GRADIENT -> bgGradientRadio.isChecked = true
            CardBackgroundHelper.BACKGROUND_TYPE_ICON -> bgIconRadio.isChecked = true
        }
        updateBackgroundOptionsVisibility()
        updateColorPreviews()
        updateBackgroundPreview()

        backgroundTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            val bgTypeValue = when (checkedId) {
                R.id.card_bg_theme -> CardBackgroundHelper.BACKGROUND_TYPE_THEME
                R.id.card_bg_custom -> CardBackgroundHelper.BACKGROUND_TYPE_CUSTOM
                R.id.card_bg_gradient -> CardBackgroundHelper.BACKGROUND_TYPE_GRADIENT
                R.id.card_bg_icon -> CardBackgroundHelper.BACKGROUND_TYPE_ICON
                else -> CardBackgroundHelper.BACKGROUND_TYPE_THEME
            }
            prefs.cardBackgroundType = bgTypeValue
            updateBackgroundOptionsVisibility()
            updateBackgroundPreview()
        }

        findViewById<View>(R.id.select_custom_color)!!.setOnClickListener {
            showColorPicker(prefs.cardBackgroundColor) { color ->
                prefs.cardBackgroundColor = color
                updateColorPreviews()
                updateBackgroundPreview()
            }
        }

        findViewById<View>(R.id.select_gradient_start)!!.setOnClickListener {
            showColorPicker(prefs.cardBackgroundGradientStart) { color ->
                prefs.cardBackgroundGradientStart = color
                updateColorPreviews()
                updateBackgroundPreview()
            }
        }

        findViewById<View>(R.id.select_gradient_end)!!.setOnClickListener {
            showColorPicker(prefs.cardBackgroundGradientEnd) { color ->
                prefs.cardBackgroundGradientEnd = color
                updateColorPreviews()
                updateBackgroundPreview()
            }
        }

        gradientDirectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefs.cardBackgroundGradientMode = position
                updateBackgroundPreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        iconGradientDirectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefs.cardBackgroundIconGradientMode = position
                updateBackgroundPreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            gradientDirectionNames.map { getString(it) })
        gradientDirectionSpinner.adapter = adapter
        gradientDirectionSpinner.setSelection(prefs.cardBackgroundGradientMode)

        iconGradientDirectionSpinner.adapter = adapter
        iconGradientDirectionSpinner.setSelection(prefs.cardBackgroundIconGradientMode)
    }

    private fun updateBackgroundOptionsVisibility() {
        customOptions.visibility = if (bgCustomRadio.isChecked) View.VISIBLE else View.GONE
        gradientOptions.visibility = if (bgGradientRadio.isChecked) View.VISIBLE else View.GONE
        iconOptions.visibility = if (bgIconRadio.isChecked) View.VISIBLE else View.GONE
    }

    private fun updateColorPreviews() {
        setColorPreviewDrawable(customColorPreview, prefs.cardBackgroundColor)
        setColorPreviewDrawable(gradientStartPreview, prefs.cardBackgroundGradientStart)
        setColorPreviewDrawable(gradientEndPreview, prefs.cardBackgroundGradientEnd)
    }

    private fun setColorPreviewDrawable(view: View, color: Int) {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 4f * resources.displayMetrics.density
            setColor(color)
        }
        view.background = drawable
    }

    private fun updateBackgroundPreview() {
        val bgDrawable = CardBackgroundHelper.getCardBackgroundDrawable(this)
        cardBackgroundPreview.background = bgDrawable
    }

    private fun showColorPicker(initialColor: Int, onColorSelected: (Int) -> Unit) {
        currentColorPicker = ColorPickerDialog(this, initialColor, onColorSelected).also {
            it.show()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val dialog = currentColorPicker
        if (dialog?.isShowing == true && event.action == KeyEvent.ACTION_DOWN) {
            if (dialog.handleKeyDown(event.keyCode)) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun updateCardSizeLabel(textView: TextView, value: Int) {
        textView.text = getString(R.string.card_size_percent, value)
    }

    private fun updateSettingsVisibility(isGridMode: Boolean) {
        val gridColumnsSection = findViewById<LinearLayout>(R.id.grid_columns_section)!!
        val appCardSizeSection = findViewById<LinearLayout>(R.id.app_card_size_section)!!
        
        gridColumnsSection.visibility = if (isGridMode) View.VISIBLE else View.GONE
        appCardSizeSection.visibility = if (isGridMode) View.GONE else View.VISIBLE
    }
}

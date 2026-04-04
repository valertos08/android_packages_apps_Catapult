/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
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
    private lateinit var gradientDirectionLayout: TextInputLayout
    private lateinit var gradientDirectionDropdown: AutoCompleteTextView
    private lateinit var iconOptions: LinearLayout
    private lateinit var iconGradientDirectionLayout: TextInputLayout
    private lateinit var iconGradientDirectionDropdown: AutoCompleteTextView

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
                updateBackgroundPreview()
            }
        }
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
        gradientDirectionLayout = findViewById(R.id.gradient_direction_layout)!!
        gradientDirectionDropdown = findViewById(R.id.gradient_direction_dropdown)!!
        iconOptions = findViewById(R.id.card_bg_icon_options)!!
        iconGradientDirectionLayout = findViewById(R.id.icon_gradient_direction_layout)!!
        iconGradientDirectionDropdown = findViewById(R.id.icon_gradient_direction_dropdown)!!

        setupDropdowns()

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

        gradientDirectionDropdown.setOnItemClickListener { _, _, position, _ ->
            prefs.cardBackgroundGradientMode = position
            updateBackgroundPreview()
        }

        iconGradientDirectionDropdown.setOnItemClickListener { _, _, position, _ ->
            prefs.cardBackgroundIconGradientMode = position
            updateBackgroundPreview()
        }
    }

    private fun setupDropdowns() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,
            gradientDirectionNames.map { getString(it) })
        gradientDirectionDropdown.setAdapter(adapter)
        gradientDirectionDropdown.setText(adapter.getItem(prefs.cardBackgroundGradientMode), false)

        iconGradientDirectionDropdown.setAdapter(adapter)
        iconGradientDirectionDropdown.setText(adapter.getItem(prefs.cardBackgroundIconGradientMode), false)
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
        val gridColumnsSection = findViewById<View>(R.id.grid_columns_section)!!
        val appCardSizeSection = findViewById<View>(R.id.app_card_size_section)!!
        
        gridColumnsSection.visibility = if (isGridMode) View.VISIBLE else View.GONE
        appCardSizeSection.visibility = if (isGridMode) View.GONE else View.VISIBLE
    }
}

/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import com.google.android.material.button.MaterialButton
import java.util.Locale

class ColorPickerDialog(
    private val context: android.content.Context,
    private var initialColor: Int,
    private val onColorSelected: (Int) -> Unit
) : AppCompatDialog(context) {

    private lateinit var colorPreview: View
    private lateinit var colorHexValue: TextView
    private lateinit var sliderRed: Slider
    private lateinit var sliderGreen: Slider
    private lateinit var sliderBlue: Slider
    private lateinit var valueRed: TextView
    private lateinit var valueGreen: TextView
    private lateinit var valueBlue: TextView
    private lateinit var presetsGrid: RecyclerView
    private lateinit var okButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    private var currentColor: Int = initialColor

    private val presetColors = listOf(
        0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFF424242.toInt(), 0xFF9E9E9E.toInt(),
        0xFFF44336.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(), 0xFF673AB7.toInt(),
        0xFF3F51B5.toInt(), 0xFF2196F3.toInt(), 0xFF03A9F4.toInt(), 0xFF00BCD4.toInt(),
        0xFF009688.toInt(), 0xFF4CAF50.toInt(), 0xFF8BC34A.toInt(), 0xFFCDDC39.toInt(),
        0xFFFFEB3B.toInt(), 0xFFFFC107.toInt(), 0xFFFF9800.toInt(), 0xFFFF5722.toInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.color_picker_dialog)

        window?.setBackgroundDrawableResource(android.R.color.transparent)

        initViews()
        setupSliders()
        setupPresets()
        setupButtons()
        updateColorPreview()

        cancelButton.requestFocus()
    }

    private fun initViews() {
        colorPreview = findViewById(R.id.color_preview_card)!!
        colorHexValue = findViewById(R.id.color_hex_value)!!
        sliderRed = findViewById(R.id.slider_red)!!
        sliderGreen = findViewById(R.id.slider_green)!!
        sliderBlue = findViewById(R.id.slider_blue)!!
        valueRed = findViewById(R.id.value_red)!!
        valueGreen = findViewById(R.id.value_green)!!
        valueBlue = findViewById(R.id.value_blue)!!
        presetsGrid = findViewById(R.id.presets_grid)!!
        okButton = findViewById(R.id.btn_ok)!!
        cancelButton = findViewById(R.id.btn_cancel)!!

        sliderRed.value = Color.red(initialColor).toFloat()
        sliderGreen.value = Color.green(initialColor).toFloat()
        sliderBlue.value = Color.blue(initialColor).toFloat()
    }

    private fun setupSliders() {
        val updateListener = Slider.OnChangeListener { _, value, fromUser ->
            if (fromUser) {
                currentColor = Color.rgb(
                    sliderRed.value.toInt(),
                    sliderGreen.value.toInt(),
                    sliderBlue.value.toInt()
                )
                updateColorPreview()
            }
        }

        sliderRed.addOnChangeListener(updateListener)
        sliderGreen.addOnChangeListener(updateListener)
        sliderBlue.addOnChangeListener(updateListener)
    }

    private fun setupPresets() {
        presetsGrid.layoutManager = GridLayoutManager(context, 5)
        presetsGrid.adapter = PresetAdapter()
    }

    private fun setupButtons() {
        okButton.setOnClickListener {
            onColorSelected(currentColor)
            dismiss()
        }
        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onBackPressed() {
        dismiss()
    }

    private fun updateColorPreview() {
        valueRed.text = String.format(Locale.US, "%3d", Color.red(currentColor))
        valueGreen.text = String.format(Locale.US, "%3d", Color.green(currentColor))
        valueBlue.text = String.format(Locale.US, "%3d", Color.blue(currentColor))

        val hexColor = String.format(Locale.US, "#%06X", 0xFFFFFF and currentColor)
        colorHexValue.text = hexColor

        val drawable = colorPreview.background as? GradientDrawable
            ?: GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f * context.resources.displayMetrics.density
            }
        drawable.setColor(currentColor)
        colorPreview.background = drawable

        presetsGrid.adapter?.notifyDataSetChanged()
    }

    private fun selectColor(color: Int) {
        currentColor = color
        sliderRed.value = Color.red(color).toFloat()
        sliderGreen.value = Color.green(color).toFloat()
        sliderBlue.value = Color.blue(color).toFloat()
        updateColorPreview()
    }

    fun handleKeyDown(keyCode: Int): Boolean {
        val focusedId = window?.currentFocus?.id

        when (keyCode) {
            android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                when (focusedId) {
                    R.id.slider_red, R.id.slider_green, R.id.slider_blue -> {
                        presetsGrid.getChildAt(0)?.requestFocus()
                        return true
                    }
                    R.id.btn_ok, R.id.btn_cancel -> {
                        sliderBlue.requestFocus()
                        return true
                    }
                }
            }
            android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                when (focusedId) {
                    R.id.presets_grid -> {
                        sliderRed.requestFocus()
                        return true
                    }
                    R.id.slider_red -> {
                        sliderGreen.requestFocus()
                        return true
                    }
                    R.id.slider_green -> {
                        sliderBlue.requestFocus()
                        return true
                    }
                    R.id.slider_blue -> {
                        cancelButton.requestFocus()
                        return true
                    }
                }
            }
            android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                when (focusedId) {
                    R.id.slider_red -> {
                        if (sliderRed.value > 0) sliderRed.value -= 1f
                        return true
                    }
                    R.id.slider_green -> {
                        if (sliderGreen.value > 0) sliderGreen.value -= 1f
                        return true
                    }
                    R.id.slider_blue -> {
                        if (sliderBlue.value > 0) sliderBlue.value -= 1f
                        return true
                    }
                }
            }
            android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                when (focusedId) {
                    R.id.slider_red -> {
                        if (sliderRed.value < 255) sliderRed.value += 1f
                        return true
                    }
                    R.id.slider_green -> {
                        if (sliderGreen.value < 255) sliderGreen.value += 1f
                        return true
                    }
                    R.id.slider_blue -> {
                        if (sliderBlue.value < 255) sliderBlue.value += 1f
                        return true
                    }
                }
            }
            android.view.KeyEvent.KEYCODE_ENTER, android.view.KeyEvent.KEYCODE_DPAD_CENTER -> {
                when (focusedId) {
                    R.id.btn_cancel -> {
                        dismiss()
                        return true
                    }
                    R.id.btn_ok -> {
                        onColorSelected(currentColor)
                        dismiss()
                        return true
                    }
                }
            }
        }
        return false
    }

    inner class PresetAdapter : RecyclerView.Adapter<PresetAdapter.ViewHolder>() {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val colorCard: MaterialCardView = itemView.findViewById(R.id.color_card)!!
            val colorView: View = itemView.findViewById(R.id.color_item)!!
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.color_picker_preset_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val color = presetColors[position]
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12f * context.resources.displayMetrics.density
                setColor(color)
            }
            holder.colorView.background = drawable

            val isSelected = color == currentColor
            holder.colorCard.strokeWidth = if (isSelected) 3 else 0
            if (isSelected) {
                holder.colorCard.strokeColor = context.getColor(android.R.color.white)
            }

            holder.itemView.setOnClickListener {
                selectColor(color)
                okButton.requestFocus()
            }

            holder.itemView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                holder.colorCard.strokeWidth = if (hasFocus || isSelected) 3 else 0
                holder.colorCard.strokeColor = if (hasFocus) {
                    context.getColor(android.R.color.white)
                } else if (isSelected) {
                    context.getColor(android.R.color.white)
                } else {
                    0
                }
            }
        }

        override fun getItemCount() = presetColors.size
    }
}

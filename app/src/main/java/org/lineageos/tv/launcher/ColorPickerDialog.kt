/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ColorPickerDialog(
    context: Context,
    private var initialColor: Int,
    private val onColorSelected: (Int) -> Unit
) : Dialog(context) {

    private lateinit var colorPreview: View
    private lateinit var seekbarRed: SeekBar
    private lateinit var seekbarGreen: SeekBar
    private lateinit var seekbarBlue: SeekBar
    private lateinit var valueRed: TextView
    private lateinit var valueGreen: TextView
    private lateinit var valueBlue: TextView
    private lateinit var presetsGrid: RecyclerView
    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

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
        setupPresets()
        setupSeekbars()
        setupButtons()
        updateColorPreview()
        
        cancelButton.requestFocus()
    }

    private fun initViews() {
        colorPreview = findViewById(R.id.color_preview)!!
        seekbarRed = findViewById(R.id.seekbar_red)!!
        seekbarGreen = findViewById(R.id.seekbar_green)!!
        seekbarBlue = findViewById(R.id.seekbar_blue)!!
        valueRed = findViewById(R.id.value_red)!!
        valueGreen = findViewById(R.id.value_green)!!
        valueBlue = findViewById(R.id.value_blue)!!
        presetsGrid = findViewById(R.id.presets_grid)!!
        okButton = findViewById(R.id.btn_ok)!!
        cancelButton = findViewById(R.id.btn_cancel)!!

        seekbarRed.progress = Color.red(initialColor)
        seekbarGreen.progress = Color.green(initialColor)
        seekbarBlue.progress = Color.blue(initialColor)
    }

    private fun setupPresets() {
        presetsGrid.layoutManager = GridLayoutManager(context, 5)
        presetsGrid.adapter = PresetAdapter()
    }

    private fun setupSeekbars() {
        val updateListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentColor = Color.rgb(
                        seekbarRed.progress,
                        seekbarGreen.progress,
                        seekbarBlue.progress
                    )
                    updateColorPreview()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        seekbarRed.setOnSeekBarChangeListener(updateListener)
        seekbarGreen.setOnSeekBarChangeListener(updateListener)
        seekbarBlue.setOnSeekBarChangeListener(updateListener)
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val focusedId = window?.currentFocus?.id

        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                when (focusedId) {
                    R.id.seekbar_red, R.id.seekbar_green, R.id.seekbar_blue -> {
                        presetsGrid.getChildAt(0)?.requestFocus()
                        return true
                    }
                    R.id.btn_ok, R.id.btn_cancel -> {
                        seekbarBlue.requestFocus()
                        return true
                    }
                }
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                when (focusedId) {
                    R.id.presets_grid -> {
                        seekbarRed.requestFocus()
                        return true
                    }
                    R.id.seekbar_red -> {
                        seekbarGreen.requestFocus()
                        return true
                    }
                    R.id.seekbar_green -> {
                        seekbarBlue.requestFocus()
                        return true
                    }
                    R.id.seekbar_blue -> {
                        cancelButton.requestFocus()
                        return true
                    }
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                when (focusedId) {
                    R.id.seekbar_red, R.id.seekbar_green, R.id.seekbar_blue -> {
                        val seekbar = window?.currentFocus as? SeekBar
                        seekbar?.progress = (seekbar?.progress ?: 0) - 5
                        return true
                    }
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                when (focusedId) {
                    R.id.seekbar_red, R.id.seekbar_green, R.id.seekbar_blue -> {
                        val seekbar = window?.currentFocus as? SeekBar
                        seekbar?.progress = (seekbar?.progress ?: 0) + 5
                        return true
                    }
                }
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
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
            KeyEvent.KEYCODE_BACK -> {
                dismiss()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun updateColorPreview() {
        valueRed.text = Color.red(currentColor).toString()
        valueGreen.text = Color.green(currentColor).toString()
        valueBlue.text = Color.blue(currentColor).toString()

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f * context.resources.displayMetrics.density
            setColor(currentColor)
        }
        colorPreview.background = drawable
    }

    private fun selectColor(color: Int) {
        currentColor = color
        seekbarRed.progress = Color.red(color)
        seekbarGreen.progress = Color.green(color)
        seekbarBlue.progress = Color.blue(color)
        updateColorPreview()
    }

    inner class PresetAdapter : RecyclerView.Adapter<PresetAdapter.ViewHolder>() {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
                cornerRadius = 4f * context.resources.displayMetrics.density
                setColor(color)
                if (color == currentColor) {
                    setStroke(3, context.getColor(R.color.colorPrimary))
                } else {
                    setStroke(1, 0xFF757575.toInt())
                }
            }
            holder.colorView.background = drawable

            holder.itemView.setOnClickListener {
                selectColor(color)
                okButton.requestFocus()
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = presetColors.size
    }
}

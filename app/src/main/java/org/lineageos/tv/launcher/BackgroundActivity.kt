/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import org.lineageos.tv.launcher.ext.backgroundImageUri
import org.lineageos.tv.launcher.ext.backgroundType
import java.io.File

class BackgroundActivity : ModalActivity(R.layout.activity_background) {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = copyImageToInternalStorage(it)
            if (file != null) {
                prefs.backgroundImageUri = file.absolutePath
                findViewById<TextView>(R.id.select_image_button)?.text = getString(R.string.selected_image)
            }
        }
    }

    private fun copyImageToInternalStorage(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(filesDir, "background_image.jpg")
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBackgroundSettings()
    }

    private fun setupBackgroundSettings() {
        val radioGroup = findViewById<RadioGroup>(R.id.background_type_group)
        val selectImageButton = findViewById<TextView>(R.id.select_image_button)

        val currentType = prefs.backgroundType
        when (currentType) {
            0 -> findViewById<RadioButton>(R.id.background_default)?.isChecked = true
            1 -> findViewById<RadioButton>(R.id.background_default)?.isChecked = true
            2 -> findViewById<RadioButton>(R.id.background_image)?.isChecked = true
        }

        updateVisibility(currentType, selectImageButton)

        radioGroup?.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.background_default -> 0
                R.id.background_image -> 2
                else -> 0
            }
            prefs.backgroundType = type
            updateVisibility(type, selectImageButton)
        }

        selectImageButton?.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun updateVisibility(type: Int, selectImageButton: TextView?) {
        selectImageButton?.visibility = if (type == 2) View.VISIBLE else View.GONE
        if (type == 2) {
            val hasImage = !prefs.backgroundImageUri.isNullOrEmpty()
            selectImageButton?.text = if (hasImage) getString(R.string.selected_image) else getString(R.string.select_image)
        }
    }
}

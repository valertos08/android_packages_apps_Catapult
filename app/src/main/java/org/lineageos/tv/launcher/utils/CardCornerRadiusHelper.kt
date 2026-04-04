/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.utils

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.preference.PreferenceManager
import com.google.android.material.R as MaterialR
import org.lineageos.tv.launcher.ext.cardCornerRadius
import org.lineageos.tv.launcher.ext.getAttributeColor

object CardCornerRadiusHelper {
    
    fun getCardBorderDrawable(context: Context): GradientDrawable {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val radius = prefs.cardCornerRadius.toFloat()
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(android.graphics.Color.TRANSPARENT)
        }
    }
    
    fun getCardBackgroundDrawable(context: Context): GradientDrawable {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val radius = prefs.cardCornerRadius.toFloat()
        val bgColor = context.getAttributeColor(MaterialR.attr.colorSecondaryContainer)
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(bgColor)
        }
    }
    
    fun getBannerBorderDrawable(context: Context): GradientDrawable {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val radius = prefs.cardCornerRadius.toFloat()
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(android.graphics.Color.TRANSPARENT)
        }
    }
}

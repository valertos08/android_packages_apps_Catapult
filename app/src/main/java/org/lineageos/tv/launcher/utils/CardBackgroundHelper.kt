/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import com.google.android.material.R as MaterialR
import org.lineageos.tv.launcher.ext.cardBackgroundColor
import org.lineageos.tv.launcher.ext.cardBackgroundGradientEnd
import org.lineageos.tv.launcher.ext.cardBackgroundGradientMode
import org.lineageos.tv.launcher.ext.cardBackgroundGradientStart
import org.lineageos.tv.launcher.ext.cardBackgroundIconGradientMode
import org.lineageos.tv.launcher.ext.cardBackgroundType
import org.lineageos.tv.launcher.ext.cardCornerRadius
import org.lineageos.tv.launcher.ext.getAttributeColor

object CardBackgroundHelper {
    
    const val BACKGROUND_TYPE_THEME = 0
    const val BACKGROUND_TYPE_CUSTOM = 1
    const val BACKGROUND_TYPE_GRADIENT = 2
    const val BACKGROUND_TYPE_ICON = 3
    
    fun getCardBorderDrawable(context: Context): GradientDrawable {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val radius = prefs.cardCornerRadius.toFloat()
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(Color.TRANSPARENT)
        }
    }
    
    fun getCardBackgroundDrawable(context: Context): Drawable {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val radius = prefs.cardCornerRadius.toFloat()
        val bgType = prefs.cardBackgroundType
        
        return when (bgType) {
            BACKGROUND_TYPE_CUSTOM -> {
                val color = prefs.cardBackgroundColor
                createSolidDrawable(context, radius, color)
            }
            BACKGROUND_TYPE_GRADIENT -> {
                val startColor = prefs.cardBackgroundGradientStart
                val endColor = prefs.cardBackgroundGradientEnd
                val mode = GradientDirection.fromInt(prefs.cardBackgroundGradientMode)
                createGradientDrawable(context, radius, startColor, endColor, mode)
            }
            else -> {
                createThemeBackgroundDrawable(context)
            }
        }
    }
    
    fun createThemeBackgroundDrawable(context: Context): GradientDrawable {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val radius = prefs.cardCornerRadius.toFloat()
        val bgColor = context.getAttributeColor(MaterialR.attr.colorSecondaryContainer)
        return createSolidDrawable(context, radius, bgColor)
    }
    
    fun getCardIconBackgroundDrawable(context: Context, iconDrawable: Drawable?): Drawable {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val radius = prefs.cardCornerRadius.toFloat()
        val bgType = prefs.cardBackgroundType
        
        return when (bgType) {
            BACKGROUND_TYPE_CUSTOM -> {
                val color = prefs.cardBackgroundColor
                createSolidDrawable(context, radius, color)
            }
            BACKGROUND_TYPE_GRADIENT -> {
                val startColor = prefs.cardBackgroundGradientStart
                val endColor = prefs.cardBackgroundGradientEnd
                val mode = GradientDirection.fromInt(prefs.cardBackgroundGradientMode)
                createGradientDrawable(context, radius, startColor, endColor, mode)
            }
            BACKGROUND_TYPE_ICON -> {
                val mode = GradientDirection.fromInt(prefs.cardBackgroundIconGradientMode)
                createIconGradientDrawable(context, radius, iconDrawable, mode)
            }
            else -> {
                val bgColor = context.getAttributeColor(MaterialR.attr.colorSecondaryContainer)
                createSolidDrawable(context, radius, bgColor)
            }
        }
    }
    
    fun getBannerBorderDrawable(context: Context): GradientDrawable {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val radius = prefs.cardCornerRadius.toFloat()
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(Color.TRANSPARENT)
        }
    }
    
    private fun createSolidDrawable(context: Context, radius: Float, color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(color)
        }
    }
    
    private fun createGradientDrawable(
        context: Context, 
        radius: Float, 
        startColor: Int, 
        endColor: Int,
        direction: GradientDirection
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            orientation = direction.orientation
            colors = intArrayOf(startColor, endColor)
        }
    }
    
    private fun createIconGradientDrawable(
        context: Context,
        radius: Float,
        iconDrawable: Drawable?,
        direction: GradientDirection
    ): GradientDrawable {
        if (iconDrawable == null) {
            val bgColor = context.getAttributeColor(MaterialR.attr.colorSecondaryContainer)
            return createSolidDrawable(context, radius, bgColor)
        }
        
        return try {
            val bitmap = if (iconDrawable is android.graphics.drawable.BitmapDrawable) {
                iconDrawable.bitmap
            } else {
                val canvas = android.graphics.Bitmap.createBitmap(
                    iconDrawable.intrinsicWidth.coerceAtLeast(1),
                    iconDrawable.intrinsicHeight.coerceAtLeast(1),
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                iconDrawable.setBounds(0, 0, canvas.width, canvas.height)
                iconDrawable.draw(android.graphics.Canvas(canvas))
                canvas
            }
            
            val palette = Palette.from(bitmap).generate()
            
            val vibrant = palette.getVibrantColor(Color.TRANSPARENT)
            val lightVibrant = palette.getLightVibrantColor(Color.TRANSPARENT)
            val darkVibrant = palette.getDarkVibrantColor(Color.TRANSPARENT)
            val dominant = palette.getDominantColor(Color.GRAY)
            
            val startColor: Int
            val endColor: Int
            
            if (vibrant != Color.TRANSPARENT) {
                startColor = vibrant
                endColor = if (darkVibrant != Color.TRANSPARENT) darkVibrant else vibrant
            } else if (lightVibrant != Color.TRANSPARENT) {
                startColor = lightVibrant
                endColor = if (darkVibrant != Color.TRANSPARENT) darkVibrant else lightVibrant
            } else if (darkVibrant != Color.TRANSPARENT) {
                startColor = darkVibrant
                endColor = lightVibrant.takeIf { it != Color.TRANSPARENT } ?: darkVibrant
            } else {
                startColor = dominant
                endColor = vibrant.takeIf { it != Color.TRANSPARENT } ?: dominant
            }
            
            createGradientDrawable(context, radius, startColor, endColor, direction)
        } catch (e: Exception) {
            val bgColor = context.getAttributeColor(MaterialR.attr.colorSecondaryContainer)
            createSolidDrawable(context, radius, bgColor)
        }
    }
}

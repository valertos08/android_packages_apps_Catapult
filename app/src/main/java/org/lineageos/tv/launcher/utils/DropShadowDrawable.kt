/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

class DropShadowDrawable(context: Context) : Drawable() {

    private val blurRadius = BLUR_DP * context.resources.displayMetrics.density
    private val shadowPadding = shadowPaddingFor(context)
    private val cornerRadius = CardBackgroundHelper.cardCornerRadiusPx(context)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        setShadowLayer(blurRadius, 0f, 0f, SHADOW_COLOR)
        color = Color.TRANSPARENT
    }
    private var shadowBitmap: Bitmap? = null

    override fun getPadding(padding: Rect): Boolean {
        padding.set(shadowPadding, shadowPadding, shadowPadding, shadowPadding)
        return true
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun onBoundsChange(bounds: Rect) {
        val width = bounds.width()
        val height = bounds.height()
        if (width <= 0 || height <= 0) {
            shadowBitmap = null
            return
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawRoundRect(
            shadowPadding.toFloat(),
            shadowPadding.toFloat(),
            (width - shadowPadding).toFloat(),
            (height - shadowPadding).toFloat(),
            cornerRadius,
            cornerRadius,
            paint
        )
        shadowBitmap = bitmap
    }

    override fun draw(canvas: Canvas) {
        val bitmap = shadowBitmap ?: return
        canvas.drawBitmap(bitmap, bounds.left.toFloat(), bounds.top.toFloat(), null)
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    companion object {
        private const val BLUR_DP = 8f
        private const val SHADOW_COLOR = 0x4D000000

        fun shadowPaddingFor(context: Context): Int =
            (BLUR_DP * context.resources.displayMetrics.density * 1.8f).toInt()
    }
}

/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.animation.AnimatorInflater
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.ext.favoriteCardSize

class FavoriteCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCardCommon(context, attrs, defStyleAttr) {
    override val menuResId = R.menu.favorite_app_long_press

    // Views
    private val moveOverlayView by lazy { findViewById<ImageView>(R.id.app_move_handle)!! }

    var moving: Boolean = false

    init {
        inflate(context, R.layout.favorites_app_card, this)

        stateListAnimator =
            AnimatorInflater.loadStateListAnimator(context, R.animator.app_card_state_animator)

        applyCardSizeScaling()
    }

    private fun applyCardSizeScaling() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val scale = prefs.favoriteCardSize / 100f

        val baseWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 127f, context.resources.displayMetrics
        ).toInt()
        val baseHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 71f, context.resources.displayMetrics
        ).toInt()

        val newWidth = (baseWidth * scale).toInt()
        val newHeight = (baseHeight * scale).toInt()

        cardContainer.layoutParams = cardContainer.layoutParams.apply {
            width = newWidth
        }

        cardFrame.layoutParams = cardFrame.layoutParams.apply {
            height = newHeight
        }

        moveOverlayView.layoutParams = moveOverlayView.layoutParams.apply {
            height = newHeight
        }

        nameView.layoutParams = nameView.layoutParams.apply {
            width = newWidth
        }

        nameView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f * scale)

        val iconSize = (newHeight * 0.66f).toInt()
        iconView.layoutParams = iconView.layoutParams.apply {
            width = iconSize
            height = iconSize
        }
    }

    fun setMoving() {
        moveOverlayView.isVisible = true
        moving = true
    }

    fun setMoveDone() {
        moveOverlayView.isVisible = false
        moving = false
    }
}

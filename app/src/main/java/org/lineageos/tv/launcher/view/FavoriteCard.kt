/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.google.android.material.R as MaterialR
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.ext.favoriteCardSize
import org.lineageos.tv.launcher.ext.getAttributeColor
import org.lineageos.tv.launcher.model.ActivityLauncher
import org.lineageos.tv.launcher.model.Launchable
import org.lineageos.tv.launcher.utils.CardBackgroundHelper

class FavoriteCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCardCommon(context, attrs, defStyleAttr) {
    override val menuResId = R.menu.favorite_app_long_press

    private val moveOverlayView by lazy { findViewById<ImageView>(R.id.app_move_handle)!! }

    private var currentIcon: Drawable? = null
    private var useIconGradient: Boolean = false
    var moving: Boolean = false

    init {
        inflate(context, R.layout.favorites_app_card, this)

        stateListAnimator =
            AnimatorInflater.loadStateListAnimator(context, R.animator.app_card_state_animator)

        applyCardSizeScaling()
        applyCornerRadius()
    }

    override fun setCardInfo(appInfo: Launchable) {
        super.setCardInfo(appInfo)
        currentIcon = appInfo.icon
        useIconGradient = appInfo !is ActivityLauncher
        applyCornerRadius()
    }
    
    fun applyCornerRadius() {
        val borderDrawable = CardBackgroundHelper.getCardBorderDrawable(context)
        val backgroundDrawable = if (useIconGradient && currentIcon != null) {
            CardBackgroundHelper.getCardIconBackgroundDrawable(context, currentIcon)
        } else {
            CardBackgroundHelper.createThemeBackgroundDrawable(context)
        }
        
        cardContainer.background = borderDrawable
        bannerView.background = CardBackgroundHelper.getBannerBorderDrawable(context)
        iconContainer.background = backgroundDrawable
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

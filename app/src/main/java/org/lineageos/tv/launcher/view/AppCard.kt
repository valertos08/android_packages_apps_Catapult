/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.animation.AnimatorInflater
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.preference.PreferenceManager
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.ext.appCardSize
import org.lineageos.tv.launcher.utils.CardCornerRadiusHelper

class AppCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCardCommon(context, attrs, defStyleAttr) {
    override val menuResId = R.menu.app_long_press

    private var isGridMode = false

    fun setGridMode(grid: Boolean) {
        isGridMode = grid
        applyCardSizeScaling()
        applyCornerRadius()
    }

    init {
        inflate(context, R.layout.app_card, this)

        stateListAnimator =
            AnimatorInflater.loadStateListAnimator(context, R.animator.app_card_state_animator)

        applyCardSizeScaling()
        applyCornerRadius()

        setOnFocusChangeListener { _, hasFocus ->
            nameView.isInvisible = !hasFocus
            if (hasFocus) {
                translationZ = 10f
                nameView.postDelayed({ nameView.isSelected = true }, 2000)
            } else {
                translationZ = 0f
                nameView.isSelected = false
            }
        }
    }
    
    fun applyCornerRadius() {
        val borderDrawable = CardCornerRadiusHelper.getCardBorderDrawable(context)
        val backgroundDrawable = CardCornerRadiusHelper.getCardBackgroundDrawable(context)
        
        cardContainer.background = borderDrawable
        bannerView.background = CardCornerRadiusHelper.getBannerBorderDrawable(context)
        iconContainer.background = backgroundDrawable
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isGridMode) {
            val density = context.resources.displayMetrics.density
            val columnWidth = View.MeasureSpec.getSize(widthMeasureSpec)
            
            // Card fills the column width
            val cardWidthDp = columnWidth / density
            
            // Card height maintains 177:100 aspect ratio
            val cardHeightDp = cardWidthDp * 100f / 177f
            val imageHeight = (cardHeightDp * density).toInt()
            
            // Text size scales proportionally with card width (base: 12sp at 177dp)
            val textScale = cardWidthDp / 177f
            val textSize = 12f * textScale
            
            // Update dimensions
            cardContainer.layoutParams = cardContainer.layoutParams.apply {
                this.width = columnWidth
            }
            cardFrame.layoutParams = cardFrame.layoutParams.apply {
                height = imageHeight
            }
            nameView.layoutParams = nameView.layoutParams.apply {
                this.width = columnWidth
            }
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            
            val iconSize = (imageHeight * 0.66f).toInt()
            iconView.layoutParams = iconView.layoutParams.apply {
                this.width = iconSize
                this.height = iconSize
            }
            
            // Text height = text size + padding
            val textHeight = (textSize * density).toInt() + (8 * density).toInt()
            
            // Total height = image + text
            val totalHeight = imageHeight + textHeight
            
            super.onMeasure(
                View.MeasureSpec.makeMeasureSpec(columnWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(totalHeight, View.MeasureSpec.EXACTLY)
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun applyCardSizeScaling() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val scale = prefs.appCardSize / 100f

        val baseWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 177f, context.resources.displayMetrics
        ).toInt()
        val baseHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 100f, context.resources.displayMetrics
        ).toInt()

        val newWidth = (baseWidth * scale).toInt()
        val newHeight = (baseHeight * scale).toInt()

        cardContainer.layoutParams = cardContainer.layoutParams.apply {
            width = newWidth
        }
        nameView.layoutParams = nameView.layoutParams.apply {
            width = newWidth
        }

        // Scale only applies in carousel mode (not grid mode)
        if (!isGridMode) {
            cardFrame.layoutParams = cardFrame.layoutParams.apply {
                height = newHeight
            }
            val iconSize = (newHeight * 0.66f).toInt()
            iconView.layoutParams = iconView.layoutParams.apply {
                width = iconSize
                height = iconSize
            }
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f * scale)
        }
    }
}
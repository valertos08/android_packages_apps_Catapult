/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.animation.AnimatorInflater
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.preference.PreferenceManager
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.ext.appCardSize

class AppCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCardCommon(context, attrs, defStyleAttr) {
    override val menuResId = R.menu.app_long_press

    init {
        inflate(context, R.layout.app_card, this)

        stateListAnimator =
            AnimatorInflater.loadStateListAnimator(context, R.animator.app_card_state_animator)

        applyCardSizeScaling()

        setOnFocusChangeListener { _, hasFocus ->
            nameView.isInvisible = !hasFocus
            if (hasFocus) {
                nameView.postDelayed({ nameView.isSelected = true }, 2000)
            } else {
                nameView.isSelected = false
            }
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

        cardFrame.layoutParams = cardFrame.layoutParams.apply {
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
}

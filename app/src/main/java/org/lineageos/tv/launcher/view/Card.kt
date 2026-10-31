/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import org.lineageos.tv.launcher.model.Launchable
import org.lineageos.tv.launcher.utils.DropShadowDrawable

abstract class Card @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    init {
        clipChildren = false
        clipToPadding = false
    }
    
    var packageName: String = ""
    var label: String = ""
    var launchIntent: Intent? = null
    var hasMenu: Boolean = true

    open fun setCardInfo(appInfo: Launchable) {
        label = appInfo.label
        packageName = appInfo.packageName
        launchIntent = appInfo.launchIntent
        hasMenu = appInfo.hasMenu
    }

    protected fun shadowPadding(): Int = DropShadowDrawable.shadowPaddingFor(context)

    protected fun applyCardShadow(shadowView: View) {
        shadowView.clipToOutline = false
        val pad = shadowPadding()
        (shadowView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
            lp.setMargins(-pad, -pad, -pad, -pad)
            shadowView.layoutParams = lp
        }
        shadowView.background = DropShadowDrawable(context)
    }

    companion object
}

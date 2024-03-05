/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.LinearLayout
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.Launchable

abstract class Card : LinearLayout {
    var packageName: String = ""
    var label: String = ""
    var launchIntent: Intent? = null
    var hasMenu: Boolean = true

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        // TODO: fix?
        inflate()
    }

    open fun inflate() {
        inflate(context, R.layout.app_card, this)
    }

    open fun setCardInfo(appInfo: Launchable) {
        label = appInfo.label
        packageName = appInfo.packageName
        launchIntent = appInfo.launchIntent
        hasMenu = appInfo.hasMenu
    }
}

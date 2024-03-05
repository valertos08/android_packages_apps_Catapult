/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.Channel

class ToggleChannelView : LinearLayout {
    // Views
    private val titleView by lazy { findViewById<TextView>(R.id.title) }
    private val switch by lazy { findViewById<Switch>(R.id.state_switch) }

    var moving = false
    var channelId: Long? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        inflate(context, R.layout.toggle_channel, this)
        isFocusable = true
        isClickable = true
        background =
            AppCompatResources.getDrawable(context, R.drawable.favorites_app_card_background)
    }

    fun setData(channel: Channel, hidden: Boolean) {
        titleView.text = channel.title
        channelId = channel.id
        switch.isChecked = !hidden
    }

    fun disableToggle() {
        switch.isEnabled = false
    }

    fun setMoving() {
        moving = true
    }

    fun setMoveDone() {
        moving = false
    }
}

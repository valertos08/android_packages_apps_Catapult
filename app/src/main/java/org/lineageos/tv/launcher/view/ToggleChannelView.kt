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
    private val mTitleView: TextView by lazy { findViewById(R.id.title) }
    private val mSwitch: Switch by lazy { findViewById(R.id.state_switch) }

    var mMoving = false
    var mChannelId: Long? = null

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
        mTitleView.text = channel.title
        mChannelId = channel.id
        mSwitch.isChecked = !hidden
    }

    fun disableToggle() {
        mSwitch.isEnabled = false
    }

    fun setMoving() {
        mMoving = true
    }

    fun setMoveDone() {
        mMoving = false
    }
}
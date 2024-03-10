/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.Launchable

class AddFavoriteCard : Card {
    // Views
    private val actionIconView by lazy { findViewById<ImageView>(R.id.action_image) }
    private val iconView by lazy { findViewById<ImageView>(R.id.app_icon) }
    private val nameView by lazy { findViewById<TextView>(R.id.app_name) }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        inflate(context, R.layout.favorites_add_app_card, this)
        background =
            AppCompatResources.getDrawable(context, R.drawable.favorites_app_card_background)
    }

    override fun inflate() {
        inflate(context, R.layout.favorites_add_app_card, this)
    }

    fun setActionToggle(favorite: Boolean) {
        actionIconView.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                when (favorite) {
                    true -> R.drawable.ic_remove
                    false -> R.drawable.ic_add
                }
            )
        )
    }

    override fun setCardInfo(appInfo: Launchable) {
        super.setCardInfo(appInfo)

        nameView.text = appInfo.label
        iconView.setImageDrawable(appInfo.icon)
    }
}

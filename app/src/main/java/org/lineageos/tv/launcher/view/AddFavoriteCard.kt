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

class AddFavoriteCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Card(context, attrs, defStyleAttr) {
    // Views
    private val actionIconView by lazy { findViewById<ImageView>(R.id.action_image) }
    private val iconView by lazy { findViewById<ImageView>(R.id.app_icon) }
    private val nameView by lazy { findViewById<TextView>(R.id.app_name) }

    init {
        inflate(context, R.layout.favorites_add_app_card, this)
        background =
            AppCompatResources.getDrawable(context, R.drawable.favorites_app_card_background)
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

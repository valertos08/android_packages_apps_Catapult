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
    private val mIconView: ImageView by lazy { findViewById(R.id.app_icon) }
    private val mNameView: TextView by lazy { findViewById(R.id.app_name) }
    private val mActionIconView: ImageView

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
        mActionIconView = findViewById(R.id.action_image)
    }

    override fun inflate() {
        inflate(context, R.layout.favorites_add_app_card, this)
    }

    fun setActionAdd() {
        mActionIconView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_add))
    }

    fun setActionRemove() {
        mActionIconView.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                R.drawable.ic_remove
            )
        )
    }

    override fun setCardInfo(appInfo: Launchable) {
        super.setCardInfo(appInfo)

        mNameView.text = appInfo.mLabel
        mIconView.setImageDrawable(appInfo.mIcon)
    }
}

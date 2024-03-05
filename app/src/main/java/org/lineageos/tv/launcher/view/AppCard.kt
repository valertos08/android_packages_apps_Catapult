/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.animation.AnimatorInflater
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.AppInfo
import org.lineageos.tv.launcher.model.Launchable

open class AppCard : Card, View.OnFocusChangeListener {
    private val mIconView: ImageView by lazy { findViewById(R.id.app_icon) }
    private val mNameView: TextView by lazy { findViewById(R.id.app_name) }
    private val mBannerView: ImageView by lazy { findViewById(R.id.app_banner) }
    private val mIconContainer: LinearLayout by lazy { findViewById(R.id.app_with_icon) }
    private val mCardContainer: LinearLayout by lazy { findViewById(R.id.card_container) }
    private var mHasFocus: Boolean = false

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        stateListAnimator =
            AnimatorInflater.loadStateListAnimator(context, R.anim.app_card_state_animator)
        onFocusChangeListener = this
    }

    override fun inflate() {
        inflate(context, R.layout.app_card, this)
    }

    override fun setCardInfo(appInfo: Launchable) {
        super.setCardInfo(appInfo)

        mNameView.text = appInfo.mLabel
        mIconView.setImageDrawable(appInfo.mIcon)

        if (appInfo is AppInfo && appInfo.mBanner != null) {
            // App with a banner
            mBannerView.setImageDrawable(appInfo.mBanner)
            mBannerView.visibility = View.VISIBLE
            mIconContainer.visibility = View.GONE
            mCardContainer.background =
                AppCompatResources.getDrawable(context, R.drawable.card_border_only)
        } else {
            // App with an icon
            mIconView.setImageDrawable(appInfo.mIcon)
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            mHasFocus = true
            mNameView.postDelayed({
                if (mHasFocus) {
                    mNameView.isSelected = true
                }
            }, 2000)
        } else {
            mNameView.isSelected = false
            mHasFocus = false
        }
    }
}

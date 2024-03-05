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
import org.lineageos.tv.launcher.R

class FavoriteCard : AppCard {
    private val mMoveOverlayView: ImageView by lazy { findViewById(R.id.app_move_handle) }
    var mMoving: Boolean = false

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
    }

    override fun inflate() {
        inflate(context, R.layout.favorites_app_card, this)
    }

    fun setMoving() {
        mMoveOverlayView.visibility = View.VISIBLE
        mMoving = true
    }

    fun setMoveDone() {
        mMoveOverlayView.visibility = View.GONE
        mMoving = false
    }
}
/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.tvprovider.media.tv.BasePreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import coil.load
import org.lineageos.tv.launcher.R


class WatchNextCard : Card, View.OnFocusChangeListener {
    private val mBannerView: ImageView by lazy { findViewById(R.id.app_banner) }
    private var mTitle: TextView? = null
    private val mProgressView: ProgressBar

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
        mProgressView = findViewById(R.id.watch_progress)
    }

    override fun inflate() {
        inflate(context, R.layout.watch_next_card, this)
    }

    @SuppressLint("RestrictedApi")
    fun setInfo(info: BasePreviewProgram) {
        // Choose correct size title for the preview
        mTitle = when (info.posterArtAspectRatio) {
            TvContractCompat.PreviewProgramColumns.ASPECT_RATIO_16_9 -> {
                findViewById(R.id.title_16_9)
            }

            TvContractCompat.PreviewProgramColumns.ASPECT_RATIO_4_3 -> {
                findViewById(R.id.title_16_9)
            }

            TvContractCompat.PreviewProgramColumns.ASPECT_RATIO_3_2 -> {
                findViewById(R.id.title_3_2)
            }

            else -> {
                findViewById(R.id.title_4_3)
            }
        }
        mTitle?.visibility = View.INVISIBLE

        mLabel = info.title
        mBannerView.visibility = View.VISIBLE
        mLaunchIntent = info.intent
        mTitle?.text = info.title

        if (info.lastPlaybackPositionMillis != -1 && info.durationMillis != -1) {
            val percentWatched =
                ((info.lastPlaybackPositionMillis.toDouble() / info.durationMillis) * 100).toInt()
            if (percentWatched > 3) {
                mProgressView.progress = percentWatched
                mProgressView.visibility = View.VISIBLE
            }
        }

        mBannerView.load(info.posterArtUri) {
            placeholder(AppCompatResources.getDrawable(context, R.drawable.watch_next_placeholder))
            crossfade(500)
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        mTitle?.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
        if (hasFocus) {
            mTitle?.postDelayed({ mTitle?.isSelected = true }, 2000)
        } else {
            mTitle?.isSelected = false
        }
    }
}
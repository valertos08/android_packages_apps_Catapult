/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.animation.AnimatorInflater
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.tvprovider.media.tv.BasePreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import coil.load
import org.lineageos.tv.launcher.R

class WatchNextCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Card(context, attrs, defStyleAttr) {
    // Views
    private val bannerView: ImageView by lazy { findViewById(R.id.app_banner) }
    private var title: TextView? = null
    private val progressView by lazy { findViewById<ProgressBar>(R.id.watch_progress) }

    init {
        inflate(context, R.layout.watch_next_card, this)

        stateListAnimator =
            AnimatorInflater.loadStateListAnimator(context, R.animator.app_card_state_animator)

        setOnFocusChangeListener { _, hasFocus ->
            title?.isInvisible = !hasFocus
            if (hasFocus) {
                title?.postDelayed({ title?.isSelected = true }, 2000)
            } else {
                title?.isSelected = false
            }
        }
    }

    @Suppress("RestrictedApi")
    fun setInfo(info: BasePreviewProgram) {
        // Choose correct size title for the preview
        title = when (info.posterArtAspectRatio) {
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
        title?.isInvisible = true

        label = info.title
        bannerView.isVisible = true
        launchIntent = info.intent
        title?.text = info.title

        if (info.lastPlaybackPositionMillis != -1 && info.durationMillis != -1) {
            val percentWatched =
                ((info.lastPlaybackPositionMillis.toDouble() / info.durationMillis) * 100).toInt()
            if (percentWatched > 3) {
                progressView.progress = percentWatched
                progressView.isVisible = true
            }
        }

        bannerView.load(info.posterArtUri) {
            placeholder(AppCompatResources.getDrawable(context, R.drawable.watch_next_placeholder))
            crossfade(500)
        }
    }
}

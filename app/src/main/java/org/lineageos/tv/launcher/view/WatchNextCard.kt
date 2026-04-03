/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.tvprovider.media.tv.BasePreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.ext.getAttributeResourceId
import org.lineageos.tv.launcher.ext.watchNextCardSize

class WatchNextCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Card(context, attrs, defStyleAttr) {
    // Views
    private val cardFrame: FrameLayout by lazy { findViewById(R.id.card_frame)!! }
    private val bannerBackgroundView: ImageView by lazy { findViewById(R.id.app_banner_background)!! }
    private val bannerView: ImageView by lazy { findViewById(R.id.app_banner)!! }
    private val smallBannerView: ImageView by lazy { findViewById(R.id.app_banner_small)!! }
    private val title: TextView by lazy { findViewById(R.id.title)!! }
    private val progressView: LinearProgressIndicator by lazy { findViewById(R.id.watch_progress)!! }

    init {
        inflate(context, R.layout.watch_next_card, this)

        stateListAnimator =
            AnimatorInflater.loadStateListAnimator(context, R.animator.app_card_state_animator)

        applyCardSizeScaling()
        setupNameMarquee()
    }

    private fun applyCardSizeScaling() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val scale = prefs.watchNextCardSize / 100f

        val baseWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 246f, context.resources.displayMetrics
        ).toInt()
        val baseHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 138f, context.resources.displayMetrics
        ).toInt()

        val newWidth = (baseWidth * scale).toInt()
        val newHeight = (baseHeight * scale).toInt()

        layoutParams = layoutParams.apply {
            width = newWidth
        }

        cardFrame.layoutParams = cardFrame.layoutParams.apply {
            height = newHeight
        }

        title.layoutParams = title.layoutParams.apply {
            width = newWidth
        }

        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f * scale)

        val progressMargin = (15f * scale).toInt()
        val progressPaddingBottom = (20f * scale).toInt()
        progressView.layoutParams = (progressView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            marginStart = progressMargin
            marginEnd = progressMargin
        }
        progressView.setPadding(0, 0, 0, progressPaddingBottom)
    }

    private fun setupNameMarquee() {
        setOnFocusChangeListener { _, hasFocus ->
            title.isInvisible = !hasFocus
            if (hasFocus) {
                title.postDelayed({ title.isSelected = true }, 2000)
            } else {
                title.isSelected = false
            }
        }
    }

    @Suppress("RestrictedApi")
    fun setInfo(info: BasePreviewProgram) {
        bannerView.isVisible = false
        smallBannerView.isVisible = false
        progressView.isVisible = false

        title.isInvisible = true
        label = info.title
        launchIntent = info.intent
        title.text = info.title

        if (info.lastPlaybackPositionMillis != -1 && info.durationMillis != -1) {
            val percentWatched =
                ((info.lastPlaybackPositionMillis.toDouble() / info.durationMillis) * 100).toInt()
            if (percentWatched > 3) {
                progressView.progress = percentWatched
                progressView.isVisible = true
            }
        }

        // Other than 16:9, use blurred background and smaller art
        if (info.posterArtAspectRatio != TvContractCompat.PreviewPrograms.ASPECT_RATIO_16_9) {
            bannerView.isVisible = false
            smallBannerView.isVisible = true
            bannerBackgroundView.load(info.posterArtUri) {
                placeholder(
                    context.getAttributeResourceId(
                        com.google.android.material.R.attr.colorSecondaryContainer
                    )
                )
                crossfade(500)
                scale(Scale.FILL)
                listener(onSuccess = { _, _ ->
                    bannerBackgroundView.setRenderEffect(
                        RenderEffect.createBlurEffect(
                            25f, 25f, Shader.TileMode.CLAMP
                        )
                    )

                    bannerBackgroundView.colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(context, R.color.watchNextCardFilter),
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
                )
            }

            smallBannerView.load(info.posterArtUri) {
                crossfade(500)
                transformations(RoundedCornersTransformation(5F))
            }

            return
        }

        bannerView.isVisible = true
        smallBannerView.isVisible = false
        bannerView.load(info.posterArtUri) {
            placeholder(
                context.getAttributeResourceId(
                    com.google.android.material.R.attr.colorSecondaryContainer
                )
            )
            crossfade(500)
        }
    }
}

/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.leanback.widget.HorizontalGridView
import androidx.preference.PreferenceManager
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.MainRowItem
import org.lineageos.tv.launcher.adapter.AllAppsAdapter
import org.lineageos.tv.launcher.adapter.FavoritesAdapter
import org.lineageos.tv.launcher.adapter.WatchNextAdapter
import org.lineageos.tv.launcher.ext.appCardSize
import org.lineageos.tv.launcher.ext.favoriteCardSize
import org.lineageos.tv.launcher.ext.watchNextCardSize

class MainRowItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    // Views
    private val horizontalGridView by lazy { findViewById<HorizontalGridView>(R.id.horizontal_grid)!! }
    private val titleView by lazy { findViewById<TextView>(R.id.title)!! }

    fun setData(mainRowItem: MainRowItem) {
        titleView.text = mainRowItem.label
        horizontalGridView.adapter = mainRowItem.adapter
        val spacing = calculateCardSpacing(mainRowItem.adapter)
        horizontalGridView.setItemSpacing(spacing)
        val newHeight = calculateRowHeight(mainRowItem.adapter)
        val lp = horizontalGridView.layoutParams
        if (lp.height != newHeight) {
            lp.height = newHeight
            horizontalGridView.layoutParams = lp
        }
    }

    private fun calculateCardSpacing(adapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val cardSizePercent = when (adapter) {
            is AllAppsAdapter -> prefs.appCardSize
            is FavoritesAdapter -> prefs.favoriteCardSize
            is WatchNextAdapter -> prefs.watchNextCardSize
            else -> 100
        }
        val density = context.resources.displayMetrics.density
        val baseSpacingDp = 12f
        val scale = cardSizePercent / 100f
        return (baseSpacingDp * scale * density).toInt()
    }

    private fun calculateRowHeight(adapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val (baseCardHeightDp, cardSizePercent, textAreaDp) = when (adapter) {
            is AllAppsAdapter -> Triple(100f, prefs.appCardSize, 20f)
            is FavoritesAdapter -> Triple(71f, prefs.favoriteCardSize, 20f)
            is WatchNextAdapter -> Triple(138f, prefs.watchNextCardSize, 28f)
            else -> Triple(100f, 100, 20f)
        }
        val density = context.resources.displayMetrics.density
        val scale = cardSizePercent / 100f
        val cardHeight = (baseCardHeightDp * scale * density).toInt()
        val textArea = (textAreaDp * scale * density).toInt()
        val focusScale = 1.1f
        val focusedTotal = ((cardHeight + textArea) * focusScale).toInt()
        val padding = (8f * density).toInt()
        return focusedTotal + padding * 2
    }

    init {
        inflate(context, R.layout.vertical_grid_row, this)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}

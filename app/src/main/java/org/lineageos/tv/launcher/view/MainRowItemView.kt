/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.content.SharedPreferences
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.leanback.widget.HorizontalGridView
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.MainRowItem
import org.lineageos.tv.launcher.adapter.AllAppsAdapter
import org.lineageos.tv.launcher.adapter.FavoritesAdapter
import org.lineageos.tv.launcher.adapter.WatchNextAdapter
import org.lineageos.tv.launcher.ext.allAppsGrid
import org.lineageos.tv.launcher.ext.allAppsGridColumns
import org.lineageos.tv.launcher.ext.appCardSize
import org.lineageos.tv.launcher.ext.favoriteCardSize
import org.lineageos.tv.launcher.ext.watchNextCardSize
import org.lineageos.tv.launcher.utils.GridSpacingItemDecoration

class MainRowItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val horizontalGridView by lazy { findViewById<HorizontalGridView>(R.id.horizontal_grid)!! }
    private val verticalGridRecyclerView by lazy { findViewById<RecyclerView>(R.id.vertical_grid)!! }
    private val titleView by lazy { findViewById<TextView>(R.id.title)!! }
    
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (horizontalGridView.visibility == View.VISIBLE) {
            updateCarouselPadding()
        }
    }
    
    private fun updateCarouselPadding() {
        val density = context.resources.displayMetrics.density
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val adapter = horizontalGridView.adapter ?: return
        
        val (baseCardHeightDp, cardSizePercent) = when (adapter) {
            is AllAppsAdapter -> Pair(100f, prefs.appCardSize)
            is FavoritesAdapter -> Pair(71f, prefs.favoriteCardSize)
            is WatchNextAdapter -> Pair(138f, prefs.watchNextCardSize)
            else -> return
        }
        
        val scale = cardSizePercent / 100f
        val cardHeight = (baseCardHeightDp * scale * density).toInt()
        val focusScale = 1.1f
        val focusPadding = (cardHeight * (focusScale - 1f)).toInt()
        val basePadding = (8f * density).toInt()
        val topPadding = focusPadding + basePadding
        
        horizontalGridView.setPadding(basePadding, topPadding, basePadding, basePadding)
        
        val spacing = calculateCardSpacing(adapter)
        horizontalGridView.setItemSpacing(spacing)
        
        val newHeight = calculateRowHeight(adapter)
        val lp = horizontalGridView.layoutParams
        lp.height = newHeight
        horizontalGridView.layoutParams = lp
    }

    fun setData(mainRowItem: MainRowItem) {
        titleView.text = mainRowItem.label
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val isGrid = prefs.allAppsGrid && mainRowItem.adapter is AllAppsAdapter

        horizontalGridView.visibility = if (isGrid) View.GONE else View.VISIBLE
        verticalGridRecyclerView.visibility = if (isGrid) View.VISIBLE else View.GONE

        if (isGrid) {
            setupGridView(mainRowItem.adapter as AllAppsAdapter)
        } else {
            if (mainRowItem.adapter is AllAppsAdapter) {
                mainRowItem.adapter.gridMode = false
            }
            horizontalGridView.adapter = mainRowItem.adapter
            updateCarouselPadding()
        }
    }

    private fun setupGridView(adapter: AllAppsAdapter) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val density = context.resources.displayMetrics.density
        val columns = prefs.allAppsGridColumns.coerceIn(4, 10)

        adapter.gridMode = true
        adapter.notifyDataSetChanged()

        verticalGridRecyclerView.adapter = adapter
        verticalGridRecyclerView.layoutManager = GridLayoutManager(context, columns)
        verticalGridRecyclerView.isNestedScrollingEnabled = true
        verticalGridRecyclerView.setPadding(0, 0, 0, 0)
        while (verticalGridRecyclerView.itemDecorationCount > 0) {
            verticalGridRecyclerView.removeItemDecorationAt(0)
        }

        // Calculate row height based on card dimensions
        val screenWidth = context.resources.displayMetrics.widthPixels
        val horizontalPadding = (16 * density).toInt()
        val availableWidth = screenWidth - horizontalPadding
        val spacing = (12f * density).toInt()
        
        // Card width = available width / columns
        val cardWidth = (availableWidth / columns.toFloat()).toInt()
        
        // Card image height = cardWidth * 100 / 177
        val cardImageHeight = (cardWidth * 100f / 177f).toInt()
        
        // Text scales with card width (base: 12sp at 177dp)
        val textScale = cardWidth / (177f * density)
        val textSizeSp = 12f * textScale
        val textHeight = (textSizeSp * density).toInt() + (8 * density).toInt() // extra padding for safety
        
        // Total card height
        val cardTotalHeight = cardImageHeight + textHeight
        
        // Row height with focus scale (1.1x) + padding for 2 rows
        val focusScale = 1.1f
        val focusedCardHeight = (cardTotalHeight * focusScale).toInt()
        val padding = (8f * density).toInt()
        val rowHeight = focusedCardHeight + padding * 2
        val twoRowsHeight = rowHeight * 2 + spacing
        
        verticalGridRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(columns, spacing, spacing)
        )
        
        val lp = verticalGridRecyclerView.layoutParams
        lp.height = twoRowsHeight
        verticalGridRecyclerView.layoutParams = lp
        verticalGridRecyclerView.overScrollMode = View.OVER_SCROLL_NEVER
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
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(prefsListener)
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(prefsListener)
    }
}

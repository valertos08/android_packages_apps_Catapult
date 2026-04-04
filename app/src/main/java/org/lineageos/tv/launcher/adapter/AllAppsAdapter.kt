/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.adapter

import android.view.ViewGroup
import androidx.preference.PreferenceManager
import org.lineageos.tv.launcher.model.LeanbackAppInfo
import org.lineageos.tv.launcher.view.AppCard
import org.lineageos.tv.launcher.ext.appCardSize

class AllAppsAdapter : TvAdapter<LeanbackAppInfo, AppCard>() {

    var gridMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val card = AppCard(parent.context).apply {
            setGridMode(gridMode)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
        return ViewHolder(card)
    }

    override fun handleLongClick(card: AppCard): Boolean {
        card.showPopupMenu()
        return true
    }
}

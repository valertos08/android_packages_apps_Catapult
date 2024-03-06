/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.adapter

import android.content.Context
import android.view.ViewGroup
import org.lineageos.tv.launcher.view.AppCard

class AppsAdapter(context: Context) : TvAdapter<AppCard>(context) {
    override fun onBindViewHolder(holder: TvAdapter<AppCard>.ViewHolder, position: Int) {
        holder.card.setCardInfo(launchablesList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = AppCard(parent.context)

        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        itemView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

        return ViewHolder(itemView)
    }

    override fun handleLongClick(card: AppCard): Boolean {
        card.showPopupMenu()
        return true
    }
}

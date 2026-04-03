/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.ext.appCardSize
import org.lineageos.tv.launcher.ext.favoriteCardSize
import org.lineageos.tv.launcher.ext.watchNextCardSize
import org.lineageos.tv.launcher.model.MainRowItem
import org.lineageos.tv.launcher.view.MainRowItemView

class MainVerticalAdapter(
    private val context: Context
) : ListAdapter<Pair<Long, MainRowItem>, MainVerticalAdapter.ViewHolder>(diffCallback) {
    
    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    
    inner class ViewHolder(
        private val mainRowItemView: MainRowItemView,
    ) : RecyclerView.ViewHolder(mainRowItemView) {
        fun bind(item: Pair<Long, MainRowItem>, position: Int) {
            mainRowItemView.setData(item.second)

            if (position == 0) {
                mainRowItemView.requestFocus()
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        MainRowItemView(parent.context)
    )

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Pair<Long, MainRowItem>>() {
            override fun areItemsTheSame(
                oldItem: Pair<Long, MainRowItem>,
                newItem: Pair<Long, MainRowItem>
            ) = oldItem.first == newItem.first

            @Suppress("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: Pair<Long, MainRowItem>,
                newItem: Pair<Long, MainRowItem>
            ) = oldItem.second.label == newItem.second.label
                    && oldItem.second.adapter === newItem.second.adapter
        }
    }
}

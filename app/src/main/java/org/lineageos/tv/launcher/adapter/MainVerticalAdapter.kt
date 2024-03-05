/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.utils.Suggestions
import org.lineageos.tv.launcher.utils.Suggestions.orderSuggestions
import java.util.Collections

class MainVerticalAdapter(
    private val context: Context,
    private val rowList: MutableList<Pair<Long, org.lineageos.tv.launcher.model.MainRowItem>>,
) :
    RecyclerView.Adapter<MainVerticalAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val v = (viewHolder.itemView as org.lineageos.tv.launcher.view.MainRowItem)
        v.setData(rowList[i].second)
        v.layoutParams = if (rowList[i].second.adapter is AppsAdapter) {
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                context.resources.getDimension(R.dimen.main_app_row_height).toInt()
            )
        } else {
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                context.resources.getDimension(R.dimen.main_row_height).toInt()
            )
        }
    }

    override fun getItemCount(): Int {
        return rowList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(org.lineageos.tv.launcher.view.MainRowItem(context))
    }

    fun removeItem(item: Long) {
        for ((i, row) in rowList.withIndex()) {
            if (row.first == item) {
                rowList.remove(row)
                notifyItemRemoved(i)
                return
            }
        }
    }

    fun addItem(item: Pair<Long, org.lineageos.tv.launcher.model.MainRowItem>) {
        var temp = rowList.toMutableList()
        temp.add(item)
        temp =
            temp.orderSuggestions(Suggestions.getChannelOrder(context)) { it.first } as MutableList
        var index = temp.indexOf(item)
        if (index == -1) {
            index = (rowList.size - 1)
        }
        rowList.add(index, item)
        notifyItemInserted(index)
    }

    fun isChannelShowing(channelId: Long?): Boolean {
        channelId ?: return false
        val res = rowList.find { it.first == channelId }
        res ?: return false
        return true
    }

    fun findChannelIndex(channelId: Long?): Int {
        channelId ?: return -1
        return rowList.indexOfFirst { it.first == channelId }
    }

    fun itemMoved(from: Int, to: Int) {
        Collections.swap(rowList, from, to)
        notifyItemMoved(from, to)
    }
}

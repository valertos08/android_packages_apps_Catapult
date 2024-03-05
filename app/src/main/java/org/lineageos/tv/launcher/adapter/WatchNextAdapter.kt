/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.tvprovider.media.tv.BasePreviewProgram
import org.lineageos.tv.launcher.view.WatchNextCard

@SuppressLint("RestrictedApi")
class WatchNextAdapter(
    private val watchableList: MutableList<BasePreviewProgram>,
) : RecyclerView.Adapter<WatchNextAdapter.ViewHolder>() {

    inner class ViewHolder(val card: WatchNextCard) : RecyclerView.ViewHolder(card) {
        init {
            card.setOnClickListener {
                handleClick(card)
            }
        }
    }

    private fun handleClick(v: WatchNextCard) {
        val context = v.context
        context.startActivity(v.launchIntent)
        Toast.makeText(context, v.label, Toast.LENGTH_SHORT).show()
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.card.setInfo(watchableList[i])
    }

    override fun getItemCount() = watchableList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = WatchNextCard(parent.context)

        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )

        itemView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

        return ViewHolder(itemView)
    }
}

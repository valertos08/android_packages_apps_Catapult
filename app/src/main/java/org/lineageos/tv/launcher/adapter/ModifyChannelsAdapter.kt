/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.Channel
import org.lineageos.tv.launcher.utils.Suggestions
import org.lineageos.tv.launcher.view.ToggleChannelView
import java.util.Collections

class ModifyChannelsAdapter(private val mContext: Context, private val mChannels: List<Channel>) :
    RecyclerView.Adapter<ModifyChannelsAdapter.ViewHolder>() {

    val hiddenChannels: ArrayList<Long> by lazy { Suggestions.getHiddenChannels(mContext) }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener, View.OnKeyListener {

        @SuppressLint("UseSwitchCompatOrMaterialCode") // Not available for leanback
        val mSwitch: Switch = itemView.findViewById(R.id.state_switch)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            itemView.setOnKeyListener(this)
        }

        override fun onClick(v: View) {
            v as ToggleChannelView

            if (v.mMoving) {
                v.setMoveDone()
                return
            }

            if (!mSwitch.isEnabled) {
                return
            }

            if (mSwitch.isChecked) {
                mSwitch.isChecked = false
                Suggestions.hideChannel(mContext, v.mChannelId)
                v.mChannelId?.let { hiddenChannels.add(it) }
            } else {
                mSwitch.isChecked = true
                Suggestions.showChannel(mContext, v.mChannelId)
                hiddenChannels.remove(v.mChannelId)
            }
        }

        override fun onLongClick(v: View): Boolean {
            v as ToggleChannelView
            v.setMoving()
            v.mChannelId?.let { Suggestions.onChannelSelectedCallback(it, bindingAdapterPosition) }
            return true
        }

        override fun onKey(v: View?, keyCode: Int, keyEvent: KeyEvent): Boolean {
            v as ToggleChannelView

            // Leave center key for onClick handler
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                return false
            }

            // Only handle keyDown events here
            if (keyEvent.action != KeyEvent.ACTION_DOWN) {
                return v.mMoving
            }

            val pos = bindingAdapterPosition
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (v.mMoving) {
                        v.setMoveDone()
                        return true
                    }
                    return false
                }

                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (v.mMoving) {
                        if (pos == 0) {
                            return true
                        }
                        Collections.swap(mChannels, pos, pos - 1)
                        notifyItemMoved(pos, pos - 1)
                        Suggestions.saveChannelOrder(
                            mContext, pos, pos - 1, mChannels.map { it.id },
                            v.mChannelId !in hiddenChannels
                        )
                        return true
                    }
                }

                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (v.mMoving) {
                        if (pos == mChannels.size - 1) {
                            return true
                        }
                        Collections.swap(mChannels, pos, pos + 1)
                        notifyItemMoved(pos, pos + 1)
                        Suggestions.saveChannelOrder(
                            mContext, pos, pos + 1, mChannels.map { it.id },
                            v.mChannelId !in hiddenChannels
                        )
                        return true
                    }
                }
            }

            return false
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        var hidden = false
        if (hiddenChannels.contains(mChannels[i].id)) {
            hidden = true
        }
        val v = viewHolder.itemView as ToggleChannelView
        v.setData(mChannels[i], hidden)

        if (mChannels[i].id == Channel.FAVORITE_APPS_ID) {
            v.disableToggle()
        }
    }

    override fun getItemCount(): Int {
        return mChannels.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = ToggleChannelView(parent.context)

        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return ViewHolder(itemView)
    }
}

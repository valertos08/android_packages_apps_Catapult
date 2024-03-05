/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.VerticalGridView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.lineageos.tv.launcher.adapter.ModifyChannelsAdapter
import org.lineageos.tv.launcher.model.Channel
import org.lineageos.tv.launcher.utils.Suggestions
import org.lineageos.tv.launcher.utils.Suggestions.orderSuggestions

class ModifyChannelsActivity : FragmentActivity(R.layout.activity_modify_channels) {
    // Views
    private val channelsGrid by lazy { findViewById<VerticalGridView>(R.id.modify_channels_grid) }
    private val progressLoadingChannels by lazy { findViewById<ProgressBar>(R.id.progress_loading_channels) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val channelOrder = Suggestions.getChannelOrder(this)

        val layoutParams = window.attributes.apply {
            gravity = Gravity.END
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
        window.attributes = layoutParams

        lifecycleScope.launch {
            val channels = listOf(
                Channel(Channel.FAVORITE_APPS_ID, getString(R.string.favorites)),
                Channel(Channel.WATCH_NEXT_ID, getString(R.string.watch_next)),
                *Suggestions.getPreviewChannelsAsync(this@ModifyChannelsActivity).map {
                    Channel(
                        it.id,
                        Suggestions.getChannelTitle(this@ModifyChannelsActivity, it)
                    )
                }.toTypedArray(),
                Channel(Channel.ALL_APPS_ID, getString(R.string.other_apps)),
            )

            // Display the data & hide spinner
            channelsGrid.adapter =
                ModifyChannelsAdapter(
                    this@ModifyChannelsActivity,
                    channels.orderSuggestions(channelOrder) { it.id })
            channelsGrid.visibility = View.VISIBLE
            progressLoadingChannels.visibility = View.GONE
        }
    }
}

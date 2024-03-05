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

class ModifyChannelsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_channels)

        val channelsGrid: VerticalGridView = findViewById(R.id.modify_channels_grid)
        val channelOrder = Suggestions.getChannelOrder(this)

        val layoutParams = window.attributes
        layoutParams.gravity = Gravity.END
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = layoutParams

        lifecycleScope.launch {
            val channels = ArrayList<Channel>()
            channels.add(Channel(Channel.FAVORITE_APPS_ID, getString(R.string.favorites)))
            channels.add(Channel(Channel.WATCH_NEXT_ID, getString(R.string.watch_next)))
            val previewChannels =
                Suggestions.getPreviewChannelsAsync(this@ModifyChannelsActivity)
            previewChannels.map {
                channels.add(
                    Channel(
                        it.id,
                        Suggestions.getChannelTitle(this@ModifyChannelsActivity, it)
                    )
                )
            }
            channels.add(Channel(Channel.ALL_APPS_ID, getString(R.string.other_apps)))

            // Display the data & hide spinner
            channelsGrid.adapter =
                ModifyChannelsAdapter(
                    this@ModifyChannelsActivity,
                    channels.orderSuggestions(channelOrder) { it.id })
            channelsGrid.visibility = View.VISIBLE
            findViewById<ProgressBar>(R.id.progress_loading_channels).visibility = View.GONE
        }
    }
}

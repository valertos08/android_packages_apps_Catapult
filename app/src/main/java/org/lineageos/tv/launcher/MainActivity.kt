/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.VerticalGridView
import androidx.lifecycle.lifecycleScope
import androidx.tvprovider.media.tv.BasePreviewProgram
import androidx.tvprovider.media.tv.PreviewChannel
import androidx.tvprovider.media.tv.TvContractCompat
import kotlinx.coroutines.launch
import org.lineageos.tv.launcher.adapter.AppsAdapter
import org.lineageos.tv.launcher.adapter.FavoritesAdapter
import org.lineageos.tv.launcher.adapter.MainVerticalAdapter
import org.lineageos.tv.launcher.adapter.WatchNextAdapter
import org.lineageos.tv.launcher.model.Channel
import org.lineageos.tv.launcher.model.MainRowItem
import org.lineageos.tv.launcher.receiver.PackageReceiver
import org.lineageos.tv.launcher.utils.AppManager
import org.lineageos.tv.launcher.utils.Suggestions
import org.lineageos.tv.launcher.utils.Suggestions.orderSuggestions

class MainActivity : FragmentActivity(R.layout.activity_main) {
    // Views
    private val assistantButtonsContainer by lazy { findViewById<LinearLayout>(R.id.assistant_buttons) }
    private val assistantShowButton by lazy { findViewById<TextView>(R.id.assistant_title) }
    private val keyboardAssistantButton by lazy { findViewById<ImageButton>(R.id.keyboard_assistant) }
    private val mainVerticalGridView by lazy { findViewById<VerticalGridView>(R.id.main_vertical_grid) }
    private val settingButton by lazy { findViewById<ImageButton>(R.id.settings_button) }
    private val topBarContainer by lazy { findViewById<LinearLayout>(R.id.top_bar) }
    private val voiceAssistantButton by lazy { findViewById<ImageButton>(R.id.voice_assistant) }

    // Adapters
    private val favoritesAdapter by lazy { FavoritesAdapter(this@MainActivity) }
    private lateinit var mainVerticalAdapter: MainVerticalAdapter
    private val appsAdapter by lazy { AppsAdapter(this@MainActivity) }

    private lateinit var channels: List<PreviewChannel>

    private val packageReceiver by lazy { PackageReceiver() }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkCallingOrSelfPermission(TvContractCompat.PERMISSION_READ_TV_LISTINGS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(TvContractCompat.PERMISSION_READ_TV_LISTINGS), 0)
        }

        lifecycleScope.launch {
            val mainItems = getMainRows()
            mainVerticalAdapter = MainVerticalAdapter(this@MainActivity, mainItems)
            mainVerticalGridView.adapter = mainVerticalAdapter
        }

        settingButton.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
        }

        AppManager.onFavoriteAddedCallback = ::onFavoriteAdded
        AppManager.onFavoriteRemovedCallback = ::onFavoriteRemoved

        Suggestions.onChannelHiddenCallback = ::onChannelHidden
        Suggestions.onChannelShownCallback = ::onChannelShown
        Suggestions.onChannelOrderChangedCallback = ::onChannelOrderChanged
        Suggestions.onChannelSelectedCallback = ::onChannelSelected

        PackageReceiver.onPackageInstalledCallback = ::onPackageInstalled
        PackageReceiver.onPackageUninstalledCallback = ::onPackageUninstalled

        // Has to be registered in code
        // https://developer.android.com/develop/background-work/background-tasks/broadcasts/broadcast-exceptions
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, intentFilter)

        val assistIntent = Intent(Intent.ACTION_ASSIST)
        if (assistIntent.resolveActivity(packageManager) != null) {
            setupAssistantButtons(assistIntent)
        } else {
            assistantShowButton.isInvisible = true
            assistantButtonsContainer.isInvisible = true
        }
    }

    override fun onDestroy() {
        unregisterReceiver(packageReceiver)

        super.onDestroy()
    }

    private fun onPackageInstalled(packageName: String) {
        // Add the app to All apps -list
        val hiddenChannels = Suggestions.getHiddenChannels(this)
        if (Channel.ALL_APPS_ID !in hiddenChannels) {
            appsAdapter.addItem(packageName)
        }
    }

    private fun onPackageUninstalled(packageName: String) {
        val hiddenChannels = Suggestions.getHiddenChannels(this)
        if (Channel.ALL_APPS_ID !in hiddenChannels) {
            appsAdapter.removeItem(packageName)
        }

        favoritesAdapter.removeItem(packageName)
        AppManager.removeFavoriteApp(this, packageName)
    }

    private fun onFavoriteAdded(packageName: String) {
        favoritesAdapter.addItem(packageName)
    }

    private fun onFavoriteRemoved(packageName: String) {
        favoritesAdapter.removeItem(packageName)
    }

    private fun onChannelHidden(channelId: Long) {
        mainVerticalAdapter.removeItem(channelId)
    }

    private fun onChannelShown(channelId: Long) {
        if (channelId == Channel.WATCH_NEXT_ID) {
            mainVerticalAdapter.addItem(
                Pair(
                    Channel.WATCH_NEXT_ID,
                    MainRowItem(
                        getString(R.string.watch_next), WatchNextAdapter(
                            Suggestions.getWatchNextPrograms(this)
                                .filterIsInstance<BasePreviewProgram>() as MutableList<BasePreviewProgram>
                        )
                    )
                )
            )
            return
        } else if (channelId == Channel.ALL_APPS_ID) {
            mainVerticalAdapter.addItem(
                Pair(
                    Channel.ALL_APPS_ID,
                    MainRowItem(getString(R.string.other_apps), AppsAdapter(this))
                )
            )
            return
        }

        var channel: PreviewChannel? = null
        for (c in channels) {
            if (c.id == channelId) {
                channel = c
            }
        }

        channel ?: return

        val previewPrograms = Suggestions.getSuggestions(this, channel.id).take(5)
            .filterIsInstance<BasePreviewProgram>() as MutableList<BasePreviewProgram>
        if (previewPrograms.isEmpty()) {
            return
        }

        mainVerticalAdapter.addItem(
            Pair(
                channel.id, MainRowItem(
                    channel.displayName.toString(),
                    WatchNextAdapter(previewPrograms)
                )
            )
        )
    }

    private fun onChannelOrderChanged(
        moveChannelId: Long?,
        otherChannelId: Long?,
    ) {
        val isMovingChannelShowing = mainVerticalAdapter.isChannelShowing(moveChannelId)
        val isOtherChannelShowing = mainVerticalAdapter.isChannelShowing(otherChannelId)
        if (!isMovingChannelShowing || !isOtherChannelShowing) {
            return
        }

        val from = mainVerticalAdapter.findChannelIndex(moveChannelId)
        val to = mainVerticalAdapter.findChannelIndex(otherChannelId)
        mainVerticalAdapter.itemMoved(from, to)
    }

    private fun onChannelSelected(channelId: Long, index: Int) {
        if (mainVerticalAdapter.isChannelShowing(channelId)) {
            val pos = mainVerticalAdapter.findChannelIndex(channelId)
            mainVerticalGridView.layoutManager?.scrollToPosition(pos)
        }
    }

    private fun setupAssistantButtons(assistIntent: Intent) {
        voiceAssistantButton.setOnClickListener {
            startActivity(assistIntent)
        }

        val keyboardAssistantIntent = Intent(assistIntent).apply {
            putExtra(Intent.EXTRA_ASSIST_INPUT_HINT_KEYBOARD, true)
        }
        keyboardAssistantButton.setOnClickListener {
            startActivity(keyboardAssistantIntent)
        }

        val transition = Slide().apply {
            slideEdge = Gravity.START
            duration = 400
        }
        assistantShowButton.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                transition.removeTarget(assistantShowButton)
                transition.addTarget(assistantButtonsContainer)
                TransitionManager.beginDelayedTransition(topBarContainer, transition)
                assistantShowButton.isVisible = false
                assistantButtonsContainer.isVisible = true
            }
        }

        val assistantButtonFocusListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!keyboardAssistantButton.hasFocus() && !voiceAssistantButton.hasFocus()) {
                    transition.removeTarget(assistantButtonsContainer)
                    transition.addTarget(assistantShowButton)
                    TransitionManager.beginDelayedTransition(topBarContainer, transition)
                    assistantButtonsContainer.isVisible = false
                    assistantShowButton.isVisible = true
                }
            }
        }

        keyboardAssistantButton.onFocusChangeListener = assistantButtonFocusListener
        voiceAssistantButton.onFocusChangeListener = assistantButtonFocusListener
    }

    private suspend fun getMainRows(): MutableList<Pair<Long, MainRowItem>> {
        val mainItems = mutableListOf<Pair<Long, MainRowItem>>()
        val hiddenChannels = Suggestions.getHiddenChannels(this@MainActivity)

        // Add favorites-row. Can't be hidden
        mainItems.add(
            Pair(
                Channel.FAVORITE_APPS_ID,
                MainRowItem(getString(R.string.favorites), favoritesAdapter)
            )
        )

        // Add watch next -row
        if (Channel.WATCH_NEXT_ID !in hiddenChannels) {
            mainItems.add(
                Pair(
                    Channel.WATCH_NEXT_ID, MainRowItem(
                        getString(R.string.watch_next), WatchNextAdapter(
                            Suggestions.getWatchNextPrograms(this@MainActivity)
                                .filterIsInstance<BasePreviewProgram>() as MutableList<BasePreviewProgram>
                        )
                    )
                )
            )
        }

        // Add preview channels from apps
        channels = Suggestions.getPreviewChannelsAsync(this@MainActivity)
        mainItems.addAll(getMainChannelRows(hiddenChannels))

        // Add All apps -row
        if (Channel.ALL_APPS_ID !in hiddenChannels) {
            mainItems.add(
                Pair(
                    Channel.ALL_APPS_ID,
                    MainRowItem(getString(R.string.other_apps), appsAdapter)
                )
            )
        }

        return mainItems.orderSuggestions(Suggestions.getChannelOrder(this@MainActivity)) { it.first } as MutableList
    }

    private suspend fun getMainChannelRows(hiddenChannels: MutableList<Long>): MutableList<Pair<Long, MainRowItem>> {
        val mainItems = mutableListOf<Pair<Long, MainRowItem>>()
        for (channel in channels) {
            if (channel.id in hiddenChannels) {
                continue
            }

            val previewPrograms =
                Suggestions.getSuggestionsAsync(this@MainActivity, channel.id).take(5)
                    .filterIsInstance<BasePreviewProgram>() as MutableList<BasePreviewProgram>
            if (previewPrograms.isEmpty()) {
                continue
            }
            mainItems.add(
                Pair(
                    channel.id, MainRowItem(
                        Suggestions.getChannelTitle(this@MainActivity, channel),
                        WatchNextAdapter(previewPrograms)
                    )
                )
            )
        }
        return mainItems
    }
}

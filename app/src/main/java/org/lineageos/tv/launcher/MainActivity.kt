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
import android.widget.ImageButton
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


class MainActivity : FragmentActivity() {
    private lateinit var mFavoritesAdapter: FavoritesAdapter
    private lateinit var mMainVerticalAdapter: MainVerticalAdapter
    private lateinit var mAllAppsAdapter: AppsAdapter

    private lateinit var mChannels: List<PreviewChannel>
    private lateinit var mMainVerticalGridView: VerticalGridView

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkCallingOrSelfPermission(TvContractCompat.PERMISSION_READ_TV_LISTINGS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(TvContractCompat.PERMISSION_READ_TV_LISTINGS), 0)
        }

        mFavoritesAdapter = FavoritesAdapter(this@MainActivity)
        mMainVerticalGridView = findViewById(R.id.main_vertical_grid)

        lifecycleScope.launch {
            val mainItems = getMainRows()
            mMainVerticalAdapter = MainVerticalAdapter(this@MainActivity, mainItems)
            mMainVerticalGridView.adapter = mMainVerticalAdapter
        }

        val settingButton: ImageButton = findViewById(R.id.settings_button)
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
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
        intentFilter.addDataScheme("package")
        registerReceiver(PackageReceiver(), intentFilter)
    }

    private fun onPackageInstalled(packageName: String) {
        // Add the app to All apps -list
        val hiddenChannels = Suggestions.getHiddenChannels(this)
        if (Channel.ALL_APPS_ID !in hiddenChannels) {
            mAllAppsAdapter.addItem(packageName)
        }
    }

    private fun onPackageUninstalled(packageName: String) {
        val hiddenChannels = Suggestions.getHiddenChannels(this)
        if (Channel.ALL_APPS_ID !in hiddenChannels) {
            mAllAppsAdapter.removeItem(packageName)
        }
        if (Channel.FAVORITE_APPS_ID !in hiddenChannels) {
            mFavoritesAdapter.removeItem(packageName)
        }
        AppManager.removeFavoriteApp(this, packageName)
    }

    private fun onFavoriteAdded(packageName: String) {
        mFavoritesAdapter.addItem(packageName)
    }

    private fun onFavoriteRemoved(packageName: String) {
        mFavoritesAdapter.removeItem(packageName)
    }

    private fun onChannelHidden(channelId: Long) {
        mMainVerticalAdapter.removeItem(channelId)
    }

    private fun onChannelShown(channelId: Long) {
        if (channelId == Channel.WATCH_NEXT_ID) {
            mMainVerticalAdapter.addItem(
                Pair(
                    Channel.WATCH_NEXT_ID,
                    MainRowItem(
                        getString(R.string.watch_next), WatchNextAdapter(
                            this,
                            Suggestions.getWatchNextPrograms(this)
                                .filterIsInstance<BasePreviewProgram>() as ArrayList<BasePreviewProgram>
                        )
                    )
                )
            )
            return
        } else if (channelId == Channel.ALL_APPS_ID) {
            mMainVerticalAdapter.addItem(
                Pair(
                    Channel.ALL_APPS_ID,
                    MainRowItem(getString(R.string.other_apps), AppsAdapter(this))
                )
            )
            return
        }

        var channel: PreviewChannel? = null
        for (c in mChannels) {
            if (c.id == channelId) {
                channel = c
            }
        }

        channel ?: return

        val previewPrograms = Suggestions.getSuggestions(this, channel.id).take(5)
            .filterIsInstance<BasePreviewProgram>() as ArrayList<BasePreviewProgram>
        if (previewPrograms.isEmpty()) {
            return
        }

        mMainVerticalAdapter.addItem(
            Pair(
                channel.id, MainRowItem(
                    channel.displayName.toString(),
                    WatchNextAdapter(this, previewPrograms)
                )
            )
        )
    }

    private fun onChannelOrderChanged(
        moveChannelId: Long?,
        otherChannelId: Long?,
    ) {
        val isMovingChannelShowing = mMainVerticalAdapter.isChannelShowing(moveChannelId)
        val isOtherChannelShowing = mMainVerticalAdapter.isChannelShowing(otherChannelId)
        if (!isMovingChannelShowing || !isOtherChannelShowing) {
            return
        }

        val from = mMainVerticalAdapter.findChannelIndex(moveChannelId)
        val to = mMainVerticalAdapter.findChannelIndex(otherChannelId)
        mMainVerticalAdapter.itemMoved(from, to)
    }

    private fun onChannelSelected(channelId: Long, index: Int) {
        if (mMainVerticalAdapter.isChannelShowing(channelId)) {
            val pos = mMainVerticalAdapter.findChannelIndex(channelId)
            mMainVerticalGridView.layoutManager?.scrollToPosition(pos)
        }
    }

    private suspend fun getMainRows(): ArrayList<Pair<Long, MainRowItem>> {
        val mainItems: ArrayList<Pair<Long, MainRowItem>> = ArrayList()
        val hiddenChannels = Suggestions.getHiddenChannels(this@MainActivity)

        // Add favorites-row. Can't be hidden
        mainItems.add(
            Pair(
                Channel.FAVORITE_APPS_ID,
                MainRowItem(getString(R.string.favorites), mFavoritesAdapter)
            )
        )

        // Add watch next -row
        if (Channel.WATCH_NEXT_ID !in hiddenChannels) {
            mainItems.add(
                Pair(
                    Channel.WATCH_NEXT_ID, MainRowItem(
                        getString(R.string.watch_next), WatchNextAdapter(
                            this@MainActivity,
                            Suggestions.getWatchNextPrograms(this@MainActivity)
                                .filterIsInstance<BasePreviewProgram>() as ArrayList<BasePreviewProgram>
                        )
                    )
                )
            )
        }

        // Add preview channels from apps
        mChannels = Suggestions.getPreviewChannelsAsync(this@MainActivity)
        mainItems.addAll(getMainChannelRows(hiddenChannels))

        // Add All apps -row
        mAllAppsAdapter = AppsAdapter(this@MainActivity)
        if (Channel.ALL_APPS_ID !in hiddenChannels) {
            mainItems.add(
                Pair(
                    Channel.ALL_APPS_ID,
                    MainRowItem(getString(R.string.other_apps), mAllAppsAdapter)
                )
            )
        }

        return mainItems.orderSuggestions(Suggestions.getChannelOrder(this@MainActivity)) { it.first } as ArrayList
    }

    private suspend fun getMainChannelRows(hiddenChannels: ArrayList<Long>): ArrayList<Pair<Long, MainRowItem>> {
        val mainItems: ArrayList<Pair<Long, MainRowItem>> = ArrayList()
        for (channel in mChannels) {
            if (channel.id in hiddenChannels) {
                continue
            }

            val previewPrograms =
                Suggestions.getSuggestionsAsync(this@MainActivity, channel.id).take(5)
                    .filterIsInstance<BasePreviewProgram>() as ArrayList<BasePreviewProgram>
            if (previewPrograms.isEmpty()) {
                continue
            }
            mainItems.add(
                Pair(
                    channel.id, MainRowItem(
                        Suggestions.getChannelTitle(this@MainActivity, channel),
                        WatchNextAdapter(this@MainActivity, previewPrograms)
                    )
                )
            )
        }
        return mainItems
    }
}

/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.leanback.widget.VerticalGridView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.tv.launcher.adapter.AllAppsAdapter
import org.lineageos.tv.launcher.adapter.FavoritesAdapter
import org.lineageos.tv.launcher.adapter.MainVerticalAdapter
import org.lineageos.tv.launcher.adapter.PreviewProgramsAdapter
import org.lineageos.tv.launcher.adapter.WatchNextAdapter
import org.lineageos.tv.launcher.ext.favoriteApps
import org.lineageos.tv.launcher.model.AppInfo
import org.lineageos.tv.launcher.model.InternalChannel
import org.lineageos.tv.launcher.model.MainRowItem
import org.lineageos.tv.launcher.utils.AppManager
import org.lineageos.tv.launcher.utils.PermissionsGatedCallback
import org.lineageos.tv.launcher.viewmodels.LauncherViewModel

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    // View models
    private val model: LauncherViewModel by viewModels()

    // Views
    private val assistantButtonsContainer by lazy { findViewById<LinearLayout>(R.id.assistant_buttons) }
    private val assistantShowButton by lazy { findViewById<TextView>(R.id.assistant_title) }
    private val keyboardAssistantButton by lazy { findViewById<ImageButton>(R.id.keyboard_assistant) }
    private val mainVerticalGridView by lazy { findViewById<VerticalGridView>(R.id.main_vertical_grid) }
    private val settingButton by lazy { findViewById<ImageButton>(R.id.settings_button) }
    private val topBarContainer by lazy { findViewById<LinearLayout>(R.id.top_bar) }
    private val voiceAssistantButton by lazy { findViewById<ImageButton>(R.id.voice_assistant) }

    // Adapters
    private val allAppsAdapter by lazy { AllAppsAdapter() }
    private val favoritesAdapter by lazy { FavoritesAdapter() }
    private val mainVerticalAdapter by lazy { MainVerticalAdapter() }
    private val watchNextAdapter by lazy { WatchNextAdapter() }
    private val previewChannelAdapters = mutableMapOf<Long, PreviewProgramsAdapter>()

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val permissionsGatedCallback = PermissionsGatedCallback(this) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.channelsToPrograms.collectLatest {
                    mainVerticalAdapter.submitList(
                        it.map { channel ->
                            channel.first.id to MainRowItem(
                                channel.first.title,
                                when (channel.first.id) {
                                    InternalChannel.FAVORITE_APPS.id -> favoritesAdapter
                                    InternalChannel.WATCH_NEXT.id -> watchNextAdapter
                                    InternalChannel.ALL_APPS.id -> allAppsAdapter
                                    else -> previewChannelAdapters.getOrPut(channel.first.id) {
                                        PreviewProgramsAdapter()
                                    }.apply {
                                        channel.second?.let { previewPrograms ->
                                            submitList(previewPrograms)
                                        }
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.installedApps.collectLatest {
                    allAppsAdapter.submitList(it)

                    if (it.isNotEmpty()) {
                        AppManager.updateFavoriteApps(this@MainActivity, it)
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.watchNextPrograms.collectLatest {
                    watchNextAdapter.submitList(it)
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.favoriteApps.collectLatest {
                    favoritesAdapter.submitList(
                        it.mapNotNull {
                            runCatching {
                                AppInfo(
                                    packageManager.getApplicationInfo(it, 0),
                                    this@MainActivity
                                )
                            }.getOrNull()
                        } + listOf(
                            FavoritesAdapter.createAddFavoriteEntry(this@MainActivity),
                            FavoritesAdapter.createModifyChannelsEntry(this@MainActivity),
                        )
                    )
                }
            }
        }
    }

    @Suppress("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingButton.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
        }

        val assistIntent = Intent(Intent.ACTION_ASSIST)
        assistIntent.resolveActivity(packageManager)?.also {
            setupAssistantButtons(assistIntent)
        } ?: run {
            assistantShowButton.isInvisible = true
            assistantButtonsContainer.isInvisible = true
        }

        mainVerticalGridView.adapter = mainVerticalAdapter

        favoritesAdapter.onFavoritesChangedCallback = {
            sharedPreferences.favoriteApps = it
        }

        settingButton.requestFocus()

        permissionsGatedCallback.runAfterPermissionsCheck()
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
}

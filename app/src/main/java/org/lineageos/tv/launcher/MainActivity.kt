/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.app.Dialog
import android.app.role.RoleManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.leanback.widget.VerticalGridView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.lineageos.tv.launcher.adapter.AllAppsAdapter
import org.lineageos.tv.launcher.adapter.FavoritesAdapter
import org.lineageos.tv.launcher.adapter.MainVerticalAdapter
import org.lineageos.tv.launcher.adapter.PreviewProgramsAdapter
import org.lineageos.tv.launcher.adapter.WatchNextAdapter
import org.lineageos.tv.launcher.ext.favoriteApps
import org.lineageos.tv.launcher.ext.homeRoleRequestDialogDismissed
import org.lineageos.tv.launcher.ext.roleCanBeRequested
import org.lineageos.tv.launcher.ext.backgroundType
import org.lineageos.tv.launcher.ext.backgroundColor
import org.lineageos.tv.launcher.ext.backgroundImageUri
import org.lineageos.tv.launcher.ext.appCardSize
import org.lineageos.tv.launcher.ext.favoriteCardSize
import org.lineageos.tv.launcher.ext.watchNextCardSize
import org.lineageos.tv.launcher.model.AppInfo
import org.lineageos.tv.launcher.model.InternalChannel
import org.lineageos.tv.launcher.model.MainRowItem
import org.lineageos.tv.launcher.notification.NotificationUtils
import org.lineageos.tv.launcher.notification.ServiceConnectionState
import org.lineageos.tv.launcher.utils.AppManager
import org.lineageos.tv.launcher.utils.PermissionsGatedCallback
import org.lineageos.tv.launcher.viewmodels.LauncherViewModel
import org.lineageos.tv.launcher.viewmodels.NotificationViewModel
import java.util.Locale

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    // Card size tracking
    private var lastAppCardSize = 100
    private var lastFavoriteCardSize = 100
    private var lastWatchNextCardSize = 100

    // View models
    private val model: LauncherViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()

    // Views
    private val assistantButtonsContainer by lazy { findViewById<LinearLayout>(R.id.assistant_buttons)!! }
    private val assistantHintImageView by lazy { findViewById<ImageView>(R.id.assistantHintImageView)!! }
    private val keyboardAssistantButton by lazy { findViewById<ImageButton>(R.id.keyboard_assistant)!! }
    private val mainVerticalGridView by lazy { findViewById<VerticalGridView>(R.id.main_vertical_grid)!! }
    private val settingButton by lazy { findViewById<ImageButton>(R.id.settingsMaterialButton)!! }
    private val notificationCountTextView by lazy { findViewById<TextView>(R.id.notificationCountTextView)!! }
    private val topBarContainer by lazy { findViewById<LinearLayout>(R.id.top_bar)!! }
    private val voiceAssistantButton by lazy { findViewById<ImageButton>(R.id.voice_assistant)!! }

    // System services
    private val roleManager by lazy { getSystemService(RoleManager::class.java)!! }

    // Activity request launchers
    private val homeRoleActivityRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Do nothing
    }

    // Adapters
    private val allAppsAdapter by lazy { AllAppsAdapter() }
    private val favoritesAdapter by lazy { FavoritesAdapter() }
    private val mainVerticalAdapter by lazy { MainVerticalAdapter(this) }
    private val watchNextAdapter by lazy { WatchNextAdapter() }
    private val previewChannelAdapters = mutableMapOf<Long, PreviewProgramsAdapter>()

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val permissionsGatedCallback = PermissionsGatedCallback(this) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    model.watchNextPrograms,
                    model.channelsToPrograms
                ) { watchNextPrograms, channels ->
                    channels.mapNotNull { channel ->
                        // Check if "Watch Next" should be skipped
                        if (channel.first.id == InternalChannel.WATCH_NEXT.id && watchNextPrograms.isEmpty()) {
                            null
                        } else {
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
                    } to watchNextPrograms
                }.collectLatest { (updatedList, watchNextPrograms) ->
                    mainVerticalAdapter.submitList(updatedList)
                    watchNextAdapter.submitList(watchNextPrograms)
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
                model.favoriteApps.collectLatest {
                    favoritesAdapter.submitList(
                        it.mapNotNull {
                            runCatching {
                                AppInfo.create(this@MainActivity, it)
                            }.getOrNull()
                        } + listOf(
                            FavoritesAdapter.createAddFavoriteEntry(this@MainActivity),
                            FavoritesAdapter.createModifyChannelsEntry(this@MainActivity),
                        )
                    )
                }
            }
        }

        askForHomeRoleIfNeeded()
    }

    @Suppress("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyBackground()

        lastAppCardSize = sharedPreferences.appCardSize
        lastFavoriteCardSize = sharedPreferences.favoriteCardSize
        lastWatchNextCardSize = sharedPreferences.watchNextCardSize

        settingButton.setOnClickListener {
            val dialog = Dialog(this, R.style.Theme_Catapult_SideActivity)
            dialog.setContentView(R.layout.settings_button_menu)
            dialog.window?.apply {
                setGravity(Gravity.END)
                attributes = attributes.apply {
                    height = android.view.WindowManager.LayoutParams.MATCH_PARENT
                    width = (350 * resources.displayMetrics.density).toInt()
                }
            }
            dialog.findViewById<TextView>(R.id.system_settings_item)?.setOnClickListener {
                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                dialog.dismiss()
            }
            dialog.findViewById<TextView>(R.id.launcher_settings_item)?.setOnClickListener {
                startActivity(Intent(this@MainActivity, LauncherSettingsActivity::class.java))
                dialog.dismiss()
            }
            dialog.show()
        }

        notificationCountTextView.setOnClickListener {
            startActivity(Intent(this@MainActivity, SystemOptionsActivity::class.java))
        }

        val assistIntent = Intent(Intent.ACTION_ASSIST)
        assistIntent.resolveActivity(packageManager)?.also {
            setupAssistantButtons(assistIntent)
        } ?: run {
            assistantHintImageView.isInvisible = true
            assistantButtonsContainer.isInvisible = true
        }

        mainVerticalGridView.adapter = mainVerticalAdapter

        favoritesAdapter.onFavoritesChangedCallback = {
            sharedPreferences.favoriteApps = it
        }

        settingButton.requestFocus()

        permissionsGatedCallback.runAfterPermissionsCheck()

        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationViewModel.state.collect { state ->
                    when (state) {
                        ServiceConnectionState.Connected -> {}
                        ServiceConnectionState.Disconnected -> {
                            notificationCountTextView.text = ""
                        }
                        is ServiceConnectionState.Notifications -> {
                            if (state.notifications.isNotEmpty()) {
                                notificationCountTextView.text = String.format(
                                    Locale.getDefault(),
                                    "%d",
                                    state.notifications.count()
                                )
                            } else {
                                notificationCountTextView.text = ""
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (NotificationUtils.notificationPermissionGranted(this)) {
            notificationViewModel.bindService(this)
        }
    }

    override fun onResume() {
        super.onResume()
        applyBackground()
        checkCardSizeChanges()
    }

    private fun checkCardSizeChanges() {
        val currentAppSize = sharedPreferences.appCardSize
        val currentFavoriteSize = sharedPreferences.favoriteCardSize
        val currentWatchNextSize = sharedPreferences.watchNextCardSize

        if (currentAppSize != lastAppCardSize ||
            currentFavoriteSize != lastFavoriteCardSize ||
            currentWatchNextSize != lastWatchNextCardSize) {
            
            lastAppCardSize = currentAppSize
            lastFavoriteCardSize = currentFavoriteSize
            lastWatchNextCardSize = currentWatchNextSize
            
            android.os.Handler(mainLooper).post {
                refreshAllRows()
            }
        }
    }

    private fun refreshAllRows() {
        allAppsAdapter.notifyDataSetChanged()
        favoritesAdapter.notifyDataSetChanged()
        watchNextAdapter.notifyDataSetChanged()
        mainVerticalAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (NotificationUtils.notificationPermissionGranted(this)) {
            notificationViewModel.unbindService(this)
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
        assistantHintImageView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                transition.removeTarget(assistantHintImageView)
                transition.addTarget(assistantButtonsContainer)
                TransitionManager.beginDelayedTransition(topBarContainer, transition)
                assistantHintImageView.isVisible = false
                assistantButtonsContainer.isVisible = true
            }
        }

        val assistantButtonFocusListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!keyboardAssistantButton.hasFocus() && !voiceAssistantButton.hasFocus()) {
                    transition.removeTarget(assistantButtonsContainer)
                    transition.addTarget(assistantHintImageView)
                    TransitionManager.beginDelayedTransition(topBarContainer, transition)
                    assistantButtonsContainer.isVisible = false
                    assistantHintImageView.isVisible = true
                }
            }
        }

        keyboardAssistantButton.onFocusChangeListener = assistantButtonFocusListener
        voiceAssistantButton.onFocusChangeListener = assistantButtonFocusListener
    }

    private fun askForHomeRoleIfNeeded() {
        if (!AppManager.isSystemApp(this) && roleManager.roleCanBeRequested(RoleManager.ROLE_HOME)
            && !sharedPreferences.homeRoleRequestDialogDismissed
        ) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.home_role_request_dialog_title)
                .setMessage(R.string.home_role_request_dialog_message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    homeRoleActivityRequestLauncher.launch(
                        roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                    )
                }
                .setNeutralButton(R.string.home_role_request_dialog_neutral) { _, _ ->
                    // Do nothing
                }
                .setNegativeButton(R.string.home_role_request_dialog_negative) { _, _ ->
                    sharedPreferences.homeRoleRequestDialogDismissed = true
                }
                .show().also {
                    it.getButton(DialogInterface.BUTTON_NEUTRAL).requestFocus()
                }
        }
    }

    private fun applyBackground() {
        val backgroundType = sharedPreferences.backgroundType
        val decorView = window.decorView
        
        when (backgroundType) {
            0, 1 -> {
                decorView.setBackgroundColor(getColor(R.color.default_background))
            }
            2 -> {
                val uriString = sharedPreferences.backgroundImageUri
                if (!uriString.isNullOrEmpty()) {
                    try {
                        val file = java.io.File(uriString)
                        if (file.exists()) {
                            val options = android.graphics.BitmapFactory.Options().apply {
                                inJustDecodeBounds = true
                            }
                            BitmapFactory.decodeFile(file.absolutePath, options)
                            
                            val targetWidth = 1920
                            val targetHeight = 1080
                            val sampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
                            
                            val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                                inSampleSize = sampleSize
                            }
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                            
                            if (bitmap != null) {
                                val drawable = BitmapDrawable(resources, bitmap)
                                drawable.gravity = android.view.Gravity.FILL
                                decorView.background = drawable
                            } else {
                                decorView.setBackgroundColor(getColor(R.color.default_background))
                            }
                        } else {
                            decorView.setBackgroundColor(getColor(R.color.default_background))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        decorView.setBackgroundColor(getColor(R.color.default_background))
                    }
                } else {
                    decorView.setBackgroundColor(getColor(R.color.default_background))
                }
            }
        }
    }

    private fun calculateInSampleSize(options: android.graphics.BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}

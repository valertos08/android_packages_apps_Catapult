/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.app.Application
import android.content.Intent
import android.util.Log
import com.google.android.material.color.DynamicColors

class CatapultApplication : Application() {

    companion object {
        private const val TAG = "CatapultApplication"
    }

    override fun onCreate() {
        super.onCreate()

        // Observe dynamic colors changes
        DynamicColors.applyToActivitiesIfAvailable(this)
        
        // Start GlobalHotkeyService when application starts. Application.onCreate
        // also runs for background triggers (broadcasts/providers) where a plain
        // startService throws BackgroundServiceStartNotAllowedException and would
        // kill the whole app process, so never let it crash here. The foreground
        // path (MainActivity.onCreate) starts the service anyway.
        Log.d(TAG, "Application created, starting GlobalHotkeyService")
        try {
            startService(Intent(this, GlobalHotkeyService::class.java))
        } catch (e: Exception) {
            Log.w(TAG, "Could not start GlobalHotkeyService from background", e)
        }
    }
}

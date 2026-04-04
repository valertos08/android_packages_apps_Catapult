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
        
        // Start GlobalHotkeyService when application starts
        Log.d(TAG, "Application created, starting GlobalHotkeyService")
        val serviceIntent = Intent(this, GlobalHotkeyService::class.java)
        startService(serviceIntent)
    }
}

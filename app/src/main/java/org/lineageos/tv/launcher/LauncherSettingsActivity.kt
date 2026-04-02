/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.content.Intent
import android.os.Bundle

class LauncherSettingsActivity : ModalActivity(R.layout.activity_launcher_settings) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        findViewById<android.view.View>(R.id.appearance_item)?.setOnClickListener {
            startActivity(Intent(this, AppearanceActivity::class.java))
        }
    }
}
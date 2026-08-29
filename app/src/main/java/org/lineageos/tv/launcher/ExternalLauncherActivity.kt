/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ExternalLauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, AdditionalSettingsActivity::class.java))
        finish()
    }
}

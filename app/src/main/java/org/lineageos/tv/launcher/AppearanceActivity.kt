/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.content.Intent
import android.os.Bundle

class AppearanceActivity : ModalActivity(R.layout.activity_appearance) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        findViewById<android.view.View>(R.id.background_item)?.setOnClickListener {
            startActivity(Intent(this, BackgroundActivity::class.java))
        }

        findViewById<android.view.View>(R.id.app_cards_item)?.setOnClickListener {
            startActivity(Intent(this, CardsSettingsActivity::class.java))
        }
    }
}

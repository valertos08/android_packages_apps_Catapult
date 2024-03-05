/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.leanback.widget.VerticalGridView
import org.lineageos.tv.launcher.adapter.AllAppsFavoritesAdapter

class AddFavoriteActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_favorite)

        val layoutParams = window.attributes
        layoutParams.gravity = Gravity.END
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = layoutParams

        val allAppsGridView: VerticalGridView = findViewById(R.id.all_apps_add_grid)
        val allAppsAdapter = AllAppsFavoritesAdapter(applicationContext)
        allAppsGridView.adapter = allAppsAdapter
    }
}

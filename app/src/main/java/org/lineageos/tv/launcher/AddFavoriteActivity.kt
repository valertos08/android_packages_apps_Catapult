/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.VerticalGridView
import org.lineageos.tv.launcher.adapter.AllAppsFavoritesAdapter

class AddFavoriteActivity : FragmentActivity(R.layout.activity_add_favorite) {
    // Views
    private val allAppsGridView by lazy { findViewById<VerticalGridView>(R.id.all_apps_add_grid) }

    // Adapters
    private val allAppsAdapter by lazy { AllAppsFavoritesAdapter(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutParams = window.attributes.apply {
            gravity = Gravity.END
            width = WindowManager.LayoutParams.WRAP_CONTENT
        }
        window.attributes = layoutParams

        allAppsGridView.adapter = allAppsAdapter
    }
}

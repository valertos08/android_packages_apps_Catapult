/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.edit
import org.lineageos.tv.launcher.model.AppInfo
import org.lineageos.tv.launcher.model.Launchable

object AppManager {
    internal var onFavoriteAddedCallback: (packageName: String) -> Unit = {}
    internal var onFavoriteRemovedCallback: (packageName: String) -> Unit = {}

    fun getInstalledApps(context: Context): List<Launchable> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, 0).map {
            AppInfo(it, context)
        }
    }

    fun removeFavoriteApp(context: Context, packageName: String) {
        val favoriteApps = getFavoriteApps(context).toMutableList()
        favoriteApps.remove(packageName)
        setFavorites(context, favoriteApps)

        // Notify
        onFavoriteRemovedCallback(packageName)
    }


    fun addFavoriteApp(context: Context, packageName: String) {
        val favoriteApps = getFavoriteApps(context).toMutableList()
        favoriteApps.add(packageName)
        setFavorites(context, favoriteApps)

        // Notify
        onFavoriteAddedCallback(packageName)
    }

    fun getFavoriteApps(context: Context): List<String> {
        val sharedPreferences =
            context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
        val serializedList = sharedPreferences.getString("favoriteApps", "") ?: ""
        if (serializedList == "") {
            return mutableListOf()
        }
        return serializedList.split(",")
    }

    fun setFavorites(context: Context, newFavoritesSet: MutableList<String>) {
        val sharedPreferences = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
        val serializedList = newFavoritesSet.joinToString(",")
        sharedPreferences.edit {
            putString("favoriteApps", serializedList)
        }
    }

    fun uninstallApp(context: Context, packageName: String) {
        val packageUri = Uri.parse("package:$packageName")
        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
        context.startActivity(uninstallIntent, null)
    }
}

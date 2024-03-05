package org.lineageos.tv.launcher.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.lineageos.tv.launcher.model.AppInfo
import org.lineageos.tv.launcher.model.Launchable


object AppManager {
    internal var onFavoriteAddedCallback: (packageName: String) -> Unit = {}
    internal var onFavoriteRemovedCallback: (packageName: String) -> Unit = {}

    fun getInstalledApps(context: Context): ArrayList<Launchable> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, 0)
        val appsList: ArrayList<Launchable> = ArrayList()
        for (app in apps) {
            val appInfo = AppInfo(app, context)
            appsList.add(appInfo)
        }

        return appsList
    }

    fun removeFavoriteApp(context: Context, packageName: String) {
        val favoriteApps = getFavoriteApps(context)
        favoriteApps.remove(packageName)
        setFavorites(context, favoriteApps)

        // Notify
        onFavoriteRemovedCallback(packageName)
    }


    fun addFavoriteApp(context: Context, packageName: String) {
        val favoriteApps = getFavoriteApps(context)
        favoriteApps.add(packageName)
        setFavorites(context, favoriteApps)

        // Notify
        onFavoriteAddedCallback(packageName)
    }

    fun getFavoriteApps(context: Context): ArrayList<String> {
        val sharedPreferences =
            context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
        val serializedList = sharedPreferences.getString("favoriteApps", "") ?: ""
        if (serializedList == "") {
            return ArrayList()
        }
        return ArrayList(serializedList.split(","))
    }

    fun setFavorites(context: Context, newFavoritesSet: ArrayList<String>) {
        val sharedPreferences = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val serializedList = newFavoritesSet.joinToString(",")
        editor.putString("favoriteApps", serializedList)
        editor.apply()
    }

    fun uninstallApp(context: Context, packageName: String) {
        val packageUri = Uri.parse("package:$packageName")
        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
        context.startActivity(uninstallIntent, null)
    }
}
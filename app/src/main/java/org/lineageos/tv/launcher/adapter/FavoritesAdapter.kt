/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.adapter

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import org.lineageos.tv.launcher.AddFavoriteActivity
import org.lineageos.tv.launcher.ModifyChannelsActivity
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.ActivityLauncher
import org.lineageos.tv.launcher.model.AppInfo
import org.lineageos.tv.launcher.model.Launchable
import org.lineageos.tv.launcher.utils.AppManager
import org.lineageos.tv.launcher.view.Card
import org.lineageos.tv.launcher.view.FavoriteCard
import java.util.Collections


class FavoritesAdapter(context: Context) : AppsAdapter(context) {
    init {
        updateFavoriteApps(AppManager.getFavoriteApps(mContext))
    }

    override fun getaAppsList(): ArrayList<Launchable> {
        val list = ArrayList<Launchable>()
        list.add(createAddFavoriteEntry())
        list.add(createModifyChannelsEntry())
        return list
    }

    fun updateFavoriteApps(packageNames: List<String>) {
        for (packageName in packageNames) {
            val pm: PackageManager = mContext.packageManager
            try {
                val ai: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
                val appInfo = AppInfo(ai, mContext)
                mAppsList.add(mAppsList.size - STABLE_ITEM_COUNT, appInfo)
            } catch (e: PackageManager.NameNotFoundException) {
                AppManager.removeFavoriteApp(mContext, packageName)
            }
        }

        notifyItemRangeChanged(0, mAppsList.size)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        (viewHolder.itemView as FavoriteCard).setCardInfo(mAppsList[i])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = FavoriteCard(parent.context)

        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        itemView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

        return ViewHolder(itemView)
    }

    private fun createAddFavoriteEntry(): Launchable {
        return ActivityLauncher(
            mContext.getString(R.string.new_favorite),
            AppCompatResources.getDrawable(mContext, R.drawable.ic_add)!!, mContext,
            Intent(mContext, AddFavoriteActivity::class.java)
        )
    }

    private fun createModifyChannelsEntry(): Launchable {
        return ActivityLauncher(
            mContext.getString(R.string.modify_channels),
            AppCompatResources.getDrawable(mContext, R.drawable.ic_settings)!!, mContext,
            Intent(mContext, ModifyChannelsActivity::class.java)
        )
    }

    override fun handleKey(
        v: View,
        keyCode: Int,
        keyEvent: KeyEvent,
        adapterPosition: Int,
    ): Boolean {
        v as FavoriteCard

        // Leave center key for onClick handler
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            return false
        }

        // Only handle keyDown events here
        if (keyEvent.action != KeyEvent.ACTION_DOWN) {
            return v.mMoving
        }

        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (v.mMoving) {
                    v.setMoveDone()
                    return true
                }
                return false
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (v.mMoving) {
                    if (adapterPosition == 0) {
                        return true
                    }
                    Collections.swap(mAppsList, adapterPosition, adapterPosition - 1)
                    notifyItemMoved(adapterPosition, adapterPosition - 1)
                    return true
                }
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (v.mMoving) {
                    if (adapterPosition == mAppsList.size - (STABLE_ITEM_COUNT + 1)) {
                        return true
                    }
                    Collections.swap(mAppsList, adapterPosition, adapterPosition + 1)
                    notifyItemMoved(adapterPosition, adapterPosition + 1)
                    return true
                }
            }
            // Don't allow moving up or down while moving a favorite app
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_UP,
            ->
                return v.mMoving
        }

        return false
    }

    override fun handleLongClick(app: Card): Boolean {
        app as FavoriteCard
        if (app.mMoving || !app.mHasMenu) {
            return true
        }

        showPopupMenu(app, R.menu.favorite_app_long_press)
        return true
    }

    override fun handleClick(app: Card) {
        app as FavoriteCard
        if (!app.mMoving) {
            super.handleClick(app)
            return
        }

        app.setMoveDone()

        // Save new favorites order
        val newFavoritesSet = ArrayList<String>()
        for (a in mAppsList) {
            if (a.mPackageName != "") {
                newFavoritesSet.add(a.mPackageName)
            }
        }

        AppManager.setFavorites(mContext, newFavoritesSet)
    }

    override fun addItem(packageName: String) {
        val ai: ApplicationInfo = mContext.packageManager.getApplicationInfo(packageName, 0)
        val appInfo = AppInfo(ai, mContext)
        mAppsList.add(mAppsList.size - STABLE_ITEM_COUNT, appInfo) // Take 'Add new' into account
        notifyItemInserted(mAppsList.size - (STABLE_ITEM_COUNT + 1))
    }

    companion object {
        private const val STABLE_ITEM_COUNT = 2
    }
}
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
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import org.lineageos.tv.launcher.AddFavoriteActivity
import org.lineageos.tv.launcher.ModifyChannelsActivity
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.ActivityLauncher
import org.lineageos.tv.launcher.model.AppInfo
import org.lineageos.tv.launcher.model.Launchable
import org.lineageos.tv.launcher.utils.AppManager
import org.lineageos.tv.launcher.view.FavoriteCard
import java.util.Collections


class FavoritesAdapter(context: Context) : TvAdapter<FavoriteCard>(context) {
    init {
        updateFavoriteApps(AppManager.getFavoriteApps(context))
    }

    override fun getAppsList() = mutableListOf(
        createAddFavoriteEntry(),
        createModifyChannelsEntry(),
    )

    private fun updateFavoriteApps(packageNames: List<String>) {
        for (packageName in packageNames) {
            val pm: PackageManager = context.packageManager
            try {
                val ai: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
                val appInfo = AppInfo(ai, context)
                launchablesList.add(launchablesList.size - STABLE_ITEM_COUNT, appInfo)
            } catch (e: PackageManager.NameNotFoundException) {
                AppManager.removeFavoriteApp(context, packageName)
            }
        }

        notifyItemRangeChanged(0, launchablesList.size)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.card.setCardInfo(launchablesList[i])
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
            context.getString(R.string.new_favorite),
            AppCompatResources.getDrawable(context, R.drawable.ic_add)!!,
            context,
            Intent(context, AddFavoriteActivity::class.java)
        )
    }

    private fun createModifyChannelsEntry(): Launchable {
        return ActivityLauncher(
            context.getString(R.string.modify_channels),
            AppCompatResources.getDrawable(context, R.drawable.ic_settings)!!,
            context,
            Intent(context, ModifyChannelsActivity::class.java)
        )
    }

    override fun handleKey(
        card: FavoriteCard,
        keyCode: Int,
        event: KeyEvent?,
        bindingAdapterPosition: Int,
    ): Boolean {
        // Leave center key for onClick handler
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            return false
        }

        if (event == null) {
            return false
        }

        // Only handle keyDown events here
        if (event.action != KeyEvent.ACTION_DOWN) {
            return card.moving
        }

        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (card.moving) {
                    card.setMoveDone()
                    return true
                }
                return false
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (card.moving) {
                    if (bindingAdapterPosition == 0) {
                        return true
                    }
                    Collections.swap(
                        launchablesList, bindingAdapterPosition, bindingAdapterPosition - 1
                    )
                    notifyItemMoved(bindingAdapterPosition, bindingAdapterPosition - 1)
                    return true
                }
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (card.moving) {
                    if (bindingAdapterPosition == launchablesList.size - (STABLE_ITEM_COUNT + 1)) {
                        return true
                    }
                    Collections.swap(
                        launchablesList, bindingAdapterPosition, bindingAdapterPosition + 1
                    )
                    notifyItemMoved(bindingAdapterPosition, bindingAdapterPosition + 1)
                    return true
                }
            }
            // Don't allow moving up or down while moving a favorite app
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_UP,
            -> return card.moving
        }

        return false
    }

    override fun handleLongClick(card: FavoriteCard): Boolean {
        if (card.moving || !card.hasMenu) {
            return true
        }

        card.showPopupMenu()
        return true
    }

    override fun handleClick(card: FavoriteCard) {
        if (!card.moving) {
            super.handleClick(card)
            return
        }

        card.setMoveDone()

        // Save new favorites order
        val newFavoritesSet = mutableListOf<String>()
        for (a in launchablesList) {
            if (a.packageName != "") {
                newFavoritesSet.add(a.packageName)
            }
        }

        AppManager.setFavorites(context, newFavoritesSet)
    }

    override fun addItem(packageName: String) {
        val ai: ApplicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
        val appInfo = AppInfo(ai, context)
        // Take 'Add new' into account
        launchablesList.add(
            launchablesList.size - STABLE_ITEM_COUNT, appInfo
        )
        notifyItemInserted(launchablesList.size - (STABLE_ITEM_COUNT + 1))
    }

    companion object {
        private const val STABLE_ITEM_COUNT = 2
    }
}

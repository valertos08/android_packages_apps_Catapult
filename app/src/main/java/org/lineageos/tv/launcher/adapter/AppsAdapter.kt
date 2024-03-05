/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.adapter

import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.AppInfo
import org.lineageos.tv.launcher.model.Launchable
import org.lineageos.tv.launcher.utils.AppManager
import org.lineageos.tv.launcher.view.AppCard
import org.lineageos.tv.launcher.view.Card
import org.lineageos.tv.launcher.view.FavoriteCard
import kotlin.reflect.safeCast

open class AppsAdapter(protected val context: Context) :
    RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    protected val appsList by lazy { getaAppsList() }

    inner class ViewHolder(card: Card) : RecyclerView.ViewHolder(card) {
        init {
            card.apply {
                setOnClickListener {
                    handleClick(this)
                }
                setOnLongClickListener {
                    handleLongClick(this)
                }
                setOnKeyListener { v, keyCode, event ->
                    handleKey(v, keyCode, event, bindingAdapterPosition)
                }
            }
        }
    }

    protected open fun handleKey(
        v: View,
        keyCode: Int,
        keyEvent: KeyEvent,
        adapterPosition: Int,
    ) = false

    protected open fun handleClick(app: Card) {
        val context = app.context
        context.startActivity(app.launchIntent)
        Toast.makeText(context, app.label, Toast.LENGTH_SHORT).show()
    }

    protected open fun handleLongClick(app: Card): Boolean {
        showPopupMenu(app, R.menu.app_long_press)
        return true
    }

    protected open fun getaAppsList(): MutableList<Launchable> = AppManager.getInstalledApps(context)

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        (viewHolder.itemView as AppCard).setCardInfo(appsList[i])
    }

    override fun getItemCount() = appsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = AppCard(parent.context)

        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        itemView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

        return ViewHolder(itemView)
    }

    protected fun showPopupMenu(anchorView: View, menuResId: Int) {
        val popupMenu = PopupMenu(context, anchorView)
        popupMenu.menuInflater.inflate(menuResId, popupMenu.menu)
        popupMenu.setForceShowIcon(true)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_uninstall -> {
                    AppManager.uninstallApp(context, (anchorView as AppCard).packageName)
                    true
                }

                R.id.menu_mark_as_favorite -> {
                    AppManager.addFavoriteApp(context, (anchorView as AppCard).packageName)
                    true
                }

                R.id.menu_remove_favorite -> {
                    AppManager.removeFavoriteApp(context, (anchorView as AppCard).packageName)
                    true
                }

                R.id.menu_move -> {
                    FavoriteCard::class.safeCast(anchorView)?.setMoving()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    open fun removeItem(packageName: String) {
        val index = appsList.indexOfFirst { it.packageName == packageName }
        if (index != -1) {
            appsList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    open fun addItem(packageName: String) {
        val ai: ApplicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
        val appInfo = AppInfo(ai, context)
        appsList.add(appsList.size, appInfo)
        notifyItemInserted(appsList.size)
    }
}

package org.lineageos.tv.launcher.adapter

import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.KeyEvent
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.AppInfo
import org.lineageos.tv.launcher.utils.AppManager
import org.lineageos.tv.launcher.view.AppCard
import org.lineageos.tv.launcher.view.Card
import org.lineageos.tv.launcher.view.FavoriteCard

abstract class TvAdapter<T : Card>(protected val context: Context) :
    RecyclerView.Adapter<TvAdapter<T>.ViewHolder>() {

    val launchablesList by lazy { getAppsList().toMutableList() }

    inner class ViewHolder(val card: T) : RecyclerView.ViewHolder(card) {
        init {
            card.apply {
                setOnClickListener {
                    handleClick(this)
                }
                setOnLongClickListener {
                    handleLongClick(this)
                }
                setOnKeyListener { _, keyCode, event ->
                    handleKey(this, keyCode, event, bindingAdapterPosition)
                }
            }
        }
    }

    open fun handleClick(card: T) {
        val context = card.context
        context.startActivity(card.launchIntent)
        Toast.makeText(context, card.label, Toast.LENGTH_SHORT).show()
    }

    open fun handleLongClick(card: T): Boolean {
        showPopupMenu(card, R.menu.app_long_press)
        return true
    }

    open fun handleKey(
        card: T, keyCode: Int, event: KeyEvent?, bindingAdapterPosition: Int
    ) = false

    open fun getAppsList() = AppManager.getInstalledApps(context)

    override fun getItemCount() = launchablesList.size

    fun showPopupMenu(anchorView: View, menuResId: Int) {
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
                    if (anchorView is FavoriteCard) {
                        anchorView.setMoving()
                    }
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    fun removeItem(packageName: String) {
        val index = launchablesList.indexOfFirst { it.packageName == packageName }
        if (index != -1) {
            launchablesList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    open fun addItem(packageName: String) {
        val ai: ApplicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
        val appInfo = AppInfo(ai, context)
        launchablesList.add(launchablesList.size, appInfo)
        notifyItemInserted(launchablesList.size)
    }
}

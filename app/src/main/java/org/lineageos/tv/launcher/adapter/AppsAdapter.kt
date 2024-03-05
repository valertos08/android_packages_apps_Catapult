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

open class AppsAdapter(protected val mContext: Context) :
    RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    protected val mAppsList by lazy { getaAppsList() }

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
    ): Boolean {
        return false
    }

    protected open fun handleClick(app: Card) {
        val context = app.context
        context.startActivity(app.mLaunchIntent)
        Toast.makeText(context, app.mLabel, Toast.LENGTH_SHORT).show()
    }

    protected open fun handleLongClick(app: Card): Boolean {
        showPopupMenu(app, R.menu.app_long_press)
        return true
    }

    protected open fun getaAppsList(): ArrayList<Launchable> {
        return AppManager.getInstalledApps(mContext)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        (viewHolder.itemView as AppCard).setCardInfo(mAppsList[i])
    }

    override fun getItemCount(): Int {
        return mAppsList.size
    }

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
        val popupMenu = PopupMenu(mContext, anchorView)
        popupMenu.menuInflater.inflate(menuResId, popupMenu.menu)
        popupMenu.setForceShowIcon(true)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_uninstall -> {
                    AppManager.uninstallApp(mContext, (anchorView as AppCard).mPackageName)
                    true
                }

                R.id.menu_mark_as_favorite -> {
                    AppManager.addFavoriteApp(mContext, (anchorView as AppCard).mPackageName)
                    true
                }

                R.id.menu_remove_favorite -> {
                    AppManager.removeFavoriteApp(mContext, (anchorView as AppCard).mPackageName)
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

    open fun removeItem(packageName: String) {
        val index = mAppsList.indexOfFirst { it.mPackageName == packageName }
        if (index != -1) {
            mAppsList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    open fun addItem(packageName: String) {
        val ai: ApplicationInfo = mContext.packageManager.getApplicationInfo(packageName, 0)
        val appInfo = AppInfo(ai, mContext)
        mAppsList.add(mAppsList.size, appInfo)
        notifyItemInserted(mAppsList.size)
    }
}
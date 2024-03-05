package org.lineageos.tv.launcher.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.utils.Suggestions
import org.lineageos.tv.launcher.utils.Suggestions.orderSuggestions
import java.util.Collections

class MainVerticalAdapter(
    private val mContext: Context,
    private val mRowList: ArrayList<Pair<Long, org.lineageos.tv.launcher.model.MainRowItem>>,
) :
    RecyclerView.Adapter<MainVerticalAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val v = (viewHolder.itemView as org.lineageos.tv.launcher.view.MainRowItem)
        v.setData(mRowList[i].second)
        v.layoutParams = if (mRowList[i].second.adapter is AppsAdapter) {
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                mContext.resources.getDimension(R.dimen.main_app_row_height).toInt()
            )
        } else {
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                mContext.resources.getDimension(R.dimen.main_row_height).toInt()
            )
        }
    }

    override fun getItemCount(): Int {
        return mRowList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(org.lineageos.tv.launcher.view.MainRowItem(mContext))
    }

    fun removeItem(item: Long) {
        for ((i, row) in mRowList.withIndex()) {
            if (row.first == item) {
                mRowList.remove(row)
                notifyItemRemoved(i)
                return
            }
        }
    }

    fun addItem(item: Pair<Long, org.lineageos.tv.launcher.model.MainRowItem>) {
        var temp = mRowList.toMutableList()
        temp.add(item)
        temp =
            temp.orderSuggestions(Suggestions.getChannelOrder(mContext)) { it.first } as ArrayList
        var index = temp.indexOf(item)
        if (index == -1) {
            index = (mRowList.size - 1)
        }
        mRowList.add(index, item)
        notifyItemInserted(index)
    }

    fun isChannelShowing(channelId: Long?): Boolean {
        channelId ?: return false
        val res = mRowList.find { it.first == channelId }
        res ?: return false
        return true
    }

    fun findChannelIndex(channelId: Long?): Int {
        channelId ?: return -1
        return mRowList.indexOfFirst { it.first == channelId }
    }

    fun itemMoved(from: Int, to: Int) {
        Collections.swap(mRowList, from, to)
        notifyItemMoved(from, to)
    }
}
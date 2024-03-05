package org.lineageos.tv.launcher.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.tvprovider.media.tv.BasePreviewProgram
import org.lineageos.tv.launcher.view.WatchNextCard


@SuppressLint("RestrictedApi")
class WatchNextAdapter(
    private val mContext: Context,
    private val mWatchableList: ArrayList<BasePreviewProgram>,
) :
    RecyclerView.Adapter<WatchNextAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            handleClick(v as WatchNextCard)
        }
    }

    private fun handleClick(v: WatchNextCard) {
        val context = v.context
        context.startActivity(v.mLaunchIntent)
        Toast.makeText(context, v.mLabel, Toast.LENGTH_SHORT).show()
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        (viewHolder.itemView as WatchNextCard).setInfo(mWatchableList[i])
    }

    override fun getItemCount(): Int {
        return mWatchableList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = WatchNextCard(parent.context)

        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )

        itemView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

        return ViewHolder(itemView)
    }
}
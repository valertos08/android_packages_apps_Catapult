package org.lineageos.tv.launcher.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.tv.TvContract
import android.util.Log
import androidx.tvprovider.media.tv.PreviewChannel
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import androidx.tvprovider.media.tv.WatchNextProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.Channel

@SuppressLint("RestrictedApi")
object Suggestions {
    private const val TAG = "TvLauncher.Suggestions"
    private const val PACKAGE_FRAMEWORK_STUBS = "com.android.tv.frameworkpackagestubs"

    internal var onChannelHiddenCallback: (channelId: Long) -> Unit = {}
    internal var onChannelShownCallback: (channelId: Long) -> Unit = {}
    internal var onChannelSelectedCallback: (channelId: Long, index: Int) -> Unit = { _, _ -> }
    internal var onChannelOrderChangedCallback: (moveChannelId: Long?, otherChannelId: Long?) -> Unit =
        { _, _ -> }

    fun getWatchNextPrograms(context: Context): List<WatchNextProgram> {
        val cursor = context.contentResolver.query(
            TvContractCompat.WatchNextPrograms.CONTENT_URI,
            WatchNextProgram.PROJECTION,
            null,
            null,
            null
        ) ?: return ArrayList()

        val watchNextList = ArrayList<WatchNextProgram>()

        if (!cursor.moveToFirst()) {
            return ArrayList()
        }

        while (cursor.moveToNext()) {
            try {
                val watchNextProgram = WatchNextProgram.fromCursor(cursor)
                val resolvedActivity =
                    watchNextProgram.intent.resolveActivity(context.packageManager)
                if (resolvedActivity == null ||
                    resolvedActivity.packageName == PACKAGE_FRAMEWORK_STUBS
                ) {
                    // This can't be opened with any app
                    continue
                }
                watchNextList.add(watchNextProgram)
            } catch (e: Exception) {
                Log.w(TAG, "Ignoring watch next: $e")
            }
        }

        cursor.close()
        return watchNextList
    }

    fun getPreviewChannels(context: Context): List<PreviewChannel> {
        val cursor = context.contentResolver.query(
            TvContractCompat.Channels.CONTENT_URI,
            PreviewChannel.Columns.PROJECTION,
            null,
            null,
            null
        ) ?: return ArrayList()

        val previewChannelList = ArrayList<PreviewChannel>()

        if (!cursor.moveToFirst()) {
            return ArrayList()
        }

        while (cursor.moveToNext()) {
            try {
                if (!cursor.getString(PreviewChannel.Columns.COL_APP_LINK_INTENT_URI)
                        .isNullOrEmpty()
                    && !cursor.getString(PreviewChannel.Columns.COL_DISPLAY_NAME).isNullOrEmpty()
                ) {
                    previewChannelList.add(PreviewChannel.fromCursor(cursor))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Ignoring preview channel: $e")
            }
        }

        cursor.close()
        return previewChannelList
    }

    suspend fun getPreviewChannelsAsync(context: Context): List<PreviewChannel> {
        return withContext(Dispatchers.IO) {
            return@withContext getPreviewChannels(context)
        }
    }

    fun getSuggestions(context: Context, id: Long): ArrayList<PreviewProgram> {
        val cursor = context.contentResolver.query(
            TvContractCompat.buildPreviewProgramsUriForChannel(id),
            PreviewProgram.PROJECTION,
            null,
            null,
            null,
        ) ?: return ArrayList()

        val previewProgramList = ArrayList<PreviewProgram>()

        if (!cursor.moveToFirst()) {
            return ArrayList()
        }

        while (cursor.moveToNext()) {
            try {
                val previewProgram = PreviewProgram.fromCursor(cursor)
                val resolvedActivity = previewProgram.intent.resolveActivity(context.packageManager)
                if (resolvedActivity == null ||
                    resolvedActivity.packageName == PACKAGE_FRAMEWORK_STUBS
                ) {
                    // This can't be opened with any app
                    continue
                }
                previewProgramList.add(previewProgram)
            } catch (e: Exception) {
                Log.w(TAG, "Ignoring preview program: $e")
            }
        }

        cursor.close()
        return previewProgramList
    }

    suspend fun getSuggestionsAsync(context: Context, id: Long): ArrayList<PreviewProgram> {
        return withContext(Dispatchers.IO) {
            return@withContext getSuggestions(context, id)
        }
    }

    fun setHiddenChannels(context: Context, hiddenChannels: ArrayList<Long>) {
        val sharedPreferences = context.getSharedPreferences("Channels", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val serializedList = hiddenChannels.joinToString(",")
        editor.putString("hiddenChannels", serializedList)
        editor.apply()
    }

    fun getHiddenChannels(context: Context): ArrayList<Long> {
        val sharedPreferences =
            context.getSharedPreferences("Channels", Context.MODE_PRIVATE)
        val serializedList = sharedPreferences.getString("hiddenChannels", "") ?: ""
        if (serializedList == "") {
            return ArrayList()
        }
        return ArrayList(serializedList.split(",").map { it.toLong() })
    }

    fun hideChannel(context: Context, channelId: Long?) {
        channelId ?: return
        val hiddenChannels = getHiddenChannels(context)
        hiddenChannels.add(channelId)
        setHiddenChannels(context, hiddenChannels)

        // Notify
        onChannelHiddenCallback(channelId)
    }

    fun showChannel(context: Context, channelId: Long?) {
        channelId ?: return
        val hiddenChannels = getHiddenChannels(context)
        hiddenChannels.remove(channelId)
        setHiddenChannels(context, hiddenChannels)

        // Notify
        onChannelShownCallback(channelId)
    }

    fun getChannelTitle(context: Context, previewChannel: PreviewChannel): String {
        return context.resources.getString(
            R.string.channel_title, previewChannel.getAppName(context), previewChannel.displayName
        )
    }

    fun saveChannelOrder(
        context: Context,
        from: Int,
        to: Int,
        channels: List<Long>,
        notify: Boolean,
    ) {
        val sharedPreferences = context.getSharedPreferences("Channels", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val serializedList = channels.joinToString(",")
        editor.putString("channels", serializedList)
        editor.apply()

        if (!notify) {
            return
        }

        // Notify
        onChannelOrderChangedCallback(channels[from], channels[to])
    }

    fun getChannelOrder(context: Context): ArrayList<Long> {
        val sharedPreferences =
            context.getSharedPreferences("Channels", Context.MODE_PRIVATE)
        val serializedList = sharedPreferences.getString("channels", "") ?: ""
        if (serializedList == "") {
            return ArrayList()
        }

        return ArrayList(serializedList.split(",").map { it.toLong() })
    }

    fun <T, K> List<T>.orderSuggestions(orderIds: List<K>, idSelector: (T) -> K?): List<T> {
        if (orderIds.isEmpty()) {
            val (presentItems, remainingItems) = this.partition { idSelector(it) == Channel.ALL_APPS_ID }
            return remainingItems + presentItems
        }

        val (presentItems, remainingItems) = this.partition { idSelector(it) in orderIds }
        val sortedPresentItems = presentItems.sortedBy { orderIds.indexOf(idSelector(it)) }
        return sortedPresentItems + remainingItems
    }

    fun aspectRatioToFloat(aspectRatio: Int): Float {
        return when (aspectRatio) {
            TvContract.PreviewPrograms.ASPECT_RATIO_16_9 -> 16f / 9f
            TvContract.PreviewPrograms.ASPECT_RATIO_4_3 -> 4f / 3f
            TvContract.PreviewPrograms.ASPECT_RATIO_1_1 -> 1f
            TvContract.PreviewPrograms.ASPECT_RATIO_3_2 -> 3f / 2f
            TvContract.PreviewPrograms.ASPECT_RATIO_2_3 -> 2f / 3f
            else -> -1f
        }
    }

    fun PreviewChannel.getAppName(context: Context): String {
        val packageManager: PackageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(this.packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }
}